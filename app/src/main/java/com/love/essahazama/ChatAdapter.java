package com.love.essahazama;

import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT     = 0;
    private static final int TYPE_RECEIVED = 1;

    private final List<ChatMessage> messages;
    private final String            currentUser;
    private MediaPlayer mediaPlayer;

    public ChatAdapter(List<ChatMessage> messages, String currentUser) {
        this.messages    = messages;
        this.currentUser = currentUser;
    }

    @Override public int getItemViewType(int pos) {
        return messages.get(pos).getSender().equals(currentUser)
                ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SENT)
            return new SentVH(inf.inflate(R.layout.item_chat_sent, parent, false));
        else
            return new ReceivedVH(inf.inflate(R.layout.item_chat_received, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        ChatMessage msg = messages.get(pos);
        String timeStr  = new SimpleDateFormat("hh:mm a", new Locale("ar"))
                              .format(new Date(msg.getTimestamp()));
        String type = msg.getType();

        if (holder instanceof SentVH) {
            SentVH h = (SentVH) holder;
            h.tvTime.setText(timeStr);
            bindMessage(h.tvMsg, h.ivImage, h.tvSticker,
                        h.layoutAudio, h.btnPlay, h.tvDur,
                        msg, type, true);
        } else {
            ReceivedVH h = (ReceivedVH) holder;
            h.tvTime.setText(timeStr);
            String init = "essa".equals(msg.getSender()) ? "ع" : "هـ";
            h.tvAvatar.setText(init);
            h.tvAvatar.setBackgroundResource(
                "essa".equals(msg.getSender())
                    ? R.drawable.avatar_essa : R.drawable.avatar_hazama);
            bindMessage(h.tvMsg, h.ivImage, h.tvSticker,
                        h.layoutAudio, h.btnPlay, h.tvDur,
                        msg, type, false);
        }
    }

    private void bindMessage(TextView tvMsg, ImageView ivImg, TextView tvSticker,
                              LinearLayout layoutAudio, TextView btnPlay, TextView tvDur,
                              ChatMessage msg, String type, boolean sent) {
        // إخفاء الكل أولاً
        tvMsg.setVisibility(View.GONE);
        ivImg.setVisibility(View.GONE);
        tvSticker.setVisibility(View.GONE);
        layoutAudio.setVisibility(View.GONE);

        switch (type) {
            case ChatMessage.TYPE_TEXT:
            default:
                tvMsg.setText(msg.getText());
                tvMsg.setVisibility(View.VISIBLE);
                break;

            case ChatMessage.TYPE_STICKER:
                tvSticker.setText(msg.getText());
                tvSticker.setVisibility(View.VISIBLE);
                break;

            case ChatMessage.TYPE_IMAGE:
                ivImg.setVisibility(View.VISIBLE);
                if (msg.getMediaUrl() != null) {
                    Glide.with(ivImg.getContext())
                         .load(msg.getMediaUrl())
                         .placeholder(android.R.drawable.ic_menu_gallery)
                         .into(ivImg);
                }
                break;

            case ChatMessage.TYPE_AUDIO:
                layoutAudio.setVisibility(View.VISIBLE);
                tvDur.setText(msg.getText() != null ? msg.getText() : "0:00");
                btnPlay.setOnClickListener(v -> playAudio(msg.getMediaUrl(), btnPlay));
                break;

            case ChatMessage.TYPE_VIDEO:
                ivImg.setVisibility(View.VISIBLE);
                if (msg.getMediaUrl() != null) {
                    Glide.with(ivImg.getContext())
                         .load(msg.getMediaUrl())
                         .placeholder(android.R.drawable.ic_media_play)
                         .into(ivImg);
                }
                ivImg.setOnClickListener(v ->
                    Toast.makeText(v.getContext(), "افتح الفيديو", Toast.LENGTH_SHORT).show());
                break;
        }
    }

    private void playAudio(String url, TextView btnPlay) {
        if (url == null) return;
        try {
            if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            btnPlay.setText("⏸");
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> mp.start());
            mediaPlayer.setOnCompletionListener(mp -> {
                btnPlay.setText("▶");
                mp.release(); mediaPlayer = null;
            });
        } catch (Exception e) {
            btnPlay.setText("▶");
        }
    }

    @Override public int getItemCount() { return messages.size(); }

    static class SentVH extends RecyclerView.ViewHolder {
        TextView tvMsg, tvTime, tvSticker, btnPlay, tvDur;
        ImageView ivImage;
        LinearLayout layoutAudio;
        SentVH(View v) {
            super(v);
            tvMsg       = v.findViewById(R.id.tvSentMsg);
            tvTime      = v.findViewById(R.id.tvSentTime);
            tvSticker   = v.findViewById(R.id.tvSentSticker);
            ivImage     = v.findViewById(R.id.ivSentImage);
            layoutAudio = v.findViewById(R.id.layoutSentAudio);
            btnPlay     = v.findViewById(R.id.btnPlaySent);
            tvDur       = v.findViewById(R.id.tvSentAudioDur);
        }
    }

    static class ReceivedVH extends RecyclerView.ViewHolder {
        TextView tvMsg, tvTime, tvAvatar, tvSticker, btnPlay, tvDur;
        ImageView ivImage;
        LinearLayout layoutAudio;
        ReceivedVH(View v) {
            super(v);
            tvMsg       = v.findViewById(R.id.tvReceivedMsg);
            tvTime      = v.findViewById(R.id.tvReceivedTime);
            tvAvatar    = v.findViewById(R.id.tvSenderAvatar);
            tvSticker   = v.findViewById(R.id.tvReceivedSticker);
            ivImage     = v.findViewById(R.id.ivReceivedImage);
            layoutAudio = v.findViewById(R.id.layoutReceivedAudio);
            btnPlay     = v.findViewById(R.id.btnPlayReceived);
            tvDur       = v.findViewById(R.id.tvReceivedAudioDur);
        }
    }
}
