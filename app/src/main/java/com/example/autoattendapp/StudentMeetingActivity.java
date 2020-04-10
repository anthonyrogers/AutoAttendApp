package com.example.autoattendapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class StudentMeetingActivity extends AppCompatActivity {

    TextView roomText = findViewById(R.id.roomText);
    TextView inText = findViewById(R.id.inText);
    TextView outText = findViewById(R.id.outText);
    TextView durationText = findViewById(R.id.durText);
    TextView attendText = findViewById(R.id.attendText);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_meeting);

        //TODO change to meeting info from database
        roomText.setText("100");
        inText.setText("8:00");
        outText.setText("9:00");
        durationText.setText("1:00");
        attendText.setText("Yes");
    }
}