package com.love.essahazama;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MemoryDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "memory_title";
    public static final String EXTRA_DATE  = "memory_date";
    public static final String EXTRA_EMOJI = "memory_emoji";
    public static final String EXTRA_KEY   = "memory_key";   // key فريد للذكرى

    private EditText etNotes;
    private String   memoryKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_detail);

        // استقبال البيانات
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String date  = getIntent().getStringExtra(EXTRA_DATE);
        String emoji = getIntent().getStringExtra(EXTRA_EMOJI);
        memoryKey    = getIntent().getStringExtra(EXTRA_KEY);

        if (memoryKey == null) memoryKey = title != null ? title : "memory";

        // تعبئة الحقول
        TextView tvEmoji = findViewById(R.id.tvDetailEmoji);
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvDate  = findViewById(R.id.tvDetailDate);
        etNotes          = findViewById(R.id.etMemoryNotes);
        Button btnSave   = findViewById(R.id.btnSaveMemory);
        Button btnAdd    = findViewById(R.id.btnAddImage);

        if (tvEmoji != null && emoji != null) tvEmoji.setText(emoji);
        if (tvTitle != null && title != null) tvTitle.setText(title);
        if (tvDate  != null && date  != null) tvDate.setText(date);

        // تحميل الملاحظات المحفوظة
        SharedPreferences prefs = getSharedPreferences("memory_notes", Context.MODE_PRIVATE);
        String savedNotes = prefs.getString(memoryKey, "");
        if (etNotes != null) etNotes.setText(savedNotes);

        // حفظ الملاحظات
        if (btnSave != null) btnSave.setOnClickListener(v -> {
            String notes = etNotes != null ? etNotes.getText().toString() : "";
            prefs.edit().putString(memoryKey, notes).apply();
            Toast.makeText(this, "✅ تم الحفظ 💜", Toast.LENGTH_SHORT).show();
            finish();
        });

        // زر إضافة صورة (قريباً)
        if (btnAdd != null) btnAdd.setOnClickListener(v ->
            Toast.makeText(this, "قريباً 🌸", Toast.LENGTH_SHORT).show());
    }
}
