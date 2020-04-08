package com.example.autoattendapp;

import android.os.Parcelable;

import java.util.ArrayList;

public class Teacher extends User implements Parcelable {

    public Teacher(String id, String firstName, String lastName, String email) {
        super(id, firstName, lastName, email);
    }
    /*
    public Teacher(String firstName, String lastName, String ID, ArrayList<Course> courses, Beacon beacon) {
        super(firstName, lastName, ID, courses, beacon);
    }
*/
}
