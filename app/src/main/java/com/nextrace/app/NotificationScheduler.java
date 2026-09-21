package com.nextrace.app;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

public final class NotificationScheduler {
    private static final String PREFS = "nextrace_notifications";
    private static final String PAYLOAD = "payload";
    private static final String CODES = "codes";

    private NotificationScheduler() {}

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel training = new NotificationChannel("training", "Treinos", NotificationManager.IMPORTANCE_DEFAULT);
        training.setDescription("Treinos do dia, musculação e lembretes de registro.");
        NotificationChannel races = new NotificationChannel("races", "Provas", NotificationManager.IMPORTANCE_HIGH);
        races.setDescription("Lembretes da próxima prova e preparação.");
        NotificationChannel summaries = new NotificationChannel("summaries", "Resumos", NotificationManager.IMPORTANCE_DEFAULT);
        summaries.setDescription("Resumo semanal e lembretes contextuais.");
        nm.createNotificationChannel(training);
        nm.createNotificationChannel(races);
        nm.createNotificationChannel(summaries);
    }

    public static void schedulePayload(Context context, String payload) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(PAYLOAD, payload).apply();
        cancelAll(context);
        createChannels(context);
        try {
            JSONObject root = new JSONObject(payload);
            JSONArray events = root.optJSONArray("events");
            if (events == null) return;
            JSONArray codes = new JSONArray();
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            long now = System.currentTimeMillis();
            for (int i = 0; i < events.length(); i++) {
                JSONObject e = events.optJSONObject(i);
                if (e == null) continue;
                long at = e.optLong("at", 0);
                if (at <= now) continue;
                String key = e.optString("key", "nextrace_" + i + "_" + at);
                int code = key.hashCode() & 0x7fffffff;
                Intent intent = new Intent(context, NotificationReceiver.class);
                intent.putExtra("code", code);
                intent.putExtra("title", e.optString("title", "NextRace"));
                intent.putExtra("body", e.optString("body", ""));
                intent.putExtra("channel", e.optString("channel", "training"));
                intent.putExtra("workoutId", e.optString("workoutId", ""));
                PendingIntent pi = PendingIntent.getBroadcast(
                        context, code, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
                codes.put(code);
            }
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(CODES, codes.toString()).apply();
        } catch (Exception ignored) {}
    }

    public static void rescheduleSaved(Context context) {
        String payload = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(PAYLOAD, "");
        if (!payload.isEmpty()) schedulePayload(context, payload);
    }

    public static void cancelAll(Context context) {
        try {
            String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(CODES, "[]");
            JSONArray codes = new JSONArray(raw);
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            for (int i = 0; i < codes.length(); i++) {
                int code = codes.optInt(i, -1);
                if (code < 0) continue;
                Intent intent = new Intent(context, NotificationReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(
                        context, code, intent,
                        PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
                );
                if (pi != null) {
                    alarm.cancel(pi);
                    pi.cancel();
                }
            }
        } catch (Exception ignored) {}
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(CODES, "[]").apply();
    }
}
