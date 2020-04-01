package com.example.autoattendapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class User implements Parcelable {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String ID;
    private ArrayList<Course> courses;
    private Beacon beacon;

    public User(String firstName, String lastName, String email, String password){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    /*
    public User(String firstName, String lastName, String ID, ArrayList<Course> courses, Beacon beacon) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ID = ID;
        this.courses = courses;
        this.beacon = beacon;
    }*/

    protected User(Parcel in) {
        firstName = in.readString();
        lastName = in.readString();
        email = in.readString();
        password = in.readString();
        ID = in.readString();
        courses = in.readArrayList(Course.class.getClassLoader());
        beacon = in.readParcelable(Beacon.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(email);
        dest.writeString(password);
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

    public String getPassword() {
        return password;
    }
    public void setPassword(String password){this.password = password;}

    public ArrayList<Course> getCourses() {
        return courses;
    }
    public void setCourses(ArrayList<Course> courses){this.courses = courses;}

    public Beacon getBeacon() { return beacon; }
    public void setBeacon(Beacon beacon){this.beacon = beacon;}

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
