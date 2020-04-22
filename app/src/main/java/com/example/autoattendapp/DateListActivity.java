package com.example.autoattendapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;


public class DateListActivity extends AppCompatActivity {

    public final static String INTENT_ARG = "intent_arg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_list);


    }
}
