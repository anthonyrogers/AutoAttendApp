package com.example.autoattendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;


public class DateListActivity extends AppCompatActivity implements DateRecyclerViewAdapter.ItemClickListener {

    public final static String INTENT_ARG = "intent_arg";
    public final static String CLASS_ID = "class_id";
    public final static String COURSE = "course";
    public final static String USERTYPE = "usertype";

    RecyclerView dateRecyclerView;
    String classID;
    String course;
    String userType;
    List<String> classTimes;
    DBManager dbManager;
    private String date = null;

    private Handler onStudentsRecordsReceived = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if(msg.what == DBManager.DB_ERROR) {
                Toast.makeText(getApplicationContext(), "Failed to get attendance records", Toast.LENGTH_LONG);
            }
            ArrayList<? extends Parcelable> attendanceRecords = (ArrayList<? extends Parcelable>) msg.obj;
            Intent studentListActivityIntent = new Intent(DateListActivity.this, StudentListActivity.class);
            studentListActivityIntent.putParcelableArrayListExtra(StudentListActivity.ATTENDANCE_ARG, attendanceRecords);
            studentListActivityIntent.putExtra(StudentListActivity.CLASS_ID, classID);
            studentListActivityIntent.putExtra(StudentListActivity.DATE, date);
            startActivity(studentListActivityIntent);
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_list);
        dbManager = DBManager.getInstance();
        Intent current = getIntent();
        course = current.getStringExtra(COURSE);
        userType = current.getStringExtra(USERTYPE);
        setTitle(course);
        classTimes = current.getStringArrayListExtra(INTENT_ARG);
        if(classTimes != null) {
            classTimes = Lists.reverse(classTimes);
            classID = current.getStringExtra(CLASS_ID);
            dateRecyclerView = findViewById(R.id.dateRecyclerView);
            dateRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            DateRecyclerViewAdapter adapter = new DateRecyclerViewAdapter(this, classTimes);
            adapter.setClickListener(this);
            dateRecyclerView.setAdapter(adapter);
        }
    }

    // recycler view item click open the class
    @Override
    public void onItemClick(View view, int position) {
        Intent intent;
        if (userType.equals("Student")) {
            intent = new Intent(DateListActivity.this, StudentMeetingActivity.class);
            intent.putExtra("classID", classID);
            intent.putExtra("date", classTimes.get(position));
            startActivity(intent);
        } else {
            if(date != null) return;
            dbManager.getStudentsAttendance(onStudentsRecordsReceived, classID, classTimes.get(position));
            date = classTimes.get(position);
        }

    }
}
