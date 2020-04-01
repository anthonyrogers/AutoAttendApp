package com.example.autoattendapp;

import android.os.Parcelable;

import java.util.ArrayList;

public class Student extends User implements Parcelable {

    public Student(String firstName, String lastName, String ID, ArrayList<Course> courses) {
        super(firstName, lastName, ID, courses, null);
    }
}
