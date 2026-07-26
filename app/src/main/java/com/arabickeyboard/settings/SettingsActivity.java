package com.arabickeyboard.settings;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.arabickeyboard.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * شاشة الإعدادات الرئيسية.
 * تحتوي على 3 تبويبات:
 * 1. اختصارات النصوص (Text Shortcuts)
 * 2. استبدال المسافة (Space Replacement)
 * 3. الكتابة التلقائية (Auto Typing)
 */
public class SettingsActivity extends AppCompatActivity {

    // ================== Tab Titles ==================

    private static final String[] TAB_TITLES = {
            "الاختصارات",
            "المسافة",
            "كتابة تلقائية"
    };

    // ================== Views ==================

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    // ================== Lifecycle ==================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupToolbar();
        setupViewPager();
    }

    // ================== Setup ==================

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewPager() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        // Adapter يحتوي على 3 Fragments
        SettingsPagerAdapter adapter = new SettingsPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // ربط الـ Tabs بالـ ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(TAB_TITLES[position]);
        }).attach();
    }

    // ================== ViewPager Adapter ==================

    /**
     * Adapter يُرجع الـ Fragment المناسب لكل تبويب.
     */
    private static class SettingsPagerAdapter extends FragmentStateAdapter {

        public SettingsPagerAdapter(FragmentActivity activity) {
            super(activity);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:  return new ShortcutsFragment();
                case 1:  return new SpaceReplacementFragment();
                case 2:  return new AutoTypingFragment();
                default: return new ShortcutsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return TAB_TITLES.length;
        }
    }
}
