package com.love.essahazama;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

public class LockActivity extends AppCompatActivity {

    public static final String PREFS_APP    = "app_prefs";
    public static final String KEY_APP_PIN  = "app_pin";
    public static final String EXTRA_MODE   = "mode";
    public static final String MODE_SET     = "set";
    public static final String MODE_VERIFY  = "verify";

    private String  mode;
    private String  enteredPin  = "";
    private String  savedPin    = null;
    private TextView tvDisplay, tvTitle, tvSubtitle;
    private View[]  dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        mode     = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = MODE_VERIFY;

        SharedPreferences prefs = getSharedPreferences(PREFS_APP, Context.MODE_PRIVATE);
        savedPin = prefs.getString(KEY_APP_PIN, null);

        tvTitle    = findViewById(R.id.tvLockTitle);
        tvSubtitle = findViewById(R.id.tvLockSubtitle);
        tvDisplay  = findViewById(R.id.tvPinDisplay);

        dots = new View[]{
            findViewById(R.id.dot1), findViewById(R.id.dot2),
            findViewById(R.id.dot3), findViewById(R.id.dot4)
        };

        if (MODE_SET.equals(mode)) {
            if (tvTitle    != null) tvTitle.setText("🔑 أنشئ رقمك السري");
            if (tvSubtitle != null) tvSubtitle.setText("اختر 4 أرقام لحماية التطبيق");
        } else {
            if (tvTitle    != null) tvTitle.setText("🔒 أدخل رقمك السري");
            if (tvSubtitle != null) tvSubtitle.setText("مرحباً بك 💜");
        }

        // أزرار الأرقام
        int[] btnIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                        R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};
        for (int i = 0; i < btnIds.length; i++) {
            final int digit = i;
            View btn = findViewById(btnIds[i]);
            if (btn != null) btn.setOnClickListener(v -> addDigit(String.valueOf(digit)));
        }

        View btnDelete = findViewById(R.id.btnDelete);
        if (btnDelete != null) btnDelete.setOnClickListener(v -> deleteDigit());

        // بيومتريك فقط في وضع التحقق
        View btnBio = findViewById(R.id.btnBiometric);
        if (btnBio != null) {
            if (MODE_VERIFY.equals(mode) && savedPin != null && isBiometricAvailable()) {
                btnBio.setVisibility(View.VISIBLE);
                btnBio.setOnClickListener(v -> showBiometric());
                showBiometric();
            } else {
                btnBio.setVisibility(View.GONE);
            }
        }
    }

    private void addDigit(String digit) {
        if (enteredPin.length() >= 4) return;
        enteredPin += digit;
        updateDots();
        if (enteredPin.length() == 4) checkPin();
    }

    private void deleteDigit() {
        if (enteredPin.isEmpty()) return;
        enteredPin = enteredPin.substring(0, enteredPin.length() - 1);
        updateDots();
    }

    private void updateDots() {
        for (int i = 0; i < dots.length; i++) {
            if (dots[i] != null)
                dots[i].setBackgroundResource(
                    i < enteredPin.length() ? R.drawable.dot_purple : R.drawable.dot_pin_empty);
        }
    }

    private void checkPin() {
        if (MODE_SET.equals(mode)) {
            // حفظ PIN الجديد
            getSharedPreferences(PREFS_APP, Context.MODE_PRIVATE)
                .edit().putString(KEY_APP_PIN, enteredPin).apply();
            setResult(RESULT_OK);
            finish();

        } else {
            // التحقق
            if (enteredPin.equals(savedPin)) {
                // ✅ فتح MainActivity مرة واحدة فقط
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                wrongPin();
            }
        }
    }

    private void wrongPin() {
        enteredPin = "";
        updateDots();
        if (tvDisplay != null)
            tvDisplay.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));
        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vib != null && vib.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                vib.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
            else
                vib.vibrate(300);
        }
        if (tvSubtitle != null) tvSubtitle.setText("❌ رقم خاطئ، حاول مجدداً");
    }

    private boolean isBiometricAvailable() {
        BiometricManager bm = BiometricManager.from(this);
        return bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
               == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void showBiometric() {
        BiometricPrompt prompt = new BiometricPrompt(this,
            ContextCompat.getMainExecutor(this),
            new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult r) {
                    // ✅ نفس السلوك: فتح MainActivity وإغلاق LockActivity
                    Intent intent = new Intent(LockActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                @Override
                public void onAuthenticationFailed() {
                    if (tvSubtitle != null) tvSubtitle.setText("❌ فشل البصمة");
                }
            });

        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
            .setTitle("بصمة الإصبع")
            .setSubtitle("ادخل بالبصمة")
            .setNegativeButtonText("إلغاء")
            .build();

        prompt.authenticate(info);
    }

    // ✅ منع الرجوع للخلف (لا يمكن تخطي شاشة القفل)
    @Override
    public void onBackPressed() {
        if (MODE_VERIFY.equals(mode)) {
            // لا شيء — لا يمكن تخطي القفل
        } else {
            super.onBackPressed();
        }
    }
}
