package com.example.autoattendapp;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    public void loadUser(String uID, final Handler handler) {
        database.collection(DOC_USERS).document(uID)
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
                    User user;
                    if(userType == User.TEACHER) {
                        String beaconID = task.getResult().getString(BEACON);
                        user = new Teacher(firstName, lastName, userID, email, classIDs, beaconID);
                    } else {
                        user = new Student(firstName, lastName, userID, email, classIDs);
                    }
                    msg.arg1 = userType;
                    msg.obj = user;
                    handler.sendMessage(msg);
                } else {
                    Message msg = Message.obtain();
                    msg.arg1 = -1;
                    handler.sendMessage(msg);
                }
            }
        });
    }


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
                    Toast.makeText(context, "Class not added", Toast.LENGTH_SHORT).show();
                }
            }
        });

        database.collection("classes").document(classID)
                .update("students", FieldValue.arrayUnion(userUid))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Log.d("DBManager", "added user to class");
                } else {
                    Log.d("DBManager", "Error: fail to add user to class", task.getException());
                }
            }
        });
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
        mapClass.put("students", new ArrayList<String>());
        mapClass.put("teachID", userUid);
        mapClass.put("meetings", meetingList);

        //generate class code
        final int min = 100000;
        final int max = 999999;
        final int random = new Random().nextInt((max - min) + 1) + min;
        String code = String.valueOf(random);
        mapClass.put("code", code);
        DocumentReference docRef = database.collection("attendance").document();
        String myId = docRef.getId();
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
        // add a meeting associated with the class
        addSkeletonAttendance(myId, startDay, endDay, meetingList);
    }

    private void addSkeletonAttendance(String classID, String startDay, String endDay, final ArrayList<MeetingOfClass> meetingList) {
        final Map<String, Object> attendanceMap = new HashMap<>();
        attendanceMap.put("classID", classID);
        LocalDate start = getDateFromString(startDay);
        LocalDate end = getDateFromString(endDay);
        List<String> allDates = getAllDates(start, end, meetingList);
        List<Map<String, Object>> dates = new ArrayList<>();
        for(String date: allDates) {
            Map<String, Object> currentDate = new HashMap<>();
            currentDate.put("date", date);
            currentDate.put("students", new ArrayList<HashMap<String, String>>());
            dates.add(currentDate);
        }
        attendanceMap.put("dates", dates);
        database.collection("attendance")
                .add(attendanceMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(!task.isSuccessful()){
                    Log.d("DBManager", "Error: failed to add attendance map", task.getException());
                }
            }
        });
    }


    private List<String> getAllDates(LocalDate start, LocalDate end, List<MeetingOfClass> meetings) {
        List<String> allDates = new ArrayList<>();
        for(LocalDate date = start; date.isBefore(end) || date.isEqual(end); date = date.plusDays(1)) {
            int day = date.getDayOfWeek().ordinal() + 1;
            for(MeetingOfClass meeting : meetings) {
                if (MeetingOfClass.getIndexOfWeekDay(meeting.weekday) == day) {
                    allDates.add(getStringFromDate(date));
                    break;
                }
            }
        }
        return allDates;
    }

    private LocalDate getDateFromString(String date) {
        String[] tokenized = date.split("/");
        int[] dmy = new int[3];
        for(int i=0; i < 3; i++) {
            dmy[i] = (int) Integer.valueOf(tokenized[i]);
        }
        return LocalDate.of(dmy[2], dmy[0], dmy[1]);
    }

    private String getStringFromDate(LocalDate date){
        String[] tokenized = date.toString().split("-");
        String year = tokenized[0];
        String month = tokenized[1];
        String day = tokenized[2];
        return String.join("/", month, day, year);
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

    /*
     * for course list only ========================================================================
     */
    // get the class ids of user
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

    // get class info by class id
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

    /*
     * for viewing class only ========================================================================
     */
    // get class info by class id
    public void getClassInfoById(final AddClassContent owner, final String id){
        database.collection(CLASSES).document(id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    String name = task.getResult().getString("course");
                    String classroom = task.getResult().getString("classroom");
                    String start_day = task.getResult().getString("start_day");
                    String end_day = task.getResult().getString("end_day");
                    owner.getClassInfoFromDB(true, name, classroom, start_day, end_day);
                } else {
                    owner.getClassInfoFromDB(false,null, null, null, null);
                }
            }
        });
    }

    // get meeting info by class id
    public void getMeetingsOfClass(final AddClassContent owner, final String id){
        FirebaseFirestore database = MyGlobal.getInstance().gDB;
        database.collection("meetings")
                .whereEqualTo("classId", id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        String weekday ="", startTime="", endTime="";
                        List<AddClassContent.MeetingInfo> list = new ArrayList<AddClassContent.MeetingInfo>();
                        AddClassContent.MeetingInfo meetingInfo;
                        for (QueryDocumentSnapshot snap : documentSnapshots) {
                            Log.d("Get meeting ====>", snap.getId() + " => " + snap.getData());
                            weekday = snap.getData().get("weekday").toString();
                            startTime = snap.getData().get("start_time").toString();
                            endTime = snap.getData().get("end_time").toString();
                            meetingInfo = new AddClassContent.MeetingInfo(weekday,startTime,endTime);
                            list.add(meetingInfo);
                        }
                        owner.getMeetingInfoFromDB(true, list);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        owner.getMeetingInfoFromDB(true, null);
                    }
                });
    }

    public void deleteClassTeacher(final String id) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final String userUid = firebaseUser.getUid();
        DocumentReference userRef = database.collection("users").document(userUid);

        //get students in class, then delete class from students class list, then delete class doc
        database.collection("classes").document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> studentList = (ArrayList<String>) document.get("students");
                        Log.d("STUDENT LIST !!!!!!!!!!", studentList.toString());
                        for(int i=0; i < studentList.size(); i++) {
                            deleteClassFromUser(id, studentList.get(i));
                        }
                        deleteClassDocument(id);
                    }
                }
            }
        });
        //delete class from teacher
        deleteClassFromUser(id, userUid);


    }

    public void deleteClassStudent(String classID) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final String userUid = firebaseUser.getUid();
        deleteClassFromUser(classID, userUid);
        deleteStudentFromClass(classID, userUid);
    }

    public void deleteClassFromUser(String classID, String userID) {
        database.collection("users").document(userID)
                .update("classes", FieldValue.arrayRemove(classID))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("database", "class successfully deleted from user!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("database", "Error deleting class from user", e);
                    }
                });
    }

    public void deleteClassDocument(String id) {
        database.collection("classes").document(id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("database", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("database", "Error deleting document", e);
                    }
                });
    }

    public void deleteStudentFromClass(String classID, String studentID) {
        database.collection("classes").document(classID)
                .update("students", FieldValue.arrayRemove(studentID))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("database", "student successfully deleted from class!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("database", "Error deleting student from class", e);
                    }
                });
    }

    public void getClassCode(String classID, final Context context) {
        database.collection(CLASSES).document(classID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    String code = "Class code: " + task.getResult().getString("code");
                    Toast.makeText(context, code, Toast.LENGTH_LONG).show();
                } else {
                    String code = "Code does not exist";
                    Toast.makeText(context, code, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
