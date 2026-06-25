package com.example.meditrack.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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

    private DatabaseHelper dbHelper;
    private EditText etName, etDose, etInterval;
    private Spinner spinnerFrequency;
    private Button btnAdd;
    private RecyclerView recyclerView;
    private MedicineAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medicines, container, false);

        dbHelper = new DatabaseHelper(getContext());

        etName = view.findViewById(R.id.et_med_name);
        etDose = view.findViewById(R.id.et_med_dose);
        etInterval = view.findViewById(R.id.et_interval);
        spinnerFrequency = view.findViewById(R.id.spinner_frequency);
        btnAdd = view.findViewById(R.id.btn_add_medicine);
        recyclerView = view.findViewById(R.id.recycler_medicines);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup spinner
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(getContext(),
                R.array.frequency_options, android.R.layout.simple_spinner_item);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapterSpinner);

        spinnerFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 2) { // Every X Hours
                    etInterval.setVisibility(View.VISIBLE);
                } else {
                    etInterval.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnAdd.setOnClickListener(v -> addMedicine());
        loadMedicines();

        return view;
    }

    private void addMedicine() {
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
            try {
                medicine.intervalHours = Integer.parseInt(etInterval.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter valid interval hours", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Set next dose time
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, medicine.intervalHours);
        medicine.nextDoseTime = cal.getTime();

        long insertedId = dbHelper.addMedicine(medicine);
        medicine.id = (int) insertedId;
        scheduleMedicineReminder(medicine);

        Toast.makeText(getContext(), "Medicine added!", Toast.LENGTH_SHORT).show();
        etName.setText("");
        etDose.setText("");
        etInterval.setText("");
        loadMedicines();
    }

    private void scheduleMedicineReminder(Medicine medicine) {
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), MedicineReceiver.class);
        intent.putExtra("medicine_name", medicine.name);
        intent.putExtra("medicine_dose", medicine.dose);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(),
                medicine.id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar cal = Calendar.getInstance();
        cal.setTime(medicine.nextDoseTime);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        }
    }

    private void loadMedicines() {
        List<Medicine> medicines = dbHelper.getAllMedicines();
        if (adapter == null) {
            adapter = new MedicineAdapter(medicines, dbHelper);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(medicines);
        }
    }
}