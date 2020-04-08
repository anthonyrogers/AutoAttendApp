package com.example.autoattendapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class StudentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        Student student = Account.getStudentAccount();
        if(student == null) {
            Toast.makeText(getApplicationContext(), "Failed to load user", Toast.LENGTH_LONG);
        }
        setTitle(student.getFirstName() + " " + student.getLastName());
    }
}
