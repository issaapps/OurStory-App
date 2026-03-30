package com.love.essahazama;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private static final String DB_URL     = "https://essahazamaapp-default-rtdb.firebaseio.com";
    private static final String DB_PATH    = "essa_hazama/messages";
    private static final String PREFS      = "chat_prefs";
    private static final String KEY_USER   = "current_user";
    public  static final String CHANNEL_ID = "love_chat_channel";
    public  static final int    NOTIF_ID   = 1001;

    private String            currentUser;
    private List<ChatMessage> msgList = new ArrayList<>();
    private ChatAdapter       adapter;
    private DatabaseReference dbRef;
    private ValueEventListener listener;

    private MediaRecorder mediaRecorder;
    private String        audioFilePath;
    private boolean       isRecording = false;

    private RecyclerView     rvChat;
    private LinearLayout     layoutLogin, layoutInput, layoutRecording;
    private HorizontalScrollView stickerPanel;
    private LinearLayout     stickerContainer;
    private EditText         etMsg;
    private TextView         tvStatus;
    private View             onlineDot;

    private ActivityResultLauncher<String> imagePicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> { if (uri != null) uploadMedia(uri, ChatMessage.TYPE_IMAGE); });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        rvChat           = view.findViewById(R.id.rvChat);
        layoutLogin      = view.findViewById(R.id.layoutLogin);
        layoutInput      = view.findViewById(R.id.layoutInput);
        layoutRecording  = view.findViewById(R.id.layoutRecording);
        stickerPanel     = view.findViewById(R.id.stickerPanel);
        stickerContainer = view.findViewById(R.id.stickerContainer);
        etMsg            = view.findViewById(R.id.etChatMsg);
        tvStatus         = view.findViewById(R.id.tvChatStatus);
        onlineDot        = view.findViewById(R.id.onlineDot);

        Button   btnEssa   = view.findViewById(R.id.btnLoginEssa);
        Button   btnHazama = view.findViewById(R.id.btnLoginHazama);
        TextView btnSend   = view.findViewById(R.id.btnSendChat);
        TextView btnSticker= view.findViewById(R.id.btnSticker);
        TextView btnImage  = view.findViewById(R.id.btnImage);
        TextView btnAudio  = view.findViewById(R.id.btnAudio);

        if (etMsg != null)
            etMsg.setHintTextColor(android.graphics.Color.parseColor("#A78BFA"));

        createNotificationChannel();
        requestNotificationPermission();
        FirebaseMessaging.getInstance().subscribeToTopic("love_chat");

        SharedPreferences prefs = requireContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        currentUser = prefs.getString(KEY_USER, null);
        if (currentUser != null) showChat();

        if (btnEssa   != null) btnEssa.setOnClickListener(v   -> loginAs("essa",   prefs));
        if (btnHazama != null) btnHazama.setOnClickListener(v -> loginAs("hazama", prefs));
        if (btnSend   != null) btnSend.setOnClickListener(v   -> sendTextMessage());
        if (etMsg     != null) etMsg.setOnEditorActionListener((tv, actionId, ev) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) { sendTextMessage(); return true; }
            return false;
        });
        if (btnSticker != null) btnSticker.setOnClickListener(v -> toggleStickerPanel());
        if (btnImage   != null) btnImage.setOnClickListener(v -> imagePicker.launch("image/*"));
        if (btnAudio   != null) btnAudio.setOnClickListener(v -> toggleRecording(btnAudio));

        buildStickerPanel();

        // ✅ إلغاء الإشعار عند فتح الدردشة
        cancelNotification();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // ✅ إلغاء الإشعار عند العودة للصفحة
        cancelNotification();
    }

    /** إلغاء إشعار الدردشة */
    private void cancelNotification() {
        if (getContext() == null) return;
        NotificationManager nm = (NotificationManager)
            getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(NOTIF_ID);
    }

    // ── Stickers ──────────────────────────────────────────
    private void buildStickerPanel() {
        if (stickerContainer == null) return;
        stickerContainer.removeAllViews();
        for (String sticker : StickerData.STICKERS) {
            TextView tv = new TextView(getContext());
            tv.setText(sticker);
            tv.setTextSize(32);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(72, 72);
            lp.setMargins(6, 0, 6, 0);
            tv.setLayoutParams(lp);
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setClickable(true);
            tv.setFocusable(true);
            tv.setOnClickListener(v -> sendSticker(sticker));
            stickerContainer.addView(tv);
        }
    }

    private void toggleStickerPanel() {
        if (stickerPanel == null) return;
        stickerPanel.setVisibility(
            stickerPanel.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    private void sendSticker(String sticker) {
        if (dbRef == null) return;
        ChatMessage msg = new ChatMessage(
            currentUser, ChatMessage.TYPE_STICKER, null, sticker, System.currentTimeMillis());
        dbRef.push().setValue(msg);
        if (stickerPanel != null) stickerPanel.setVisibility(View.GONE);
    }

    // ── Audio ─────────────────────────────────────────────
    private void toggleRecording(TextView btnAudio) {
        if (!isRecording) startRecording(btnAudio);
        else stopRecordingAndSend(btnAudio);
    }

    private void startRecording(TextView btnAudio) {
        try {
            audioFilePath = requireContext().getCacheDir()
                    + "/audio_" + System.currentTimeMillis() + ".3gp";
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            btnAudio.setText("⏹");
            if (layoutRecording != null) layoutRecording.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Toast.makeText(getContext(), "تأكد من صلاحية الميكروفون", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecordingAndSend(TextView btnAudio) {
        try {
            if (mediaRecorder != null) { mediaRecorder.stop(); mediaRecorder.release(); mediaRecorder = null; }
            isRecording = false;
            btnAudio.setText("🎤");
            if (layoutRecording != null) layoutRecording.setVisibility(View.GONE);
            uploadMedia(Uri.fromFile(new java.io.File(audioFilePath)), ChatMessage.TYPE_AUDIO);
        } catch (Exception e) {
            isRecording = false;
            btnAudio.setText("🎤");
        }
    }

    // ── Upload ────────────────────────────────────────────
    private void uploadMedia(Uri uri, String type) {
        if (dbRef == null) { Toast.makeText(getContext(), "تأكد من الاتصال", Toast.LENGTH_SHORT).show(); return; }
        Toast.makeText(getContext(), "⬆️ جارٍ الرفع...", Toast.LENGTH_SHORT).show();
        String ext = type.equals(ChatMessage.TYPE_IMAGE) ? ".jpg"
                   : type.equals(ChatMessage.TYPE_VIDEO) ? ".mp4" : ".3gp";
        StorageReference ref = FirebaseStorage.getInstance()
            .getReference("essa_hazama/" + type + "/" + System.currentTimeMillis() + ext);
        ref.putFile(uri)
           .addOnSuccessListener(snap -> ref.getDownloadUrl().addOnSuccessListener(dl -> {
               String dur = type.equals(ChatMessage.TYPE_AUDIO) ? "🎵 صوت" : null;
               dbRef.push().setValue(new ChatMessage(currentUser, type, dl.toString(), dur, System.currentTimeMillis()));
               Toast.makeText(getContext(), "✅ تم الإرسال!", Toast.LENGTH_SHORT).show();
           }))
           .addOnFailureListener(e -> Toast.makeText(getContext(), "فشل: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ── Login ─────────────────────────────────────────────
    private void loginAs(String user, SharedPreferences prefs) {
        currentUser = user;
        prefs.edit().putString(KEY_USER, user).apply();
        showChat();
    }

    private void showChat() {
        if (layoutLogin != null) layoutLogin.setVisibility(View.GONE);
        if (rvChat      != null) rvChat.setVisibility(View.VISIBLE);
        if (layoutInput != null) layoutInput.setVisibility(View.VISIBLE);
        adapter = new ChatAdapter(msgList, currentUser);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setStackFromEnd(true);
        if (rvChat != null) { rvChat.setLayoutManager(llm); rvChat.setAdapter(adapter); }
        connectFirebase();
    }

    // ── Firebase ──────────────────────────────────────────
    private void connectFirebase() {
        if (tvStatus  != null) tvStatus.setText(getString(R.string.chat_connecting));
        if (onlineDot != null) onlineDot.setVisibility(View.INVISIBLE);
        try {
            dbRef    = FirebaseDatabase.getInstance(DB_URL).getReference(DB_PATH);
            listener = new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    int prev = msgList.size();
                    msgList.clear();
                    for (DataSnapshot child : snap.getChildren()) {
                        ChatMessage msg = child.getValue(ChatMessage.class);
                        if (msg != null) { msg.setId(child.getKey()); msgList.add(msg); }
                    }
                    if (adapter != null) adapter.notifyDataSetChanged();
                    if (rvChat != null && !msgList.isEmpty())
                        rvChat.smoothScrollToPosition(msgList.size() - 1);
                    if (tvStatus  != null) tvStatus.setText(getString(R.string.chat_connected));
                    if (onlineDot != null) onlineDot.setVisibility(View.VISIBLE);

                    // إشعار برسالة جديدة
                    if (prev > 0 && msgList.size() > prev) {
                        ChatMessage latest = msgList.get(msgList.size() - 1);
                        if (!latest.getSender().equals(currentUser))
                            showLocalNotification(latest);
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError err) {
                    if (tvStatus  != null) tvStatus.setText(getString(R.string.chat_offline));
                    if (onlineDot != null) onlineDot.setVisibility(View.INVISIBLE);
                }
            };
            dbRef.orderByChild("timestamp").addValueEventListener(listener);
        } catch (Exception e) {
            if (tvStatus != null) tvStatus.setText(getString(R.string.chat_offline));
        }
    }

    private void sendTextMessage() {
        if (dbRef == null || etMsg == null) return;
        String text = etMsg.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        dbRef.push().setValue(new ChatMessage(currentUser, text, System.currentTimeMillis()))
             .addOnFailureListener(e -> Toast.makeText(getContext(), "فشل الإرسال", Toast.LENGTH_SHORT).show());
        etMsg.setText("");
    }

    // ── Notification ──────────────────────────────────────
    private void showLocalNotification(ChatMessage msg) {
        if (getContext() == null) return;

        String senderName = "essa".equals(msg.getSender()) ? "Essa 💜" : "حزامه 🌸";
        String body = ChatMessage.TYPE_IMAGE.equals(msg.getType())
                ? "أرسل لك " + senderName + " صورة 📷"
                : ChatMessage.TYPE_AUDIO.equals(msg.getType())
                        ? "أرسل لك " + senderName + " رسالة صوتية 🎤"
                        : "أرسل لك " + senderName + " رسالة";

        // ✅ Intent يفتح MainActivity ويذهب مباشرة لتاب الدردشة
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.putExtra("open_tab", 2); // tab index للدردشة
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            getContext(), 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("💜 رسالة جديدة")
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)  // ✅ يختفي تلقائياً عند الضغط
            .setVibrate(new long[]{0, 200, 100, 200})
            .setContentIntent(pendingIntent);  // ✅ يفتح الدردشة عند الضغط

        NotificationManager nm = (NotificationManager)
            getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NOTIF_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getContext() != null) {
            NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "دردشة الحب 💜", NotificationManager.IMPORTANCE_HIGH);
            ch.enableVibration(true);
            NotificationManager nm = (NotificationManager)
                getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && getActivity() != null)
            getActivity().requestPermissions(
                new String[]{"android.permission.POST_NOTIFICATIONS"}, 100);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (dbRef != null && listener != null) dbRef.removeEventListener(listener);
        if (mediaRecorder != null) { mediaRecorder.release(); mediaRecorder = null; }
    }
}
