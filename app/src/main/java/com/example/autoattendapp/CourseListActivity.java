package com.example.autoattendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
import java.util.List;
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
    public static int MsgType_FreshList = 1;

    Handler msgHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if(msg.arg1 == MsgType_FreshList) {
                freshClassList();
            } else {
                Log.d("CourseListActivity msg Error", "Error? Handle.");
            }
            return false;
        }
    });

    Handler listDateClass = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if(msg.what == DBManager.DB_ERROR) {
                Toast.makeText(getApplicationContext(), "Failed to get past meetings", Toast.LENGTH_LONG).show();
                return false;
            }
            Object[] response = (Object []) msg.obj;
            ArrayList<String> pastMeetings = (ArrayList<String>) response[0];
            String classID = (String) response[1];
            String course = (String) response[2];
            Intent dateListActivity = new Intent(CourseListActivity.this, DateListActivity.class);
            dateListActivity.putStringArrayListExtra(DateListActivity.INTENT_ARG, pastMeetings);
            dateListActivity.putExtra(DateListActivity.CLASS_ID, classID);
            dateListActivity.putExtra(DateListActivity.COURSE, course);
            dateListActivity.putExtra(DateListActivity.USERTYPE, mUserType);
            startActivity(dateListActivity);
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        mUserType = getIntent().getExtras().getString("userType");
        /*if(mUserType.equals("Teacher"))
            setTitle("Course List (Teacher)");
        else
            setTitle("Course List (Student)");*/
        //set user's name as title
        final FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    setTitle(task.getResult().getString("firstname") + " " + task.getResult().getString("lastname"));
                } else {
                }
            }
        });
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
        MyGlobal.getInstance().handlerCourseListAcitviey = msgHandler;

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

    // recycler view item click open the class
    @Override
    public void onItemClick(View view, int position) {
        //Intent intent = new Intent(CourseListActivity.this, AddClassContent.class);

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
        db.getDateListForClass(listDateClass, classID);
        //intent.putExtra("classId-view", classID);
        //startActivity(intent);
    }

    @Override
    public void onRemoveClick(View view, int position){
        String classID = mAdapter.getClass(position).classID;
        if(mUserType.equals("Teacher")) {
            db.deleteClassTeacher(classID);
            //freshClassList();
        } else {
            db.deleteClassStudent(classID, this);
        }
        /*
        final Intent intent = new Intent(CourseListActivity.this, CourseListActivity.class);
        intent.putExtra("userType", mUserType);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                startActivity(intent);
            }
        }, 1000);*/
    }

    //modify a class
    @Override
    public void onModifyClick(View view, int position) {
        if(!mUserType.equals("Teacher")){
            Toast.makeText(this, "Student cannot modify class info.", Toast.LENGTH_SHORT).show();
            return;
        }
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
            Toast.makeText(this, "class info error2.", Toast.LENGTH_SHORT).show();
            return;
        }
        intent.putExtra("classId-modify", classID);
        startActivity(intent);
    }

    @Override
    public void onViewCodeClick(View view, int position) {
        String classID = mAdapter.getClass(position).classID;
        db.getClassCode(classID, this);
    }

}
