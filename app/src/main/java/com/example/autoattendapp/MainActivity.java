package com.example.autoattendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {
//NOTE: I created a gmail account for firebase so anyone can go in to it to
    // to create tables or edit firebase in the future. The login is as follows:
    // AutoAttendanceApp1@gmail.com
    // pass: Mobile123!

    private final String TAG = "MainActivity ===>";

    private FirebaseAuth mAuth;

    private Button createAccount;
    private Button loginButton;

    EditText email;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //grabs instance of authentication
        mAuth = FirebaseAuth.getInstance();
        // try to authenticate the App
        mAuth.signInWithEmailAndPassword("autoattendanceapp1@gmail.com", "MobileAutoAttendance123!")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // check if user was successfully authenticated
                        if (task.isSuccessful()) {
                            Log.d(TAG, "App successfully authenticated");
                        } else {
                            Log.d(TAG, "Failed to authenticate app");
                            Toast.makeText(getApplicationContext(), "Failed to connect database", Toast.LENGTH_LONG).show();
                        }
                    }
                });


        // launch create account activity when user presses register
        createAccount = findViewById(R.id.createAccount);
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createAccountIntent = new Intent(MainActivity.this, CreateAccountActivity.class);
                startActivity(createAccountIntent);
            }
        });

        // email and password fields
        email = findViewById(R.id.emailTxtBox);
        password = findViewById(R.id.passwordTxtBox);

        // authenticate the user
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get the text in the fields
                String userEmail = email.getText().toString();
                String userPass = password.getText().toString();

                // check to see if user entered email/password
                if (userEmail == null || userEmail.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please enter an email", Toast.LENGTH_LONG).show();
                    return;
                }
                if (userPass == null || userPass.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please enter your password", Toast.LENGTH_LONG).show();
                    return;
                }
                Log.d(TAG, userEmail + " " + userPass);
                // try to access database's table: users
                FirebaseFirestore database = MyGlobal.getInstance().gDB;
                CollectionReference usersRef = database.collection("users");
                database.collection("users")
                        .whereEqualTo("email", userEmail)
                        .whereEqualTo("password", userPass)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot documentSnapshots) {
                                String firstName = "", lastName = "", userEmail = "", userPass = "";
                                int usertype = 0;
                                int count = 0;
                                for (QueryDocumentSnapshot snap : documentSnapshots) {
                                    count += 1;
                                    Log.d(TAG, snap.getId() + " => " + snap.getData());
                                    firstName = snap.getData().get("firstname").toString();
                                    lastName = snap.getData().get("lastname").toString();
                                    userEmail = snap.getData().get("email").toString();
                                    userPass = snap.getData().get("password").toString();
                                    usertype = Integer.parseInt(snap.getData().get("usertype").toString());
                                }
                                if (count > 0) {
                                    Intent intent;
                                    if (usertype == 0) {
                                        MyGlobal.getInstance().gUser = new Student(firstName, lastName, userEmail, userPass);
                                        intent = new Intent(MainActivity.this, StudentActivity.class);
                                    } else {
                                        MyGlobal.getInstance().gUser = new Teacher(firstName, lastName, userEmail, userPass);
                                        intent = new Intent(MainActivity.this, TeacherActivity.class);
                                    }
                                    finish();
                                    startActivity(intent);
                                } else
                                    Toast.makeText(getApplicationContext(), "Failed to authenticate user", Toast.LENGTH_LONG).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Failed to authenticate user", e);
                                Toast.makeText(getApplicationContext(), "Failed to connect server.", Toast.LENGTH_LONG).show();
                            }
                        });

            }
        });
    }
}
