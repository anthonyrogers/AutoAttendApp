package com.example.autoattendapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttendanceRecord {

    private String classID;
    private String date;
    private String firstName;
    private String lastName;
    private String studentID;
    private List<Map<String, String>> times;

    public AttendanceRecord(String classID, String date, String firstName, String lastName, String studentID, List<Map<String, String>> times) {
        this.classID = classID;
        this.date = date;
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentID = studentID;
        this.times = times;
    }

    public String getClassID() {
        return classID;
    }

    public String getDate() {
        return date;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getStudentID() {
        return studentID;
    }

    public List<Map<String, String>> getTimes() {
        return times;
    }
}
