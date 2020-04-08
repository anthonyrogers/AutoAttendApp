package com.example.autoattendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
public class MainActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier {

    private final String TAG = "MainActivity ===>";

    private LoginInfo mLoginInfo;
    private Button createAccount;
    private Button loginButton;
    private EditText email;
    private EditText password;
    BeaconManager mBeaconManager;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //calls 3rd party beacon sdk and starts the instance
        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        // Binds this activity to the BeaconService
        mBeaconManager.bind(this);

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

        createAccount.setEnabled(false);
        loginButton.setEnabled(false);

        // load the login info, user don't need to type again.
        readLoginInfoFromFile();
        email.setText(mLoginInfo.getEmail());
        password.setText(mLoginInfo.getPassword());
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
        Log.d(TAG, userEmail + " " + userPass);

        mAuth.signInWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");

                    //will figure out which activity to redirect to after I do the create Account portion

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // try to access database's table: users
       /* FirebaseFirestore database = MyGlobal.getInstance().gDB;
        //CollectionReference usersRef = database.collection("users");
        database.collection("users")
                .whereEqualTo("email", userEmail)
                .whereEqualTo("password", userPass)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        String firstName = "", lastName = "", userEmail = "", userPass = "", id="";
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
                            id = snap.getId();
                            Log.d(TAG, "user id: "+id);
                            break;
                        }
                        if (count > 0) {    //create user
                            Intent intent;
                            if (usertype == 0) {
                                MyGlobal.getInstance().gUser = new Student(id, firstName, lastName, userEmail, userPass);
                                MyGlobal.getInstance().gUser.setID(id);
                                intent = new Intent(MainActivity.this, StudentActivity.class);

                            } else {
                                MyGlobal.getInstance().gUser = new Teacher(id, firstName, lastName, userEmail, userPass);
                                intent = new Intent(MainActivity.this, TeacherActivity.class);
                            }
                            // if email or password changed, save them to file
                            if (!mLoginInfo.getEmail().equals(email.getText().toString()) ||
                                    !mLoginInfo.getPassword().equals(password.getText().toString())) {
                                try {
                                    saveLoginInfoToFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
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

        */
    }

    //THE TWO OVERRIDE METHODS ARE FOR THE BEACON CONNECTION AND RESPONSE
    @Override
    public void onBeaconServiceConnect() {
        // Encapsulates a beacon identifier of arbitrary byte length
        ArrayList<Identifier> identifiers = new ArrayList<>();

        // Set null to indicate that we want to match beacons with any value
        identifiers.add(null);
        // Represents a criteria of fields used to match beacon
        Region region = new Region("BeaconID", identifiers);
        try {
            // Tells the BeaconService to start looking for beacons that match the passed Region object
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        // Specifies a class that should be called each time the BeaconService gets ranging data, once per second by default
        mBeaconManager.addRangeNotifier(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
        if (collection.size() > 0) {
            Log.i(TAG, collection.iterator().next().getIdentifier(0).toString());
        }
    }
}
