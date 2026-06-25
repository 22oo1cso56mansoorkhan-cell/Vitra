package com.example.meditrack.models;

public class EmergencyContact {
    public long id;
    public long profileId;
    public String name;
    public String phone;

    public EmergencyContact() {
        this.id = -1;
        this.profileId = -1;
        this.name = "";
        this.phone = "";
    }
}