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


public class DateListActivity extends AppCompatActivity {

    public final static String INTENT_ARG = "intent_arg";
    public final static String CLASS_ID = "class_id";
    public final static String COURSE = "course";

    RecyclerView dateRecyclerView;
    String classID;
    String course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_list);
        Intent current = getIntent();
        List<String> classTimes = current.getStringArrayListExtra(INTENT_ARG);
        if(classTimes != null) {
            classTimes = Lists.reverse(classTimes);
            classID = current.getStringExtra(CLASS_ID);
            dateRecyclerView = findViewById(R.id.dateRecyclerView);
            dateRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            dateRecyclerView.setAdapter(new DateRecyclerViewAdapter(this, classTimes));
        }
        course = current.getStringExtra(COURSE);
        setTitle(course);
    }
}
