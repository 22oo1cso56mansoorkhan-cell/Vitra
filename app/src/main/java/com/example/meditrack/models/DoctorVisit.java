package com.example.meditrack.models;

import java.util.Date;

public class DoctorVisit {
    public int id;
    public Date date;  // This is the field name
    public String doctorName;
    public String prescription;
    public Date followUpDate;
    public String notes;

    public DoctorVisit() {
        this.date = new Date();
    }

    public DoctorVisit(String doctorName, Date date, String prescription, Date followUpDate, String notes) {
        this.doctorName = doctorName;
        this.date = date;
        this.prescription = prescription;
        this.followUpDate = followUpDate;
        this.notes = notes;
    }
}