package com.example.autoattendapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DateRecyclerViewAdapter extends RecyclerView.Adapter<DateRecyclerViewAdapter.ViewHolder> {
    List<String> pastMeetings;
    Context context;


    public DateRecyclerViewAdapter(Context context, List<String> pastMeetings) {
        this.pastMeetings = pastMeetings;
        this.context = context;
    }
    @NonNull
    @Override
    public DateRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.daterecyclerview_row, parent,false);
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
