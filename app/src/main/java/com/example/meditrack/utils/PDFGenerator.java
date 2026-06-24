package com.example.meditrack.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.meditrack.database.DatabaseHelper;
import com.example.meditrack.models.UserProfile;
import com.example.meditrack.models.VitalsRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PDFGenerator {

    private static final String TAG = "PDFGenerator";

    public static String generateReport(Context context, DatabaseHelper dbHelper) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String fileName = "MediTrack_Report_" + dateFormat.format(new Date()) + ".pdf";

            // Create directory
            File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "MediTrackReports");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e(TAG, "Failed to create directory");
                    return null;
                }
            }

            File file = new File(dir, fileName);

            // Simple text report instead of PDF (to avoid iText issues)
            StringBuilder report = new StringBuilder();
            report.append("====================================\n");
            report.append("        MEDITRACK HEALTH REPORT      \n");
            report.append("====================================\n\n");

            report.append("Generated on: ").append(new SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date())).append("\n\n");

            // User Profile
            UserProfile profile = dbHelper.getProfile();
            if (profile != null && !profile.name.isEmpty()) {
                report.append("PATIENT INFORMATION\n");
                report.append("------------------------------------\n");
                report.append("Name: ").append(profile.name).append("\n");
                report.append("Age: ").append(profile.age).append("\n");
                report.append("Blood Group: ").append(profile.bloodGroup).append("\n");
                report.append("Conditions: ").append(profile.conditions).append("\n");
                report.append("Allergies: ").append(profile.allergies).append("\n");
                report.append("Emergency Contact: ").append(profile.emergencyName).append(" (").append(profile.emergencyPhone).append(")\n\n");
            }

            // Vitals
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -30);
            Date startDate = cal.getTime();
            Date endDate = new Date();

            List<VitalsRecord> vitals = dbHelper.getVitalsBetween(startDate, endDate);

            if (!vitals.isEmpty()) {
                report.append("VITALS HISTORY (Last 30 Days)\n");
                report.append("------------------------------------\n");
                SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

                for (VitalsRecord v : vitals) {
                    report.append(displayFormat.format(v.date)).append(": ");
                    report.append("BP ").append(v.systolic).append("/").append(v.diastolic);
                    report.append(" | Sugar ").append(v.bloodSugar).append(" mg/dL");
                    report.append(" | Temp ").append(v.temperature).append("°C");
                    report.append(" | Weight ").append(v.weight).append(" kg");
                    report.append(" | SpO2 ").append(v.spo2).append("%\n");
                }
                report.append("\n");
            } else {
                report.append("No vitals recorded in the last 30 days.\n\n");
            }

            report.append("====================================\n");
            report.append("End of Report\n");
            report.append("====================================\n");

            // Write to file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(report.toString().getBytes());
            fos.close();

            return file.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "Error generating report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}