package com.love.essahazama;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MemoriesFragment extends Fragment {

    private MemoryAdapter adapter;
    private List<Memory>  allMemories;
    private TextView      tvMemCount;
    private String        activeFilter = "all";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memories, container, false);

        RecyclerView rv      = view.findViewById(R.id.rvMemories);
        Button btnAdd        = view.findViewById(R.id.btnAddMemory);
        Button chipAll       = view.findViewById(R.id.chipAll);
        Button chipDates     = view.findViewById(R.id.chipDates);
        Button chipBirthdays = view.findViewById(R.id.chipBirthdays);
        Button chipTravel    = view.findViewById(R.id.chipTravel);
        Button chipSpecial   = view.findViewById(R.id.chipSpecial);  // قد يكون null
        EditText etSearch    = view.findViewById(R.id.etSearchMemory);
        if (etSearch != null) etSearch.setHintTextColor(android.graphics.Color.parseColor("#A78BFA")); // قد يكون null
        tvMemCount           = view.findViewById(R.id.tvMemCount);     // قد يكون null

        allMemories = buildMemories();
        updateCount(allMemories.size());

        // FIX: استخدام adapter.update() بدلاً من adapter.updateList()
        adapter = new MemoryAdapter(
            allMemories,
            (memory, pos) -> showDetailDialog(memory, pos),
            (memory, pos) -> {
                memory.setFavourite(!memory.isFavourite());
                adapter.notifyItemChanged(pos);
                Toast.makeText(getContext(),
                        memory.isFavourite() ? "⭐ أضيفت للمفضلة!" : "تمت الإزالة",
                        Toast.LENGTH_SHORT).show();
            }
        );

        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rv.setAdapter(adapter);

        // ── Filter chips ──────────────────────────────────
        chipAll.setOnClickListener(v -> {
            activeFilter = "all";
            resetChips(chipAll, chipDates, chipBirthdays, chipTravel, chipSpecial);
            on(chipAll);
            applyFilter(etSearch != null ? etSearch.getText().toString() : "");
        });
        chipDates.setOnClickListener(v -> {
            // FIX: تصفية بالعربية لتتطابق مع buildMemories()
            activeFilter = "موعد";
            resetChips(chipAll, chipDates, chipBirthdays, chipTravel, chipSpecial);
            on(chipDates);
            applyFilter(etSearch != null ? etSearch.getText().toString() : "");
        });
        chipBirthdays.setOnClickListener(v -> {
            activeFilter = "عيد";
            resetChips(chipAll, chipDates, chipBirthdays, chipTravel, chipSpecial);
            on(chipBirthdays);
            applyFilter(etSearch != null ? etSearch.getText().toString() : "");
        });
        chipTravel.setOnClickListener(v -> {
            activeFilter = "سفر";
            resetChips(chipAll, chipDates, chipBirthdays, chipTravel, chipSpecial);
            on(chipTravel);
            applyFilter(etSearch != null ? etSearch.getText().toString() : "");
        });
        if (chipSpecial != null) {
            chipSpecial.setOnClickListener(v -> {
                activeFilter = "خاص";
                resetChips(chipAll, chipDates, chipBirthdays, chipTravel, chipSpecial);
                on(chipSpecial);
                applyFilter(etSearch != null ? etSearch.getText().toString() : "");
            });
        }

        // ── Live search (nullable safe) ───────────────────
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                    applyFilter(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        btnAdd.setOnClickListener(v -> showAddDialog());
        return view;
    }

    // ── Filter + Search combined ──────────────────────────
    private void applyFilter(String query) {
        List<Memory> result = new ArrayList<>();
        for (Memory m : allMemories) {
            boolean tagMatch   = activeFilter.equals("all") || m.getTag().contains(activeFilter);
            boolean queryMatch = TextUtils.isEmpty(query)
                    || m.getTitle().contains(query)
                    || m.getDate().contains(query)
                    || m.getTag().contains(query);
            if (tagMatch && queryMatch) result.add(m);
        }
        adapter.update(result);
        updateCount(result.size());
    }

    private void updateCount(int n) {
        if (tvMemCount != null) tvMemCount.setText(n + " ذكرى جميلة 💜");
    }

    // ── Detail dialog ─────────────────────────────────────
    private void showDetailDialog(Memory m, int pos) {
        if (getContext() == null) return;
        android.content.Intent intent = new android.content.Intent(getContext(), MemoryDetailActivity.class);
        intent.putExtra(MemoryDetailActivity.EXTRA_TITLE, m.getTitle());
        intent.putExtra(MemoryDetailActivity.EXTRA_DATE,  m.getDate());
        intent.putExtra(MemoryDetailActivity.EXTRA_EMOJI, m.getEmoji());
        intent.putExtra(MemoryDetailActivity.EXTRA_KEY,   m.getTitle() + "_" + m.getDate());
        startActivity(intent);
    }

    private void _unused(Memory m, int pos) {
        String desc = m.getDesc() != null && !m.getDesc().isEmpty()
                ? "\n\n" + m.getDesc() : "";
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
            .setTitle(m.getEmoji() + "  " + m.getTitle())
            .setMessage("📅 " + m.getDate() + "\n🏷️ " + m.getTag() + desc)
            .setPositiveButton("💜 حسناً", null)
            .setNegativeButton(m.isFavourite() ? "✖ إزالة من المفضلة" : "⭐ مفضلة",
                (d, w) -> {
                    m.setFavourite(!m.isFavourite());
                    adapter.notifyItemChanged(pos);
                })
            .show();
    }

    // ── Add Memory dialog ─────────────────────────────────
    private void showAddDialog() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#F3E8FF"));
        layout.setPadding(52, 40, 52, 28);

        EditText etTitle = inp(getString(R.string.memory_title_hint));
        EditText etDate  = inp(getString(R.string.memory_date_hint));
        EditText etTag   = inp(getString(R.string.memory_tag_hint));
        EditText etDesc  = inp(getString(R.string.memory_desc_hint));

        layout.addView(etTitle); layout.addView(sep());
        layout.addView(etDate);  layout.addView(sep());
        layout.addView(etTag);   layout.addView(sep());
        layout.addView(etDesc);

        new AlertDialog.Builder(getContext())
            .setTitle(getString(R.string.add_memory_title))
            .setView(layout)
            .setPositiveButton(getString(R.string.add_btn), (d, w) -> {
                String t  = etTitle.getText().toString().trim();
                String dt = etDate.getText().toString().trim();
                String tg = etTag.getText().toString().trim();
                String ds = etDesc.getText().toString().trim();

                if (TextUtils.isEmpty(t)) {
                    Toast.makeText(getContext(), "أضف عنواناً للذكرى", Toast.LENGTH_SHORT).show();
                    return;
                }
                Memory mem = new Memory("💜", t,
                        TextUtils.isEmpty(dt) ? "٢٠٢٥" : dt,
                        TextUtils.isEmpty(tg) ? "خاص"  : tg,
                        ds,
                        Color.parseColor("#F3E8FF"));
                allMemories.add(0, mem);
                applyFilter("");
                Toast.makeText(getContext(),
                        getString(R.string.memory_added), Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(getString(R.string.cancel_btn), null)
            .show();
    }

    // ── Helpers ───────────────────────────────────────────
    private void on(Button b) {
        b.setBackgroundResource(R.drawable.chip_active);
        b.setTextColor(Color.WHITE);
    }

    private void resetChips(Button... chips) {
        for (Button c : chips) {
            if (c == null) continue;
            c.setBackgroundResource(R.drawable.chip_idle);
            c.setTextColor(Color.parseColor("#D8B4FE"));
        }
    }

    private EditText inp(String hint) {
        EditText e = new EditText(getContext());
        e.setHint(hint);
        e.setHintTextColor(Color.parseColor("#A78BFA"));
        e.setTextColor(Color.parseColor("#1E0A2E"));
        e.setBackground(null);
        e.setPadding(0, 10, 0, 10);
        return e;
    }

    private View sep() {
        View v = new View(getContext());
        v.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        v.setBackgroundColor(Color.parseColor("#33A855F7"));
        return v;
    }

    // ── Memories data – starts from Oct 25 2025 ──────────
    private List<Memory> buildMemories() {
        List<Memory> l = new ArrayList<>();
        // FIX: التصنيفات بالعربية لتتطابق مع فلتر الـ chips
        l.add(new Memory("💬", "أول محادثة",
                "٢٥ أكتوبر ٢٠٢٥", "موعد",
                "أول كلمة قالها Essa لحزامه، بداية كل شيء الجميل 🌱",
                Color.parseColor("#EFF6FF")));
        l.add(new Memory("❤️", "الاعتراف بالحب",
                "١١ ديسمبر ٢٠٢٥", "موعد",
                "اليوم الذي قال فيه Essa كلمة الحب لأول مرة 💜",
                Color.parseColor("#FDF2F8")));
        l.add(new Memory("💜", "أول رسالة صوتية",
                "ديسمبر ٢٠٢٥", "موعد",
                "صوت حزامه أجمل موسيقى سمعها Essa 🎵",
                Color.parseColor("#F3E8FF")));
        l.add(new Memory("🌙", "جلسة تحت القمر",
                "٢٠٢٥", "موعد", "", Color.parseColor("#F5F3FF")));
        l.add(new Memory("🎂", "عيد ميلادها",
                "٢٠٢٦", "عيد",
                "يوم أضاءت حزامه الدنيا بميلادها 🎉",
                Color.parseColor("#FFFBEB")));
        l.add(new Memory("✈️", "أول رحلة معاً",
                "٢٠٢٦", "سفر", "", Color.parseColor("#EFF6FF")));
        l.add(new Memory("🎁", "مفاجأة خاصة",
                "٢٠٢٦", "خاص", "", Color.parseColor("#F0FDF4")));
        l.add(new Memory("⭐", "لحظة لا تُنسى",
                "٢٠٢٦", "خاص", "", Color.parseColor("#FAFAF0")));
        l.add(new Memory("🌺", "نزهة الربيع",
                "٢٠٢٦", "سفر", "", Color.parseColor("#F0FDF4")));
        l.add(new Memory("💍", "وعد الأبد",
                "٢٠٢٦", "خاص",
                "وعد لن يُنسى، وحب لن ينتهي 💜",
                Color.parseColor("#FDF2F8")));
        return l;
    }
}
