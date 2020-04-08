package com.example.autoattendapp;

import android.os.Parcelable;

import java.util.ArrayList;

public class Teacher extends User implements Parcelable {


    public Teacher(String firstName, String lastName, String ID, String email, ArrayList<String> courses, Beacon beacon) {
        super(firstName, lastName, ID, email, courses, beacon);
    }

}
