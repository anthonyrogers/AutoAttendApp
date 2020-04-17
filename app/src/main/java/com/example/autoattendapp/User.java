package com.example.autoattendapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class User implements Parcelable {

    private String ID;
    private String firstName;
    private String lastName;
    private String email;
    private ArrayList<String> courses;
    private String beacon;

    public static final int STUDENT = 0;
    public static final int TEACHER = 1;



    public User(String firstName, String lastName, String ID, String email, ArrayList<String> courses, String beacon) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ID = ID;
        this.email = email;
        this.courses = courses;
        this.beacon = beacon;
    }

    protected User(Parcel in) {
        ID = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        email = in.readString();
        ID = in.readString();
        courses = in.readArrayList(Course.class.getClassLoader());
        beacon = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ID);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(email);
        dest.writeString(ID);
        dest.writeList(courses);
        dest.writeString(beacon);
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
    public void setFirstName(String firstName){ this.firstName = firstName; }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName){this.lastName = lastName;}

    public String getID() {
        return ID;
    }

    public void setID(String id){this.ID = id;}

    public String getEmail() {
        return email;
    }
    public void setEmail(String email){this.email = email;}

    public ArrayList<String> getCourses() {
        return courses;
    }

    public void setCourses(ArrayList<String> courses){this.courses = courses;}

    public String getBeacon() { return beacon; }
    public void setBeacon(String beacon){this.beacon = beacon;}

    public int getType() {
        return -1;
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

    @Override
    public String toString() {
        String ret = String.format("Name: %s %s\nID: %s",
                firstName, lastName, ID);
        return ret;
    }

}
