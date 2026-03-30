package com.love.essahazama;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    private final String[] morning = {
        "صباح الجمال يا وجه الخير،\nنورتِ يومي بابتسامتكِ 🌸",
        "يسعد صباحكِ يا حب عمري،\nكل يوم معكِ هو عيد جديد 💜",
        "يا صباح الحب والشوق،\nعيونكِ هي شمس حياتي ☀️"
    };
    private final String[] afternoon = {
        "طاب يومكِ يا أغلى الناس،\nمكانكِ في قلبي دائماً 💕",
        "نصف النهار مرّ وأنا أفكر فيكِ،\nغلاوتكِ تزيد كل لحظة 🌺",
        "أنتِ أجمل ما في هذا اليوم،\nربي يحفظكِ لي دائماً 💜"
    };
    private final String[] evening = {
        "مساء الحب والحنان،\nيا أغلى من الروح 🌙",
        "طلتكِ في المساء تشبه القمر،\nيا منيرة حياتي ✨",
        "أجمل مساء هو الذي\nينتهي بسماع صوتكِ 💌"
    };
    private final String[] night = {
        "تصبحي على خير يا ملاكي،\nأحلام سعيدة معكِ 💜",
        "بين النجوم أرى وجهكِ،\nأنتِ قمري في عتمة الليل 🌟",
        "نوم الهنا يا نبض قلبي،\nاستودعتكِ الله في كل حين 🤍"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView tvDaysHome  = view.findViewById(R.id.tvDaysHome);
        TextView tvQuote     = view.findViewById(R.id.tvQuote);
        TextView tvQuickDays = view.findViewById(R.id.tvQuickDays);

        LinearLayout cardCounter  = view.findViewById(R.id.cardQuickCounter);
        LinearLayout cardMessage  = view.findViewById(R.id.cardQuickMessage);
        LinearLayout cardMemories = view.findViewById(R.id.cardQuickMemories);

        // Count from first talk: Oct 25 2025
        long days = LoveDates.daysSinceTalk();
        tvDaysHome.setText(days + " يوم منذ أول حديث 💕");
        tvQuickDays.setText(days + " يوم");
        tvQuote.setText(getGazal());

        // Tab indices: 0=home 1=counter 2=chat 3=messages 4=memories
        cardCounter.setOnClickListener(v  -> nav(1));
        cardMessage.setOnClickListener(v  -> nav(3));
        cardMemories.setOnClickListener(v -> nav(4));

        return view;
    }

    private void nav(int tab) {
        if (!(getActivity() instanceof MainActivity)) return;
        MainActivity main = (MainActivity) getActivity();
        switch (tab) {
            case 1: main.loadFragment(new CounterFragment(),  1); break;
            case 3: main.loadFragment(new MessagesFragment(), 3); break;
            case 4: main.loadFragment(new MemoriesFragment(), 4); break;
        }
    }

    private String getGazal() {
        int hour  = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int index = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % 3;
        if (hour >= 5  && hour < 12) return morning[index];
        if (hour >= 12 && hour < 17) return afternoon[index];
        if (hour >= 17 && hour < 21) return evening[index];
        return night[index];
    }
}
