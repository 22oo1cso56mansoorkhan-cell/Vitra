package com.example.meditrack.fragments;

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
import androidx.fragment.app.FragmentTransaction;

import com.example.meditrack.R;
import com.example.meditrack.database.DatabaseHelper;
import com.example.meditrack.models.Symptom;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistorySymptomsFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_symptoms, container, false);

        // Handle add button click
        Button btnAdd = view.findViewById(R.id.btn_add_symptom);
        btnAdd.setOnClickListener(v -> {
            try {
                // Create new instance of AddSymptomFragment
                Fragment addFragment = new AddSymptomFragment();

                // Get FragmentManager and start transaction
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.commit();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Load symptoms
        loadSymptoms(view);

        return view;
    }

    private void loadSymptoms(View view) {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(getContext());
            List<Symptom> symptoms = dbHelper.getAllSymptoms();

            LinearLayout layout = view.findViewById(R.id.layout_symptoms);
            layout.removeAllViews(); // Clear existing views

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

            if (symptoms == null || symptoms.isEmpty()) {
                TextView tvEmpty = new TextView(getContext());
                tvEmpty.setText("No symptoms logged yet");
                tvEmpty.setTextSize(16);
                tvEmpty.setPadding(16, 16, 16, 16);
                layout.addView(tvEmpty);
            } else {
                for (Symptom s : symptoms) {
                    View item = getLayoutInflater().inflate(R.layout.item_symptom, null);
                    TextView tvDate = item.findViewById(R.id.tv_symptom_date);
                    TextView tvName = item.findViewById(R.id.tv_symptom_name);
                    TextView tvSeverity = item.findViewById(R.id.tv_symptom_severity);
                    TextView tvNotes = item.findViewById(R.id.tv_symptom_notes);

                    tvDate.setText(dateFormat.format(s.date));
                    tvName.setText(s.symptomName);
                    tvSeverity.setText("Severity: " + getSeverityText(s.severity));

                    if (s.notes != null && !s.notes.isEmpty()) {
                        tvNotes.setText(s.notes);
                        tvNotes.setVisibility(View.VISIBLE);
                    } else {
                        tvNotes.setVisibility(View.GONE);
                    }

                    layout.addView(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading symptoms: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getSeverityText(int severity) {
        switch (severity) {
            case 1: return "Mild";
            case 2: return "Moderate";
            case 3: return "Severe";
            case 4: return "Very Severe";
            case 5: return "Critical";
            default: return "Unknown";
        }
    }

    // This method will be called when returning from AddSymptomFragment
    @Override
    public void onResume() {
        super.onResume();
        // Refresh the list when returning to this fragment
        if (getView() != null) {
            loadSymptoms(getView());
        }
    }
}