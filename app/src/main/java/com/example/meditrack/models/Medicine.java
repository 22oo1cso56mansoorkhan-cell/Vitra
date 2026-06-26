package com.example.meditrack.models;

import java.util.Calendar;
import java.util.Date;

public class Medicine {
    public int id;
    public String name;
    public String dose;
    public String frequency; // "once", "twice", "every_x_hours"
    public int intervalHours;
    public Date startDate;
    public Date nextDoseTime;
    public boolean isActive;
    public int doseCount;
    public Date endDate;

    public Medicine() {
        // Default constructor
    }

    // Optional: Add a constructor for convenience
    public Medicine(String name, String dose, String frequency, int intervalHours) {
        this.name = name;
        this.dose = dose;
        this.frequency = frequency;
        this.intervalHours = intervalHours;
        this.startDate = new Date();
        this.isActive = true;
        this.doseCount = 0;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, intervalHours);
        this.nextDoseTime = cal.getTime();
    }
}