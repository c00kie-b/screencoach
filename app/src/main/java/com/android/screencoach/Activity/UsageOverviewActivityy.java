package com.android.achievix.Activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.achievix.R;
import com.android.achievix.Adapter.ViewPagerAdapterr;
import com.android.achievix.Fragment.AppLaunchesFragmentt;
import com.android.achievix.Fragment.UsageOverviewFragmentt;
import com.google.android.material.tabs.TabLayout;

public class UsageOverviewActivityy extends AppCompatActivity {
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_overvieww);

        TabLayout tabLayout = findViewById(R.id.stats_tab_layout);
        ViewPager viewPager = findViewById(R.id.stats_fragment_container);

        UsageOverviewFragmentt usageOverviewFragment = new UsageOverviewFragmentt();
        AppLaunchesFragmentt appLaunchesFragment = new AppLaunchesFragmentt();


        ViewPagerAdapterr viewPagerAdapter = new ViewPagerAdapterr(getSupportFragmentManager());
        viewPagerAdapter.addFragment(usageOverviewFragment, "Usage Overview");
        viewPagerAdapter.addFragment(appLaunchesFragment, "App Launches");

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void finish(View v) {
        finish();
    }
}
