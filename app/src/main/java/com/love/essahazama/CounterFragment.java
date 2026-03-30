package com.love.essahazama;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.util.concurrent.TimeUnit;

public class CounterFragment extends Fragment {

    private TextView    tvBigDays, tvYears, tvMonths, tvRemDays;
    private TextView    tvHours, tvMinutes, tvSeconds;
    private TextView    tvConfessionDays, tvTotalHours, tvTotalMins, tvLovePct;
    private ProgressBar progressLove;

    private final Handler  handler = new Handler(Looper.getMainLooper());
    private       Runnable ticker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_counter, container, false);

        tvBigDays        = view.findViewById(R.id.tvBigDays);
        tvYears          = view.findViewById(R.id.tvYears);
        tvMonths         = view.findViewById(R.id.tvMonths);
        tvRemDays        = view.findViewById(R.id.tvRemDays);
        tvHours          = view.findViewById(R.id.tvHours);
        tvMinutes        = view.findViewById(R.id.tvMinutes);
        tvSeconds        = view.findViewById(R.id.tvSeconds);
        tvConfessionDays = view.findViewById(R.id.tvConfessionDays);
        tvTotalHours     = view.findViewById(R.id.tvTotalHours);
        tvTotalMins      = view.findViewById(R.id.tvTotalMins);
        tvLovePct        = view.findViewById(R.id.tvLovePct);
        progressLove     = view.findViewById(R.id.progressLove);

        ticker = new Runnable() {
            @Override public void run() {
                refresh();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(ticker);
        return view;
    }

    private void refresh() {
        long ms   = LoveDates.msSinceTalk();
        int[] bd  = LoveDates.breakdown();

        long totalDays = TimeUnit.MILLISECONDS.toDays(ms);
        long totalHrs  = TimeUnit.MILLISECONDS.toHours(ms);
        long totalMins = TimeUnit.MILLISECONDS.toMinutes(ms);

        tvBigDays.setText(String.valueOf(totalDays));
        tvYears.setText(String.valueOf(bd[0]));
        tvMonths.setText(String.valueOf(bd[1]));
        tvRemDays.setText(String.valueOf(bd[2]));
        if (tvHours   != null) tvHours.setText(String.format("%02d", bd[3]));
        if (tvMinutes != null) tvMinutes.setText(String.format("%02d", bd[4]));
        if (tvSeconds != null) tvSeconds.setText(String.format("%02d", bd[5]));

        if (tvTotalHours != null)
            tvTotalHours.setText(String.format("%,d", totalHrs));
        if (tvTotalMins != null)
            tvTotalMins.setText(String.format("%,d", totalMins));
        if (tvLovePct    != null) tvLovePct.setText("100%");
        if (progressLove != null) progressLove.setProgress(100);

        if (tvConfessionDays != null) {
            long confDays = LoveDates.daysSinceConfession();
            tvConfessionDays.setText(confDays + " " + getString(R.string.days_since_confession));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(ticker);
    }
}