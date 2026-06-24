package com.example.meditrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meditrack.models.Medicine;
import com.example.meditrack.R;
import com.example.meditrack.database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    private List<Medicine> medicines;
    private DatabaseHelper dbHelper;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

    public MedicineAdapter(List<Medicine> medicines, DatabaseHelper dbHelper) {
        this.medicines = medicines;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine m = medicines.get(position);
        holder.tvName.setText(m.name);
        holder.tvDose.setText(m.dose);
        holder.tvFrequency.setText("Frequency: " + getFrequencyText(m.frequency, m.intervalHours));

        if (m.isActive) {
            holder.tvStatus.setText("Active");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.success));
        } else {
            holder.tvStatus.setText("Inactive");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.text_secondary));
        }

        holder.btnDelete.setOnClickListener(v -> {
            dbHelper.deleteMedicine(m.id);
            medicines.remove(position);
            notifyItemRemoved(position);
        });
    }

    private String getFrequencyText(String frequency, int intervalHours) {
        if (frequency.equals("once")) return "Once Daily";
        if (frequency.equals("twice")) return "Twice Daily";
        return "Every " + intervalHours + " hours";
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    public void updateData(List<Medicine> newMedicines) {
        this.medicines = newMedicines;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDose, tvFrequency, tvStatus;
        Button btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_med_name);
            tvDose = itemView.findViewById(R.id.tv_med_dose);
            tvFrequency = itemView.findViewById(R.id.tv_med_frequency);
            tvStatus = itemView.findViewById(R.id.tv_med_status);
            btnDelete = itemView.findViewById(R.id.btn_delete_medicine);
        }
    }
}