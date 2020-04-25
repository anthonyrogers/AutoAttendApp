package com.example.autoattendapp;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.transition.Transition;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.Arrays;
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
    public static final int DB_ERROR = -1;
    public static final int DB_SUCCESS = 1;
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

    // define classes db variables
    public final static String COURSE = "course";
    public final static String DOC_CLASSES = "classes";
    public final static String CLASSROOM = "classroom";
    public final static String COURSE_CODE = "code";
    public final static String STUDENTS = "students";
    public final static String CLASS_MTGS = "meetings";
    public final static String START_TIME = "startTime";
    public final static String END_TIME = "endTime";
    public final static String START_DAY = "start_day";
    public final static String END_DAY = "end_day";
    public final static String TEACH_ID = "teachID";
    public final static String DURATION = "duration";
    public final static String PAST_MEETINGS = "pastMeetings";

    // define attendance db variables
    public final static String DOC_ATTEND = "attendance";
    public final static String CLASS_ID = "classID";
    public final static String STUDENT_ID = "studentID";
    public final static String FIRST_NAME = "firstName";
    public final static String LAST_NAME = "lastName";
    public final static String DATES = "dates";
    public final static String DATE = "date";
    public final static String WEEKDAY = "weekday";
    public final static String TIME_IN = "timeIn";
    public final static String TIME_OUT = "timeOut";
    public final static String TIMES = "times";



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

    /**
     * This function takes in the information about a student and adds them to the database
     *
     * @param authID - The Firebase Auth ID of the user
     * @param firstName - The student's first name
     * @param lastName - The student's last name
     * @param email - The student's email
     */
    public void addStudent(String authID, String firstName, String lastName, String email) {
        Map<String, Object> docMap = new HashMap<>();
        docMap.put(FIRSTNAME, firstName);
        docMap.put(LASTNAME, lastName);
        docMap.put(EMAIL, email);
        docMap.put(USERTYPE, User.STUDENT);
        docMap.put(CLASSES, new ArrayList<String>());
        database.collection(DOC_USERS).document(authID).set(docMap);
    }

    /**
     * This function takes in the information about a teacher and adds them to the database
     *
     * @param authID - The Firebase Auth ID of the user
     * @param firstName - The teacher's first name
     * @param lastName - The teacher's last name
     * @param email - The teacher's email
     * @param beaconID - The teacher's beacon ID
     */
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
    /**
     * This function loads a User object from the given ID
     * and sends the object to the calling handler
     *
     * @param uID - the Firebase Auth ID of the User
     * @param handler - a callback handler
     */
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
                    msg.arg1 = DB_ERROR;
                    handler.sendMessage(msg);
                }
            }
        });
    }


    public void getClassList() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            String userUid = firebaseUser.getUid();
            DocumentReference userRef = database.collection(DOC_USERS).document(userUid);
            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    classes = (List<String>) documentSnapshot.get(DOC_CLASSES);
                    Log.d("class", classes.get(0));
                }
            });
        }
    }

    //check student class code, if class exists call addStudentToClass
    public void checkClassCode(String classCode, final Context context) {
        database.collection(DOC_CLASSES)
                .whereEqualTo(COURSE_CODE, classCode)
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

    /**
     * This function adds a class to the students document
     *
     * @param classID - the ID of the class to be added to
     * @param context - the context of the activity calling the function
     */
    public void addStudentToClass(final String classID, final Context context) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String userUid = firebaseUser.getUid();
        DocumentReference userRef = database.collection(DOC_USERS).document(userUid);
        userRef.update(
                DOC_CLASSES, FieldValue.arrayUnion(classID)
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

        database.collection(DOC_CLASSES).document(classID)
                .update(STUDENTS, FieldValue.arrayUnion(userUid))
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
        DocumentReference userRef = database.collection(DOC_CLASSES).document(classID);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    ArrayList<HashMap<String, String>> name = (ArrayList<HashMap<String, String>>) task.getResult().get(CLASS_MTGS);
                    for(HashMap<String, String> meeting : name){
                      //this is for the dates and creating pending intents for each class
                        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
                        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
                          /*  this block will convert the hours in the database to military time and then set a calendar
                              to that date which will be passed along to the alarm manager after creating the pending intnets
                              the first block is for the start time and the second block is for the endtime. I generate the
                              unique ids for the request code of the intents and then save them in shared preferences. This will delete
                              the pending intents if a user removes the class from their list. */
                        try {
                            Calendar now = Calendar.getInstance();
                            now.set(Calendar.SECOND, 0);
                            now.set(Calendar.MILLISECOND, 0);

                            Date date = parseFormat.parse(meeting.get(START_TIME));
                            String time[] = displayFormat.format(date).split(":");
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(System.currentTimeMillis());
                            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
                            calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
                            calendar.set(Calendar.DAY_OF_WEEK, findDayOfWeek(meeting.get(WEEKDAY)));
                            if (calendar.before(now)) {    //this condition is used for future reminder that means your reminder not fire for past time
                                calendar.add(Calendar.DATE, 7);
                            }
                            int requestcode = generateRandomNumber();

                            //setup for startup time
                            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                            Intent i = new Intent(context, ServiceForBeacon.class);
                            i.setAction("start");
                            i.putExtra("ClassID", classID);
                            PendingIntent pi = PendingIntent.getForegroundService(context, requestcode, i, 0);
                            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pi);
                            Log.i("MEETING TIMES START list ==>", "" + calendar.getTimeInMillis());


                            Date date2 = parseFormat.parse(meeting.get(END_TIME));
                            String time2[] = displayFormat.format(date2).split(":");
                            Calendar calendar2 = Calendar.getInstance();
                            calendar2.setTimeInMillis(System.currentTimeMillis());
                            calendar2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time2[0]));
                            calendar2.set(Calendar.MINUTE, Integer.parseInt(time2[1]));
                            calendar2.set(Calendar.DAY_OF_WEEK, findDayOfWeek(meeting.get(WEEKDAY)));

                            if (calendar2.before(now)) {    //this condition is used for future reminder that means your reminder not fire for past time
                                calendar2.add(Calendar.DATE, 7);
                            }

                            Intent intent = new Intent(context, ServiceForBeacon.class);
                            intent.putExtra("ClassID", classID);
                            intent.setAction("stop");
                            PendingIntent pi2 = PendingIntent.getForegroundService(context, requestcode, intent, 0);
                            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pi2);
                            Log.i("MEETING TIMES START list ==>", "" + calendar2.getTimeInMillis());

                            //We only need to store one request code id for both start and end intents because we
                            //set and a different action to each one which makes that intent filterable and unique
                            SharedPreferences sharedPref = context.getSharedPreferences(INTENT_FILE_KEY, Context.MODE_PRIVATE);
                            Set<String> set = new HashSet<>(sharedPref.getStringSet(classID, new HashSet<String>()));
                            set.add(requestcode + "");
                            sharedPref.edit().putStringSet(classID,set).apply();

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
    private String getCurrentDate(){
        DateFormat df = new SimpleDateFormat("EEE, MM/dd/yyyy");
        String date = df.format(Calendar.getInstance().getTime());
        Log.i("CURRENT DATE", date);
        return date;
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
            case  MeetingOfClass.MONDAY:
                return 1;
            case MeetingOfClass.TUESDAY:
                return 2;
            case MeetingOfClass.WEDNESDAY:
                return 3;
            case MeetingOfClass.THURSDAY:
                return 4;
            case MeetingOfClass.FRIDAY:
                return 5;
            case MeetingOfClass.SATURDAY:
                return 6;
            case MeetingOfClass.SUNDAY:
                return 7;
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
        final Map<String, Object> mapClass = new HashMap<String, Object>();
        mapClass.put(COURSE, course);
        mapClass.put(CLASSROOM, classroom);
        mapClass.put(START_DAY, startDay);
        mapClass.put(END_DAY, endDay);
        mapClass.put(STUDENTS, new ArrayList<String>());
        mapClass.put(TEACH_ID, userUid);
        mapClass.put(CLASS_MTGS, meetingList);
        mapClass.put(PAST_MEETINGS, new ArrayList<String>());
        Log.d("size", String.valueOf(meetingList.size()));
        Log.d("day", meetingList.get(0).weekday);

        //map of weekday to duration
        Map<String, String> duration = new HashMap<>();
        for (int i = 0; i < meetingList.size(); i++) {
            MeetingOfClass meeting = meetingList.get(i);
            String weekday = meeting.weekday;
            String weekdayAbbreviation = weekday.substring(0, 3);
            SimpleDateFormat format = new SimpleDateFormat("HH:mm aa");
            Date in = null;
            try {
                in = format.parse(meeting.startTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date out = null;
            try {
                out = format.parse(meeting.endTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long difference = out.getTime() - in.getTime();
            difference = difference/1000/60;
            Log.d("Difference", String.valueOf(difference));
            duration.put(weekdayAbbreviation, String.valueOf(difference));
        }
        mapClass.put("duration", duration);

        //generate class code
        final int min = 100000;
        final int max = 999999;
        final int random = new Random().nextInt((max - min) + 1) + min;
        String code = String.valueOf(random);
        mapClass.put(COURSE_CODE, code);
        DocumentReference docRef = database.collection(DOC_ATTEND).document();
        String myId = docRef.getId();
        // Add a new class
        database.collection(DOC_CLASSES)
                .add(mapClass)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("addClassToTeacher ==>","added a class: "+documentReference.getId());
                        /*for (int i=0; i< meetingList.size(); i++){
                            addMeetingsToClass(documentReference.getId(),
                                    meetingList.get(i).weekday,
                                    meetingList.get(i).startTime,
                                    meetingList.get(i).endTime);
                        }*/
                        DocumentReference userRef = database.collection(DOC_USERS).document(userUid);
                        userRef.update(
                                CLASSES, FieldValue.arrayUnion(documentReference.getId())
                        ).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    //Log.d("DBManager", "added class to user");
                                    Handler handler = MyGlobal.getInstance().handlerCourseListAcitviey;
                                    Message msg = Message.obtain();
                                    msg.arg1 = CourseListActivity.MsgType_FreshList;
                                    handler.sendMessage(msg);
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
        //addSkeletonAttendance(myId, startDay, endDay, meetingList);
    }


    /**
     * This function adds the empty attendance document as a framework
     * once a classs has been added to the database
     *
     * @param classID - the ID of the class to be added
     * @param startDay - the start date of the class
     * @param endDay - the end date of the class
     * @param meetingList - ArrayList of MeetingOfClass objects
     */
    private void addSkeletonAttendance(String classID, String startDay, String endDay, final ArrayList<MeetingOfClass> meetingList) {
        final Map<String, Object> attendanceMap = new HashMap<>();
        attendanceMap.put(CLASS_ID, classID);
        LocalDate start = getDateFromString(startDay);
        LocalDate end = getDateFromString(endDay);
        List<String> allDates = getAllDates(start, end, meetingList);
        List<Map<String, Object>> dates = new ArrayList<>();
        for(String date: allDates) {
            Map<String, Object> currentDate = new HashMap<>();
            currentDate.put(DATE, date);
            currentDate.put(STUDENTS, new ArrayList<HashMap<String, String>>());
            dates.add(currentDate);
        }
        attendanceMap.put(DATES, dates);
        database.collection(DOC_ATTEND)
                .add(attendanceMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(!task.isSuccessful()){
                    Log.d("DBManager", "Error: failed to add attendance map", task.getException());
                }
            }
        });
    }


    /**
     * This function returns a list of strings representing the
     * dates the class will occur up to and including the end date
     *
     * @param start - the start date of the class
     * @param end - the end date of the class
     * @param meetings - ArrayList of MeetingOfClass objects
     * @return - List of dates between start/end
     */
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

    /**
     * This function takes in a String of format MM/DD/YYYY
     * and returns a LocalDate object representation of it
     *
     * @param date - the String representation of the date
     * @return - LocalDate object from the date
     */
    private LocalDate getDateFromString(String date) {
        String[] tokenized = date.split("/");
        int[] dmy = new int[3];
        for(int i=0; i < 3; i++) {
            dmy[i] = (int) Integer.valueOf(tokenized[i]);
        }
        return LocalDate.of(dmy[2], dmy[0], dmy[1]);
    }

    /**
     * This function takes in a LocalDate object and returns
     * a string in the format of MM/DD/YYYYY
     *
     * @param date - the LocalDate object
     * @return - String representation of the date (MM/DD/YYYY)
     */
    private String getStringFromDate(LocalDate date){
        String[] tokenized = date.toString().split("-");
        String year = tokenized[0];
        String month = tokenized[1];
        String day = tokenized[2];
        return String.join("/", month, day, year);
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

    // get class info by class id to courseListActivity
    public void getClassInfoById(final CourseListActivity owner, final String id){
        database.collection(CLASSES).document(id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    String name = task.getResult().getString(COURSE);
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
                    String course = task.getResult().getString(COURSE);
                    String classroom = task.getResult().getString(CLASSROOM);
                    String start_day = task.getResult().getString(START_DAY);
                    String end_day = task.getResult().getString(END_DAY);
                    ArrayList<AddClassContent.MeetingInfo> meetings = new ArrayList<AddClassContent.MeetingInfo>();
                    ArrayList<HashMap<String, String>> names = (ArrayList<HashMap<String, String>>) task.getResult().get(CLASS_MTGS);
                    for(HashMap<String, String> meeting : names){
                        AddClassContent.MeetingInfo meetingInfo = new AddClassContent.MeetingInfo(meeting.get(WEEKDAY),
                                meeting.get(START_TIME), meeting.get(END_TIME));
                        meetings.add(meetingInfo);
                    }
                    owner.getClassInfoFromDB(true, course, classroom, start_day, end_day, meetings);
                } else {
                    owner.getClassInfoFromDB(false,null, null, null, null,null);
                }
            }
        });
    }

    // add a class to teacher account
    public void ModifyClassOfTeacher(String classID, String course, String classroom, String startDay, String endDay,
                                  final ArrayList<MeetingOfClass> meetingList){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser == null)
            return;
        DocumentReference classes = database.collection(DOC_CLASSES).document(classID);
        Map<String, String> duration = new HashMap<>();
        for (int i = 0; i < meetingList.size(); i++) {
            MeetingOfClass meeting = meetingList.get(i);
            String weekday = meeting.weekday;
            String weekdayAbbreviation = weekday.substring(0, 3);
            SimpleDateFormat format = new SimpleDateFormat("HH:mm aa");
            Date in = null;
            try {
                in = format.parse(meeting.startTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date out = null;
            try {
                out = format.parse(meeting.endTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long difference = out.getTime() - in.getTime();
            difference = difference/1000/60;
            Log.d("Difference", String.valueOf(difference));
            duration.put(weekdayAbbreviation, String.valueOf(difference));
        }
        CollectionReference meeting = classes.collection(CLASS_MTGS);
        classes.update(COURSE, course);
        classes.update(CLASSROOM, classroom);
        classes.update(START_DAY, startDay);
        classes.update(END_DAY, endDay);
        classes.update("duration", duration);
        classes.update(CLASS_MTGS, meetingList)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            //Log.d("DBManager ===>", "modified class");
                            Handler handler = MyGlobal.getInstance().handlerCourseListAcitviey;
                            Message msg = Message.obtain();
                            msg.arg1 = CourseListActivity.MsgType_FreshList;
                            handler.sendMessage(msg);
                        } else {
                            Log.d("DBManager", "Error: fail to add user to class", task.getException());
                        }
                    }
                });
    }

    public void deleteClassTeacher(final String id) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final String userUid = firebaseUser.getUid();
        DocumentReference userRef = database.collection(DOC_USERS).document(userUid);

        //get students in class, then delete class from students class list, then delete class doc
        database.collection(DOC_CLASSES).document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> studentList = (ArrayList<String>) document.get(STUDENTS);
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

    public void deleteClassStudent(String classID, Context context){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final String userUid = firebaseUser.getUid();

        deleteClassFromUser(classID, userUid);
        deleteStudentFromClass(classID, userUid);


        //this is retrieving the Intent unique ids that were created when the student added the class
        //and this will tell the system to no longer send those pending intents.
        SharedPreferences sharedPref = context.getSharedPreferences(INTENT_FILE_KEY, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(sharedPref.getStringSet(classID, new HashSet<String>()));
        for(String str : set){
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, ServiceForBeacon.class);
            intent.putExtra("ClassID", classID);
            intent.setAction("stop");
            PendingIntent pi2 = PendingIntent.getForegroundService(context, Integer.parseInt(str), intent, PendingIntent.FLAG_CANCEL_CURRENT);
            pi2.cancel();
            am.cancel(pi2);

            Intent intent2 = new Intent(context, ServiceForBeacon.class);
            intent2.putExtra("ClassID", classID);
            intent2.setAction("start");
            PendingIntent pi = PendingIntent.getForegroundService(context, Integer.parseInt(str), intent2, PendingIntent.FLAG_CANCEL_CURRENT);
            pi.cancel();
            am.cancel(pi);
            Log.i("INTENTS CANCELED", "FOR " + classID + " CLASS");
        }
    }

    public void deleteClassFromUser(String classID, String userID) {
        database.collection(DOC_USERS).document(userID)
                .update(CLASSES, FieldValue.arrayRemove(classID))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Log.d("database", "class successfully deleted from user!");
                        Handler handler = MyGlobal.getInstance().handlerCourseListAcitviey;
                        Message msg = Message.obtain();
                        msg.arg1 = CourseListActivity.MsgType_FreshList;
                        handler.sendMessage(msg);
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
        database.collection(DOC_CLASSES).document(id)
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
        database.collection(DOC_CLASSES).document(classID)
                .update(STUDENTS, FieldValue.arrayRemove(studentID))
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
                    String code = "Class code: " + task.getResult().getString(COURSE_CODE);
                    Toast.makeText(context, code, Toast.LENGTH_LONG).show();
                } else {
                    String code = "Code does not exist";
                    Toast.makeText(context, code, Toast.LENGTH_LONG).show();
                }
            }
        });
    }



    /**
     * This method retrieves a list of students and their attendances from a given
     * class at a given time, and send the array to a handler on success
     *
     * @param handler - the callback handler used to send the result
     * @param classID - the id of the class being shown
     * @param date - the date to show the attendances
     */
    public void getStudentsAttendance(final Handler handler, String classID, String date) {
        database.collection(DOC_ATTEND)
                .whereEqualTo(DATE, date)
                .whereEqualTo(CLASS_ID, classID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Message msg = Message.obtain();
                        if(!task.isSuccessful()) {
                            msg.what = DB_ERROR;
                            msg.obj = task.getException().toString();
                            handler.sendMessage(msg);
                            return;
                        }
                        List<AttendanceRecord> attendances = new ArrayList<>();
                        for(QueryDocumentSnapshot document: task.getResult()){
                            String classID = (String) document.get(CLASS_ID);
                            String date = (String) document.get(DATE);
                            String firstName = (String) document.get(FIRST_NAME);
                            String lastName = (String) document.get(LAST_NAME);
                            String studentID = (String) document.get(STUDENT_ID);
                            ArrayList<Map<String, String>> times = (ArrayList<Map<String, String>>) document.get(TIMES);
                            attendances.add(new AttendanceRecord(classID, date, firstName, lastName, studentID, times));
                        }
                        msg.what = DB_SUCCESS;
                        msg.obj = attendances;
                        handler.sendMessage(msg);
                    }
                });
    }


    /**
     * This function gets a List of past meetings and sends the resulting
     * arraylist to the calling handler
     *
     * @param handler - callback handler to send List of past meetings on success
     * @param classID - the class to get the past meetings
     */

    public void getDateListForClass(final Handler handler, final String classID) {
        database.collection(DOC_CLASSES).document(classID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Object[] handlerObjects = new Object[3];
                        Message msg = Message.obtain();
                        if(!task.isSuccessful()){
                            msg.what = DB_ERROR;
                            handler.sendMessage(msg);
                            return;
                        }
                        List<String> pastMeetings = (ArrayList<String>) task.getResult().get(PAST_MEETINGS);
                        String className = (String) task.getResult().get(COURSE);
                        //System.out.println(Arrays.toString(pastMeetings.toArray()));
                        msg.what = DB_SUCCESS;
                        handlerObjects[0] = pastMeetings;
                        handlerObjects[1] = classID;
                        handlerObjects[2] = className;
                        msg.obj = handlerObjects;
                        handler.sendMessage(msg);
                    }
                });
    }

    public void getStudentRecord(final Handler handler, String classID, String studentID, String date) {
        database.collection(DOC_ATTEND)
                .whereEqualTo(DATE, date)
                .whereEqualTo(CLASS_ID, classID)
                .whereEqualTo(STUDENT_ID, studentID)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Message msg = Message.obtain();
                        if(!task.isSuccessful()){
                            msg.what = DB_ERROR;
                            handler.sendMessage(msg);
                            return;
                        }
                        for(QueryDocumentSnapshot document : task.getResult()) {
                            String classID = (String) document.get(CLASS_ID);
                            String date = (String) document.get(DATE);
                            String firstName = (String) document.get(FIRST_NAME);
                            String lastName = (String) document.get(LAST_NAME);
                            String studentID = (String) document.get(STUDENT_ID);
                            ArrayList<Map<String, String>> times = (ArrayList<Map<String, String>>) document.get(TIMES);
                            AttendanceRecord record = new AttendanceRecord(classID, date, firstName, lastName, studentID, times);
                            msg.what = DB_SUCCESS;
                            msg.obj = record;
                            handler.sendMessage(msg);
                            return;
                        }
                        msg.what = DB_ERROR;
                        handler.sendMessage(msg);
                    }
                });
    }


    /**
     * This method gets the class duration for the specific day of week
     * and sends it to a callback handler
     *
     * @param handler - the callback handler to send message
     * @param classID - the ID of the class
     * @param date - the date the class occurred
     */
    public void getClassDuration(final Handler handler, final String classID, final String date) {
        database.collection(DOC_CLASSES).document(classID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Message msg = Message.obtain();
                        if(!task.isSuccessful()) {
                            msg.what = DB_ERROR;
                            handler.sendMessage(msg);
                            return;
                        }
                        Map<String, String> durationMap = (Map<String, String>) task.getResult().get(DURATION);
                        if(durationMap == null) {
                            msg.what = DB_ERROR;
                            handler.sendMessage(msg);
                            return;
                        }
                        String dayOfWeek = date.substring(0, 3);
                        String classDuration = durationMap.get(dayOfWeek);
                        Long classDur = Long.parseLong(classDuration);
                        msg.obj = classDur;
                        msg.what = DB_SUCCESS;
                        handler.sendMessage(msg);
                    }
                });
    }

    /**
     * This function calculates the total amount of time spent in a classroom
     *
     * @param totalTime - array of Map: timeIn: timeOut for a single attendance
     * @return total: double - total amount of time student spent in class
     */
    public static long getStudentDuration(ArrayList<Map<String, String>> totalTime) {
        long total = 0;
        for(int i=0; i < totalTime.size(); i++) {
            if(totalTime.get(i).get(TIME_IN) == null || totalTime.get(i).get(TIME_OUT) == null) {
                break;
            }
            SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a");
            SimpleDateFormat parseFormat = new SimpleDateFormat("HH:mm");
            String timeIn = totalTime.get(i).get(TIME_IN);
            String timeOut = totalTime.get(i).get(TIME_OUT);

            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            Date in = null;
            try {
                in = format.parse(timeIn);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date out = null;
            try {
                out = format.parse(timeOut);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            long difference = out.getTime() - in.getTime();
            difference = difference/1000/60;
            total = total + difference;
        }
        return total;
    }

    //finds the attendance document ID by date and studentID
    //updates the document with the time out
    public void markTimeOut(String classID, String date, final String timeOut) {
        final FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser == null)
            return;
        final String studentID = firebaseUser.getUid();

        database.collection(DOC_ATTEND)
                .whereEqualTo(STUDENT_ID, studentID)
                .whereEqualTo(CLASS_ID, classID)
                .whereEqualTo(DATE, date)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String attendanceID = document.getId();
                                DocumentReference attendanceRef = database.collection(DOC_ATTEND).document(attendanceID);
                                attendanceRef
                                        .update(TIME_OUT, timeOut)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("time out", "logged");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("time out", "could not be logged", e);
                                            }
                                        });
                            }
                        } else {
                            Log.d("database", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
