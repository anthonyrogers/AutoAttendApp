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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
//NOTE: I created a gmail account for firebase so anyone can go in to it to
    // to create tables or edit firebase in the future. The login is as follows:
    // AutoAttendanceApp1@gmail.com
    // pass: Mobile123!


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

        //grabs instance of databases
        FirebaseDatabase database = FirebaseDatabase.getInstance();

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
                if(userEmail == null || userEmail.length() == 0){
                    Toast.makeText(getApplicationContext(), "Please enter an email", Toast.LENGTH_LONG).show();
                    return;
                }
                if(userPass == null || userPass.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please enter your password", Toast.LENGTH_LONG).show();
                    return;
                }
                System.out.println(userEmail + " " + userPass);
                // try to authenticate the user
                mAuth.signInWithEmailAndPassword(userEmail, userPass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                // check if user was successfully authenticated
                                if(task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(), "User successfully authenticated", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Failed to authenticate user", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });


    }
}
