package com.android.achievix.Service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;
import com.android.achievix.Model.AppBlockModell;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyAccessibilityService extends AccessibilityService {

    private static Set<String> lockedApps = new HashSet<>();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
            if (lockedApps.contains(packageName)) {
                Toast.makeText(this, "This application is locked", Toast.LENGTH_SHORT).show();
                performGlobalAction(GLOBAL_ACTION_HOME);
            }
        }
    }

    @Override
    public void onInterrupt() {
    }

    public static void setLockedApps(List<AppBlockModell> apps) {
        lockedApps.clear();
        for (AppBlockModell app : apps) {
            lockedApps.add(app.getPackageName());
        }
    }

    public static void unblockAllApps() {
        lockedApps.clear();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        setServiceInfo(info);
    }
}
