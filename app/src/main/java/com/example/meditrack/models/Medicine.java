package com.example.meditrack.models;

import java.util.Date;

public class Medicine {
    public int id;
    public String name = "";
    public String dose = "";
    public String frequency = "";
    public int intervalHours = 0;
    public Date startDate;
    public Date endDate;
    public Date nextDoseTime;
    public boolean isActive = true;
    public int doseCount = 0;
}