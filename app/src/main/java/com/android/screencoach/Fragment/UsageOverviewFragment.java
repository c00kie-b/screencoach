package com.android.achievix.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.usage.UsageEvents;
import com.android.achievix.Model.AppUsageModel;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.achievix.Fragment.ModelHandler;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.achievix.Activity.AppInsightsActivity;
import com.android.achievix.Adapter.AppUsageAdapter;
import com.android.achievix.Model.AppUsageModel;
import com.android.achievix.R;
import com.android.achievix.Utility.UsageUtil;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsageOverviewFragment extends Fragment {
    private RecyclerView recyclerView;
    private AppUsageAdapter appUsageAdapter;
    private TextView usageStats;
    private LinearLayout usageLayout;
    private LinearLayout loadingLayout;
    private Spinner sortSpinner;
    private final String[] sort = {"Daily", "Weekly", "Monthly", "Yearly"};
    private String sortValue = "Daily";
    private GetInstalledAppsUsageTask getInstalledAppsUsageTask;

    private TextView screenTimeTextView;
    private TextView screenTimeTipTextView;

    private ModelHandler modelHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usage_overview, container, false);

        screenTimeTextView = view.findViewById(R.id.screentime);
        screenTimeTipTextView = view.findViewById(R.id.screentime_tip);


        initializeViews(view);
        setupSpinner();
        setupRecyclerView();

        modelHandler = new ModelHandler(requireContext());

        getInstalledAppsUsageTask = new GetInstalledAppsUsageTask(requireActivity(), sortValue);
        getInstalledAppsUsageTask.execute();



        categorizeScreenTime();


        return view;
    }

    private void initializeViews(View view) {
        usageStats = view.findViewById(R.id.tv_total_usage_overview);
        recyclerView = view.findViewById(R.id.recycler_view_usage_overview);
        sortSpinner = view.findViewById(R.id.usage_spinner);
        loadingLayout = view.findViewById(R.id.loading_usage_overview);
        usageLayout = view.findViewById(R.id.ll_usage_overview);

        // Request necessary permissions
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        // Request usage access permission if not granted
        if (!hasUsageStatsPermission(requireContext())) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    private boolean hasUsageStatsPermission(Context context) {
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
        return stats != null && !stats.isEmpty();
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), R.layout.spinner_dropdown_item, sort);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(null);
        sortSpinner.setSelection(Arrays.asList(sort).indexOf(sortValue));
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortValue = parent.getItemAtPosition(position).toString();
                new GetInstalledAppsUsageTask(requireActivity(), sortValue).execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
    }

    private static String convertMillisToHoursAndMinutes(long millis) {
        long hours = millis / 1000 / 60 / 60;
        long minutes = (millis / 1000 / 60) % 60;
        return hours + " hrs " + minutes + " mins";
    }

    public void categorizeScreenTime() {
        modelHandler.preprocessAndPredict(requireContext(), new ModelHandler.PredictionCallback() {
            @Override
            public void onPredictionCompleted(String category) {
                updateScreenTimeCategory(category);
            }
        });
    }


    private void updateScreenTimeCategory(String category) {
        String tip;
        switch (category) {
            case "Productive":
                tip = "";
                break;
            case "Neutral":
                tip = "";
                break;
            case "Distracted":
                tip = "";
                break;
            default:
                tip = "";
                category = "Unknown";
                break;
        }

        screenTimeTextView.setText("Screen Time Category: " + category);
        screenTimeTipTextView.setText("Tip: " + tip);
    }

    private List<AppInfo> getInstalledApplications() {
        List<AppInfo> appInfos = new ArrayList<>();
        PackageManager packageManager = requireActivity().getPackageManager();
        List<ApplicationInfo> applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : applications) {
            String appName = (String) packageManager.getApplicationLabel(app);
            String packageName = app.packageName;
            Drawable icon = packageManager.getApplicationIcon(app);
            String versionName = "";
            int versionCode = 0;
            String category = getAppCategory(packageManager, app);

            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                versionName = packageInfo.versionName;
                versionCode = packageInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            appInfos.add(new AppInfo(appName, packageName, versionName, versionCode, icon, category));
        }
        return appInfos;
    }

    public String getAppCategory(PackageManager pm, ApplicationInfo appInfo) {
        // Here you can use the code snippet I provided above to return a category based on ApplicationInfo
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int category = appInfo.category;
            switch (category) {
                case ApplicationInfo.CATEGORY_GAME:
                    return "Game";
                case ApplicationInfo.CATEGORY_AUDIO:
                    return "Audio";
                case ApplicationInfo.CATEGORY_VIDEO:
                    return "Video";
                case ApplicationInfo.CATEGORY_IMAGE:
                    return "Image";
                case ApplicationInfo.CATEGORY_SOCIAL:
                    return "Social Media";
                case ApplicationInfo.CATEGORY_NEWS:
                    return "News";
                case ApplicationInfo.CATEGORY_MAPS:
                    return "Maps";
                case ApplicationInfo.CATEGORY_PRODUCTIVITY:
                    return "Productivity";
                default:
                    return "Other";  // Default case for categories not handled
            }
        } else {
            // Fallback or custom logic for older Android versions
            return categorizeBasedOnPackageName(appInfo.packageName);
        }
    }

    private String categorizeBasedOnPackageName(String packageName) {
        // Example of manual categorization based on package names
        if (packageName.contains("facebook") || packageName.contains("instagram") || packageName.contains("twitter")) {
            return "Social Media";
        } else if (packageName.contains("map") || packageName.contains("navigation")) {
            return "Maps";
        } else if (packageName.contains("fitness") || packageName.contains("health")) {
            return "Health";
        } else {
            return "Other";  // Default category
        }
    }

    private List<UsageStats> getAppUsageStats(Context context, String intervalType) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();

        switch (intervalType) {
            case "Daily":
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                break;
            case "Weekly":
                calendar.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            case "Monthly":
                calendar.add(Calendar.MONTH, -1);
                break;
            case "Yearly":
                calendar.add(Calendar.YEAR, -1);
                break;
        }

        long startTime = calendar.getTimeInMillis();
        return usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
    }

    private void getNumberOfUsesAndPeriods(UsageStatsManager usageStatsManager, long startTime, long endTime, AppInfo appInfo) {
        UsageEvents usageEvents = usageStatsManager.queryEvents(startTime, endTime);
        UsageEvents.Event event = new UsageEvents.Event();

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND && event.getPackageName().equals(appInfo.packageName)) {
                Calendar eventCalendar = Calendar.getInstance();
                eventCalendar.setTimeInMillis(event.getTimeStamp());
                int hour = eventCalendar.get(Calendar.HOUR_OF_DAY);

                if (hour >= 6 && hour < 12) {
                    appInfo.usagePeriods.put("Morning", appInfo.usagePeriods.get("Morning") + 1);
                } else if (hour >= 12 && hour < 18) {
                    appInfo.usagePeriods.put("Afternoon", appInfo.usagePeriods.get("Afternoon") + 1);
                } else if (hour >= 18 && hour < 24) {
                    appInfo.usagePeriods.put("Evening", appInfo.usagePeriods.get("Evening") + 1);
                } else {
                    appInfo.usagePeriods.put("Night", appInfo.usagePeriods.get("Night") + 1);
                }
            }
        }
    }

    private void saveAppInfoToCSV(List<AppInfo> appInfos, List<UsageStats> usageStatsList, UsageStatsManager usageStatsManager, long startTime, long endTime) {
        File csvFile = new File(requireActivity().getExternalFilesDir(null), "installed_apps.csv");
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile))) {
            String[] header = { "Package Name", "Version Name", "Version Code", "Category", "Total Time in Foreground", "Number of Uses", "Morning Uses", "Afternoon Uses", "Evening Uses", "Night Uses"};
            writer.writeNext(header);

            for (AppInfo appInfo : appInfos) {
                UsageStats usageStats = null;
                for (UsageStats stats : usageStatsList) {
                    if (stats.getPackageName().equals(appInfo.packageName)) {
                        usageStats = stats;
                        break;
                    }
                }

                long totalTimeInForegroundMillis = usageStats != null ? usageStats.getTotalTimeInForeground() : 0;

                String totalTimeInForeground = convertMillisToHoursAndMinutes(totalTimeInForegroundMillis);
                getNumberOfUsesAndPeriods(usageStatsManager, startTime, endTime, appInfo);

                String[] data = {
                        appInfo.appName,
                        appInfo.packageName,
                        appInfo.versionName,
                        String.valueOf(appInfo.versionCode),
                        appInfo.category,
                        totalTimeInForeground,
                        String.valueOf(appInfo.usagePeriods.values().stream().mapToInt(Integer::intValue).sum()),
                        String.valueOf(appInfo.usagePeriods.get("Morning")),
                        String.valueOf(appInfo.usagePeriods.get("Afternoon")),
                        String.valueOf(appInfo.usagePeriods.get("Evening")),
                        String.valueOf(appInfo.usagePeriods.get("Night"))
                };
                writer.writeNext(data);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static class AppInfo {
        String appName;
        String packageName;
        String versionName;
        int versionCode;
        Drawable icon;
        String category;
        Map<String, Integer> usagePeriods;

        public AppInfo(String appName, String packageName, String versionName, int versionCode, Drawable icon, String category) {
            this.appName = appName;
            this.packageName = packageName;
            this.versionName = versionName;
            this.versionCode = versionCode;
            this.icon = icon;
            this.category = category;
            this.usagePeriods = new HashMap<>();
            this.usagePeriods.put("", 0);
            this.usagePeriods.put("", 0);
            this.usagePeriods.put("", 0);
            this.usagePeriods.put("", 0);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getInstalledAppsUsageTask != null) {
            getInstalledAppsUsageTask.cancel(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetInstalledAppsUsageTask extends AsyncTask<Void, Void, List<AppUsageModel>> {
        private final Context context;
        private final String sort;
        private List<AppInfo> installedApps;
        private List<UsageStats> usageStatsList;
        private UsageStatsManager usageStatsManager;
        private long startTime, endTime;
        private long totalForegroundTimeMillis = 0;

        GetInstalledAppsUsageTask(Context context, String sort) {
            this.context = context;
            this.sort = sort;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingLayout.setVisibility(View.VISIBLE);
            usageLayout.setVisibility(View.GONE);
        }

        @Override
        protected List<AppUsageModel> doInBackground(Void... voids) {
            installedApps = getInstalledApplications();
            usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            Calendar calendar = Calendar.getInstance();
            endTime = calendar.getTimeInMillis();

            switch (sort) {
                case "Daily":
                    calendar.add(Calendar.DAY_OF_YEAR, -1);
                    break;
                case "Weekly":
                    calendar.add(Calendar.WEEK_OF_YEAR, -1);
                    break;
                case "Monthly":
                    calendar.add(Calendar.MONTH, -1);
                    break;
                case "Yearly":
                    calendar.add(Calendar.YEAR, -1);
                    break;
            }

            startTime = calendar.getTimeInMillis();
            usageStatsList = getAppUsageStats(context, sort);

            // Calculate total foreground time
            for (UsageStats stats : usageStatsList) {
                totalForegroundTimeMillis += stats.getTotalTimeInForeground();
            }

            new Thread(() -> saveAppInfoToCSV(installedApps, usageStatsList, usageStatsManager, startTime, endTime)).start();

            for (AppInfo appInfo : installedApps) {
                Log.d("AppInfo", "App: " + appInfo.appName + ", Package: " + appInfo.packageName +
                        ", Version: " + appInfo.versionName + ", Code: " + appInfo.versionCode);
            }

            return UsageUtil.Companion.getInstalledAppsUsage(context, sort);
        }

        @Override
        protected void onPostExecute(List<AppUsageModel> result) {
            if (isCancelled()) {
                return;
            }

            if (appUsageAdapter == null) {
                appUsageAdapter = new AppUsageAdapter(result);
                recyclerView.setAdapter(appUsageAdapter);
            } else {
                appUsageAdapter.updateData(result);
            }

            appUsageAdapter.setOnItemClickListener(view -> {
                int position = recyclerView.getChildAdapterPosition(view);
                AppUsageModel app = appUsageAdapter.getItemAt(position);

                Intent intent = new Intent(requireActivity(), AppInsightsActivity.class);
                intent.putExtra("appName", app.getName());
                intent.putExtra("packageName", app.getPackageName());
                intent.putExtra("position", position);
                startActivity(intent);
            });

            // Convert totalForegroundTimeMillis to hours and minutes
            String totalForegroundTime = convertMillisToHoursAndMinutes(totalForegroundTimeMillis);

            // Display the total time usage in the usageStats TextView
            usageStats.setText("Total Time Usage (" + sort + "): " + totalForegroundTime);

            loadingLayout.setVisibility(View.GONE);
            usageLayout.setVisibility(View.VISIBLE);
        }
    }

}
