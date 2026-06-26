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
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private DatabaseHelper dbHelper;

    // User info fields
    private EditText etName, etAge, etBloodGroup, etConditions, etAllergies;
    private TextInputLayout tilName, tilAge, tilBloodGroup, tilConditions, tilAllergies;
    private Button btnSave, btnEdit, btnCancelEdit;

    // Emergency contacts
    private LinearLayout emergencyContactsContainer;
    private Button btnAddEmergencyContact;
    private List<EmergencyContactView> contactViews = new ArrayList<>();

    private boolean isEditMode = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        dbHelper = new DatabaseHelper(getContext());

        // Hook up all the input fields and their layouts
        etName = view.findViewById(R.id.et_name);
        etAge = view.findViewById(R.id.et_age);
        etBloodGroup = view.findViewById(R.id.et_blood_group);
        etConditions = view.findViewById(R.id.et_conditions);
        etAllergies = view.findViewById(R.id.et_allergies);

        // TextInputLayouts for enabling/disabling
        tilName = view.findViewById(R.id.til_name);
        tilAge = view.findViewById(R.id.til_age);
        tilBloodGroup = view.findViewById(R.id.til_blood_group);
        tilConditions = view.findViewById(R.id.til_conditions);
        tilAllergies = view.findViewById(R.id.til_allergies);

        btnSave = view.findViewById(R.id.btn_save_profile);
        btnEdit = view.findViewById(R.id.btn_edit_profile);
        btnCancelEdit = view.findViewById(R.id.btn_cancel_edit);

        // Emergency contacts section
        emergencyContactsContainer = view.findViewById(R.id.emergency_contacts_container);
        btnAddEmergencyContact = view.findViewById(R.id.btn_add_emergency_contact);

        // Load existing profile data
        loadProfile();

        // Setup the "Edit" button
        btnEdit.setOnClickListener(v -> toggleEditMode(true));

        // Setup the "Cancel Edit" button
        btnCancelEdit.setOnClickListener(v -> {
            toggleEditMode(false);
            // Reload the profile to revert changes
            loadProfile();
            Toast.makeText(getContext(), "Changes cancelled", Toast.LENGTH_SHORT).show();
        });

        // Setup the "Add Emergency Contact" button
        btnAddEmergencyContact.setOnClickListener(v -> addEmergencyContactInput("", ""));

        // Save button
        btnSave.setOnClickListener(v -> saveProfile());

        return view;
    }

    private void toggleEditMode(boolean enable) {
        isEditMode = enable;

        // Enable/disable all input fields
        etName.setEnabled(enable);
        etAge.setEnabled(enable);
        etBloodGroup.setEnabled(enable);
        etConditions.setEnabled(enable);
        etAllergies.setEnabled(enable);

        // Update visual appearance of TextInputLayouts
        updateTextInputLayoutState(tilName, enable);
        updateTextInputLayoutState(tilAge, enable);
        updateTextInputLayoutState(tilBloodGroup, enable);
        updateTextInputLayoutState(tilConditions, enable);
        updateTextInputLayoutState(tilAllergies, enable);

        // Enable/disable emergency contact buttons
        btnAddEmergencyContact.setEnabled(enable);

        // Show/hide buttons
        btnEdit.setVisibility(enable ? View.GONE : View.VISIBLE);
        btnCancelEdit.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(enable);

        // Enable/disable remove buttons on existing emergency contacts
        for (EmergencyContactView contactView : contactViews) {
            contactView.removeButton.setEnabled(enable);
        }
    }

    private void updateTextInputLayoutState(TextInputLayout til, boolean enabled) {
        // Simply enable/disable the TextInputLayout
        // Material Design will automatically handle the color changes
        til.setEnabled(enabled);
    }

    private void loadProfile() {
        UserProfile profile = dbHelper.getProfile();

        // Clear existing contact views
        emergencyContactsContainer.removeAllViews();
        contactViews.clear();

        if (profile != null && profile.id > 0) {
            // Fill in the user details
            etName.setText(profile.name != null ? profile.name : "");
            etAge.setText(profile.age > 0 ? String.valueOf(profile.age) : "");
            etBloodGroup.setText(profile.bloodGroup != null ? profile.bloodGroup : "");
            etConditions.setText(profile.conditions != null ? profile.conditions : "");
            etAllergies.setText(profile.allergies != null ? profile.allergies : "");

            // Load all emergency contacts
            List<EmergencyContact> contacts = dbHelper.getEmergencyContacts(profile.id);
            if (contacts != null && !contacts.isEmpty()) {
                for (EmergencyContact contact : contacts) {
                    addEmergencyContactInput(contact.name, contact.phone, false);
                }
            } else {
                addEmergencyContactInput("", "", false);
            }
        } else {
            // Empty profile - show one empty contact field
            addEmergencyContactInput("", "", false);
        }

        // If in edit mode, keep fields editable; otherwise keep them disabled
        toggleEditMode(isEditMode);
    }

    private void addEmergencyContactInput(String name, String phone) {
        addEmergencyContactInput(name, phone, true);
    }

    private void addEmergencyContactInput(String name, String phone, boolean addToList) {
        View contactView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_emergency_contact, emergencyContactsContainer, false);

        TextInputEditText etContactName = contactView.findViewById(R.id.et_contact_name);
        TextInputEditText etContactPhone = contactView.findViewById(R.id.et_contact_phone);
        MaterialButton btnRemove = contactView.findViewById(R.id.btn_remove_contact);

        etContactName.setText(name != null ? name : "");
        etContactPhone.setText(phone != null ? phone : "");

        // Set enabled state based on edit mode
        etContactName.setEnabled(isEditMode);
        etContactPhone.setEnabled(isEditMode);
        btnRemove.setEnabled(isEditMode);

        btnRemove.setOnClickListener(v -> {
            if (isEditMode) {
                emergencyContactsContainer.removeView(contactView);
                contactViews.remove(getContactViewIndex(contactView));
                Toast.makeText(getContext(), "Contact removed", Toast.LENGTH_SHORT).show();
            }
        });

        emergencyContactsContainer.addView(contactView);

        if (addToList) {
            contactViews.add(new EmergencyContactView(contactView, etContactName, etContactPhone, btnRemove));
        }
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
        if (!isEditMode) {
            Toast.makeText(getContext(), "Click Edit to modify profile", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Validate inputs
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Please enter your name", Toast.LENGTH_SHORT).show();
                etName.requestFocus();
                return;
            }

            String ageStr = etAge.getText().toString().trim();
            if (ageStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter your age", Toast.LENGTH_SHORT).show();
                etAge.requestFocus();
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageStr);
                if (age < 1 || age > 150) {
                    Toast.makeText(getContext(), "Please enter a valid age (1-150)", Toast.LENGTH_SHORT).show();
                    etAge.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid age", Toast.LENGTH_SHORT).show();
                etAge.requestFocus();
                return;
            }

            // 1. Save the main profile
            UserProfile profile = new UserProfile();
            profile.name = name;
            profile.age = age;
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
                    contact.profileId = (int) profileId;
                    dbHelper.saveEmergencyContact(contact);
                }

                Toast.makeText(getContext(), "✅ Profile saved with " + contacts.size() + " emergency contacts!", Toast.LENGTH_LONG).show();

                // Exit edit mode after save
                toggleEditMode(false);
                loadProfile(); // Reload to ensure consistency
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

            // Only add if at least one field has data
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
        MaterialButton removeButton;

        EmergencyContactView(View root, TextInputEditText name, TextInputEditText phone, MaterialButton remove) {
            this.rootView = root;
            this.nameInput = name;
            this.phoneInput = phone;
            this.removeButton = remove;
        }
    }
}