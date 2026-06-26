package com.example.meditrack.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.meditrack.database.DatabaseHelper;
import com.example.meditrack.models.Medicine;
import com.example.meditrack.services.MedicineReceiver;

import java.util.Calendar;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            rescheduleAllAlarms(context);
        }
    }

    private void rescheduleAllAlarms(Context context) {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            List<Medicine> medicines = dbHelper.getAllMedicines();

            if (medicines == null || medicines.isEmpty()) {
                return;
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            for (Medicine medicine : medicines) {
                if (!medicine.isActive) continue;

                Intent intent = new Intent(context, MedicineReceiver.class);
                intent.putExtra("medicine_name", medicine.name);
                intent.putExtra("medicine_dose", medicine.dose);
                intent.putExtra("medicine_id", medicine.id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                        medicine.id, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                Calendar cal = Calendar.getInstance();
                cal.setTime(medicine.nextDoseTime);

                if (cal.getTimeInMillis() > System.currentTimeMillis()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                                cal.getTimeInMillis(), pendingIntent);
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}