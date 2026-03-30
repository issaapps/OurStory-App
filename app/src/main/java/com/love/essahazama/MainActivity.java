package com.love.essahazama;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    public LinearLayout navHome, navCounter, navChat, navMessages, navMemories, navSettings;
    private View dotHome, dotCounter, dotChat, dotMessages, dotMemories, dotSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navHome     = findViewById(R.id.navHome);
        navCounter  = findViewById(R.id.navCounter);
        navChat     = findViewById(R.id.navChat);
        navMessages = findViewById(R.id.navMessages);
        navMemories = findViewById(R.id.navMemories);
        navSettings = findViewById(R.id.navSettings);

        dotHome     = findViewById(R.id.navHomeDot);
        dotCounter  = findViewById(R.id.navCounterDot);
        dotChat     = findViewById(R.id.navChatDot);
        dotMessages = findViewById(R.id.navMessagesDot);
        dotMemories = findViewById(R.id.navMemoriesDot);
        dotSettings = findViewById(R.id.navSettingsDot);

        navHome.setOnClickListener(v     -> loadFragment(new HomeFragment(),     0));
        navCounter.setOnClickListener(v  -> loadFragment(new CounterFragment(),  1));
        navChat.setOnClickListener(v     -> loadFragment(new ChatFragment(),      2));
        navMessages.setOnClickListener(v -> loadFragment(new MessagesFragment(), 3));
        navMemories.setOnClickListener(v -> loadFragment(new MemoriesFragment(), 4));
        navSettings.setOnClickListener(v -> loadFragment(new SettingsFragment(), 5));

        // ✅ افتح التاب المطلوب من الإشعار
        int openTab = getIntent().getIntExtra("open_tab", 0);
        openTabByIndex(openTab);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // ✅ عند الضغط على الإشعار والتطبيق مفتوح
        int openTab = intent.getIntExtra("open_tab", -1);
        if (openTab >= 0) openTabByIndex(openTab);
    }

    private void openTabByIndex(int tab) {
        switch (tab) {
            case 1: loadFragment(new CounterFragment(),  1); break;
            case 2: loadFragment(new ChatFragment(),      2); break;
            case 3: loadFragment(new MessagesFragment(), 3); break;
            case 4: loadFragment(new MemoriesFragment(), 4); break;
            case 5: loadFragment(new SettingsFragment(), 5); break;
            default: loadFragment(new HomeFragment(), 0); break;
        }
    }

    public void loadFragment(Fragment fragment, int tab) {
        updateNavState(tab);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        tx.replace(R.id.fragmentContainer, fragment);
        tx.commit();
    }

    private void updateNavState(int activeTab) {
        navHome.setAlpha(0.45f);     navCounter.setAlpha(0.45f);
        navChat.setAlpha(0.45f);     navMessages.setAlpha(0.45f);
        navMemories.setAlpha(0.45f); navSettings.setAlpha(0.45f);

        dotHome.setVisibility(View.INVISIBLE);     dotCounter.setVisibility(View.INVISIBLE);
        dotChat.setVisibility(View.INVISIBLE);     dotMessages.setVisibility(View.INVISIBLE);
        dotMemories.setVisibility(View.INVISIBLE); dotSettings.setVisibility(View.INVISIBLE);

        switch (activeTab) {
            case 0: navHome.setAlpha(1f);     dotHome.setVisibility(View.VISIBLE);     break;
            case 1: navCounter.setAlpha(1f);  dotCounter.setVisibility(View.VISIBLE);  break;
            case 2: navChat.setAlpha(1f);     dotChat.setVisibility(View.VISIBLE);     break;
            case 3: navMessages.setAlpha(1f); dotMessages.setVisibility(View.VISIBLE); break;
            case 4: navMemories.setAlpha(1f); dotMemories.setVisibility(View.VISIBLE); break;
            case 5: navSettings.setAlpha(1f); dotSettings.setVisibility(View.VISIBLE); break;
        }
    }
}
