package com.android.achievix.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.achievix.Model.AppUsageModell;
import com.android.achievix.R;
import com.android.achievix.Activity.AppInsightsActivityy;
import com.android.achievix.Adapter.AppUsageAdapter;
import com.android.achievix.Utility.UsageUtill;

import java.util.List;

public class UsageOverviewFragmentt extends Fragment {
    private RecyclerView recyclerView;
    private AppUsageAdapter appUsageAdapter;
    private TextView usageStats;
    private LinearLayout usageLayout;
    private LinearLayout loadingLayout;
    private Spinner sortSpinner;
    private final String[] sort = {"Daily", "Weekly", "Monthly", "Yearly"};
    private String sortValue = "Daily";
    private GetInstalledAppsUsageTask getInstalledAppsUsageTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usage_overvieww, container, false);

        initializeViews(view);
        setupSpinner();
        setupRecyclerView();

        getInstalledAppsUsageTask = new GetInstalledAppsUsageTask(requireActivity(), sortValue);
        getInstalledAppsUsageTask.execute();
        return view;
    }

    private void initializeViews(View view) {
        usageStats = view.findViewById(R.id.tv_total_usage_overview);
        recyclerView = view.findViewById(R.id.recycler_view_usage_overview);
        sortSpinner = view.findViewById(R.id.usage_spinner);
        loadingLayout = view.findViewById(R.id.loading_usage_overview);
        usageLayout = view.findViewById(R.id.ll_usage_overview);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), R.layout.spinner_dropdown_itemm, sort);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_itemm);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean isFirstTime = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isFirstTime) {
                    sortValue = parent.getItemAtPosition(position).toString();
                    new GetInstalledAppsUsageTask(requireActivity(), sortValue).execute();
                }
                isFirstTime = false;
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (getInstalledAppsUsageTask != null) {
            getInstalledAppsUsageTask.cancel(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetInstalledAppsUsageTask extends AsyncTask<Void, Void, List<AppUsageModell>> {
        private final Context context;
        private final String sort;

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
        protected List<AppUsageModell> doInBackground(Void... voids) {
            return UsageUtill.Companion.getInstalledAppsUsage(context, sort);
        }

        @Override
        protected void onPostExecute(List<AppUsageModell> result) {
            if (isCancelled()) {
                return;
            }


            recyclerView.setAdapter(appUsageAdapter);
            appUsageAdapter.setOnItemClickListener(view -> {
                int position = recyclerView.getChildAdapterPosition(view);


                Intent intent = new Intent(requireActivity(), AppInsightsActivityy.class);

                startActivity(intent);
            });

            usageStats.setText(convertMillisToHoursAndMinutes(UsageUtill.totalUsage));
            loadingLayout.setVisibility(View.GONE);
            usageLayout.setVisibility(View.VISIBLE);
        }
    }
}