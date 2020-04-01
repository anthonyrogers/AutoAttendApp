package com.example.autoattendapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class User implements Parcelable {

    private String firstName;
    private String lastName;
    private String ID;
    private ArrayList<Course> courses;
    private Beacon beacon;

    public User(String firstName, String lastName, String ID, ArrayList<Course> courses, Beacon beacon) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ID = ID;
        this.courses = courses;
        this.beacon = beacon;
    }

    protected User(Parcel in) {
        firstName = in.readString();
        lastName = in.readString();
        ID = in.readString();
        courses = in.readArrayList(Course.class.getClassLoader());
        beacon = in.readParcelable(Beacon.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(ID);
        dest.writeList(courses);
        dest.writeParcelable(beacon, flags);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getID() {
        return ID;
    }

    public ArrayList<Course> getCourses() {
        return courses;
    }

    public Beacon getBeacon() {
        return beacon;
    }

    @Override
    public boolean equals(Object user) {
        if(((User) user).getID() == this.ID) {
            return true;
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
