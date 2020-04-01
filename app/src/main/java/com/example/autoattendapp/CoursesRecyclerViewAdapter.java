package com.example.autoattendapp;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

public class CoursesRecyclerViewAdapter extends RecyclerView.Adapter<CoursesRecyclerViewAdapter.ViewHolder> {

    private List<Course> courses;

    public CoursesRecyclerViewAdapter(List<Course> courses) {
        this.courses = courses;
    }

    public void updateCourses(List<Course> courses) {
        this.courses = courses;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CoursesRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View recyclerViewItem = layoutInflater.inflate(R.layout.courses_rv_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(recyclerViewItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CoursesRecyclerViewAdapter.ViewHolder holder, int position) {
        Course course = courses.get(position);
        TextView textView = holder.textView;
        textView.setText(course.getCourseName());
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.courseName);
        }
    }
}
