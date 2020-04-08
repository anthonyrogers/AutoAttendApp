package com.example.autoattendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateStudentAccount extends AppCompatActivity {

    EditText firstname, lastname, email, password, TUID;
    Button student;
    ArrayList<Course> courses;
    FirebaseAuth fAuth;
    private final String TAG = "CreateAccountActivity ===>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_stuent_account);

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
        String firstName = firstname.getText().toString().trim();
        String lastName = lastname.getText().toString().trim();
        String userEmail = email.getText().toString().trim();
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
                    String tuid = TUID.getText().toString().trim();
                    String firstName = firstname.getText().toString().trim();
                    String lastName = lastname.getText().toString().trim();
                    String userEmail = email.getText().toString().trim();
                    String userPass = password.getText().toString().trim();
                    ArrayList<Course> courses = new ArrayList<>();
                    Beacon beacon = new Beacon("");
                    // Create a new user
                    Map<String, Object> mapUser = new HashMap<>();
                    mapUser.put("ID", tuid);
                    mapUser.put("firstname", firstName);
                    mapUser.put("lastname", lastName);
                    mapUser.put("email", userEmail);
                    mapUser.put("password", userPass);
                    mapUser.put("courses", courses);
                    mapUser.put("beacon", beacon);

                    //User user = new User(firstName, lastName, userEmail, userPass);
                    FirebaseFirestore database = MyGlobal.getInstance().gDB;
                    // Add a new document with a generated ID
                    database.collection("users").document(fAuth.getUid()).set(mapUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent intent = new Intent(CreateStudentAccount.this, StudentActivity.class);
                            finish();
                            startActivity(intent);
                        }
                    });

                    Toast.makeText(getApplicationContext(), "Succesfully registered", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to register user", Toast.LENGTH_LONG).show();
                }
            }
        });


    }
}
