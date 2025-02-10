
package com.android.achievix.Permission;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.achievix.Activity.UsageOverviewActivity;
import com.android.achievix.R;
import com.android.achievix.View.ExpandableTextView;

public class GetUsageStatsPermissionActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "UsagePermissionPrefs";
    private static final String PREF_PERMISSION_GRANTED = "PermissionGranted";
    int mode;
    Button finishButton;
    TextView status;
    ExpandableTextView expandableTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the permission has been granted previously
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean permissionGranted = prefs.getBoolean(PREF_PERMISSION_GRANTED, false);

        if (permissionGranted && checkForPermission(this)) {
            // If permission is already granted, skip this activity and go to the next
            startUsageOverviewActivity();
            return;
        }

        setContentView(R.layout.activity_get_usage_stats_permission);

        expandableTextView = findViewById(R.id.expandableTextViewUsage);
        status = findViewById(R.id.usage_per);
        finishButton = findViewById(R.id.grant_usage_access);
        granted();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        granted();
    }

    @SuppressLint("SetTextI18n")
    public void granted() {
        if (checkForPermission(this)) {
            status.setText("Permission Granted");
            finishButton.setVisibility(View.GONE);

            // Save permission granted state to SharedPreferences
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(PREF_PERMISSION_GRANTED, true);
            editor.apply();

            // Proceed to the next activity
            startUsageOverviewActivity();
        } else {
            status.setText("Permission not Granted");
            finishButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkForPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public void toggle(View v) {
        expandableTextView.toggle();
    }

    public void getUsagePermission(View view) {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    public void done(View view) {
        startUsageOverviewActivity();
    }

    private void startUsageOverviewActivity() {
        Intent intent = new Intent(GetUsageStatsPermissionActivity.this, UsageOverviewActivity.class);
        startActivity(intent);
        finish();
    }
}