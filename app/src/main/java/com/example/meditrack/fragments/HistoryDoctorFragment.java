package com.example.meditrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.meditrack.database.DatabaseHelper;
import com.example.meditrack.models.DoctorVisit;
import com.example.meditrack.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryDoctorFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_doctor, container, false);

        // === NEW: Add button to open AddDoctorVisitFragment ===
        Button btnAdd = view.findViewById(R.id.btn_add_visit);
        btnAdd.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new AddDoctorVisitFragment())
                        .addToBackStack(null)
                        .commit()
        );

        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        List<DoctorVisit> visits = dbHelper.getAllDoctorVisits();

        LinearLayout layout = view.findViewById(R.id.layout_doctor_visits);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        if (visits.isEmpty()) {
            TextView tvEmpty = new TextView(getContext());
            tvEmpty.setText("No doctor visits logged yet");
            tvEmpty.setTextSize(16);
            tvEmpty.setPadding(16, 16, 16, 16);
            layout.addView(tvEmpty);
        } else {
            for (DoctorVisit v : visits) {
                View item = getLayoutInflater().inflate(R.layout.item_doctor_visit, null);
                TextView tvDate = item.findViewById(R.id.tv_visit_date);
                TextView tvDoctor = item.findViewById(R.id.tv_visit_doctor);
                TextView tvPrescription = item.findViewById(R.id.tv_visit_prescription);
                TextView tvFollowUp = item.findViewById(R.id.tv_visit_followup);
                TextView tvNotes = item.findViewById(R.id.tv_visit_notes); // NEW

                tvDate.setText(dateFormat.format(v.date));
                tvDoctor.setText("Doctor: " + v.doctorName);
                tvPrescription.setText("Prescription: " + v.prescription);

                if (v.followUpDate != null) {
                    tvFollowUp.setText("Follow-up: " + dateFormat.format(v.followUpDate));
                } else {
                    tvFollowUp.setText("No follow-up scheduled");
                }

                // === NEW: Display notes if they exist ===
                if (v.notes != null && !v.notes.trim().isEmpty()) {
                    tvNotes.setText("📝 " + v.notes);
                    tvNotes.setVisibility(View.VISIBLE);
                } else {
                    tvNotes.setVisibility(View.GONE);
                }

                layout.addView(item);
            }
        }

        return view;
    }
}