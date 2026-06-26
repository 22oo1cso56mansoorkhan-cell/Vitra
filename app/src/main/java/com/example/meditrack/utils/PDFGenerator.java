package com.example.meditrack.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.graphics.Canvas;
import android.graphics.Color;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PDFGenerator {

    private static final String TAG = "PDFGenerator";

    // Color constants
    private static final int COLOR_PRIMARY = Color.rgb(33, 150, 243); // Blue
    private static final int COLOR_SECONDARY = Color.rgb(66, 66, 66); // Dark Gray
    private static final int COLOR_TEXT = Color.rgb(33, 33, 33);
    private static final int COLOR_LIGHT_TEXT = Color.rgb(117, 117, 117);
    private static final int COLOR_HEADER_BG = Color.rgb(227, 242, 253); // Light Blue
    private static final int COLOR_WHITE = Color.WHITE;
    private static final int COLOR_LIGHT_GRAY = Color.rgb(245, 245, 245);
    private static final int COLOR_BORDER = Color.rgb(224, 224, 224);
    private static final int COLOR_SUCCESS = Color.rgb(76, 175, 80); // Green
    private static final int COLOR_WARNING = Color.rgb(255, 152, 0); // Orange

    public static String generateReport(Context context, DatabaseHelper dbHelper) {
        FileOutputStream fos = null;
        PdfDocument pdfDocument = null;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String fileName = "MediTrack_Report_" + dateFormat.format(new Date()) + ".pdf";

            // Create directory
            File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File reportDir = new File(documentsDir, "MediTrackReports");

            if (!reportDir.exists()) {
                if (!reportDir.mkdirs()) {
                    Log.e(TAG, "Failed to create directory");
                    return null;
                }
            }

            File pdfFile = new File(reportDir, fileName);

            // Create PDF document with multiple pages if needed
            pdfDocument = new PdfDocument();

            // Page dimensions (A4: 595 x 842 points)
            int pageWidth = 595;
            int pageHeight = 842;
            int margin = 40;
            int contentWidth = pageWidth - (2 * margin);

            // Start first page
            int y = margin;
            boolean needsNewPage = false;

            // Create first page
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            // Draw header
            y = drawHeader(canvas, pageWidth, pageHeight, margin, y);

            // Draw patient information
            UserProfile profile = dbHelper.getProfile();
            y = drawPatientInfo(canvas, margin, y, contentWidth, profile);

            // Draw vitals section
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            List<VitalsRecord> vitals = dbHelper.getVitalsBetween(calendar.getTime(), new Date());

            y = drawVitalsSection(canvas, margin, y, contentWidth, vitals);

            // Finish first page
            pdfDocument.finishPage(page);

            // If content exceeds first page, create additional pages
            if (needsNewPage || y > pageHeight - margin) {
                // We'll handle this in a more sophisticated way if needed
                // For now, we'll just warn
                Log.w(TAG, "Content might be truncated - consider implementing multi-page support");
            }

            // Write to file
            fos = new FileOutputStream(pdfFile);
            pdfDocument.writeTo(fos);
            fos.flush();

            // Scan the file so it appears in file manager
            MediaScannerConnection.scanFile(
                    context,
                    new String[]{pdfFile.getAbsolutePath()},
                    null,
                    null
            );

            return pdfFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "PDF Error", e);
            return null;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing FileOutputStream", e);
            }

            if (pdfDocument != null) {
                pdfDocument.close();
            }
        }
    }

    private static int drawHeader(Canvas canvas, int pageWidth, int pageHeight, int margin, int y) {
        // Draw header background
        Paint headerBgPaint = new Paint();
        headerBgPaint.setColor(COLOR_PRIMARY);
        canvas.drawRect(0, 0, pageWidth, 80, headerBgPaint);

        // Draw header title
        Paint titlePaint = new Paint();
        titlePaint.setColor(COLOR_WHITE);
        titlePaint.setTextSize(28);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setAntiAlias(true);

        canvas.drawText("MediTrack", margin, 50, titlePaint);

        // Draw header subtitle
        Paint subtitlePaint = new Paint();
        subtitlePaint.setColor(Color.argb(200, 255, 255, 255));
        subtitlePaint.setTextSize(16);
        subtitlePaint.setAntiAlias(true);

        canvas.drawText("Health Report", margin + 120, 50, subtitlePaint);

        // Draw header line
        Paint linePaint = new Paint();
        linePaint.setColor(COLOR_BORDER);
        linePaint.setStrokeWidth(2);
        canvas.drawLine(margin, 80, pageWidth - margin, 80, linePaint);

        // Date
        Paint datePaint = new Paint();
        datePaint.setColor(COLOR_LIGHT_TEXT);
        datePaint.setTextSize(12);
        datePaint.setAntiAlias(true);

        String dateStr = "Generated on: " +
                new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(new Date());
        canvas.drawText(dateStr, margin, 100, datePaint);

        return 120; // Next Y position
    }

    private static int drawPatientInfo(Canvas canvas, int margin, int y, int contentWidth, UserProfile profile) {
        Paint sectionTitlePaint = new Paint();
        sectionTitlePaint.setColor(COLOR_SECONDARY);
        sectionTitlePaint.setTextSize(20);
        sectionTitlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        sectionTitlePaint.setAntiAlias(true);

        Paint labelPaint = new Paint();
        labelPaint.setColor(COLOR_LIGHT_TEXT);
        labelPaint.setTextSize(13);
        labelPaint.setAntiAlias(true);

        Paint valuePaint = new Paint();
        valuePaint.setColor(COLOR_TEXT);
        valuePaint.setTextSize(13);
        valuePaint.setAntiAlias(true);

        Paint linePaint = new Paint();
        linePaint.setColor(COLOR_BORDER);
        linePaint.setStrokeWidth(1);

        // Section background
        Paint sectionBgPaint = new Paint();
        sectionBgPaint.setColor(COLOR_HEADER_BG);

        // Draw section background
        int sectionStartY = y - 10;
        canvas.drawRoundRect(margin, sectionStartY, margin + contentWidth, sectionStartY + 180, 8, 8, sectionBgPaint);

        // Section title
        canvas.drawText("Patient Information", margin + 15, y + 10, sectionTitlePaint);
        y += 35;

        if (profile != null && profile.name != null && !profile.name.isEmpty()) {
            // Draw patient info in two columns
            String[][] info = {
                    {"Name", profile.name},
                    {"Age", String.valueOf(profile.age)},
                    {"Blood Group", profile.bloodGroup != null ? profile.bloodGroup : "Not specified"},
                    {"Conditions", profile.conditions != null && !profile.conditions.isEmpty() ?
                            profile.conditions : "None"},
                    {"Allergies", profile.allergies != null && !profile.allergies.isEmpty() ?
                            profile.allergies : "None"},
                    {"Emergency Contact", profile.emergencyName + " (" + profile.emergencyPhone + ")"}
            };

            for (String[] pair : info) {
                // Draw label
                canvas.drawText(pair[0] + ":", margin + 15, y, labelPaint);

                // Draw value
                float labelWidth = labelPaint.measureText(pair[0] + ":");
                canvas.drawText(pair[1], margin + 15 + labelWidth + 10, y, valuePaint);

                y += 22;
            }
        } else {
            canvas.drawText("No patient information available", margin + 15, y + 10, valuePaint);
            y += 30;
        }

        y += 15;
        return y;
    }

    private static int drawVitalsSection(Canvas canvas, int margin, int y, int contentWidth, List<VitalsRecord> vitals) {
        Paint sectionTitlePaint = new Paint();
        sectionTitlePaint.setColor(COLOR_SECONDARY);
        sectionTitlePaint.setTextSize(20);
        sectionTitlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        sectionTitlePaint.setAntiAlias(true);

        Paint labelPaint = new Paint();
        labelPaint.setColor(COLOR_LIGHT_TEXT);
        labelPaint.setTextSize(11);
        labelPaint.setAntiAlias(true);

        Paint valuePaint = new Paint();
        valuePaint.setColor(COLOR_TEXT);
        valuePaint.setTextSize(12);
        valuePaint.setAntiAlias(true);

        Paint linePaint = new Paint();
        linePaint.setColor(COLOR_BORDER);
        linePaint.setStrokeWidth(1);

        // Section title
        canvas.drawText("Vitals History (Last 30 Days)", margin, y, sectionTitlePaint);
        y += 30;

        // Draw table header
        Paint headerPaint = new Paint();
        headerPaint.setColor(COLOR_PRIMARY);
        headerPaint.setTextSize(12);
        headerPaint.setTypeface(Typeface.DEFAULT_BOLD);
        headerPaint.setAntiAlias(true);

        Paint headerBgPaint = new Paint();
        headerBgPaint.setColor(COLOR_HEADER_BG);

        // Table header background
        int headerY = y - 5;
        canvas.drawRect(margin, headerY, margin + contentWidth, headerY + 25, headerBgPaint);

        // Table headers
        float[] columnWidths = {80, 120, 100, 80, 80, 70};
        float x = margin + 10;
        String[] headers = {"Date", "Blood Pressure", "Blood Sugar", "Temp", "Weight", "SpO₂"};

        for (int i = 0; i < headers.length; i++) {
            canvas.drawText(headers[i], x, y + 15, headerPaint);
            x += columnWidths[i];
        }

        y += 30;

        // Draw table content
        SimpleDateFormat display = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        if (vitals == null || vitals.isEmpty()) {
            Paint emptyPaint = new Paint();
            emptyPaint.setColor(COLOR_LIGHT_TEXT);
            emptyPaint.setTextSize(12);
            emptyPaint.setAntiAlias(true);
            canvas.drawText("No vitals recorded in the last 30 days.", margin + 15, y + 10, emptyPaint);
            y += 30;
        } else {
            Paint rowPaint = new Paint();
            rowPaint.setTextSize(12);
            rowPaint.setAntiAlias(true);

            Paint valuePaintRow = new Paint();
            valuePaintRow.setTextSize(12);
            valuePaintRow.setColor(COLOR_TEXT);
            valuePaintRow.setAntiAlias(true);

            int rowCount = 0;
            for (VitalsRecord v : vitals) {
                // Alternate row colors for better readability
                if (rowCount % 2 == 0) {
                    Paint altRowPaint = new Paint();
                    altRowPaint.setColor(COLOR_LIGHT_GRAY);
                    canvas.drawRect(margin, y - 3, margin + contentWidth, y + 18, altRowPaint);
                }

                x = margin + 10;

                // Date
                canvas.drawText(display.format(v.date), x, y + 10, valuePaintRow);
                x += columnWidths[0];

                // BP
                String bp = v.systolic + "/" + v.diastolic;
                canvas.drawText(bp, x, y + 10, valuePaintRow);
                x += columnWidths[1];

                // Blood Sugar
                String sugar = v.bloodSugar + " mg/dL";
                canvas.drawText(sugar, x, y + 10, valuePaintRow);
                x += columnWidths[2];

                // Temperature
                String temp = v.temperature + "°C";
                canvas.drawText(temp, x, y + 10, valuePaintRow);
                x += columnWidths[3];

                // Weight
                String weight = v.weight + " kg";
                canvas.drawText(weight, x, y + 10, valuePaintRow);
                x += columnWidths[4];

                // SpO₂
                String spo2 = v.spo2 + "%";
                canvas.drawText(spo2, x, y + 10, valuePaintRow);

                y += 22;
                rowCount++;

                // Prevent writing beyond page
                if (y > 780) {
                    break;
                }
            }
        }

        // Draw footer line
        Paint footerLinePaint = new Paint();
        footerLinePaint.setColor(COLOR_BORDER);
        footerLinePaint.setStrokeWidth(1);
        footerLinePaint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(margin, y + 20, margin + contentWidth, y + 20, footerLinePaint);

        // Draw footer text
        Paint footerPaint = new Paint();
        footerPaint.setColor(COLOR_LIGHT_TEXT);
        footerPaint.setTextSize(10);
        footerPaint.setAntiAlias(true);

        String footerText = "Generated by MediTrack App • " +
                new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        float footerWidth = footerPaint.measureText(footerText);
        canvas.drawText(footerText, margin + contentWidth - footerWidth, y + 35, footerPaint);

        return y + 50;
    }
}