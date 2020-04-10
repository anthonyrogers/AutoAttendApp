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


    // define User db variables
    public final static String DOC_USERS = "users";
    public final static String FIRSTNAME = "firstname";
    public final static String LASTNAME = "lastname";
    public final static String EMAIL = "email";
    public final static String USERTYPE = "usertype";
    public final static String CLASSES = "classes";
    public final static String BEACON = "beacon";

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
        docMap.put(FIRSTNAME, firstName);
        docMap.put(LASTNAME, lastName);
        docMap.put(EMAIL, email);
        docMap.put(USERTYPE, User.STUDENT);
        docMap.put(CLASSES, new ArrayList<String>());
        database.collection(DOC_USERS).document(authID).set(docMap);
    }

    public void addTeacher(String authID, String firstName, String lastName, String email, String beaconID) {
        Map<String, Object> docMap = new HashMap<>();
        docMap.put(FIRSTNAME, firstName);
        docMap.put(LASTNAME, lastName);
        docMap.put(EMAIL, email);
        docMap.put(USERTYPE, User.TEACHER);
        docMap.put(CLASSES, new ArrayList<String>());
        docMap.put(BEACON, beaconID);
        database.collection(DOC_USERS).document(authID).set(docMap);
    }

    public void loadUser(final Handler handler) {
        database.collection(DOC_USERS).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    String firstName = task.getResult().getString(FIRSTNAME);
                    String lastName = task.getResult().getString(LASTNAME);
                    String email = task.getResult().getString(EMAIL);
                    int userType = task.getResult().getLong(USERTYPE).intValue();
                    ArrayList<String> classIDs = (ArrayList<String>) task.getResult().get(CLASSES);
                    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Message msg = Message.obtain();
                    if(userType == User.TEACHER) {
                        String beaconID = task.getResult().getString(BEACON);
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
