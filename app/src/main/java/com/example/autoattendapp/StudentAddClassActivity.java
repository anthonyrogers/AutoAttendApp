package com.example.autoattendapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StudentAddClassActivity extends AppCompatActivity {

    EditText classCode;
    Button addButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_add_class);
        setTitle("Add Class");

        addButton = findViewById(R.id.studentAddClassButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                classCode = findViewById(R.id.classCodeText);
                String code = classCode.getText().toString();

                if(code.equals("")) {
                    Toast.makeText(StudentAddClassActivity.this, "Please enter class code", Toast.LENGTH_SHORT).show();
                } else {
                    //TODO: check for class code in class database and if found, add to users courses using DBmanager
                }
            }
        });
    }
}
