package com.example.autoattendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StudentMeetingActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_meeting);

        final ListView timeList = findViewById(R.id.timeList);
        final TextView durationText = findViewById(R.id.durText);
        final TextView attendText = findViewById(R.id.attendText);

        //TODO: pass these from selected meeting date as extras
        //String classID = getIntent().getExtras().getString("classID");
        //final String date = getIntent().getExtras().getString("date");;

        final String classID = "RJhAZsxZn5HqdMwJWwD8";
        final String date = "Mon, 04/22/2020";
        setTitle(date);

        final FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser == null)
            return;
        final String studentID = firebaseUser.getUid();

        database.collection("attendance")
                .whereEqualTo("studentID", studentID)
                .whereEqualTo("classID", classID)
                .whereEqualTo("date", date)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ArrayList<Map<String, String>> totalTime = (ArrayList<Map<String, String>>) document.get("times");
                                long total = 0;
                                ArrayList<String> timestamps = new ArrayList<>();

                                for(int i=0; i < totalTime.size(); i++) {
                                    if(totalTime.get(i).get("timeIn") == null || totalTime.get(i).get("timeOut") == null) {
                                        break;
                                    }
                                    String timeIn = totalTime.get(i).get("timeIn");
                                    String timeOut = totalTime.get(i).get("timeOut");
                                    Log.d("timeIn", timeIn);
                                    Log.d("timeOut", timeOut);

                                    timestamps.add("Time In: " + timeIn);
                                    timestamps.add("Time Out: " + timeOut);

                                    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                                    Date in = null;
                                    try {
                                        in = format.parse(timeIn);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    Date out = null;
                                    try {
                                        out = format.parse(timeOut);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    long difference = out.getTime() - in.getTime();
                                    difference = difference/1000/60;
                                    total = total + difference;
                                }
                                final long finalTotal = total;

                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                        StudentMeetingActivity.this,
                                        android.R.layout.simple_list_item_1,
                                        timestamps);
                                timeList.setAdapter(arrayAdapter);

                                String dur = String.valueOf(total);
                                durationText.setText(dur + " minutes");

                                database.collection("classes").document(classID)
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            Map<String, String> durationMap = (Map<String, String>) task.getResult().get("duration");
                                            assert durationMap != null;
                                            String dayOfWeek = date.substring(0, 3);
                                            Log.d ("Day", dayOfWeek);
                                            String classDuration = durationMap.get(dayOfWeek);
                                            Long classDur = Long.parseLong(classDuration);
                                            Double percentage = Double.valueOf(finalTotal) / Double.valueOf(classDur) * 100;
                                            Log.d ("Percent", String.valueOf(percentage) + " %");
                                            String percent = String.valueOf(percentage);
                                            if(percentage < 25) {
                                                attendText.setText(percent + " %");
                                                attendText.setTextColor(Color.RED);
                                            } else if ((25 <= percentage) && (percentage <= 75)) {
                                                attendText.setText(percent + " %");
                                                attendText.setTextColor(Color.parseColor("#fcb603"));
                                            } else {
                                                attendText.setText(percent + " %");
                                                attendText.setTextColor(Color.GREEN);
                                            }
                                        } else {
                                            Log.d("Database", "error getting duration");
                                        }
                                    }
                                });
                                if (total == 0) {
                                    attendText.setText("Absent");
                                    attendText.setTextColor(Color.RED);
                                }

                            }
                        } else {
                            Log.d("database", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }
}
