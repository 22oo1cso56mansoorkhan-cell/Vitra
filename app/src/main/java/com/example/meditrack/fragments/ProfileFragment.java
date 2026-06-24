package com.example.meditrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.meditrack.R;
import com.example.meditrack.database.DatabaseHelper;
import com.example.meditrack.models.UserProfile;

public class ProfileFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private EditText etName, etAge, etBloodGroup, etConditions, etAllergies, etEmergencyName, etEmergencyPhone;
    private Button btnSave;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        dbHelper = new DatabaseHelper(getContext());

        etName = view.findViewById(R.id.et_name);
        etAge = view.findViewById(R.id.et_age);
        etBloodGroup = view.findViewById(R.id.et_blood_group);
        etConditions = view.findViewById(R.id.et_conditions);
        etAllergies = view.findViewById(R.id.et_allergies);
        etEmergencyName = view.findViewById(R.id.et_emergency_name);
        etEmergencyPhone = view.findViewById(R.id.et_emergency_phone);
        btnSave = view.findViewById(R.id.btn_save_profile);

        loadProfile();
        btnSave.setOnClickListener(v -> saveProfile());

        return view;
    }

    private void loadProfile() {
        UserProfile profile = dbHelper.getProfile();
        if (profile != null && profile.id > 0) {
            etName.setText(profile.name);
            etAge.setText(String.valueOf(profile.age));
            etBloodGroup.setText(profile.bloodGroup);
            etConditions.setText(profile.conditions);
            etAllergies.setText(profile.allergies);
            etEmergencyName.setText(profile.emergencyName);
            etEmergencyPhone.setText(profile.emergencyPhone);
        }
    }

    private void saveProfile() {
        UserProfile profile = new UserProfile();
        profile.name = etName.getText().toString().trim();
        try {
            profile.age = Integer.parseInt(etAge.getText().toString());
        } catch (NumberFormatException e) {
            profile.age = 0;
        }
        profile.bloodGroup = etBloodGroup.getText().toString().trim();
        profile.conditions = etConditions.getText().toString().trim();
        profile.allergies = etAllergies.getText().toString().trim();
        profile.emergencyName = etEmergencyName.getText().toString().trim();
        profile.emergencyPhone = etEmergencyPhone.getText().toString().trim();

        dbHelper.saveProfile(profile);
        Toast.makeText(getContext(), "Profile saved!", Toast.LENGTH_SHORT).show();
    }
}