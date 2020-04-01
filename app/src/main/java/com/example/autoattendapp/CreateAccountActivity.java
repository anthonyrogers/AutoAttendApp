package com.example.autoattendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
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
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {

    EditText firstname, lastname, email, password;
    Button student, professor;
    //FirebaseAuth fAuth;
    boolean mIsStudent;
    private final String TAG = "CreateAccountActivity ===>";

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

        //fAuth = FirebaseAuth.getInstance();

        student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsStudent = true;
                addNewUser();
            }
        });

        professor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsStudent = false;
                addNewUser();
            }
        });
    }

    private void addNewUser() {
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
        /*
        fAuth.createUserWithEmailAndPassword(userEmail,userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Succesfully registered", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to register user", Toast.LENGTH_LONG).show();
                }
            }
        });*/
        //User user = new User(firstName, lastName, userEmail, userPass);
        FirebaseFirestore database = MyGlobal.getInstance().gDB;
        CollectionReference citiesRef = database.collection("users");

        // Create a new user
        Map<String, Object> mapUser = new HashMap<>();
        mapUser.put("firstname", firstName);
        mapUser.put("lastname", lastName);
        mapUser.put("email", userEmail);
        mapUser.put("password", userPass);
        if(mIsStudent)
            mapUser.put("usertype", 0);
        else
            mapUser.put("usertype", 1);

        // Add a new document with a generated ID
        database.collection("users")
                .add(mapUser)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String firstName = firstname.getText().toString().trim();
                        String lastName = lastname.getText().toString().trim();
                        String userEmail = email.getText().toString().trim();
                        String userPass = password.getText().toString().trim();
                        String id = documentReference.getId();
                        //Log.d(TAG, "user id: "+id);
                        Intent intent;
                        if (mIsStudent){
                            MyGlobal.getInstance().gUser = new Student(id, firstName, lastName, userEmail, userPass);
                            intent = new Intent(CreateAccountActivity.this, StudentActivity.class);
                        }
                        else {
                            MyGlobal.getInstance().gUser = new Teacher(id, firstName, lastName, userEmail, userPass);
                            intent = new Intent(CreateAccountActivity.this, TeacherActivity.class);
                        }
                        LoginInfo loginInfo = MyGlobal.getInstance().gLoginInfo;
                        try {
                            saveLoginInfoToFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        finish();
                        startActivity(intent);

                        //Log.d(TAG,MyGlobal.getInstance().gUser.getClass().getName().toString());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });


    }

    private void saveLoginInfoToFile() throws IOException {
        Context context = getApplicationContext();
        FileOutputStream fos = context.openFileOutput("loginInfo", Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);

        LoginInfo loginInfo = MyGlobal.getInstance().gLoginInfo;
        loginInfo.setEmail(email.getText().toString());
        loginInfo.setPassword(password.getText().toString());

        os.writeObject(loginInfo);
        os.close();
        fos.close();
        Log.d(TAG, "saved Login info to file.\n");
    }
}
