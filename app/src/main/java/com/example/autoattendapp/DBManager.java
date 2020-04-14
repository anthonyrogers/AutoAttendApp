package com.example.autoattendapp;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
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
    private List<String> classes;

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
                        Account.setTeacherAccount(new Teacher(firstName, lastName, userID, email, classIDs, beaconID));
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

    public void getClassList() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            String userUid = firebaseUser.getUid();
            DocumentReference userRef = database.collection("users").document(userUid);
            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    classes = (List<String>) documentSnapshot.get("classes");
                    Log.d("class", classes.get(0));
                }
            });
        }
    }


    //check student class code, if class exists call addStudentToClass
    public void checkClassCode(String classCode, final Context context) {
        database.collection("classes")
                .whereEqualTo("code", classCode)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String classID = document.getId();
                                Log.d("ClassID", classID);
                                addStudentToClass(classID, context);
                            }
                        } else {
                            Log.d("ClassCodeError", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void addStudentToClass(String classID, final Context context) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            String userUid = firebaseUser.getUid();
            DocumentReference userRef = database.collection("users").document(userUid);
            userRef.update(
                    "classes", FieldValue.arrayUnion(classID)
            ).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(context, "Class added", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Class not found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // add a class to teacher account
    public void addClassToTeacher(String course, String classroom, String startDay, String endDay,
                                  final ArrayList<MeetingOfClass> meetingList){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser == null)
            return;
        final String userUid = firebaseUser.getUid();
        final Map<String, Object> mapClass = new HashMap<>();
        mapClass.put("course", course);
        mapClass.put("classroom", classroom);
        mapClass.put("start_day", startDay);
        mapClass.put("end_day", endDay);
        //mapClass.put("meetings", meetingIDs);

        // Add a new class
        database.collection("classes")
                .add(mapClass)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("addClassToTeacher ==>","added a class: "+documentReference.getId());
                        for (int i=0; i< meetingList.size(); i++){
                            addMeetingsToClass(documentReference.getId(),
                                    meetingList.get(i).weekday,
                                    meetingList.get(i).startTime,
                                    meetingList.get(i).endTime);
                        }

                        DocumentReference userRef = database.collection("users").document(userUid);
                        userRef.update(
                                "classes", FieldValue.arrayUnion(documentReference.getId())
                        ).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    //Log.d("DBManager", "added class to user");
                                } else {
                                    Log.d("DBManager", "Error: fail to add class to user", task.getException());
                                }
                            }
                        });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("addClassToTeacher ==>", "Error: fail to adding class", e);
                    }
                });
    }

    // add a meeting to its class
    private void addMeetingsToClass(String classId, String weekday, String starttime,String endtime){
        ArrayList<String> meetingIDs = new ArrayList<String>();
        Map<String, Object> mapMeeting = new HashMap<>();
        mapMeeting.put("classId", classId);
        mapMeeting.put("weekday", weekday);
        mapMeeting.put("start_time", starttime);
        mapMeeting.put("end_time", endtime);
        database.collection("meetings")
                .add(mapMeeting)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("addClassToTeacher ==>","added a mapMeeting.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("addClassToTeacher ==>", "Error: fail to adding mapMeeting", e);
                    }
                });

    }

    // return the class ids of user
    public void getClassIdsOfUser(final CourseListActivity owner) {
        database.collection(DOC_USERS).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    ArrayList<String> classIDs = (ArrayList<String>) task.getResult().get(CLASSES);
                    owner.loadClassIdsOfUser(true, classIDs);
                } else {
                    owner.loadClassIdsOfUser(false, null);
                }
            }
        });
    }

    // return class info by class id
    public void getClassInfoById(final CourseListActivity owner, final String id){
        database.collection(CLASSES).document(id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    String name = task.getResult().getString("course");
                    owner.getClassInfoById(true, id, name);
                } else {
                    owner.getClassInfoById(false, null, null);
                }
            }
        });
    }
}
