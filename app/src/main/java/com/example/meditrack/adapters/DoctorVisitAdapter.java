package com.example.meditrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meditrack.R;
import com.example.meditrack.database.DatabaseHelper;
import com.example.meditrack.models.DoctorVisit;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DoctorVisitAdapter extends RecyclerView.Adapter<DoctorVisitAdapter.ViewHolder> {

    private List<DoctorVisit> visits;
    private DatabaseHelper dbHelper;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public DoctorVisitAdapter(List<DoctorVisit> visits, DatabaseHelper dbHelper) {
        this.visits = visits;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor_visit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoctorVisit visit = visits.get(position);

        // Set doctor name
        if (visit.doctorName != null && !visit.doctorName.isEmpty()) {
            holder.tvDoctorName.setText(visit.doctorName);
        } else {
            holder.tvDoctorName.setText("Unknown Doctor");
        }

        // Set visit date - FIXED: Check if date exists
        if (visit.date != null) {
            try {
                holder.tvVisitDate.setText(dateFormat.format(visit.date));
            } catch (Exception e) {
                e.printStackTrace();
                holder.tvVisitDate.setText("Invalid Date");
            }
        } else {
            holder.tvVisitDate.setText("No Date");
        }

        // Set prescription
        if (visit.prescription != null && !visit.prescription.isEmpty()) {
            holder.tvPrescription.setText("💊 " + visit.prescription);
            holder.tvPrescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvPrescription.setVisibility(View.GONE);
        }

        // Set follow-up date
        if (visit.followUpDate != null) {
            try {
                holder.tvFollowUp.setText("📅 Follow-up: " + dateFormat.format(visit.followUpDate));
                holder.tvFollowUp.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
                holder.tvFollowUp.setVisibility(View.GONE);
            }
        } else {
            holder.tvFollowUp.setVisibility(View.GONE);
        }

        // Set notes
        if (visit.notes != null && !visit.notes.isEmpty()) {
            holder.tvNotes.setText("📝 " + visit.notes);
            holder.tvNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return visits != null ? visits.size() : 0;
    }

    public void updateData(List<DoctorVisit> newVisits) {
        this.visits = newVisits;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorName, tvVisitDate, tvPrescription, tvFollowUp, tvNotes;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tv_doctor_name);
            tvVisitDate = itemView.findViewById(R.id.tv_visit_date);
            tvPrescription = itemView.findViewById(R.id.tv_prescription);
            tvFollowUp = itemView.findViewById(R.id.tv_follow_up);
            tvNotes = itemView.findViewById(R.id.tv_notes);
        }
    }
}