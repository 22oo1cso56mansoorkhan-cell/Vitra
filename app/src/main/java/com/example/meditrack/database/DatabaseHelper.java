package com.example.meditrack.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.meditrack.models.DoctorVisit;
import com.example.meditrack.models.EmergencyContact;
import com.example.meditrack.models.Medicine;
import com.example.meditrack.models.Prescription;
import com.example.meditrack.models.Symptom;
import com.example.meditrack.models.UserProfile;
import com.example.meditrack.models.VitalsRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "meditrack.db";
    private static final int DB_VERSION = 2;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    // Table constants
    private static final String TABLE_USER = "user_profile";
    private static final String TABLE_VITALS = "vitals";
    private static final String TABLE_MEDICINES = "medicines";
    private static final String TABLE_SYMPTOMS = "symptoms";
    private static final String TABLE_DOCTOR = "doctor_visits";
    private static final String TABLE_PRESCRIPTIONS = "prescriptions";
    private static final String TABLE_EMERGENCY_CONTACTS = "emergency_contacts";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create User Profile Table
        String createUser = "CREATE TABLE " + TABLE_USER + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, age INTEGER, blood_group TEXT, " +
                "conditions TEXT, allergies TEXT, " +
                "emergency_name TEXT, emergency_phone TEXT)";
        db.execSQL(createUser);

        // Create Vitals Table
        String createVitals = "CREATE TABLE " + TABLE_VITALS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, systolic INTEGER, diastolic INTEGER, " +
                "blood_sugar INTEGER, is_fasting INTEGER, " +
                "temperature REAL, weight REAL, spo2 INTEGER, notes TEXT)";
        db.execSQL(createVitals);

        // Create Medicines Table
        String createMedicines = "CREATE TABLE " + TABLE_MEDICINES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, dose TEXT, frequency TEXT, " +
                "interval_hours INTEGER, start_date TEXT, end_date TEXT, " +
                "next_dose_time TEXT, is_active INTEGER, dose_count INTEGER)";
        db.execSQL(createMedicines);

        // Create Symptoms Table
        String createSymptoms = "CREATE TABLE " + TABLE_SYMPTOMS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, symptom_name TEXT, severity INTEGER, notes TEXT)";
        db.execSQL(createSymptoms);

        // Create Doctor Visits Table
        String createDoctor = "CREATE TABLE " + TABLE_DOCTOR + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, doctor_name TEXT, prescription TEXT, " +
                "follow_up_date TEXT, notes TEXT)";
        db.execSQL(createDoctor);

        // Create Prescriptions Table
        String createPrescriptions = "CREATE TABLE " + TABLE_PRESCRIPTIONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, doctor_name TEXT, file_path TEXT, notes TEXT)";
        db.execSQL(createPrescriptions);

        // Create Emergency Contacts Table
        String createEmergencyContacts = "CREATE TABLE " + TABLE_EMERGENCY_CONTACTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "profile_id INTEGER, " +
                "name TEXT, " +
                "phone TEXT, " +
                "FOREIGN KEY(profile_id) REFERENCES " + TABLE_USER + "(id))";
        db.execSQL(createEmergencyContacts);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String createEmergencyContacts = "CREATE TABLE " + TABLE_EMERGENCY_CONTACTS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "profile_id INTEGER, " +
                    "name TEXT, " +
                    "phone TEXT, " +
                    "FOREIGN KEY(profile_id) REFERENCES " + TABLE_USER + "(id))";
            db.execSQL(createEmergencyContacts);
        }
    }

    // =====================================================
    // ============ USER PROFILE METHODS ============
    // =====================================================

    public long saveProfile(UserProfile profile) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;

        try {
            Cursor cursor = db.query(TABLE_USER,
                    new String[]{"id"},
                    null, null, null, null, null);

            ContentValues values = new ContentValues();
            values.put("name", profile.name);
            values.put("age", profile.age);
            values.put("blood_group", profile.bloodGroup);
            values.put("conditions", profile.conditions);
            values.put("allergies", profile.allergies);
            values.put("emergency_name", profile.emergencyName);
            values.put("emergency_phone", profile.emergencyPhone);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                db.update(TABLE_USER, values, "id = ?", new String[]{String.valueOf(id)});
            } else {
                id = db.insert(TABLE_USER, null, values);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            id = -1;
        } finally {
            db.close();
        }
        return id;
    }

    public UserProfile getProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        UserProfile profile = null;

        try {
            Cursor cursor = db.query(TABLE_USER, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                profile = new UserProfile();
                profile.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                profile.name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                profile.age = cursor.getInt(cursor.getColumnIndexOrThrow("age"));
                profile.bloodGroup = cursor.getString(cursor.getColumnIndexOrThrow("blood_group"));
                profile.conditions = cursor.getString(cursor.getColumnIndexOrThrow("conditions"));
                profile.allergies = cursor.getString(cursor.getColumnIndexOrThrow("allergies"));
                profile.emergencyName = cursor.getString(cursor.getColumnIndexOrThrow("emergency_name"));
                profile.emergencyPhone = cursor.getString(cursor.getColumnIndexOrThrow("emergency_phone"));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            profile = null;
        } finally {
            db.close();
        }

        if (profile == null) {
            profile = new UserProfile();
            profile.id = -1;
        }
        return profile;
    }

    // =====================================================
    // ============ EMERGENCY CONTACTS METHODS ============
    // =====================================================

    public List<EmergencyContact> getEmergencyContacts(long profileId) {
        List<EmergencyContact> contacts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor cursor = db.query(TABLE_EMERGENCY_CONTACTS,
                    null,
                    "profile_id = ?",
                    new String[]{String.valueOf(profileId)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    EmergencyContact contact = new EmergencyContact();
                    contact.id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                    contact.profileId = cursor.getLong(cursor.getColumnIndexOrThrow("profile_id"));
                    contact.name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    contact.phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                    contacts.add(contact);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return contacts;
    }

    public long saveEmergencyContact(EmergencyContact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;

        try {
            ContentValues values = new ContentValues();
            values.put("profile_id", contact.profileId);
            values.put("name", contact.name);
            values.put("phone", contact.phone);
            id = db.insert(TABLE_EMERGENCY_CONTACTS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
            id = -1;
        } finally {
            db.close();
        }
        return id;
    }

    public void deleteEmergencyContacts(long profileId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_EMERGENCY_CONTACTS, "profile_id = ?", new String[]{String.valueOf(profileId)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public void deleteEmergencyContact(long contactId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_EMERGENCY_CONTACTS, "id = ?", new String[]{String.valueOf(contactId)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    // =====================================================
    // ============ VITALS METHODS ============
    // =====================================================

    public long addVitals(VitalsRecord vitals) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", dateFormat.format(vitals.date));
        values.put("systolic", vitals.systolic);
        values.put("diastolic", vitals.diastolic);
        values.put("blood_sugar", vitals.bloodSugar);
        values.put("is_fasting", vitals.isFasting ? 1 : 0);
        values.put("temperature", vitals.temperature);
        values.put("weight", vitals.weight);
        values.put("spo2", vitals.spo2);
        values.put("notes", vitals.notes);
        long id = db.insert(TABLE_VITALS, null, values);
        db.close();
        return id;
    }

    public List<VitalsRecord> getAllVitals() {
        List<VitalsRecord> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_VITALS, null, null, null, null, null, "date DESC");
        while (cursor.moveToNext()) {
            VitalsRecord v = new VitalsRecord();
            v.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            try {
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                if (dateStr != null && !dateStr.isEmpty()) {
                    v.date = dateFormat.parse(dateStr);
                } else {
                    v.date = new Date();
                }
            } catch (Exception e) {
                e.printStackTrace();
                v.date = new Date();
            }
            v.systolic = cursor.getInt(cursor.getColumnIndexOrThrow("systolic"));
            v.diastolic = cursor.getInt(cursor.getColumnIndexOrThrow("diastolic"));
            v.bloodSugar = cursor.getInt(cursor.getColumnIndexOrThrow("blood_sugar"));
            v.isFasting = cursor.getInt(cursor.getColumnIndexOrThrow("is_fasting")) == 1;
            v.temperature = cursor.getFloat(cursor.getColumnIndexOrThrow("temperature"));
            v.weight = cursor.getFloat(cursor.getColumnIndexOrThrow("weight"));
            v.spo2 = cursor.getInt(cursor.getColumnIndexOrThrow("spo2"));
            v.notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            list.add(v);
        }
        cursor.close();
        db.close();
        return list;
    }

    public List<VitalsRecord> getVitalsBetween(Date start, Date end) {
        List<VitalsRecord> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_VITALS +
                " WHERE date BETWEEN ? AND ? ORDER BY date ASC";
        Cursor cursor = db.rawQuery(query, new String[]{
                dateFormat.format(start), dateFormat.format(end)
        });
        while (cursor.moveToNext()) {
            VitalsRecord v = new VitalsRecord();
            v.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            try {
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                if (dateStr != null && !dateStr.isEmpty()) {
                    v.date = dateFormat.parse(dateStr);
                } else {
                    v.date = new Date();
                }
            } catch (Exception e) {
                e.printStackTrace();
                v.date = new Date();
            }
            v.systolic = cursor.getInt(cursor.getColumnIndexOrThrow("systolic"));
            v.diastolic = cursor.getInt(cursor.getColumnIndexOrThrow("diastolic"));
            v.bloodSugar = cursor.getInt(cursor.getColumnIndexOrThrow("blood_sugar"));
            v.isFasting = cursor.getInt(cursor.getColumnIndexOrThrow("is_fasting")) == 1;
            v.temperature = cursor.getFloat(cursor.getColumnIndexOrThrow("temperature"));
            v.weight = cursor.getFloat(cursor.getColumnIndexOrThrow("weight"));
            v.spo2 = cursor.getInt(cursor.getColumnIndexOrThrow("spo2"));
            v.notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            list.add(v);
        }
        cursor.close();
        db.close();
        return list;
    }

    // =====================================================
    // ============ MEDICINES METHODS ============
    // =====================================================

    public long addMedicine(Medicine medicine) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", medicine.name);
        values.put("dose", medicine.dose);
        values.put("frequency", medicine.frequency);
        values.put("interval_hours", medicine.intervalHours);
        values.put("start_date", dateFormat.format(medicine.startDate));
        values.put("end_date", medicine.endDate != null ? dateFormat.format(medicine.endDate) : null);
        values.put("next_dose_time", medicine.nextDoseTime != null ? dateFormat.format(medicine.nextDoseTime) : null);
        values.put("is_active", medicine.isActive ? 1 : 0);
        values.put("dose_count", medicine.doseCount);
        long id = db.insert(TABLE_MEDICINES, null, values);
        db.close();
        return id;
    }

    public List<Medicine> getAllMedicines() {
        List<Medicine> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MEDICINES, null, null, null, null, null, "id DESC");
        while (cursor.moveToNext()) {
            Medicine m = new Medicine();
            m.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            m.name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            m.dose = cursor.getString(cursor.getColumnIndexOrThrow("dose"));
            m.frequency = cursor.getString(cursor.getColumnIndexOrThrow("frequency"));
            m.intervalHours = cursor.getInt(cursor.getColumnIndexOrThrow("interval_hours"));

            try {
                String startDateStr = cursor.getString(cursor.getColumnIndexOrThrow("start_date"));
                if (startDateStr != null && !startDateStr.isEmpty()) {
                    m.startDate = dateFormat.parse(startDateStr);
                } else {
                    m.startDate = new Date();
                }

                String endDateStr = cursor.getString(cursor.getColumnIndexOrThrow("end_date"));
                if (endDateStr != null && !endDateStr.isEmpty()) {
                    try {
                        m.endDate = dateFormat.parse(endDateStr);
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                        m.endDate = null;
                    }
                } else {
                    m.endDate = null;
                }

                String nextDoseStr = cursor.getString(cursor.getColumnIndexOrThrow("next_dose_time"));
                if (nextDoseStr != null && !nextDoseStr.isEmpty()) {
                    try {
                        m.nextDoseTime = dateFormat.parse(nextDoseStr);
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                        m.nextDoseTime = null;
                    }
                } else {
                    m.nextDoseTime = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                m.startDate = new Date();
            }

            m.isActive = cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1;
            m.doseCount = cursor.getInt(cursor.getColumnIndexOrThrow("dose_count"));
            list.add(m);
        }
        cursor.close();
        db.close();
        return list;
    }

    public void updateMedicine(Medicine medicine) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", medicine.name);
        values.put("dose", medicine.dose);
        values.put("frequency", medicine.frequency);
        values.put("interval_hours", medicine.intervalHours);
        values.put("is_active", medicine.isActive ? 1 : 0);
        values.put("dose_count", medicine.doseCount);
        values.put("next_dose_time", medicine.nextDoseTime != null ? dateFormat.format(medicine.nextDoseTime) : null);
        db.update(TABLE_MEDICINES, values, "id = ?", new String[]{String.valueOf(medicine.id)});
        db.close();
    }

    public void deleteMedicine(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEDICINES, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // =====================================================
// ============ SYMPTOMS METHODS ============
// =====================================================

    public long addSymptom(Symptom symptom) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", dateFormat.format(symptom.date));
        values.put("symptom_name", symptom.symptomName);
        values.put("severity", symptom.severity);
        values.put("notes", symptom.notes);
        long id = db.insert(TABLE_SYMPTOMS, null, values);
        db.close();
        return id;
    }

    public List<Symptom> getAllSymptoms() {
        List<Symptom> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SYMPTOMS, null, null, null, null, null, "date DESC");
        while (cursor.moveToNext()) {
            Symptom s = new Symptom();
            s.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            try {
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                if (dateStr != null && !dateStr.isEmpty()) {
                    s.date = dateFormat.parse(dateStr);
                } else {
                    s.date = new Date();
                }
            } catch (Exception e) {
                e.printStackTrace();
                s.date = new Date();
            }
            s.symptomName = cursor.getString(cursor.getColumnIndexOrThrow("symptom_name"));
            s.severity = cursor.getInt(cursor.getColumnIndexOrThrow("severity"));
            s.notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            list.add(s);
        }
        cursor.close();
        db.close();
        return list;
    }

    // Add this new method to get symptoms by date range
    public List<Symptom> getSymptomsBetween(Date start, Date end) {
        List<Symptom> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_SYMPTOMS +
                " WHERE date BETWEEN ? AND ? ORDER BY date DESC";
        Cursor cursor = db.rawQuery(query, new String[]{
                dateFormat.format(start), dateFormat.format(end)
        });
        while (cursor.moveToNext()) {
            Symptom s = new Symptom();
            s.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            try {
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                if (dateStr != null && !dateStr.isEmpty()) {
                    s.date = dateFormat.parse(dateStr);
                } else {
                    s.date = new Date();
                }
            } catch (Exception e) {
                e.printStackTrace();
                s.date = new Date();
            }
            s.symptomName = cursor.getString(cursor.getColumnIndexOrThrow("symptom_name"));
            s.severity = cursor.getInt(cursor.getColumnIndexOrThrow("severity"));
            s.notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            list.add(s);
        }
        cursor.close();
        db.close();
        return list;
    }

    // Add this method to get symptoms by severity level
    public List<Symptom> getSymptomsBySeverity(int minSeverity) {
        List<Symptom> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_SYMPTOMS +
                " WHERE severity >= ? ORDER BY date DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(minSeverity)});
        while (cursor.moveToNext()) {
            Symptom s = new Symptom();
            s.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            try {
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                if (dateStr != null && !dateStr.isEmpty()) {
                    s.date = dateFormat.parse(dateStr);
                } else {
                    s.date = new Date();
                }
            } catch (Exception e) {
                e.printStackTrace();
                s.date = new Date();
            }
            s.symptomName = cursor.getString(cursor.getColumnIndexOrThrow("symptom_name"));
            s.severity = cursor.getInt(cursor.getColumnIndexOrThrow("severity"));
            s.notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            list.add(s);
        }
        cursor.close();
        db.close();
        return list;
    }

    // Add this method to delete a symptom
    public void deleteSymptom(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SYMPTOMS, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Add this method to get symptoms by name search
    public List<Symptom> searchSymptoms(String searchQuery) {
        List<Symptom> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_SYMPTOMS +
                " WHERE symptom_name LIKE ? ORDER BY date DESC";
        Cursor cursor = db.rawQuery(query, new String[]{"%" + searchQuery + "%"});
        while (cursor.moveToNext()) {
            Symptom s = new Symptom();
            s.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            try {
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                if (dateStr != null && !dateStr.isEmpty()) {
                    s.date = dateFormat.parse(dateStr);
                } else {
                    s.date = new Date();
                }
            } catch (Exception e) {
                e.printStackTrace();
                s.date = new Date();
            }
            s.symptomName = cursor.getString(cursor.getColumnIndexOrThrow("symptom_name"));
            s.severity = cursor.getInt(cursor.getColumnIndexOrThrow("severity"));
            s.notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            list.add(s);
        }
        cursor.close();
        db.close();
        return list;
    }

    // =====================================================
    // ============ DOCTOR VISITS METHODS ============
    // =====================================================

    public long addDoctorVisit(DoctorVisit visit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", dateFormat.format(visit.date));
        values.put("doctor_name", visit.doctorName);
        values.put("prescription", visit.prescription);
        values.put("follow_up_date", visit.followUpDate != null ? dateFormat.format(visit.followUpDate) : null);
        values.put("notes", visit.notes);
        long id = db.insert(TABLE_DOCTOR, null, values);
        db.close();
        return id;
    }

    public List<DoctorVisit> getAllDoctorVisits() {
        List<DoctorVisit> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DOCTOR, null, null, null, null, null, "date DESC");
        while (cursor.moveToNext()) {
            DoctorVisit v = new DoctorVisit();
            v.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            try {
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                if (dateStr != null && !dateStr.isEmpty()) {
                    v.date = dateFormat.parse(dateStr);
                } else {
                    v.date = new Date();
                }

                String followUpStr = cursor.getString(cursor.getColumnIndexOrThrow("follow_up_date"));
                if (followUpStr != null && !followUpStr.isEmpty()) {
                    v.followUpDate = dateFormat.parse(followUpStr);
                } else {
                    v.followUpDate = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                v.date = new Date();
            }
            v.doctorName = cursor.getString(cursor.getColumnIndexOrThrow("doctor_name"));
            v.prescription = cursor.getString(cursor.getColumnIndexOrThrow("prescription"));
            v.notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            list.add(v);
        }
        cursor.close();
        db.close();
        return list;
    }

    // =====================================================
    // ============ PRESCRIPTIONS METHODS ============
    // =====================================================

    public long addPrescription(Prescription prescription) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", dateFormat.format(prescription.date));
        values.put("doctor_name", prescription.doctorName);
        values.put("file_path", prescription.filePath);
        values.put("notes", prescription.notes);
        long id = db.insert(TABLE_PRESCRIPTIONS, null, values);
        db.close();
        return id;
    }

    public List<Prescription> getAllPrescriptions() {
        List<Prescription> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRESCRIPTIONS, null, null, null, null, null, "date DESC");
        while (cursor.moveToNext()) {
            Prescription p = new Prescription();
            p.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            try {
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                if (dateStr != null && !dateStr.isEmpty()) {
                    p.date = dateFormat.parse(dateStr);
                } else {
                    p.date = new Date();
                }
            } catch (Exception e) {
                e.printStackTrace();
                p.date = new Date();
            }
            p.doctorName = cursor.getString(cursor.getColumnIndexOrThrow("doctor_name"));
            p.filePath = cursor.getString(cursor.getColumnIndexOrThrow("file_path"));
            p.notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            list.add(p);
        }
        cursor.close();
        db.close();
        return list;
    }
}