package com.example.meditrack.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.meditrack.R;
import com.example.meditrack.database.DatabaseHelper;
import com.example.meditrack.models.Medicine;
import com.example.meditrack.models.UserProfile;
import com.example.meditrack.models.VitalsRecord;
import com.example.meditrack.utils.PDFGenerator;

// MPAndroidChart imports
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TextView tvWelcome, tvMedCount, tvVitalsCount, tvSymptomCount;
    private LineChart chartVitals;
    private Button btnSOS, btnGenerateReport;
    private LinearLayout layoutRecentVitals;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        dbHelper = new DatabaseHelper(getContext());

        tvWelcome = view.findViewById(R.id.tv_welcome);
        tvMedCount = view.findViewById(R.id.tv_med_count);
        tvVitalsCount = view.findViewById(R.id.tv_vitals_count);
        tvSymptomCount = view.findViewById(R.id.tv_symptom_count);
        chartVitals = view.findViewById(R.id.chart_vitals);
        btnSOS = view.findViewById(R.id.btn_sos);
        btnGenerateReport = view.findViewById(R.id.btn_generate_report);
        layoutRecentVitals = view.findViewById(R.id.layout_recent_vitals);

        loadDashboardData();
        setupChart();

        btnSOS.setOnClickListener(v -> sendSOS());
        btnGenerateReport.setOnClickListener(v -> generateReport());

        return view;
    }

    private void loadDashboardData() {
        UserProfile profile = dbHelper.getProfile();
        if (profile != null && !profile.name.isEmpty()) {
            tvWelcome.setText("Hello, " + profile.name + "! 👋");
        } else {
            tvWelcome.setText("Welcome to MediTrack! 👋");
        }

        List<Medicine> medicines = dbHelper.getAllMedicines();
        int activeCount = 0;
        for (Medicine m : medicines) {
            if (m.isActive) activeCount++;
        }
        tvMedCount.setText(activeCount + " active");

        List<VitalsRecord> vitals = dbHelper.getAllVitals();
        tvVitalsCount.setText(vitals.size() + " records");

        List<VitalsRecord> recent = dbHelper.getVitalsBetween(getWeekAgo(), new Date());
        tvSymptomCount.setText(recent.size() + " this week");

        layoutRecentVitals.removeAllViews();
        List<VitalsRecord> lastFew = dbHelper.getAllVitals();
        int count = 0;
        for (VitalsRecord v : lastFew) {
            if (count >= 3) break;
            View item = getLayoutInflater().inflate(R.layout.item_vital_small, null);
            TextView tvDate = item.findViewById(R.id.tv_date);
            TextView tvBP = item.findViewById(R.id.tv_bp);
            TextView tvSugar = item.findViewById(R.id.tv_sugar);

            tvDate.setText(dateFormat.format(v.date));
            tvBP.setText(v.systolic + "/" + v.diastolic);
            tvSugar.setText(v.bloodSugar + " mg/dL");

            layoutRecentVitals.addView(item);
            count++;
        }
    }

    private void setupChart() {
        List<VitalsRecord> vitals = dbHelper.getAllVitals();
        if (vitals.isEmpty()) {
            chartVitals.setNoDataText("No vitals to show");
            return;
        }

        List<Entry> entries = new ArrayList<>();
        int size = vitals.size();
        for (int i = 0; i < size && i < 30; i++) {
            VitalsRecord v = vitals.get(size - 1 - i);
            entries.add(new Entry(i, v.systolic));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Systolic BP");
        dataSet.setColor(getResources().getColor(R.color.primary));
        dataSet.setCircleColor(getResources().getColor(R.color.primary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);

        LineData lineData = new LineData(dataSet);
        chartVitals.setData(lineData);
        chartVitals.invalidate();
    }

    private Date getWeekAgo() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        return cal.getTime();
    }

    private void sendSOS() {
        UserProfile profile = dbHelper.getProfile();
        if (profile == null || profile.emergencyPhone.isEmpty()) {
            Toast.makeText(getContext(), "Please set emergency contact in profile", Toast.LENGTH_LONG).show();
            return;
        }

        String message = "SOS from MediTrack!\n";
        message += "Patient: " + profile.name + "\n";
        message += "Blood Group: " + profile.bloodGroup + "\n";
        message += "Conditions: " + profile.conditions + "\n";

        List<VitalsRecord> recent = dbHelper.getVitalsBetween(getWeekAgo(), new Date());
        if (!recent.isEmpty()) {
            VitalsRecord last = recent.get(0);
            message += "Latest vitals - BP: " + last.systolic + "/" + last.diastolic +
                    ", Sugar: " + last.bloodSugar + " mg/dL\n";
        }

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + profile.emergencyPhone));
        intent.putExtra("sms_body", message);
        startActivity(intent);
    }

    private void generateReport() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Generate Report");
        builder.setMessage("Generating PDF report for the last 30 days...");
        builder.setPositiveButton("OK", (dialog, which) -> {
            String path = PDFGenerator.generateReport(getContext(), dbHelper);
            if (path != null) {
                Toast.makeText(getContext(), "Report saved: " + path, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Failed to generate report", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}