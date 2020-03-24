package com.example.autoattendapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
//NOTE: I created a gmail account for firebase so anyone can go in to it to
    // to create tables or edit firebase in the future. The login is as follows:
    // AutoAttendanceApp1@gmail.com
    // pass: Mobile123!


    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //grabs instance of authentication
        mAuth = FirebaseAuth.getInstance();

        //grabs instance of databases
        FirebaseDatabase database = FirebaseDatabase.getInstance();
    }
}
