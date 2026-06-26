package com.example.meditrack.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.meditrack.R;
import com.example.meditrack.database.DatabaseHelper;
import com.example.meditrack.models.DoctorVisit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddDoctorVisitFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private EditText etDoctorName, etVisitDate, etPrescription, etFollowUpDate, etNotes;
    private Button btnSave, btnCancel;
    private Calendar visitCalendar = Calendar.getInstance();
    private Calendar followUpCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_doctor_visit, container, false);

        dbHelper = new DatabaseHelper(getContext());

        // Initialize views
        etDoctorName = view.findViewById(R.id.et_doctor_name);
        etVisitDate = view.findViewById(R.id.et_visit_date);
        etPrescription = view.findViewById(R.id.et_prescription);
        etFollowUpDate = view.findViewById(R.id.et_follow_up_date);
        etNotes = view.findViewById(R.id.et_notes);
        btnSave = view.findViewById(R.id.btn_save_visit);
        btnCancel = view.findViewById(R.id.btn_cancel);

        // Set up date pickers
        etVisitDate.setOnClickListener(v -> showDatePicker(visitCalendar, etVisitDate));
        etFollowUpDate.setOnClickListener(v -> showDatePicker(followUpCalendar, etFollowUpDate));

        // Set current date as default
        etVisitDate.setText(dateFormat.format(new Date()));

        btnSave.setOnClickListener(v -> saveDoctorVisit());
        btnCancel.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        return view;
    }

    private void showDatePicker(Calendar calendar, EditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    editText.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveDoctorVisit() {
        String doctorName = etDoctorName.getText().toString().trim();
        String visitDateStr = etVisitDate.getText().toString().trim();
        String prescription = etPrescription.getText().toString().trim();
        String followUpDateStr = etFollowUpDate.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (doctorName.isEmpty()) {
            Toast.makeText(getContext(), "Please enter doctor's name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (visitDateStr.isEmpty()) {
            Toast.makeText(getContext(), "Please select visit date", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            DoctorVisit visit = new DoctorVisit();
            visit.doctorName = doctorName;
            visit.date = dateFormat.parse(visitDateStr);
            visit.prescription = prescription;
            visit.notes = notes;

            if (!followUpDateStr.isEmpty()) {
                visit.followUpDate = dateFormat.parse(followUpDateStr);
            }

            long id = dbHelper.addDoctorVisit(visit);
            if (id != -1) {
                Toast.makeText(getContext(), "Doctor visit saved successfully!", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            } else {
                Toast.makeText(getContext(), "Failed to save doctor visit", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}