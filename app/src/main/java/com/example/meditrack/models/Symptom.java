package com.example.meditrack.models;

import java.util.Date;

public class Symptom {
    public int id;
    public Date date;
    public String symptomName;
    public int severity; // 1-5
    public String notes;

    public Symptom() {
        // Default constructor
    }

    public Symptom(String symptomName, int severity, String notes) {
        this.symptomName = symptomName;
        this.severity = severity;
        this.notes = notes;
        this.date = new Date();
    }
}