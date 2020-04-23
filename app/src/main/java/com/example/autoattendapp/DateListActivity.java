package com.example.autoattendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.Lists;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_list);
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

        }

    }
}
