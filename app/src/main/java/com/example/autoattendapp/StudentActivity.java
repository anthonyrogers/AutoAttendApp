package com.example.autoattendapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class StudentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        //sets the title to user name
        Student student = Account.getStudentAccount();
        if(student == null) {
            Toast.makeText(getApplicationContext(), "Failed to load user", Toast.LENGTH_LONG);
        }
        setTitle(student.getFirstName() + " " + student.getLastName());

    }
}
