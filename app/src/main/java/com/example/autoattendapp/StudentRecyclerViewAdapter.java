package com.example.autoattendapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentRecyclerViewAdapter extends RecyclerView.Adapter<StudentRecyclerViewAdapter.ViewHolder> {
    List<AttendanceRecord> students;
    Context context;
    public ItemClickListener mClickListener;
    private Double classDuration;

    public StudentRecyclerViewAdapter(Context context, List<AttendanceRecord> students, Double duration) {
        this.students = students;
        this.context = context;
        this.classDuration = duration;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.studentrecyclerview_row, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        AttendanceRecord record = students.get(position);
        holder.textViewName.setText(String.join(" ", record.getFirstName(), record.getLastName()));
        long studentDuration = DBManager.getStudentDuration((record.getTimes()));
        Double percentage = Double.valueOf(studentDuration) / Double.valueOf(classDuration) * 100;
        GradientDrawable drawable = (GradientDrawable) holder.colorView.getBackground();
        if(percentage < 25) {
            drawable.setColor(Color.RED);
        } else if ((25 <= percentage) && (percentage <= 75)) {
            drawable.setColor(Color.parseColor("#fcb603"));
        } else {
            drawable.setColor(Color.GREEN);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onItemClick(v, position);
            }
        });
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName;
        public View colorView;
        ViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.tvDeviceName);
            colorView = itemView.findViewById(R.id.attendanceColor);
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
