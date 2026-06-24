package com.example.meditrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meditrack.R;
import com.example.meditrack.adapters.VitalsAdapter;
import com.example.meditrack.database.DatabaseHelper;
import com.example.meditrack.models.VitalsRecord;

import java.util.Date;
import java.util.List;

public class VitalsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private EditText etSystolic, etDiastolic, etSugar, etTemp, etWeight, etSpO2, etNotes;
    private Switch switchFasting;
    private Button btnSave;
    private RecyclerView recyclerView;
    private VitalsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vitals, container, false);

        dbHelper = new DatabaseHelper(getContext());

        etSystolic = view.findViewById(R.id.et_systolic);
        etDiastolic = view.findViewById(R.id.et_diastolic);
        etSugar = view.findViewById(R.id.et_sugar);
        etTemp = view.findViewById(R.id.et_temp);
        etWeight = view.findViewById(R.id.et_weight);
        etSpO2 = view.findViewById(R.id.et_spo2);
        etNotes = view.findViewById(R.id.et_notes);
        switchFasting = view.findViewById(R.id.switch_fasting);
        btnSave = view.findViewById(R.id.btn_save_vitals);
        recyclerView = view.findViewById(R.id.recycler_vitals);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadVitals();

        btnSave.setOnClickListener(v -> saveVitals());

        return view;
    }

    private void saveVitals() {
        try {
            VitalsRecord record = new VitalsRecord();
            record.date = new Date();
            record.systolic = Integer.parseInt(etSystolic.getText().toString());
            record.diastolic = Integer.parseInt(etDiastolic.getText().toString());
            record.bloodSugar = Integer.parseInt(etSugar.getText().toString());
            record.isFasting = switchFasting.isChecked();
            record.temperature = Float.parseFloat(etTemp.getText().toString());
            record.weight = Float.parseFloat(etWeight.getText().toString());
            record.spo2 = Integer.parseInt(etSpO2.getText().toString());
            record.notes = etNotes.getText().toString();

            dbHelper.addVitals(record);
            Toast.makeText(getContext(), "Vitals saved!", Toast.LENGTH_SHORT).show();

            clearFields();
            loadVitals();

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please fill all fields with valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        etSystolic.setText("");
        etDiastolic.setText("");
        etSugar.setText("");
        etTemp.setText("");
        etWeight.setText("");
        etSpO2.setText("");
        etNotes.setText("");
        switchFasting.setChecked(true);
    }

    private void loadVitals() {
        List<VitalsRecord> vitals = dbHelper.getAllVitals();
        if (adapter == null) {
            adapter = new VitalsAdapter(vitals);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(vitals);
        }
    }
}