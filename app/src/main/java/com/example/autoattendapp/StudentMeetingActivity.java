package com.example.autoattendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StudentMeetingActivity extends AppCompatActivity {


    private final String ABSENT = "absent";
    private final String YELLOW = "#fcb603";

    public final static String CLASS_DUR_ARG = "classDur";
    public final static String ATTENDANCE_ARG = "attendance";
    public final static String DATE_ARG = "date";

    private Double classDur;
    private AttendanceRecord record;
    private String date;

    ListView timeList;
    TextView durationText;
    TextView attendText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_meeting);

        Intent current = getIntent();

        timeList = findViewById(R.id.timeList);
        durationText = findViewById(R.id.durText);
        attendText = findViewById(R.id.attendText);

        classDur = current.getDoubleExtra(CLASS_DUR_ARG, 0);
        record = current.getParcelableExtra(ATTENDANCE_ARG);
        date = current.getStringExtra(DATE_ARG);

        setTitle(date);

        double studentDur = DBManager.getStudentDuration(record.getTimes());
        String dur = String.valueOf(studentDur);
        durationText.setText(dur + " minutes");

        Double percentage = Double.valueOf(studentDur) / Double.valueOf(classDur) * 100;
        DecimalFormat df = new DecimalFormat("#.##");
        String percent = String.valueOf(df.format(percentage)) + " %";

        if(studentDur == 0)
            attendText.setText(ABSENT);
        else
            attendText.setText(percent);

        if(percentage < 25) {
            attendText.setTextColor(Color.RED);
        } else if ((25 <= percentage) && (percentage <= 75)) {
            attendText.setTextColor(Color.parseColor(YELLOW));
        } else {
            attendText.setTextColor(Color.GREEN);
        }

        SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat parseFormat = new SimpleDateFormat("HH:mm");

        ArrayList<Map<String, String>> times = record.getTimes();
        ArrayList<String> timestamps = new ArrayList<>();
        for(Map<String, String> time : times) {
            if(time.get(DBManager.TIME_IN) == null || time.get(DBManager.TIME_OUT) == null)
                break;
            String timeIn = time.get(DBManager.TIME_IN);
            String timeOut = time.get(DBManager.TIME_OUT);
            try {
                // just changed military time to standard 12 hour format so its easier to read by user
                timestamps.add("Time In: " + displayFormat.format(parseFormat.parse(timeIn)));
                timestamps.add("Time Out: " + displayFormat.format(parseFormat.parse(timeOut)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                StudentMeetingActivity.this,
                android.R.layout.simple_list_item_1,
                timestamps);
        timeList.setAdapter(arrayAdapter);
    }
}
