package com.nunonelas.foregroundtasks;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ForegroundService extends Service {
    private static int DELAY_TIME_MILISEC = 5000;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();
        // TODO: remove getMainLooper
        final Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                UsageStatsManager usm = getUsageStatsManager();
                long time = getCurrentTime();
                List<UsageStats> appList = getUsageStats(usm, time);
                List<ActivityManager.RunningAppProcessInfo> tasks = getRunningAppProcesses();
                String foregroundTask = getForegroundTask(appList, tasks);
                Log.e("ForegroundService", "Current App in foreground is: " + foregroundTask);
                handler.postDelayed(this, DELAY_TIME_MILISEC);
            }
        };
        handler.post(runnable);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    private UsageStatsManager getUsageStatsManager() {
        return (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
    }

    private List<UsageStats> getUsageStats(UsageStatsManager usm, long time) {
        return usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
    }

    private List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() {
        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        return am.getRunningAppProcesses();
    }

    private String getForegroundTask(List<UsageStats> appList,
                                     List<ActivityManager.RunningAppProcessInfo> tasks) {
        String currentApp = "NULL";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if (appList != null && !appList.isEmpty()) {
                SortedMap<Long, UsageStats> mySortedMap = getSortedUsageStats(appList);
                if (!mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            currentApp = tasks.get(0).processName;
        }
        return currentApp;
    }

    private SortedMap<Long, UsageStats> getSortedUsageStats(List<UsageStats> appList) {
        SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
        for (UsageStats usageStats : appList) {
            mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
        }
        return mySortedMap;
    }
}
