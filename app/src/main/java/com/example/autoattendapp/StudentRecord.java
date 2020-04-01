package com.example.autoattendapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class StudentRecord implements Parcelable {

    private Date date;
    private boolean attended;
    private double duration;

    public StudentRecord(Date date, boolean attended, double duration) {
        this.date = date;
        this.attended = attended;
        this.duration = duration;
    }

    protected StudentRecord(Parcel in) {
        this.date = (Date) in.readSerializable();
        this.attended = in.readBoolean();
        this.duration = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(date);
        dest.writeBoolean(attended);
        dest.writeDouble(duration);
    }

    public static final Creator<StudentRecord> CREATOR = new Creator<StudentRecord>() {
        @Override
        public StudentRecord createFromParcel(Parcel source) {
            return new StudentRecord(source);
        }

        @Override
        public StudentRecord[] newArray(int size) {
            return new StudentRecord[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

}
