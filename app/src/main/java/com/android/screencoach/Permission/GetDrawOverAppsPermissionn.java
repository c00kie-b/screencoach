package com.android.achievix.Permission;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.achievix.R;

public class GetDrawOverAppsPermissionn extends AppCompatActivity {
    Button finishButton;
    TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_draw_over_apps_permissionn);

        status = findViewById(R.id.draw_over_apps_per);
        finishButton = findViewById(R.id.grant_over_apps);
        granted();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        granted();
    }

    @SuppressLint("SetTextI18n")
    public void granted() {
        if (Settings.canDrawOverlays(this)) {
            status.setText("Permission Granted");
            finishButton.setVisibility(View.GONE);
        }
    }

    public void getDrawOverAppsPermission(View view) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        startActivity(intent);
    }

    public void done(View view) {
        Intent intent = new Intent(GetDrawOverAppsPermissionn.this, GetNotificationAccesss.class);
        startActivity(intent);
        finish();
    }
}