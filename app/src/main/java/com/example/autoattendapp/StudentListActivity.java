package com.example.autoattendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class StudentListActivity extends AppCompatActivity {

    public static final String ATTENDANCE_ARG = "attendance";
    public static final String DATE = "date";
    public static final String CLASS_ID = "classID";

    DBManager dbManager;
    RecyclerView recyclerView;
    StudentRecyclerViewAdapter adapter;
    List<AttendanceRecord> attendanceRecords;
    String date;
    String classID;
    Double classDur;

    private Handler receiveClassDuration = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if(msg.what == DBManager.DB_ERROR) {
                Toast.makeText(getApplicationContext(), "Failed to get class duration", Toast.LENGTH_LONG).show();
                return false;
            }
            Long classDuration = (Long) msg.obj;
            classDur = Double.valueOf(classDuration);
            initRecyclerView();
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);
        Intent current = getIntent();
        attendanceRecords = current.getParcelableArrayListExtra(ATTENDANCE_ARG);
        for(AttendanceRecord record:attendanceRecords) {
            System.out.println(String.join(" ", record.getFirstName(), record.getLastName()));
        }
        date = current.getStringExtra(DATE);
        classID = current.getStringExtra(CLASS_ID);
        recyclerView = findViewById(R.id.studentRecyclerView);
        dbManager = DBManager.getInstance();
        setTitle(date);
        dbManager.getClassDuration(receiveClassDuration, classID, date);
    }

    private void initRecyclerView(){
        adapter = new StudentRecyclerViewAdapter(getApplicationContext(), attendanceRecords, classDur);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
