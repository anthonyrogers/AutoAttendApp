package com.example.autoattendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/*
    NOTE: I created a gmail account for firebase so anyone can go in to it to
    to create tables or edit firebase in the future. The login is as follows:
    AutoAttendanceApp1@gmail.com
    pass: Mobile123!

    Testing:
    Teacher User: (Usertype: 1)
    Email: tuf001@temple.edu
    Password: 123456

    Student User: (Usertype: 0)
    Email: Your temple email
    Password: 123456
*/
public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity ===>";

    private LoginInfo mLoginInfo;
    private Button createAccount;
    private Button loginButton;
    private EditText email;
    private EditText password;
    FirebaseAuth mAuth;
    DBManager dbManager;
    Intent mServiceIntent;


    Handler loginHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if(msg.arg1 == User.TEACHER) {
                Intent teacherIntent = new Intent(MainActivity.this, TeacherActivity.class);
                startActivity(teacherIntent);
            } else if(msg.arg1 == User.STUDENT){
                Intent studentIntent = new Intent(MainActivity.this, StudentActivity.class);
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
        setContentView(R.layout.activity_main);

        //Code for the Beacon Service
        mServiceIntent = new Intent(this, ServiceForBeacon.class);

        //Checks to see if service is running
        if (!isMyServiceRunning(ServiceForBeacon.class)) {
            Intent serviceIntent = new Intent(this, ServiceForBeacon.class);
            ContextCompat.startForegroundService(this, serviceIntent);

        }

        dbManager = DBManager.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //location is needed so this will check for permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1234);
        }

        // email and password fields
        email = findViewById(R.id.emailTxtBox);
        password = findViewById(R.id.passwordTxtBox);

        // launch create account activity when user presses register
        createAccount = findViewById(R.id.createAccount);
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { register(); }
        });

        // authenticate the user
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {login(); }
        });

        // load the login info, user don't need to type again.
        readLoginInfoFromFile();
        email.setText(mLoginInfo.getEmail());
        password.setText(mLoginInfo.getPassword());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    // read login info from file
    private void readLoginInfoFromFile() {
        Context context = getApplicationContext();
        try {
            FileInputStream fis = context.openFileInput("loginInfo");
            ObjectInputStream is = new ObjectInputStream(fis);
            mLoginInfo = (LoginInfo) is.readObject();
            is.close();
            fis.close();
            Log.d(TAG, "Loaded Info.\n");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (mLoginInfo == null) {
            mLoginInfo = new LoginInfo("", "");
        }
        MyGlobal.getInstance().gLoginInfo = mLoginInfo;
    }

    private void saveLoginInfoToFile() throws IOException {
        Context context = getApplicationContext();
        FileOutputStream fos = context.openFileOutput("loginInfo", Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);

        mLoginInfo.setEmail(email.getText().toString());
        mLoginInfo.setPassword(password.getText().toString());

        os.writeObject(mLoginInfo);
        os.close();
        fos.close();
        Log.d(TAG, "saved Login info to file.\n");
    }

    // go to register activity
    private void register(){
        Intent accountType = new Intent(MainActivity.this, AccountType.class);
        //Intent accountType = new Intent(MainActivity.this, AddClassContent.class);
        finish();
        startActivity(accountType);
    }

    // when login button
    private void login(){

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

        mAuth.signInWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    try {
                        saveLoginInfoToFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    dbManager.loadUser(loginHandler);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmailAndPassword:failure", task.getException());
                    Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
