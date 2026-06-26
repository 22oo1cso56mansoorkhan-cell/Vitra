package com.example.meditrack.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meditrack.R;
import com.example.meditrack.adapters.MedicineAdapter;
import com.example.meditrack.database.DatabaseHelper;
import com.example.meditrack.models.Medicine;
import com.example.meditrack.services.MedicineReceiver;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MedicineFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private DatabaseHelper dbHelper;
    private EditText etName;
    private EditText etDose;
    private EditText etInterval;
    private Spinner spinnerFrequency;
    private Button btnAdd;
    private RecyclerView recyclerView;
    private MedicineAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_medicines, container, false);

        // Initialize Database Helper
        dbHelper = new DatabaseHelper(getContext());

        // Initialize views
        etName = view.findViewById(R.id.et_med_name);
        etDose = view.findViewById(R.id.et_med_dose);
        etInterval = view.findViewById(R.id.et_interval);
        spinnerFrequency = view.findViewById(R.id.spinner_frequency);
        btnAdd = view.findViewById(R.id.btn_add_medicine);
        recyclerView = view.findViewById(R.id.recycler_medicines);

        // Check if views are found
        if (etName == null || etDose == null || etInterval == null ||
                spinnerFrequency == null || btnAdd == null || recyclerView == null) {
            Toast.makeText(getContext(), "Error initializing views", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Check and request permissions
        checkAndRequestPermissions();

        // Setup spinner
        setupSpinner();

        // Set click listener for add button
        btnAdd.setOnClickListener(v -> addMedicine());

        // Load medicines
        loadMedicines();

        return view;
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(getContext(),
                R.array.frequency_options, android.R.layout.simple_spinner_item);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapterSpinner);

        spinnerFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 2) { // Every X Hours
                    etInterval.setVisibility(View.VISIBLE);
                    etInterval.setEnabled(true);
                } else {
                    etInterval.setVisibility(View.GONE);
                    etInterval.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                etInterval.setVisibility(View.GONE);
                etInterval.setEnabled(false);
            }
        });

        // Initially hide interval field
        etInterval.setVisibility(View.GONE);
        etInterval.setEnabled(false);
    }

    private void checkAndRequestPermissions() {
        Context context = getContext();
        if (context == null) return;

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }
        }

        // Check and request exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // Request permission
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Notification permission granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addMedicine() {
        // Check if context is available
        if (getContext() == null) return;

        String name = etName.getText().toString().trim();
        String dose = etDose.getText().toString().trim();

        if (name.isEmpty() || dose.isEmpty()) {
            Toast.makeText(getContext(), "Please enter medicine name and dose", Toast.LENGTH_SHORT).show();
            return;
        }

        Medicine medicine = new Medicine();
        medicine.name = name;
        medicine.dose = dose;
        medicine.startDate = new Date();
        medicine.isActive = true;
        medicine.doseCount = 0;

        int position = spinnerFrequency.getSelectedItemPosition();
        if (position == 0) {
            medicine.frequency = "once";
            medicine.intervalHours = 24;
        } else if (position == 1) {
            medicine.frequency = "twice";
            medicine.intervalHours = 12;
        } else {
            medicine.frequency = "every_x_hours";
            String intervalText = etInterval.getText().toString().trim();
            if (intervalText.isEmpty()) {
                Toast.makeText(getContext(), "Please enter interval hours", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                medicine.intervalHours = Integer.parseInt(intervalText);
                if (medicine.intervalHours < 1) {
                    Toast.makeText(getContext(), "Interval must be at least 1 hour", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter valid interval hours", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Set next dose time
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, medicine.intervalHours);
        medicine.nextDoseTime = cal.getTime();

        // Add to database and get ID
        long id = dbHelper.addMedicine(medicine);
        if (id == -1) {
            Toast.makeText(getContext(), "Failed to add medicine", Toast.LENGTH_SHORT).show();
            return;
        }
        medicine.id = (int) id;

        // Schedule reminder
        boolean scheduled = scheduleMedicineReminder(medicine);
        if (scheduled) {
            Toast.makeText(getContext(), "Medicine added! Reminder set for " +
                    formatTime(medicine.nextDoseTime), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Medicine added but reminder scheduling failed", Toast.LENGTH_SHORT).show();
        }

        // Clear fields
        etName.setText("");
        etDose.setText("");
        etInterval.setText("");
        loadMedicines();
    }

    private boolean scheduleMedicineReminder(Medicine medicine) {
        Context context = getContext();
        if (context == null) return false;

        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return false;

            Intent intent = new Intent(context, MedicineReceiver.class);
            intent.putExtra("medicine_name", medicine.name);
            intent.putExtra("medicine_dose", medicine.dose);
            intent.putExtra("medicine_id", medicine.id);

            // Use unique request code based on medicine ID
            int requestCode = medicine.id;

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    requestCode, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Calendar cal = Calendar.getInstance();
            cal.setTime(medicine.nextDoseTime);

            // Check if we can schedule exact alarms (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    // Use setAlarmClock as fallback
                    alarmManager.setAlarmClock(
                            new AlarmManager.AlarmClockInfo(cal.getTimeInMillis(), null),
                            pendingIntent
                    );
                    return true;
                }
            }

            // Schedule with appropriate method
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        cal.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String formatTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        String ampm = hour >= 12 ? "PM" : "AM";
        hour = hour % 12;
        hour = hour == 0 ? 12 : hour;
        return String.format("%02d:%02d %s", hour, minute, ampm);
    }

    private void loadMedicines() {
        if (getContext() == null) return;

        List<Medicine> medicines = dbHelper.getAllMedicines();
        if (adapter == null) {
            adapter = new MedicineAdapter(medicines, dbHelper);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(medicines);
        }
    }
}