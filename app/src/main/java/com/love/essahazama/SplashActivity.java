package com.love.essahazama;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.TimeUnit;

public class SplashActivity extends AppCompatActivity {

    private TextView tvDaysCount, tvHours, tvMinutes, tvSeconds;
    private Button btnEnter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable ticker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Find views
        tvDaysCount = findViewById(R.id.tvDaysCount);
        tvHours     = findViewById(R.id.tvSplashHours);
        tvMinutes   = findViewById(R.id.tvSplashMinutes);
        tvSeconds   = findViewById(R.id.tvSplashSeconds);
        btnEnter    = findViewById(R.id.btnEnter);

        // Animation
        View root = findViewById(android.R.id.content);
        if (root != null) {
            AlphaAnimation fade = new AlphaAnimation(0f, 1f);
            fade.setDuration(1200);
            root.startAnimation(fade);
        }

        // Setup Ticker
        ticker = new Runnable() {
            @Override
            public void run() {
                updateCounter();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(ticker);

        // Button listener
        if (btnEnter != null) {
            btnEnter.setOnClickListener(v -> {
                handler.removeCallbacks(ticker);
                decideNext();
            });
        }

        // Optional: Auto transition after a longer delay if needed, 
        // but typically with an "Enter" button, we might wait for the user.
        // For now, let's keep a 5-second auto-transition or just rely on the button.
        // The user's original had 2 seconds. Let's keep it but maybe increase it 
        // so they can actually see the counter.
        handler.postDelayed(this::decideNext, 5000);
    }

    private void updateCounter() {
        long ms = LoveDates.msSinceTalk();
        long totalDays = TimeUnit.MILLISECONDS.toDays(ms);
        int[] bd = LoveDates.breakdown(); // [y, m, d, h, min, s]

        if (tvDaysCount != null) tvDaysCount.setText(String.valueOf(totalDays));
        if (tvHours     != null) tvHours.setText(String.format("%02d", bd[3]));
        if (tvMinutes   != null) tvMinutes.setText(String.format("%02d", bd[4]));
        if (tvSeconds   != null) tvSeconds.setText(String.format("%02d", bd[5]));
    }

    private void decideNext() {
        // Prevent double calling
        handler.removeCallbacksAndMessages(null);
        
        SharedPreferences prefs = getSharedPreferences(
                LockActivity.PREFS_APP, Context.MODE_PRIVATE);
        String pin = prefs.getString(LockActivity.KEY_APP_PIN, null);

        if (pin != null) {
            Intent intent = new Intent(this, LockActivity.class);
            intent.putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_VERIFY);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
