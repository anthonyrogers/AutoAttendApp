package com.example.autoattendapp;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBManager {

    public static DBManager dbManager = null;
    FirebaseFirestore database;

    private DBManager() {
        database = MyGlobal.getInstance().gDB;
    }

    public static DBManager getInstance(){
        if(dbManager == null) {
            dbManager = new DBManager();
        }
        return dbManager;
    }

    public Student loadStudent(String authID) {
        return null;
    }

    public void addStudent(String authID, String firstName, String lastName, String email) {
        Map<String, Object> docMap = new HashMap<>();
        docMap.put("firstname", firstName);
        docMap.put("lastname", lastName);
        docMap.put("email", email);
        docMap.put("usertype", User.STUDENT);
        docMap.put("classes", new ArrayList<String>());
        database.collection("users").document(authID).set(docMap);
    }

    public void addTeacher(String authID, String firstName, String lastName, String email, String beaconID) {
        Map<String, Object> docMap = new HashMap<>();
        docMap.put("firstname", firstName);
        docMap.put("lastname", lastName);
        docMap.put("email", email);
        docMap.put("usertype", User.TEACHER);
        docMap.put("classes", new ArrayList<String>());
        docMap.put("beacon", beaconID);
        database.collection("users").document(authID).set(docMap);
    }

    public void loadUser(final Handler handler) {
        database.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    String firstName = task.getResult().getString("firstname");
                    String lastName = task.getResult().getString("lastname");
                    String email = task.getResult().getString("email");
                    int userType = task.getResult().getLong("usertype").intValue();
                    ArrayList<String> classIDs = (ArrayList<String>) task.getResult().get("classes");
                    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Message msg = Message.obtain();
                    if(userType == User.TEACHER) {
                        String beaconID = task.getResult().getString("beacon");
                        Account.setTeacherAccount(new Teacher(firstName, lastName, userID, email, classIDs, new Beacon(beaconID)));
                    } else {
                        Account.setStudentAccount(new Student(firstName, lastName, userID, email, classIDs));
                    }
                    msg.arg1 = userType;
                    handler.sendMessage(msg);
                } else {
                    Message msg = Message.obtain();
                    msg.arg1 = -1;
                    handler.sendMessage(msg);
                }
            }
        });
    }

    /*
    public String addClass(String className, String location, Date startDate, Date endDate, Map<DayOfWeek, LocalTime> meetings, Teacher teacher) {
        DocumentReference ref = database.collection("classes").document();
        String classID = ref.getId();
        Map<String, Object> docMap = new HashMap<>();
        docMap.put("class", className);
        docMap.put("location", location);
        docMap.put("")
        return classID;
    }*/


}
