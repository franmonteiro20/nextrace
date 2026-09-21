package com.nextrace.app;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= 33 &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationScheduler.createChannels(context);
        int code = intent.getIntExtra("code", (int) (System.currentTimeMillis() & 0x7fffffff));
        String title = intent.getStringExtra("title");
        String body = intent.getStringExtra("body");
        String channel = intent.getStringExtra("channel");
        String workoutId = intent.getStringExtra("workoutId");
        if (channel == null || channel.isEmpty()) channel = "training";

        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (workoutId != null && !workoutId.isEmpty()) open.putExtra("workoutId", workoutId);
        PendingIntent content = PendingIntent.getActivity(
                context, code, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, channel)
                : new Notification.Builder(context);
        b.setSmallIcon(R.drawable.ic_notification)
         .setContentTitle(title == null ? "NextRace" : title)
         .setContentText(body == null ? "" : body)
         .setStyle(new Notification.BigTextStyle().bigText(body == null ? "" : body))
         .setContentIntent(content)
         .setAutoCancel(true)
         .setWhen(System.currentTimeMillis())
         .setShowWhen(true);
        if ("races".equals(channel)) b.setPriority(Notification.PRIORITY_HIGH);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(code, b.build());
    }
}
