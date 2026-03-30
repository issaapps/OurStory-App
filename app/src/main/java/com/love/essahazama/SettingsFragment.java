package com.love.essahazama;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private ActivityResultLauncher<Intent> pinLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pinLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK)
                    new AlertDialog.Builder(getContext())
                        .setMessage("✅ تم حفظ كلمة السر بنجاح!")
                        .setPositiveButton("حسناً", null).show();
            });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        LinearLayout cardSet    = view.findViewById(R.id.cardSetPin);
        LinearLayout cardDelete = view.findViewById(R.id.cardHazamaPin);
        LinearLayout cardVerify = view.findViewById(R.id.cardVerifyPin);

        // ── إضافة / تعديل ────────────────────────────────
        if (cardSet != null) cardSet.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LockActivity.class);
            intent.putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_SET);
            pinLauncher.launch(intent);
        });

        // ── حذف كلمة السر ────────────────────────────────
        if (cardDelete != null) cardDelete.setOnClickListener(v ->
            new AlertDialog.Builder(getContext())
                .setTitle("حذف كلمة السر")
                .setMessage("هل تريد إزالة قفل التطبيق نهائياً؟")
                .setPositiveButton("نعم، احذف", (d, w) -> {
                    getContext().getSharedPreferences(LockActivity.PREFS_APP,
                        Context.MODE_PRIVATE).edit()
                        .remove(LockActivity.KEY_APP_PIN).apply();
                    new AlertDialog.Builder(getContext())
                        .setMessage("✅ تم حذف كلمة السر")
                        .setPositiveButton("حسناً", null).show();
                })
                .setNegativeButton("إلغاء", null).show());

        // ── اختبار ───────────────────────────────────────
        if (cardVerify != null) cardVerify.setOnClickListener(v -> {
            String savedPin = getContext()
                .getSharedPreferences(LockActivity.PREFS_APP, Context.MODE_PRIVATE)
                .getString(LockActivity.KEY_APP_PIN, null);

            if (savedPin == null) {
                new AlertDialog.Builder(getContext())
                    .setMessage("⚠️ لا توجد كلمة سر — أضف واحدة أولاً")
                    .setPositiveButton("حسناً", null).show();
                return;
            }
            Intent intent = new Intent(getContext(), LockActivity.class);
            intent.putExtra(LockActivity.EXTRA_MODE, LockActivity.MODE_VERIFY);
            intent.putExtra("custom_pin", savedPin);
            pinLauncher.launch(intent);
        });

        return view;
    }
}
