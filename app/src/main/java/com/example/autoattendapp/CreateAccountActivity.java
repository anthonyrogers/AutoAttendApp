package com.example.autoattendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class CreateAccountActivity extends AppCompatActivity {


    EditText firstname, lastname, email, password;
    Button student, professor;
    FirebaseAuth fAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        firstname = findViewById(R.id.firstNameTxtBox);
        lastname = findViewById(R.id.lastNameTxtBox);
        email = findViewById(R.id.emailTxtBox);
        password = findViewById(R.id.passwordTxtBox);

        student = findViewById(R.id.registerStudentBtn);
        professor = findViewById(R.id.registerProfBtn);

        fAuth = FirebaseAuth.getInstance();

        student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                fAuth.createUserWithEmailAndPassword(userEmail,userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Succesfully registered", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to register user", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

}
