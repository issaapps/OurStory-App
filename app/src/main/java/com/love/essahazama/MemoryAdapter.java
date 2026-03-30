package com.love.essahazama;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MemoryAdapter extends RecyclerView.Adapter<MemoryAdapter.VH> {

    public interface OnClick    { void onClick(Memory m, int pos); }
    public interface OnLongClick{ void onLongClick(Memory m, int pos); }

    private List<Memory>    list;
    private final OnClick   listener;
    private final OnLongClick longListener;

    public MemoryAdapter(List<Memory> list, OnClick listener, OnLongClick longListener) {
        this.list         = list;
        this.listener     = listener;
        this.longListener = longListener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_memory, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Memory m = list.get(pos);

        h.emoji.setText(m.getEmoji());
        h.name.setText(m.getTitle());
        h.date.setText(m.getDate());
        h.tag.setText(m.getTag());
        h.img.setBackgroundColor(m.getBgColor());

        // description
        if (!TextUtils.isEmpty(m.getDesc())) {
            h.desc.setText(m.getDesc());
            h.desc.setVisibility(View.VISIBLE);
        } else {
            h.desc.setVisibility(View.GONE);
        }

        // favourite star
        h.favStar.setVisibility(m.isFavourite() ? View.VISIBLE : View.GONE);

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(m, pos); });
        h.itemView.setOnLongClickListener(v -> {
            if (longListener != null) longListener.onLongClick(m, pos);
            return true;
        });

        // stagger fade-in animation
        h.itemView.setAlpha(0f);
        h.itemView.animate().alpha(1f).setDuration(280).setStartDelay(pos * 45L).start();
    }

    @Override public int getItemCount() { return list.size(); }

    public void update(List<Memory> newList) {
        list = newList;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView    emoji, name, date, tag, desc, favStar;
        FrameLayout img;

        VH(@NonNull View v) {
            super(v);
            emoji   = v.findViewById(R.id.tvMemEmoji);
            name    = v.findViewById(R.id.tvMemName);
            date    = v.findViewById(R.id.tvMemDate);
            tag     = v.findViewById(R.id.tvMemTag);
            desc    = v.findViewById(R.id.tvMemDesc);
            favStar = v.findViewById(R.id.tvFavStar);
            img     = v.findViewById(R.id.memImgArea);
        }
    }
}
