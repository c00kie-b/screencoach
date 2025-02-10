package com.android.achievix.Activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.achievix.Adapter.ViewPagerAdapter;
import com.android.achievix.Fragment.UsageOverviewFragment;
import com.android.achievix.R;
import com.google.android.material.tabs.TabLayout;

public class UsageOverviewActivity extends AppCompatActivity {
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_overview);

        TabLayout tabLayout = findViewById(R.id.stats_tab_layout);
        ViewPager viewPager = findViewById(R.id.stats_fragment_container);

        UsageOverviewFragment usageOverviewFragment = new UsageOverviewFragment();


        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(usageOverviewFragment, "Usage Overview");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void finish(View v) {
        finish();
    }
}
