package com.softdev.weekimessenger.Configuration;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.softdev.weekimessenger.Handlers.AppHandler;
import com.softdev.weekimessenger.R;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.text.ParseException;
import java.util.Date;

public class NotificationHandler {
    Context mContext;

    public NotificationHandler(Context mContext)
    {
        this.mContext = mContext;
    }

    // Get time in milliseconds.
    public static long getTimeMilliSec(String timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(timeStamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Clear all tray notifications.
    public static void clearNotifications() {
        NotificationManager notificationManager = (NotificationManager) AppHandler.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    // Check whether the current app state is in background or not.
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    // Showing notification (4 parameters).
    public void showNotification(String title, String message, String timeStamp, Intent intent)
    {
        if (TextUtils.isEmpty(message))
            return;

        final int notificationIcon = R.mipmap.ic_launcher;
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        final Uri notificationSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.getPackageName() + "/raw/notification");

        showNotification(mBuilder, notificationIcon, title, message, timeStamp, resultPendingIntent, notificationSound);
    }
    private void showNotification(NotificationCompat.Builder mBuilder, int icon, String title, String message, String timeStamp, PendingIntent resultPendingIntent, Uri alarmSound) {

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        if (Config.isSingleLineNotification)
        {
            AppHandler.getInstance().getDataManager().addNotification(message);
            String oldNotification = AppHandler.getInstance().getDataManager().getNotifications();
            List<String> messages = Arrays.asList(oldNotification.split("\\|"));

            for (int i = messages.size() - 1; i >= 0; i--) {
                inboxStyle.addLine(messages.get(i));
            }
        }
        else
        {
            inboxStyle.addLine(message);
        }

        Notification notification;
        notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSound(alarmSound)
                .setStyle(inboxStyle)
                .setWhen(getTimeMilliSec(timeStamp))
                .setContentText(message)
                .build();

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_ID, notification);
    }

    // Play notification sound
    public static void playMessageSound() {
        try {
            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + AppHandler.getInstance().getApplicationContext().getPackageName() + "/raw/notification_" + AppHandler.getInstance().getSharedPref().getString(
                    "n_toneMessage", "1"));
            Ringtone r = RingtoneManager.getRingtone(AppHandler.getInstance().getApplicationContext(), alarmSound);
            if (!r.isPlaying())
                r.play();
        } catch (Exception e) {
            // Show an error,
            // Skipping.
        }
    }

    public static void playGroupSound() {
        try {
            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + AppHandler.getInstance().getApplicationContext().getPackageName() + "/raw/notification_" + AppHandler.getInstance().getSharedPref().getString(
                    "n_toneGroup", "1"));
            Ringtone r = RingtoneManager.getRingtone(AppHandler.getInstance().getApplicationContext(), alarmSound);
            if (!r.isPlaying())
                r.play();
        } catch (Exception e) {
            // Show an error,
            // Skipping.
        }
    }
}
