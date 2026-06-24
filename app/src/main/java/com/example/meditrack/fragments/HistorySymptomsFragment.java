package com.example.meditrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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

        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        List<Symptom> symptoms = dbHelper.getAllSymptoms();

        LinearLayout layout = view.findViewById(R.id.layout_symptoms);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        if (symptoms.isEmpty()) {
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
                tvNotes.setText(s.notes);

                layout.addView(item);
            }
        }

        return view;
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
}