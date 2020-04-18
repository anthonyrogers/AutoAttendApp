package com.example.autoattendapp;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class DBManager {

    public static DBManager dbManager = null;
    FirebaseFirestore database;

    // define User db variables
    private final String INTENT_FILE_KEY = "IntentIDs";
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

    public void addStudentToClass(final String classID, final Context context) {
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
                    getUsersMettings(classID, context);
                } else {
                    Log.d("DBManager", "Error: fail to add user to class", task.getException());
                }
            }
        });
    }


    //this is tacked on the add class calls which will grab the students active class and set a start pending intent
    //and a stop pending intent for every class day. Each pending intent is setup for weekly schedule
    public void getUsersMettings(final String classID, final Context context) {
        DocumentReference userRef = database.collection("classes").document(classID);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    ArrayList<HashMap<String, String>> name = (ArrayList<HashMap<String, String>>) task.getResult().get("meetings");
                    for(HashMap<String, String> meeting : name){
                      //this is for the dates and creating pending intents for each class
                        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
                        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mma");
                          /*  this block will convert the hours in the database to military time and then set a calendar
                              to that date which will be passed along to the alarm manager after creating the pending intnets
                              the first block is for the start time and the second block is for the endtime. I generate the
                              unique ids for the request code of the intents and then save them in shared preferences. This will delete
                              the pending intents if a user removes the class from their list. */
                        try {
                            Date date = parseFormat.parse(meeting.get("startTime"));
                            String time[] = displayFormat.format(date).split(":");
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(System.currentTimeMillis());
                            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
                            calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
                            calendar.set(Calendar.DAY_OF_WEEK, findDayOfWeek(meeting.get("weekday")));
                            int requestcode = generateRandomNumber();

                            //setup for startup time
                            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                            Intent i = new Intent(context, ServiceForBeacon.class);
                            i.setAction("start");
                            i.putExtra("ClassID", classID);
                            PendingIntent pi = PendingIntent.getForegroundService(context, requestcode, i, 0);
                            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pi);
                            Log.i("MEETING TIMES START list ==>", "" + calendar.getTimeInMillis());


                            Date date2 = parseFormat.parse(meeting.get("endTime"));
                            String time2[] = displayFormat.format(date2).split(":");
                            Calendar calendar2 = Calendar.getInstance();
                            calendar2.setTimeInMillis(System.currentTimeMillis());
                            calendar2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time2[0]));
                            calendar2.set(Calendar.MINUTE, Integer.parseInt(time2[1]));
                            calendar2.set(Calendar.DAY_OF_WEEK, findDayOfWeek(meeting.get("weekday")));


                            Intent intent = new Intent(context, ServiceForBeacon.class);
                            intent.putExtra("ClassID", classID);
                            intent.setAction("stop");
                            PendingIntent pi2 = PendingIntent.getForegroundService(context, requestcode, intent, 0);
                            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pi2);
                            Log.i("MEETING TIMES START list ==>", "" + calendar2.getTimeInMillis());

                            //We only need to store one request code id for both start and end intents because we
                            //set and a different action to each one which makes that intent filterable and unique
                            SharedPreferences sharedPref = context.getSharedPreferences(INTENT_FILE_KEY, Context.MODE_PRIVATE);
                            sharedPref.edit().putString(classID, requestcode + "").apply();
                            Log.i("SHARED PREF SAVED",  sharedPref.getString(classID, null));



                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.i("INTENTS", "DID NOT CREATE INTENTS", task.getException());
                }
            }
        });
    }
    private int generateRandomNumber(){
        //this is for creating unique numbers for intents which we will keep track of - Anthony
        final int min = 100000;
        final int max = 999999;
        final int random = new Random().nextInt((max - min) + 1) + min;
        return random;
    }

    private int findDayOfWeek(String dayOfWeek){
        switch (dayOfWeek) {
            case  "Monday":
                return 2;
            case "Tuesday":
                return 3;
            case "Wednesday":
                return 4;
            case "Thursday":
                return 5;
            case "Friday":
                return 6;
            default:
                return 0;
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
        mapClass.put("students", new ArrayList<String>());
        mapClass.put("teachID", userUid);
        mapClass.put("meetings", meetingList);

        //generate class code
        final int min = 100000;
        final int max = 999999;
        final int random = new Random().nextInt((max - min) + 1) + min;
        String code = String.valueOf(random);
        mapClass.put("code", code);

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

    public void deleteClassStudent(String classID, Context context) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final String userUid = firebaseUser.getUid();

        deleteClassFromUser(classID, userUid);
        deleteStudentFromClass(classID, userUid);

        //everything below will removed the pending intents from each class that are saved in shared pref
        SharedPreferences sharedPref = context.getSharedPreferences(INTENT_FILE_KEY, Context.MODE_PRIVATE);
        String savedIntent = sharedPref.getString(classID, null);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ServiceForBeacon.class);
        intent.putExtra("ClassID", classID);
        intent.setAction("stop");
        PendingIntent pi2 = PendingIntent.getForegroundService(context, Integer.parseInt(savedIntent), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        pi2.cancel();
        am.cancel(pi2);

        Intent intent2 = new Intent(context, ServiceForBeacon.class);
        intent2.putExtra("ClassID", classID);
        intent2.setAction("start");
        PendingIntent pi = PendingIntent.getForegroundService(context, Integer.parseInt(savedIntent), intent2, PendingIntent.FLAG_CANCEL_CURRENT);
        pi.cancel();
        am.cancel(pi);
        Log.i("INTENTS CANCELED", "FOR " + classID + " CLASS");

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
