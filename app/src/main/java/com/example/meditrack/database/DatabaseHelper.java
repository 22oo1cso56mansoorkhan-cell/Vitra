package com.example.meditrack.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.meditrack.models.DoctorVisit;
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
    private static final int DB_VERSION = 1;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private static final String TABLE_USER = "user_profile";
    private static final String TABLE_VITALS = "vitals";
    private static final String TABLE_MEDICINES = "medicines";
    private static final String TABLE_SYMPTOMS = "symptoms";
    private static final String TABLE_DOCTOR = "doctor_visits";
    private static final String TABLE_PRESCRIPTIONS = "prescriptions";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUser = "CREATE TABLE " + TABLE_USER + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, age INTEGER, blood_group TEXT, " +
                "conditions TEXT, allergies TEXT, " +
                "emergency_name TEXT, emergency_phone TEXT)";
        db.execSQL(createUser);

        String createVitals = "CREATE TABLE " + TABLE_VITALS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, systolic INTEGER, diastolic INTEGER, " +
                "blood_sugar INTEGER, is_fasting INTEGER, " +
                "temperature REAL, weight REAL, spo2 INTEGER, notes TEXT)";
        db.execSQL(createVitals);

        String createMedicines = "CREATE TABLE " + TABLE_MEDICINES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, dose TEXT, frequency TEXT, " +
                "interval_hours INTEGER, start_date TEXT, end_date TEXT, " +
                "next_dose_time TEXT, is_active INTEGER, dose_count INTEGER)";
        db.execSQL(createMedicines);

        String createSymptoms = "CREATE TABLE " + TABLE_SYMPTOMS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, symptom_name TEXT, severity INTEGER, notes TEXT)";
        db.execSQL(createSymptoms);

        String createDoctor = "CREATE TABLE " + TABLE_DOCTOR + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, doctor_name TEXT, prescription TEXT, " +
                "follow_up_date TEXT, notes TEXT)";
        db.execSQL(createDoctor);

        String createPrescriptions = "CREATE TABLE " + TABLE_PRESCRIPTIONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, doctor_name TEXT, file_path TEXT, notes TEXT)";
        db.execSQL(createPrescriptions);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VITALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICINES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SYMPTOMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCTOR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRESCRIPTIONS);
        onCreate(db);
    }

    // ============ USER PROFILE ============
    public void saveProfile(UserProfile profile) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER, null, null);
        ContentValues values = new ContentValues();
        values.put("name", profile.name);
        values.put("age", profile.age);
        values.put("blood_group", profile.bloodGroup);
        values.put("conditions", profile.conditions);
        values.put("allergies", profile.allergies);
        values.put("emergency_name", profile.emergencyName);
        values.put("emergency_phone", profile.emergencyPhone);
        db.insert(TABLE_USER, null, values);
        db.close();
    }

    public UserProfile getProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, null, null, null, null, null, null);
        UserProfile profile = new UserProfile();
        if (cursor.moveToFirst()) {
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
        db.close();
        return profile;
    }

    // ============ VITALS ============
    public void addVitals(VitalsRecord vitals) {
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
        db.insert(TABLE_VITALS, null, values);
        db.close();
    }

    public List<VitalsRecord> getAllVitals() {
        List<VitalsRecord> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_VITALS, null, null, null, null, null, "date DESC");
        while (cursor.moveToNext()) {
            VitalsRecord v = new VitalsRecord();
            v.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            try {
                v.date = dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow("date")));
            } catch (Exception e) { v.date = new Date(); }
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
                v.date = dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow("date")));
            } catch (Exception e) { v.date = new Date(); }
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

    // ============ MEDICINES ============
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
                m.startDate = dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow("start_date")));
                String end = cursor.getString(cursor.getColumnIndexOrThrow("end_date"));
                if (end != null) m.endDate = dateFormat.parse(end);
                String next = cursor.getString(cursor.getColumnIndexOrThrow("next_dose_time"));
                if (next != null) m.nextDoseTime = dateFormat.parse(next);
            } catch (Exception e) { m.startDate = new Date(); }
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

    // ============ SYMPTOMS ============
    public void addSymptom(Symptom symptom) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", dateFormat.format(symptom.date));
        values.put("symptom_name", symptom.symptomName);
        values.put("severity", symptom.severity);
        values.put("notes", symptom.notes);
        db.insert(TABLE_SYMPTOMS, null, values);
        db.close();
    }

    public List<Symptom> getAllSymptoms() {
        List<Symptom> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SYMPTOMS, null, null, null, null, null, "date DESC");
        while (cursor.moveToNext()) {
            Symptom s = new Symptom();
            s.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            try {
                s.date = dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow("date")));
            } catch (Exception e) { s.date = new Date(); }
            s.symptomName = cursor.getString(cursor.getColumnIndexOrThrow("symptom_name"));
            s.severity = cursor.getInt(cursor.getColumnIndexOrThrow("severity"));
            s.notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            list.add(s);
        }
        cursor.close();
        db.close();
        return list;
    }

    // ============ DOCTOR VISITS ============
    public void addDoctorVisit(DoctorVisit visit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", dateFormat.format(visit.date));
        values.put("doctor_name", visit.doctorName);
        values.put("prescription", visit.prescription);
        values.put("follow_up_date", visit.followUpDate != null ? dateFormat.format(visit.followUpDate) : null);
        values.put("notes", visit.notes);
        db.insert(TABLE_DOCTOR, null, values);
        db.close();
    }

    public List<DoctorVisit> getAllDoctorVisits() {
        List<DoctorVisit> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DOCTOR, null, null, null, null, null, "date DESC");
        while (cursor.moveToNext()) {
            DoctorVisit v = new DoctorVisit();
            v.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            try {
                v.date = dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                String follow = cursor.getString(cursor.getColumnIndexOrThrow("follow_up_date"));
                if (follow != null) v.followUpDate = dateFormat.parse(follow);
            } catch (Exception e) { v.date = new Date(); }
            v.doctorName = cursor.getString(cursor.getColumnIndexOrThrow("doctor_name"));
            v.prescription = cursor.getString(cursor.getColumnIndexOrThrow("prescription"));
            v.notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            list.add(v);
        }
        cursor.close();
        db.close();
        return list;
    }

    // ============ PRESCRIPTIONS ============
    public void addPrescription(Prescription prescription) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", dateFormat.format(prescription.date));
        values.put("doctor_name", prescription.doctorName);
        values.put("file_path", prescription.filePath);
        values.put("notes", prescription.notes);
        db.insert(TABLE_PRESCRIPTIONS, null, values);
        db.close();
    }

    public List<Prescription> getAllPrescriptions() {
        List<Prescription> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRESCRIPTIONS, null, null, null, null, null, "date DESC");
        while (cursor.moveToNext()) {
            Prescription p = new Prescription();
            p.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            try {
                p.date = dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow("date")));
            } catch (Exception e) { p.date = new Date(); }
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