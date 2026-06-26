package com.example.meditrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.meditrack.R;
import com.example.meditrack.database.DatabaseHelper;
import com.example.meditrack.models.DoctorVisit;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddDoctorVisitFragment extends Fragment {

    private TextInputEditText etDoctor, etPrescription, etFollowUp, etNotes;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_doctor_visit, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        etDoctor = view.findViewById(R.id.et_doctor_name);
        etPrescription = view.findViewById(R.id.et_prescription);
        etFollowUp = view.findViewById(R.id.et_followup);
        etNotes = view.findViewById(R.id.et_notes);

        Button btnSave = view.findViewById(R.id.btn_save_visit);
        btnSave.setOnClickListener(v -> saveVisit());

        return view;
    }

    private void saveVisit() {
        String doctorName = etDoctor.getText().toString().trim();
        String prescription = etPrescription.getText().toString().trim();
        String followUpStr = etFollowUp.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (doctorName.isEmpty()) {
            etDoctor.setError("Doctor name is required");
            etDoctor.requestFocus();
            return;
        }

        DoctorVisit visit = new DoctorVisit();
        visit.date = new Date();
        visit.doctorName = doctorName;
        visit.prescription = prescription;
        visit.notes = notes;

        try {
            if (!followUpStr.isEmpty()) {
                visit.followUpDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(followUpStr);
            }
        } catch (Exception e) {
            etFollowUp.setError("Invalid date format. Use yyyy-MM-dd");
            etFollowUp.requestFocus();
            return;
        }

        long id = dbHelper.addDoctorVisit(visit);
        if (id > 0) {
            Toast.makeText(requireContext(), "Doctor visit saved successfully!", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            Toast.makeText(requireContext(), "Failed to save visit", Toast.LENGTH_SHORT).show();
        }
    }
}