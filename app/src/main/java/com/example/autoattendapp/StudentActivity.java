package com.example.autoattendapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class StudentActivity extends AppCompatActivity {

    DBManager dbManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        dbManager = DBManager.getInstance();

        //sets the title to user name
        Student student = Account.getStudentAccount();
        if(student == null) {
            Toast.makeText(getApplicationContext(), "Failed to load user", Toast.LENGTH_LONG);
        }
        setTitle(student.getFirstName() + " " + student.getLastName());

        dbManager.getClassList();

        Button addNew = findViewById(R.id.addNew);
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent studentAddIntent = new Intent(StudentActivity.this, StudentAddClassActivity.class);
                startActivity(studentAddIntent);
            }
        });

    }
}
