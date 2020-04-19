package com.example.autoattendapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

public class CourseListActivity extends AppCompatActivity implements CourseRecyclerViewAdapter.ItemClickListener{

    private CourseRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private CourseRecyclerViewAdapter.ClassComparator mClassComparator;
    private PriorityQueue<CourseRecyclerViewAdapter.ClassInfo> mClassQueue;
    private DBManager db;
    private Button mAddButton;
    private String mUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        mUserType = getIntent().getExtras().getString("userType");
        if(mUserType.equals("Teacher"))
            setTitle("Course List (Teacher)");
        else
            setTitle("Course List (Student)");
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAddButton = findViewById(R.id.buttonAddCourse);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { AddClass(); }
        });

        mClassComparator = new CourseRecyclerViewAdapter.ClassComparator();
        mClassQueue = new PriorityQueue<CourseRecyclerViewAdapter.ClassInfo>(mClassComparator);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new CourseRecyclerViewAdapter(this, mClassQueue);
        mAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        db = DBManager.getInstance();
        db.getClassIdsOfUser(this);

        //FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //final String userUid = firebaseUser.getUid();
        //Log.d("course list ==>", userUid);

        //testing attendance marking
        //db.markAttendance("1234", "04/19/2020", "test", "user", "12:00");
        //db.markTimeOut("1234", "04/19/2020", "12:50");
    }


    // click add class button
    private void AddClass(){
        if(mUserType.equals("Teacher")) {
            MyGlobal.getInstance().courseListActivity = this;
            Intent ClassContentIntent = new Intent(this, AddClassContent.class);
            startActivity(ClassContentIntent);
        }
        else{
            Intent studentAddIntent = new Intent(CourseListActivity.this, StudentAddClassActivity.class);
            startActivity(studentAddIntent);
        }
    }

    public void freshClassList(){
        //Log.d("courseListActivity ==>", "freshClassList");
        mClassQueue.clear();
        db.getClassIdsOfUser(this);
    }

    // return class ids from DBManager
    public void loadClassIdsOfUser(boolean success, ArrayList<String> classlist){
        if(!success){
            Snackbar.make(getCurrentFocus(), "Fail to load classes of user", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            //Log.d("course list ==>", "Fail to load classes of user");
            return;
        }
        Log.d("course list ==>", "classlist.size: "+ classlist.size());
        for (int i=0; i<classlist.size(); i++) {
            String id = classlist.get(i).toString();
            db.getClassInfoById(this, id);
            Log.d("course list ==>", "class id: "+ id);
        }
    }

    // return class info from DBManager
    public void getClassInfoById(boolean success, String id, String name){
        if(!success) {
            Log.d("course list ==>", "class id: "+ id);
            return;
        }
        Log.d("course list ==>", "class id: "+ id+"   "+name);
        CourseRecyclerViewAdapter.ClassInfo classInfo;
        classInfo = new CourseRecyclerViewAdapter.ClassInfo(name, id);
        mClassQueue.add(classInfo);
        mAdapter.notifyDataSetChanged();
    }

    // recycler view click
    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(CourseListActivity.this, AddClassContent.class);

        String classID = mAdapter.getClass(position).classID;
        Iterator<CourseRecyclerViewAdapter.ClassInfo> iter = mClassQueue.iterator();
        CourseRecyclerViewAdapter.ClassInfo classInfo = null;
        while (iter.hasNext()) {
            classInfo = (CourseRecyclerViewAdapter.ClassInfo) iter.next();
            if (classID.equals(classInfo.classID))
                break;
        }
        if (classInfo == null) {
            Toast.makeText(this, "class info error.", Toast.LENGTH_SHORT).show();
            return;
        }
        intent.putExtra("classId", classID);
        startActivity(intent);
    }

    @Override
    public void onRemoveClick(View view, int position){
        String classID = mAdapter.getClass(position).classID;
        if(mUserType.equals("Teacher")) {
            db.deleteClassTeacher(classID);
            freshClassList();
        } else {
            db.deleteClassStudent(classID, this);
        }
        final Intent intent = new Intent(CourseListActivity.this, CourseListActivity.class);
        intent.putExtra("userType", mUserType);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                startActivity(intent);
            }
        }, 1000);
    }

    @Override
    public void onModifyClick(View view, int position) {

    }

    @Override
    public void onViewCodeClick(View view, int position) {
        String classID = mAdapter.getClass(position).classID;
        db.getClassCode(classID, this);
    }

}
