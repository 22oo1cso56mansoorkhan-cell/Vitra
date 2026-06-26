# 📱 MediTrack - Personal Health Companion

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![SQLite](https://img.shields.io/badge/SQLite-07405E?style=for-the-badge&logo=sqlite&logoColor=white)](https://www.sqlite.org/)
[![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=for-the-badge&logo=materialdesign&logoColor=white)](https://material.io/design)

## 🏥 Overview

**MediTrack** is a comprehensive personal health tracking and medication management application designed for individuals managing chronic conditions like diabetes, hypertension, or post-surgery recovery. The app serves as a digital health diary, enabling users to log daily vitals, set medicine reminders, record symptoms, and build a health history that can be shared with doctors in a clean PDF format.

> **Note:** MediTrack is not a diagnostic tool but a consistent tracking companion that helps patients stay on top of their health routine.

---

## 🎯 Problem It Solves

Patients with chronic illnesses often face challenges:
- ❌ Forgetting to take medicines
- ❌ Missing consistent vitals tracking
- ❌ No organized health records for doctor visits

**MediTrack solves this by:**
- ✅ Centralizing all health data in one place
- ✅ Providing smart reminders and alerts
- ✅ Generating visual graphs for trend analysis
- ✅ Creating exportable PDF reports for healthcare providers

---

## ✨ Core Features

### 1. 👤 User Profile
- Medical history (conditions, blood group, allergies)
- Emergency contact information
- Personal details management

### 2. 📊 Daily Vitals Logger
- Blood Pressure (Systolic/Diastolic)
- Blood Sugar (Fasting/Post-meal)
- Body Temperature
- Weight
- SpO2 (Oxygen Saturation)
- Notes and timestamps

### 3. 💊 Medicine Reminder System
- Custom frequency (Once daily, Twice daily, Every X hours)
- Alarm notifications
- Dose count tracker
- Active/Inactive medicine management

### 4. 📝 Symptom Diary
- Log symptoms with severity ratings (1-5)
- Free text notes
- Historical symptom tracking

### 5. 📈 Visual Graphs
- Weekly and monthly trends
- Color-coded alerts (Normal/Borderline/Critical)
- Interactive charts for vitals history

### 6. 🏥 Doctor Visit Log
- Date and doctor name
- Prescription details
- Follow-up date tracking
- Additional notes

### 7. 📄 Prescription Storage
- Photo upload of prescriptions
- PDF storage
- Organized prescription history

### 8. 📑 PDF Health Report Generation
- One-tap report generation
- Summary of vitals, medicines, and symptoms
- Selectable date range
- Saves reports locally to Documents/MediTrackReports
- Standard PDF format that can be viewed or shared with any PDF reader

### 9. 🆘 Emergency SOS
- One-tap emergency button
- Sends location + health summary
- Pre-saved emergency contact
- Automatic SMS with health data

---

## 🛠️ Tech Stack

### Architecture
- **Language:** Java
- **Minimum SDK:** API 24 (Android 7.0)
- **Target SDK:** API 36 (Android 14)

### Core Libraries
| Library | Purpose |
|---------|---------|
| SQLite | Offline-first local database |
| MPAndroidChart | Interactive charts and graphs |
| Android PdfDocument API | Native PDF report generation |
| Material Design Components | Modern UI elements |
| AndroidX | Jetpack libraries support |

### Dependencies
```gradle
dependencies {
    // Core Android
    implementation 'androidx.appcompat:appcompat:1.7.0'
    implementation 'com.google.android.material:material:1.12.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.2.0'
    
    // Charts
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
    
    // Image Picker
    implementation 'com.github.dhaval2404:imagepicker:2.1'
}
