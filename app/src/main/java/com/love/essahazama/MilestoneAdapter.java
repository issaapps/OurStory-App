package com.love.essahazama;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MilestoneAdapter extends RecyclerView.Adapter<MilestoneAdapter.MilestoneViewHolder> {

    private List<Milestone> milestones;

    public MilestoneAdapter(List<Milestone> milestones) {
        this.milestones = milestones;
    }

    @NonNull
    @Override
    public MilestoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_milestone, parent, false);
        return new MilestoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MilestoneViewHolder holder, int position) {
        Milestone milestone = milestones.get(position);
        holder.tvEmoji.setText(milestone.getEmoji());
        holder.tvTitle.setText(milestone.getTitle());
        holder.tvDate.setText(milestone.getDate());
    }

    @Override
    public int getItemCount() {
        return milestones.size();
    }

    public void updateList(List<Milestone> newList) {
        this.milestones = newList;
        notifyDataSetChanged();
    }

    static class MilestoneViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvTitle, tvDate;

        MilestoneViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvMilestoneEmoji);
            tvTitle = itemView.findViewById(R.id.tvMilestoneTitle);
            tvDate = itemView.findViewById(R.id.tvMilestoneDate);
        }
    }
}
