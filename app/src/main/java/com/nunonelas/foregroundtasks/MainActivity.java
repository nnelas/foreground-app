package com.nunonelas.foregroundtasks;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isPermissionGranted(this)) {
            showDialog(this);
        }

        Intent i = new Intent(this, ForegroundService.class);
        this.startService(i);
    }

    public static boolean isPermissionGranted(Context context) {
        boolean granted;
        AppOpsManager appOps = getAppOpsManager(context);
        int mode = getMode(context, appOps);

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (context.checkCallingOrSelfPermission(
                    android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        return granted;
    }

    private static AppOpsManager getAppOpsManager(Context context) {
        return (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
    }

    private static int getMode(Context context, AppOpsManager appOps) {
        return appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
    }

    private void showDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Missing permission!")
                .setMessage("This application needs access to your usage. Do you want to allow?")

                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                    }
                })

                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
