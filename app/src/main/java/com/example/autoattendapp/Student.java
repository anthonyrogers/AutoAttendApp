package com.example.autoattendapp;

import android.os.Parcelable;

import java.util.ArrayList;

public class Student extends User implements Parcelable {

    public Student(String firstName, String lastName, String ID, String email, ArrayList<String> courses){
        super(firstName, lastName, ID, email, courses, null);
    }
}
