package com.example.autoattendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {

    EditText firstname, lastname, email, password, TUID;
    Button student;
    ArrayList<Course> courses;
    FirebaseAuth fAuth;
    DBManager dbManager;
    private final String TAG = "CreateAccountActivity ===>";

    Handler loginHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if(msg.arg1 == User.TEACHER) {
                Intent teacherIntent = new Intent(CreateAccountActivity.this, TeacherActivity.class);
                startActivity(teacherIntent);
            } else if(msg.arg1 == User.STUDENT){
                Intent studentIntent = new Intent(CreateAccountActivity.this, StudentActivity.class);
                startActivity(studentIntent);
            } else {
                Log.d("Error", "Error? Handle.");
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        dbManager = DBManager.getInstance();

        TUID = findViewById(R.id.TUIDtxtBox);
        firstname = findViewById(R.id.firstNameTxtBox);
        lastname = findViewById(R.id.lastNameTxtBox);
        email = findViewById(R.id.emailTxtBox);
        password = findViewById(R.id.passwordTxtBox);
        student = findViewById(R.id.registerStudBtn);
        courses = new ArrayList<>();

        fAuth = FirebaseAuth.getInstance();
        student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewUser();
            }
        });
    }

    private void addNewUser() {
        String tuid = TUID.getText().toString().trim();
        final String firstName = firstname.getText().toString().trim();
        final String lastName = lastname.getText().toString().trim();
        final String userEmail = email.getText().toString().trim();
        String userPass = password.getText().toString().trim();

        if(firstName == null || firstName.length() == 0){
            Toast.makeText(getApplicationContext(), "Please enter your first name", Toast.LENGTH_LONG).show();
            return;
        }
        if(lastName == null || lastName.length() == 0) {
            Toast.makeText(getApplicationContext(), "Please enter your last name", Toast.LENGTH_LONG).show();
            return;
        }
        if(userEmail == null || userEmail.length() == 0) {
            Toast.makeText(getApplicationContext(), "Please enter an email", Toast.LENGTH_LONG).show();
            return;
        }
        if(userPass == null || userPass.length() == 0) {
            Toast.makeText(getApplicationContext(), "Please enter a password", Toast.LENGTH_LONG).show();
            return;
        }
        if(tuid == null || tuid.length() == 0) {
            Toast.makeText(getApplicationContext(), "Please enter a TUID", Toast.LENGTH_LONG).show();
            return;
        }

        fAuth.createUserWithEmailAndPassword(userEmail,userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    dbManager.addStudent(fAuth.getUid(), firstName, lastName, userEmail);
                    dbManager.loadUser(loginHandler);
                    Toast.makeText(getApplicationContext(), "Succesfully registered", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to register user", Toast.LENGTH_LONG).show();
                }
            }
        });


    }
}
