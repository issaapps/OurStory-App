package com.love.essahazama;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MessagesFragment extends Fragment {

    private static final String[] GAZAL = {
        "يا من سكنتِ فؤادي والروح تطلبكِ…\nأنتِ الحياةُ وفي عينيكِ أوطاني 💜",
        "أنتِ النور الذي يضيء عتمي،\nأحبكِ يا حزامه أكثر من كل الكلمات 🌹",
        "كل يوم معكِ هو عمر جديد،\nربي لا يحرمني منكِ في أي يوم 💕",
        "حزامه… يا أجمل صدفة في حياتي،\nلو عاد الزمن لاخترتكِ في كل مرة 💜",
        "قلبي يرفض أن ينسى ابتسامتكِ،\nكأنها نُقشت فيه منذ الأزل 🌸",
        "بعيداً عنكِ تُصبح الأيام ثقيلة،\nوقريباً منكِ كل شيء يصبح جميلاً ✨",
        "أنتِ أكثر مما كنت أحلم به،\nوأجمل مما تستطيع الكلمات وصفه 💜"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        try {
            TextView tvMsg  = view.findViewById(R.id.tvDailyMessage);
            TextView tvDate = view.findViewById(R.id.tvMsgDate);
            Button btnLove  = view.findViewById(R.id.btnLoveReact);
            Button btnReply = view.findViewById(R.id.btnReplyReact);
            EditText etMsg  = view.findViewById(R.id.etNewMessage);
            Button btnSend  = view.findViewById(R.id.btnSendMessage);

            // رسالة اليوم
            int idx = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % GAZAL.length;
            if (tvMsg != null) tvMsg.setText(GAZAL[idx]);

            // التاريخ
            if (tvDate != null) {
                String date = new SimpleDateFormat("dd MMMM", new Locale("ar"))
                        .format(new Date());
                tvDate.setText(date);
            }

            // زر الحب
            if (btnLove != null) {
                btnLove.setOnClickListener(v -> {
                    vibrate();
                    Toast.makeText(getContext(), "💜 حزامه استلمت حبك!", Toast.LENGTH_SHORT).show();
                });
            }

            // زر الرد
            if (btnReply != null && etMsg != null) {
                btnReply.setOnClickListener(v -> {
                    etMsg.requestFocus();
                    etMsg.setHint("اكتب ردك الجميل لحزامه…");
                });
            }

            // زر الإرسال
            if (btnSend != null && etMsg != null) {
                btnSend.setOnClickListener(v -> {
                    String text = etMsg.getText().toString().trim();
                    if (TextUtils.isEmpty(text)) {
                        Toast.makeText(getContext(), "اكتب رسالتك أولاً 💌", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // حفظ الرسالة
                    SharedPreferences prefs = requireContext()
                            .getSharedPreferences("love_messages", Context.MODE_PRIVATE);
                    prefs.edit().putString("msg_" + System.currentTimeMillis(), text).apply();
                    etMsg.setText("");
                    vibrate();
                    Toast.makeText(getContext(), "💜 تم الإرسال بكل الحب!", Toast.LENGTH_LONG).show();
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    private void vibrate() {
        try {
            Vibrator vib = (Vibrator) requireContext()
                    .getSystemService(Context.VIBRATOR_SERVICE);
            if (vib != null && vib.hasVibrator()) vib.vibrate(60);
        } catch (Exception ignored) {}
    }
}
