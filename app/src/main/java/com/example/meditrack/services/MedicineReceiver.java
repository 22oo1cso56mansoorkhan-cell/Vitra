package com.example.meditrack.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.meditrack.R;
import com.example.meditrack.MainActivity;

public class MedicineReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "medicine_reminder_channel";
    private static final String CHANNEL_NAME = "Medicine Reminders";
    private static final String CHANNEL_DESCRIPTION = "Reminders for taking medicines";
    private static final int NOTIFICATION_ID = 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the data from intent
        String medicineName = intent.getStringExtra("medicine_name");
        String medicineDose = intent.getStringExtra("medicine_dose");
        int medicineId = intent.getIntExtra("medicine_id", -1);

        // Validate data
        if (medicineName == null || medicineId == -1) {
            return; // Invalid data, don't show notification
        }

        // Create notification channel for Android O+
        createNotificationChannel(context);

        // Create intent to open app when notification is tapped
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("💊 Medicine Reminder")
                .setContentText("Time to take " + medicineName + " (" + medicineDose + ")")
                .setSmallIcon(android.R.drawable.ic_menu_agenda) // Using built-in Android icon
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 500, 200, 500}) // Vibration pattern
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        // Show notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            // Use medicineId to create unique notification ID
            notificationManager.notify(medicineId + NOTIFICATION_ID, notification);
        }
    }

    private void createNotificationChannel(Context context) {
        // For Android 8.0 (API 26) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            channel.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null);
            channel.setShowBadge(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}