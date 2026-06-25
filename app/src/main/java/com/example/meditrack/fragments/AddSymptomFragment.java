package com.example.meditrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.meditrack.R;
import com.example.meditrack.database.DatabaseHelper;
import com.example.meditrack.models.Symptom;

import java.util.Date;

public class AddSymptomFragment extends Fragment {

    private EditText etSymptomName, etNotes;
    private RadioGroup rgSeverity;
    private Button btnSaveSymptom;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_symptom, container, false);

        etSymptomName = view.findViewById(R.id.et_symptom_name);
        etNotes = view.findViewById(R.id.et_symptom_notes);
        rgSeverity = view.findViewById(R.id.rg_severity);
        btnSaveSymptom = view.findViewById(R.id.btn_save_symptom);

        btnSaveSymptom.setOnClickListener(v -> saveSymptom());

        return view;
    }

    private void saveSymptom() {
        try {
            String name = etSymptomName.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();

            if (name.isEmpty()) {
                etSymptomName.setError("Please enter symptom name");
                etSymptomName.requestFocus();
                return;
            }

            // Get selected severity
            int selectedId = rgSeverity.getCheckedRadioButtonId();
            int severity = 1; // Default mild

            if (selectedId == R.id.rb_mild) severity = 1;
            else if (selectedId == R.id.rb_moderate) severity = 2;
            else if (selectedId == R.id.rb_severe) severity = 3;
            else if (selectedId == R.id.rb_very_severe) severity = 4;
            else if (selectedId == R.id.rb_critical) severity = 5;

            Symptom symptom = new Symptom();
            symptom.symptomName = name;
            symptom.severity = severity;
            symptom.notes = notes;
            symptom.date = new Date(); // Current date/time

            DatabaseHelper dbHelper = new DatabaseHelper(getContext());
            long id = dbHelper.addSymptom(symptom);

            if (id != -1) {
                Toast.makeText(getContext(), "Symptom added successfully!", Toast.LENGTH_SHORT).show();
                // Go back to previous fragment
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            } else {
                Toast.makeText(getContext(), "Failed to add symptom", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}