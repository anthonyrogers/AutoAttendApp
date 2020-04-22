package com.example.autoattendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

        String classID = "zScqgJUNpxLDzbY2IgY8";
        final String date = "04/22/2020";
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
                                    String timeIn = totalTime.get(i).get("timeIn");
                                    String timeOut = totalTime.get(i).get("timeOut");
                                    Log.d("timeIn", timeIn);
                                    Log.d("timeOut", timeOut);
                                    if(timeIn.equals(null) || timeOut.equals(null)) { break;}
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

                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                        StudentMeetingActivity.this,
                                        android.R.layout.simple_list_item_1,
                                        timestamps);
                                timeList.setAdapter(arrayAdapter);

                                String dur = String.valueOf(total);
                                durationText.setText(dur + " minutes");
                                if (total == 0) {
                                    attendText.setText("Absent");
                                } else {
                                    attendText.setText("Present");
                                }






                            }
                        } else {
                            Log.d("database", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }
}
