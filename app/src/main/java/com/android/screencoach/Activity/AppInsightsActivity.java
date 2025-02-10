package com.android.achievix.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.achievix.R;
import com.android.achievix.Utility.UsageUtil;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class AppInsightsActivity extends AppCompatActivity {
    private static final UsageUtil usageUtil = new UsageUtil();
    private static String packageName = "";
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private String appCategory;
    private int appPosition;
    private float dailyUsage, weeklyUsage, monthlyUsage, yearlyUsage;
    private TextView dailyUsageTextView, weeklyUsageTextView, monthlyUsageTextView,
            yearlyUsageTextView, appPositionTextView, appCategoryTextView;
    private LinearLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_insights);

        context = getApplicationContext();

        appPosition = getIntent().getIntExtra("position", 0) + 1;
        packageName = getIntent().getStringExtra("packageName");
        String appName = getIntent().getStringExtra("appName");

        TextView appNameTextView = findViewById(R.id.app_name);
        appNameTextView.setText(appName);



        dailyUsageTextView = findViewById(R.id.daily_usage);
        weeklyUsageTextView = findViewById(R.id.weekly_usage);
        monthlyUsageTextView = findViewById(R.id.monthly_usage);
        yearlyUsageTextView = findViewById(R.id.yearly_usage);
        appPositionTextView = findViewById(R.id.app_rank);
        appCategoryTextView = findViewById(R.id.app_cate);
        loadingLayout = findViewById(R.id.ly1);

        ImageView appIconImageView = findViewById(R.id.app_icon);
        try {
            Drawable icon = getPackageManager().getApplicationIcon(packageName);
            appIconImageView.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        new LoadDataAsyncTask().execute();
    }

    public void loadGraph() {
        long DAY_IN_MILLIS = 86400 * 1000;



        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startMillis = calendar.getTimeInMillis();
        long endMillis = System.currentTimeMillis();

        for (int daysAgo = 0; daysAgo < 7; daysAgo++) {
            float usageTime = usageUtil.getUsageByPackageNameAndMillis(this, packageName, startMillis, endMillis);

            if (usageTime <= (float) 0) {
                usageTime = 0;
            }

            usageTime = usageTime / (1000 * 60 * 60);



            startMillis -= DAY_IN_MILLIS;
            endMillis -= DAY_IN_MILLIS;
        }

        ArrayList<String> labels = new ArrayList<>();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -6);

        for (int i = 0; i < 7; i++) {
            labels.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DATE, 1);
        }

        String title = "Weekly Usage Stats (in Hours)";


    }

    private void getStatsInfo() {
        dailyUsage = usageUtil.getUsageByPackageName(this, packageName, "Daily");
        weeklyUsage = usageUtil.getUsageByPackageName(this, packageName, "Weekly");
        monthlyUsage = usageUtil.getUsageByPackageName(this, packageName, "Monthly");
        yearlyUsage = usageUtil.getUsageByPackageName(this, packageName, "Yearly");

        dailyUsage = dailyUsage / (1000 * 60 * 60);
        weeklyUsage = weeklyUsage / (1000 * 60 * 60);
        monthlyUsage = monthlyUsage / (1000 * 60 * 60);
        yearlyUsage = yearlyUsage / (1000 * 60 * 60);
    }

    private void getAppCategoryTitle() {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo applicationInfo = null;

        try {
            applicationInfo = pm.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        int appCategoryCode = Objects.requireNonNull(applicationInfo).category;
        appCategory = (String) ApplicationInfo.getCategoryTitle(context, appCategoryCode);
    }


    @SuppressLint("StaticFieldLeak")
    private class LoadDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getStatsInfo();
            getAppCategoryTitle();
            return null;
        }

        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        @Override
        protected void onPostExecute(Void results) {
            super.onPostExecute(results);

            loadingLayout.setVisibility(View.GONE);
            dailyUsageTextView.setText(String.format("%02d.%02d", (int) dailyUsage, (int) ((dailyUsage - (int) dailyUsage) * 60)));
            weeklyUsageTextView.setText(String.format("%02d.%02d", (int) weeklyUsage, (int) ((weeklyUsage - (int) weeklyUsage) * 60)));
            monthlyUsageTextView.setText(String.format("%02d.%02d", (int) monthlyUsage, (int) ((monthlyUsage - (int) monthlyUsage) * 60)));
            yearlyUsageTextView.setText(String.format("%02d.%02d", (int) yearlyUsage, (int) ((yearlyUsage - (int) yearlyUsage) * 60)));
            appCategoryTextView.setText(appCategory != null ? appCategory : "Other");
            appPositionTextView.setText(appPosition < 10 ? "0" + appPosition : appPosition > 99 ? "99+" : String.valueOf(appPosition));

            loadGraph();
        }
    }

    public void finish(View v) {
        finish();
    }
}