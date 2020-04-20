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

public class StudentMeetingActivity extends AppCompatActivity {

    final TextView dateText = findViewById(R.id.dateText);
    final TextView inText = findViewById(R.id.inText);
    final TextView outText = findViewById(R.id.outText);
    final TextView durationText = findViewById(R.id.durText);
    final TextView attendText = findViewById(R.id.attendText);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_meeting);
        setTitle("Meeting");
        String classID = "1234";
        final String date = "04/19/2020";

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
                                dateText.setText(date);
                                inText.setText((String) document.get("timeIn"));
                                outText.setText((String) document.get("timeOut"));
                                //TODO fix these
                                durationText.setText((String) document.get(""));
                                attendText.setText((String) document.get(""));
                            }
                        } else {
                            Log.d("database", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }
}
