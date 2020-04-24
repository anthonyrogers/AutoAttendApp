package com.example.autoattendapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceRecord implements Parcelable{

    private String classID;
    private String date;
    private String firstName;
    private String lastName;
    private String studentID;
    private ArrayList<Map<String, String>> times;

    public AttendanceRecord(String classID, String date, String firstName, String lastName, String studentID, ArrayList<Map<String, String>> times) {
        this.classID = classID;
        this.date = date;
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentID = studentID;
        this.times = times;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(classID);
        dest.writeString(date);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(studentID);
        dest.writeInt(times.size());
        for(int i=0; i < times.size(); i++) {
            dest.writeString(times.get(i).get(DBManager.TIME_IN));
            dest.writeString(times.get(i).get(DBManager.TIME_OUT));
        }
    }

    protected AttendanceRecord(Parcel in) {
        classID = in.readString();
        date = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        studentID = in.readString();
        int size = in.readInt();
        times = new ArrayList<>();
        for(int i=0; i < size; i++) {
            Map<String, String> time = new HashMap<>();
            time.put(DBManager.TIME_IN, in.readString());
            time.put(DBManager.TIME_OUT, in.readString());
            times.add(time);
        }
    }

    public static Creator<AttendanceRecord> CREATOR = new Creator<AttendanceRecord>() {
        @Override
        public AttendanceRecord createFromParcel(Parcel source) {
            return new AttendanceRecord(source);
        }

        @Override
        public AttendanceRecord[] newArray(int size) {
            return new AttendanceRecord[size];
        }
    };

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

    public ArrayList<Map<String, String>> getTimes() {
        return times;
    }

    @Override
    public String toString() {
        String s =  String.format("%s %s %s %s %s\n", classID, date, firstName, lastName, studentID);
        for(Map<String, String> time: times) {
            s += String.format("\nTime in: %s\nTime out: %s\n", time.get(DBManager.TIME_IN), time.get(DBManager.TIME_OUT));
        }
        return s;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
