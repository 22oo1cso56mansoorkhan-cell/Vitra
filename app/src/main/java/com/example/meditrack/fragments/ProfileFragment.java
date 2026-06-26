package com.example.meditrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.meditrack.R;
import com.example.meditrack.database.DatabaseHelper;
import com.example.meditrack.models.EmergencyContact;
import com.example.meditrack.models.UserProfile;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private DatabaseHelper dbHelper;

    // User info fields
    private EditText etName, etAge, etBloodGroup, etConditions, etAllergies;
    private Button btnSave;

    // Emergency contacts container
    private LinearLayout emergencyContactsContainer;
    private Button btnAddEmergencyContact;

    // List to keep track of all emergency contact views
    private List<EmergencyContactView> contactViews = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        dbHelper = new DatabaseHelper(getContext());

        // Hook up all the input fields
        etName = view.findViewById(R.id.et_name);
        etAge = view.findViewById(R.id.et_age);
        etBloodGroup = view.findViewById(R.id.et_blood_group);
        etConditions = view.findViewById(R.id.et_conditions);
        etAllergies = view.findViewById(R.id.et_allergies);
        btnSave = view.findViewById(R.id.btn_save_profile);

        // Emergency contacts section
        emergencyContactsContainer = view.findViewById(R.id.emergency_contacts_container);
        btnAddEmergencyContact = view.findViewById(R.id.btn_add_emergency_contact);

        // Load existing profile data
        loadProfile();

        // Setup the "Add Emergency Contact" button
        btnAddEmergencyContact.setOnClickListener(v -> addEmergencyContactInput("", ""));

        // Save button
        btnSave.setOnClickListener(v -> saveProfile());

        return view;
    }

    private void loadProfile() {
        UserProfile profile = dbHelper.getProfile();
        if (profile != null && profile.id > 0) {
            // Fill in the user details
            etName.setText(profile.name);
            etAge.setText(String.valueOf(profile.age));
            etBloodGroup.setText(profile.bloodGroup);
            etConditions.setText(profile.conditions);
            etAllergies.setText(profile.allergies);

            // Load all emergency contacts
            List<EmergencyContact> contacts = dbHelper.getEmergencyContacts(profile.id);
            if (contacts != null && !contacts.isEmpty()) {
                for (EmergencyContact contact : contacts) {
                    addEmergencyContactInput(contact.name, contact.phone);
                }
            } else {
                addEmergencyContactInput("", "");
            }
        } else {
            addEmergencyContactInput("", "");
        }
    }

    private void addEmergencyContactInput(String name, String phone) {
        View contactView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_emergency_contact, emergencyContactsContainer, false);

        TextInputEditText etContactName = contactView.findViewById(R.id.et_contact_name);
        TextInputEditText etContactPhone = contactView.findViewById(R.id.et_contact_phone);
        MaterialButton btnRemove = contactView.findViewById(R.id.btn_remove_contact);

        etContactName.setText(name);
        etContactPhone.setText(phone);

        btnRemove.setOnClickListener(v -> {
            emergencyContactsContainer.removeView(contactView);
            contactViews.remove(getContactViewIndex(contactView));
            Toast.makeText(getContext(), "Contact removed", Toast.LENGTH_SHORT).show();
        });

        emergencyContactsContainer.addView(contactView);
        contactViews.add(new EmergencyContactView(contactView, etContactName, etContactPhone));
    }

    private int getContactViewIndex(View view) {
        for (int i = 0; i < contactViews.size(); i++) {
            if (contactViews.get(i).rootView == view) {
                return i;
            }
        }
        return -1;
    }

    private void saveProfile() {
        try {
            // 1. Save the main profile
            UserProfile profile = new UserProfile();
            profile.name = etName.getText().toString().trim();

            try {
                profile.age = Integer.parseInt(etAge.getText().toString().trim());
            } catch (NumberFormatException e) {
                profile.age = 0;
            }

            profile.bloodGroup = etBloodGroup.getText().toString().trim();
            profile.conditions = etConditions.getText().toString().trim();
            profile.allergies = etAllergies.getText().toString().trim();
            profile.emergencyName = "";
            profile.emergencyPhone = "";

            long profileId = dbHelper.saveProfile(profile);

            if (profileId > 0) {
                // 2. Save all emergency contacts
                dbHelper.deleteEmergencyContacts(profileId);

                List<EmergencyContact> contacts = getAllEmergencyContacts();
                for (EmergencyContact contact : contacts) {
                    contact.profileId = profileId;
                    dbHelper.saveEmergencyContact(contact);
                }

                Toast.makeText(getContext(), "✅ Profile saved with " + contacts.size() + " emergency contacts!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "❌ Failed to save profile. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private List<EmergencyContact> getAllEmergencyContacts() {
        List<EmergencyContact> contacts = new ArrayList<>();

        for (EmergencyContactView contactView : contactViews) {
            String name = contactView.nameInput.getText().toString().trim();
            String phone = contactView.phoneInput.getText().toString().trim();

            if (!name.isEmpty() || !phone.isEmpty()) {
                EmergencyContact contact = new EmergencyContact();
                contact.name = name;
                contact.phone = phone;
                contacts.add(contact);
            }
        }
        return contacts;
    }

    private static class EmergencyContactView {
        View rootView;
        TextInputEditText nameInput;
        TextInputEditText phoneInput;

        EmergencyContactView(View root, TextInputEditText name, TextInputEditText phone) {
            this.rootView = root;
            this.nameInput = name;
            this.phoneInput = phone;
        }
    }
}