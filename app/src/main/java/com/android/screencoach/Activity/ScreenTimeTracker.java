package com.android.achievix.Activity;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.achievix.R;
import com.android.achievix.View.ExpandableTextView;
import com.android.achievix.Activity.UsageOverviewActivity;

public class ScreenTimeTracker extends AppCompatActivity {
    ImageView btnback;
    int mode;
    Button finishButton;
    TextView status;
    ExpandableTextView expandableTextView;

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
        Intent intent = new Intent(ScreenTimeTracker.this, UsageOverviewActivity.class);
        startActivity(intent);
        finish();
    }
}
