package com.example.autoattendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
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
import java.util.Date;

public class StudentMeetingActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_meeting);

        final TextView inText = findViewById(R.id.inText);
        final TextView outText = findViewById(R.id.outText);
        final TextView durationText = findViewById(R.id.durText);
        final TextView attendText = findViewById(R.id.attendText);

        //TODO: pass these from selected meeting date as extras
        String classID = getIntent().getExtras().getString("classID");
        final String date = getIntent().getExtras().getString("date");;
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
                                String timeIn = (String) document.get("timeIn");
                                String timeOut = (String) document.get("timeOut");
                                inText.setText(timeIn);
                                outText.setText(timeOut);

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
                                //Log.d("In:", String.valueOf(in));
                                //Log.d("Out:", String.valueOf(out));
                                long difference = out.getTime() - in.getTime();
                                //Log.d("difference", out.getTime()+ "-" +in.getTime());
                                //Log.d("Diff:", String.valueOf(difference));
                                String dur = String.valueOf(difference/1000/60);
                                durationText.setText(dur + " minutes");

                                if(timeIn != null) {
                                    attendText.setText("Yes");
                                } else {
                                    attendText.setText("No");
                                }

                            }
                        } else {
                            Log.d("database", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }
}
