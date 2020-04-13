package com.example.autoattendapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;

public class CreateTeacherAccount extends AppCompatActivity {

    EditText firstname, lastname, email, password, TUID, beaconID;
    Button teacherbtn;
    FirebaseAuth fAuth;
    DBManager dbManager;
    private final String TAG = "CreateAccountActivity ===>";

    Handler loginHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if(msg.arg1 == User.TEACHER) {
                Intent teacherIntent = new Intent(CreateTeacherAccount.this, TeacherActivity.class);
                startActivity(teacherIntent);
            } else if(msg.arg1 == User.STUDENT){
                Intent studentIntent = new Intent(CreateTeacherAccount.this, StudentActivity.class);
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
        dbManager = DBManager.getInstance();
        setContentView(R.layout.activity_create_teacher_account);

        TUID = findViewById(R.id.TUIDtxtBox);
        firstname = findViewById(R.id.firstNameTxtBox);
        lastname = findViewById(R.id.lastNameTxtBox);
        email = findViewById(R.id.emailTxtBox);
        password = findViewById(R.id.passwordTxtBox);
        teacherbtn = findViewById(R.id.registerStudentBtn);
        beaconID = findViewById(R.id.BeaconIDtxtBox);

        fAuth = FirebaseAuth.getInstance();
        teacherbtn.setOnClickListener(new View.OnClickListener() {
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
        final String beacon = beaconID.getText().toString().trim();

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
        if(beacon == null || beacon.length() == 0){
            Toast.makeText(getApplicationContext(), "A Beacon ID is required to use the app", Toast.LENGTH_LONG).show();
            return;
        }

        fAuth.createUserWithEmailAndPassword(userEmail,userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Successfully Registered", Toast.LENGTH_LONG).show();
                    dbManager.addTeacher(fAuth.getUid(), firstName, lastName, userEmail, beacon);
                    dbManager.loadUser(loginHandler);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof FirebaseAuthUserCollisionException){
                    Toast.makeText(getApplicationContext(), "Failed: Email already exists", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
