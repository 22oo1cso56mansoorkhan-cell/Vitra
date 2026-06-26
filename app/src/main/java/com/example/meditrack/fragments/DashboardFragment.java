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
import com.example.meditrack.models.EmergencyContact;
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

    // Store user profile ID for emergency contacts
    private long currentProfileId = -1;

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
        if (profile != null && profile.id > 0) {
            currentProfileId = profile.id;
            if (!profile.name.isEmpty()) {
                tvWelcome.setText("Hello, " + profile.name + "! 👋");
            } else {
                tvWelcome.setText("Welcome to MediTrack! 👋");
            }
        } else {
            currentProfileId = -1;
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

    // =====================================================
    // ============ UPDATED SOS METHOD ============
    // =====================================================

    private void sendSOS() {
        // Get the user profile
        UserProfile profile = dbHelper.getProfile();
        if (profile == null || profile.id <= 0) {
            Toast.makeText(getContext(), "Please set up your profile first", Toast.LENGTH_LONG).show();
            return;
        }

        // Get all emergency contacts for this profile
        List<EmergencyContact> emergencyContacts = dbHelper.getEmergencyContacts(profile.id);

        // Check if there are any emergency contacts
        if (emergencyContacts == null || emergencyContacts.isEmpty()) {
            Toast.makeText(getContext(), "Please add emergency contacts in profile", Toast.LENGTH_LONG).show();
            return;
        }

        // Build the SOS message
        String message = buildSOSMessage(profile);

        // Send SOS to all emergency contacts
        sendSOSToAllContacts(emergencyContacts, message);
    }

    /**
     * Build the SOS message with all patient information
     */
    private String buildSOSMessage(UserProfile profile) {
        StringBuilder message = new StringBuilder();
        message.append("🚨 SOS Emergency Alert from MediTrack! 🚨\n\n");
        message.append("📍 Patient Details:\n");
        message.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        message.append("👤 Name: ").append(profile.name.isEmpty() ? "Not set" : profile.name).append("\n");
        message.append("🎂 Age: ").append(profile.age > 0 ? profile.age : "Not set").append("\n");
        message.append("🩸 Blood Group: ").append(profile.bloodGroup.isEmpty() ? "Not set" : profile.bloodGroup).append("\n");
        message.append("🏥 Conditions: ").append(profile.conditions.isEmpty() ? "None" : profile.conditions).append("\n");
        message.append("🤧 Allergies: ").append(profile.allergies.isEmpty() ? "None" : profile.allergies).append("\n");
        message.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        // Add latest vitals
        List<VitalsRecord> recentVitals = dbHelper.getVitalsBetween(getWeekAgo(), new Date());
        if (!recentVitals.isEmpty()) {
            VitalsRecord last = recentVitals.get(0);
            message.append("📊 Latest Vitals:\n");
            message.append("   ❤️ BP: ").append(last.systolic).append("/").append(last.diastolic).append(" mmHg\n");
            message.append("   🍬 Blood Sugar: ").append(last.bloodSugar).append(" mg/dL\n");
            message.append("   🌡️ Temperature: ").append(last.temperature).append(" °C\n");
            message.append("   ⚖️ Weight: ").append(last.weight).append(" kg\n");
            if (last.spo2 > 0) {
                message.append("   💨 SpO2: ").append(last.spo2).append("%\n");
            }
            message.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        }

        message.append("📱 This is an automated emergency alert from MediTrack.\n");
        message.append("Please contact the patient immediately!\n");
        message.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        message.append("⏰ Time: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        return message.toString();
    }

    /**
     * Send SOS to all emergency contacts
     */
    private void sendSOSToAllContacts(List<EmergencyContact> contacts, String message) {
        // If there's only one contact, send directly via SMS
        if (contacts.size() == 1) {
            EmergencyContact contact = contacts.get(0);
            if (contact.phone.isEmpty()) {
                Toast.makeText(getContext(), "Emergency contact phone number is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show dialog with contact info
            showSOSConfirmation(contact, message);
            return;
        }

        // Multiple contacts - show dialog to choose or send to all
        showMultipleContactsDialog(contacts, message);
    }

    /**
     * Show dialog for single contact
     */
    private void showSOSConfirmation(EmergencyContact contact, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("🚨 SOS Emergency");
        builder.setMessage("Send SOS to:\n\n📱 " + contact.name + "\n📞 " + contact.phone + "\n\n" + message);
        builder.setPositiveButton("Send SOS", (dialog, which) -> {
            sendSingleSMS(contact.phone, message);
            Toast.makeText(getContext(), "SOS sent to " + contact.name, Toast.LENGTH_LONG).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Show dialog for multiple contacts
     */
    private void showMultipleContactsDialog(List<EmergencyContact> contacts, String message) {
        // Create array of contact names for the dialog
        String[] contactNames = new String[contacts.size() + 1];
        contactNames[0] = "Send to ALL contacts";
        for (int i = 0; i < contacts.size(); i++) {
            EmergencyContact contact = contacts.get(i);
            contactNames[i + 1] = contact.name + " (" + contact.phone + ")";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("🚨 Select Emergency Contact");
        builder.setItems(contactNames, (dialog, which) -> {
            if (which == 0) {
                // Send to ALL
                sendSOSToAll(contacts, message);
            } else {
                // Send to specific contact
                EmergencyContact selected = contacts.get(which - 1);
                showSOSConfirmation(selected, message);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Send SOS to all contacts
     */
    private void sendSOSToAll(List<EmergencyContact> contacts, String message) {
        int sentCount = 0;
        for (EmergencyContact contact : contacts) {
            if (!contact.phone.isEmpty()) {
                sendSingleSMS(contact.phone, message);
                sentCount++;
            }
        }

        if (sentCount > 0) {
            Toast.makeText(getContext(), "SOS sent to " + sentCount + " emergency contacts!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "No valid phone numbers found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Send a single SMS
     */
    private void sendSingleSMS(String phoneNumber, String message) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + phoneNumber));
            intent.putExtra("sms_body", message);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to send SMS to " + phoneNumber, Toast.LENGTH_SHORT).show();
        }
    }

    // =====================================================
    // ============ GENERATE REPORT METHOD ============
    // =====================================================

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