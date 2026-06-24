package com.example.meditrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meditrack.R;
import com.example.meditrack.models.VitalsRecord;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VitalsAdapter extends RecyclerView.Adapter<VitalsAdapter.ViewHolder> {

    private List<VitalsRecord> vitals;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public VitalsAdapter(List<VitalsRecord> vitals) {
        this.vitals = vitals;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vital, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VitalsRecord v = vitals.get(position);
        holder.tvDate.setText(dateFormat.format(v.date));
        holder.tvDetails.setText("BP: " + v.systolic + "/" + v.diastolic +
                " | Sugar: " + v.bloodSugar + " mg/dL" +
                " | Temp: " + v.temperature + "°C");

        // Determine status
        String status;
        int color;
        if (v.systolic > 140 || v.diastolic > 90) {
            status = "High";
            color = holder.itemView.getContext().getColor(R.color.danger);
        } else if (v.systolic < 90 || v.diastolic < 60) {
            status = "Low";
            color = holder.itemView.getContext().getColor(R.color.warning);
        } else {
            status = "Normal";
            color = holder.itemView.getContext().getColor(R.color.success);
        }
        holder.tvStatus.setText(status);
        holder.tvStatus.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return vitals.size();
    }

    public void updateData(List<VitalsRecord> newVitals) {
        this.vitals = newVitals;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDetails, tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_vital_date);
            tvDetails = itemView.findViewById(R.id.tv_vital_details);
            tvStatus = itemView.findViewById(R.id.tv_vital_status);
        }
    }
}