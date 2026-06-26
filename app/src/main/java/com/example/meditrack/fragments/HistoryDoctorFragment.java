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
import com.example.meditrack.models.DoctorVisit;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryDoctorFragment extends Fragment {

    private LinearLayout layoutDoctorVisits;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_doctor, container, false);

        layoutDoctorVisits = view.findViewById(R.id.layout_doctor_visits);

        // Find the add button
        Button btnAdd = view.findViewById(R.id.btn_add_visit);

        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> openAddDoctorVisitFragment());
        }

        loadDoctorVisits();

        return view;
    }

    private void openAddDoctorVisitFragment() {
        try {
            AddDoctorVisitFragment fragment = new AddDoctorVisitFragment();

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, fragment)  // Changed to nav_host_fragment
                        .addToBackStack("add_doctor_visit")
                        .commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDoctorVisits() {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(getContext());
            List<DoctorVisit> visits = dbHelper.getAllDoctorVisits();

            if (layoutDoctorVisits != null) {
                layoutDoctorVisits.removeAllViews();
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            if (visits == null || visits.isEmpty()) {
                TextView tvEmpty = new TextView(getContext());
                tvEmpty.setText("No doctor visits recorded yet");
                tvEmpty.setTextSize(16);
                tvEmpty.setPadding(16, 16, 16, 16);
                if (layoutDoctorVisits != null) {
                    layoutDoctorVisits.addView(tvEmpty);
                }
            } else {
                for (DoctorVisit visit : visits) {
                    View item = getLayoutInflater().inflate(R.layout.item_doctor_visit, null);

                    TextView tvDoctorName = item.findViewById(R.id.tv_doctor_name);
                    TextView tvVisitDate = item.findViewById(R.id.tv_visit_date);
                    TextView tvPrescription = item.findViewById(R.id.tv_prescription);
                    TextView tvFollowUp = item.findViewById(R.id.tv_follow_up);
                    TextView tvNotes = item.findViewById(R.id.tv_notes);

                    // Set doctor name
                    tvDoctorName.setText(visit.doctorName != null ? visit.doctorName : "Unknown Doctor");

                    // Set visit date
                    if (visit.date != null) {
                        tvVisitDate.setText(dateFormat.format(visit.date));
                    } else {
                        tvVisitDate.setText("Date not set");
                    }

                    // Set prescription
                    if (visit.prescription != null && !visit.prescription.isEmpty()) {
                        tvPrescription.setText("💊 " + visit.prescription);
                        tvPrescription.setVisibility(View.VISIBLE);
                    } else {
                        tvPrescription.setVisibility(View.GONE);
                    }

                    // Set follow-up date
                    if (visit.followUpDate != null) {
                        tvFollowUp.setText("📅 Follow-up: " + dateFormat.format(visit.followUpDate));
                        tvFollowUp.setVisibility(View.VISIBLE);
                    } else {
                        tvFollowUp.setVisibility(View.GONE);
                    }

                    // Set notes
                    if (visit.notes != null && !visit.notes.isEmpty()) {
                        tvNotes.setText("📝 " + visit.notes);
                        tvNotes.setVisibility(View.VISIBLE);
                    } else {
                        tvNotes.setVisibility(View.GONE);
                    }

                    if (layoutDoctorVisits != null) {
                        layoutDoctorVisits.addView(item);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading doctor visits: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the list when returning to this fragment
        loadDoctorVisits();
    }
}