package com.example.autoattendapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class AccountType extends AppCompatActivity {

    ImageView student, teacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_type);

        student = findViewById(R.id.studentAccPic);
        teacher = findViewById(R.id.teacherAccPic);

        student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createAcc = new Intent(AccountType.this, CreateAccountActivity.class);
                startActivity(createAcc);
            }
        });

        teacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
