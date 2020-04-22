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

    RecyclerView dateRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_list);
        Intent current = getIntent();
        List<String> classTimes = current.getStringArrayListExtra(INTENT_ARG);
        classTimes = Lists.reverse(classTimes);
        String classID = current.getStringExtra(CLASS_ID);
        dateRecyclerView = findViewById(R.id.dateRecyclerView);
        dateRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dateRecyclerView.setAdapter(new DateRecyclerViewAdapter(classTimes));
    }

    private class DateRecyclerViewAdapter extends RecyclerView.Adapter<DateRecyclerViewAdapter.ViewHolder> {
        List<String> pastMeetings;

        public DateRecyclerViewAdapter(List<String> pastMeetings) {
            this.pastMeetings = pastMeetings;
        }
        @NonNull
        @Override
        public DateRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.recyclerview_row, parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DateRecyclerViewAdapter.ViewHolder holder, int position) {
            String date = pastMeetings.get(position);
            System.out.println(date);
            holder.textViewName.setText(date);
        }

        @Override
        public int getItemCount() {
            return pastMeetings.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView textViewName;
            ViewHolder(View itemView) {
                super(itemView);
                textViewName = itemView.findViewById(R.id.tvDeviceName);
            }
        }
    }
}
