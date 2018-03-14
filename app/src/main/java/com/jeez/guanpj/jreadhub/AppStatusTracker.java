package com.jeez.guanpj.jreadhub;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.jeez.guanpj.jreadhub.constant.AppStatus;

public class AppStatusTracker implements Application.ActivityLifecycleCallbacks {
    private static final long MAX_INTERVAL = 5 * 60 * 1000;
    private static AppStatusTracker tracker;
    private Application application;
    private int appStatus = AppStatus.STATUS_FORCE_KILLED;
    private boolean isForground;
    private int activeCount;
    private long timestamp;
    private boolean isScreenOff;
    private DeamonReceiver receiver;

    private AppStatusTracker(Application application) {
        this.application = application;
        application.registerActivityLifecycleCallbacks(this);
    }

    public static void init(Application application) {
        tracker = new AppStatusTracker(application);
    }

    public static AppStatusTracker getInstance() {
        return tracker;
    }

    public void setAppStatus(int status) {
        this.appStatus = status;
        if (this.appStatus == AppStatus.STATUS_ONLINE) {
            if (this.receiver == null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_SCREEN_OFF);
                receiver = new DeamonReceiver();
                this.application.registerReceiver(receiver, filter);
            }
        } else {
            if (this.application != null) {
                this.application.unregisterReceiver(receiver);
                receiver = null;
            }
        }
    }

    public int getAppStatus() {
        return this.appStatus;
    }

    public boolean isForground() {
        return isForground;
    }

    private void onScreenOff(boolean isScreenOff) {
        this.isScreenOff = isScreenOff;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        activeCount++;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        isForground = true;
        timestamp = 0l;
        isScreenOff = false;
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        activeCount--;
        if (activeCount == 0) {
            isForground = false;
            timestamp = System.currentTimeMillis();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    private class DeamonReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                AppStatusTracker.getInstance().onScreenOff(true);
            }
        }
    }
}
