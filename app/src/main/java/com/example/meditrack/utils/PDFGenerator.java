package com.example.meditrack.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
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
            SimpleDateFormat fileFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

            String fileName = "MediTrack_Report_" + fileFormat.format(new Date()) + ".pdf";

            File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

            File reportDir = new File(documentsDir, "MediTrackReports");

            if (!reportDir.exists()) {
                reportDir.mkdirs();
            }

            File pdfFile = new File(reportDir, fileName);

            PdfDocument pdfDocument = new PdfDocument();

            PdfDocument.PageInfo pageInfo =
                    new PdfDocument.PageInfo.Builder(595, 842, 1).create();

            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            Canvas canvas = page.getCanvas();

            Paint titlePaint = new Paint();
            titlePaint.setTextSize(22);
            titlePaint.setTypeface(Typeface.DEFAULT_BOLD);

            Paint headingPaint = new Paint();
            headingPaint.setTextSize(16);
            headingPaint.setTypeface(Typeface.DEFAULT_BOLD);

            Paint textPaint = new Paint();
            textPaint.setTextSize(12);

            int y = 50;

            canvas.drawText("MediTrack Health Report", 40, y, titlePaint);

            y += 30;

            String generated =
                    new SimpleDateFormat(
                            "dd MMM yyyy HH:mm",
                            Locale.getDefault())
                            .format(new Date());

            canvas.drawText("Generated on: " + generated, 40, y, textPaint);

            y += 40;

            // -----------------------------
            // Patient Information
            // -----------------------------

            UserProfile profile = dbHelper.getProfile();

            canvas.drawText("Patient Information", 40, y, headingPaint);

            y += 25;

            if (profile != null && !profile.name.isEmpty()) {

                canvas.drawText("Name : " + profile.name, 50, y, textPaint);
                y += 20;

                canvas.drawText("Age : " + profile.age, 50, y, textPaint);
                y += 20;

                canvas.drawText("Blood Group : " + profile.bloodGroup, 50, y, textPaint);
                y += 20;

                canvas.drawText("Conditions : " + profile.conditions, 50, y, textPaint);
                y += 20;

                canvas.drawText("Allergies : " + profile.allergies, 50, y, textPaint);
                y += 20;

                canvas.drawText(
                        "Emergency : "
                                + profile.emergencyName
                                + " ("
                                + profile.emergencyPhone
                                + ")",
                        50,
                        y,
                        textPaint);

                y += 35;
            }

            // -----------------------------
            // Vitals
            // -----------------------------

            canvas.drawText("Vitals History (Last 30 Days)", 40, y, headingPaint);

            y += 25;

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -30);

            List<VitalsRecord> vitals =
                    dbHelper.getVitalsBetween(calendar.getTime(), new Date());

            SimpleDateFormat display =
                    new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

            if (vitals.isEmpty()) {

                canvas.drawText(
                        "No vitals available.",
                        50,
                        y,
                        textPaint);

            } else {

                for (VitalsRecord v : vitals) {

                    canvas.drawText(
                            display.format(v.date),
                            50,
                            y,
                            textPaint);

                    y += 18;

                    canvas.drawText(
                            "BP: "
                                    + v.systolic
                                    + "/"
                                    + v.diastolic
                                    + "   Sugar: "
                                    + v.bloodSugar
                                    + " mg/dL",
                            70,
                            y,
                            textPaint);

                    y += 18;

                    canvas.drawText(
                            "Temp: "
                                    + v.temperature
                                    + "°C   Weight: "
                                    + v.weight
                                    + " kg   SpO₂: "
                                    + v.spo2
                                    + "%",
                            70,
                            y,
                            textPaint);

                    y += 30;

                    // Prevent writing beyond page
                    if (y > 780) {
                        break;
                    }
                }
            }

            pdfDocument.finishPage(page);

            FileOutputStream fos = new FileOutputStream(pdfFile);

            pdfDocument.writeTo(fos);

            pdfDocument.close();

            fos.close();

            return pdfFile.getAbsolutePath();

        } catch (Exception e) {

            Log.e(TAG, "PDF Error", e);

            return null;
        }
    }
}