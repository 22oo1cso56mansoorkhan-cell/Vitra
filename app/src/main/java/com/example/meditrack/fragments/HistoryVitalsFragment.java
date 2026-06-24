package com.meditrack.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meditrack.R;
import com.example.meditrack.adapters.VitalsAdapter;
import com.example.meditrack.database.DatabaseHelper;


public class HistoryVitalsFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_vitals, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_history_vitals);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        VitalsAdapter adapter = new VitalsAdapter(dbHelper.getAllVitals());
        recyclerView.setAdapter(adapter);

        return view;
    }
}