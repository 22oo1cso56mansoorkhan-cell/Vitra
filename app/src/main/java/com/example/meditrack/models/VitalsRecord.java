package com.example.meditrack.models;

import java.util.Date;

public class VitalsRecord {
    public int id;
    public Date date;
    public int systolic;
    public int diastolic;
    public int bloodSugar;
    public boolean isFasting;
    public float temperature;
    public float weight;
    public int spo2;
    public String notes = "";
}