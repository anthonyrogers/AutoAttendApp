package com.example.autoattendapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TeacherActivity extends AppCompatActivity {

    private Button mAddMeetingBtn, mAddClassBtn, mDelMeetingBtn;
    private Button mUpdateMeetingBtn, mBeaconBtn;
    private Spinner mClassSpinner;
    private Spinner mMeetingSpinner;
    private List<ClassOfTeacher> mClassesList;
    private List<MeetingOfClass> mMeetingList;
    private String TAG = "TeacherActivity ===>";

    //for dialog of Adding class
    private AlertDialog mAddClassDlg;
    private TextView mEditTextName;
    private TextView mEditTextCourse;
    private TextView mEditTextSemester;

    // for dialog of adding meeting
    private AlertDialog mAddMeetingDlg;
    private TextView mTVBeaconID;
    private TextView mTVClassRoom;
    private TextView mTVClasssTime;
    private TextView mTVWeekday;

    // meeting detail
    private TextView mSemesterTV;
    private TextView mCourseTV;
    private TextView mClassNameTV;
    private TextView mClassroomTV;
    private TextView mWeekdayTV;
    private TextView mClasstimeTV;
    private TextView mBeaconIdTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        mAddClassBtn = findViewById(R.id.addClassBtn);
        mAddMeetingBtn = findViewById(R.id.addMeetingBtn);
        mDelMeetingBtn = findViewById(R.id.delMeetingBtn);
        mUpdateMeetingBtn = findViewById(R.id.updateMeetingBtn);
        mBeaconBtn = findViewById(R.id.beaconBtn);
        mClassSpinner = findViewById(R.id.classSpinner);
        mClassSpinner.setOnItemSelectedListener(new ClassOnItemSelectedListener());
        mMeetingSpinner = findViewById(R.id.meetingSpinner);
        mMeetingSpinner.setOnItemSelectedListener(new MeetingOnItemSelectedListener());

        mClassesList = new ArrayList<ClassOfTeacher>();
        mMeetingList = new ArrayList<MeetingOfClass>();
        getClassOfTeacher();

        mAddClassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowNewClassDialog();
            }
        });

        mAddMeetingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowNewMeetingDialog(false);
            }
        });

        mDelMeetingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteSelectedMeeting();
            }
        });
        mUpdateMeetingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowNewMeetingDialog(true);
            }
        });

        mBeaconBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectToBeacon();
            }
        });

        mSemesterTV = findViewById(R.id.semesterTV);
        mCourseTV = findViewById(R.id.courseTV);
        mClassNameTV = findViewById(R.id.classNameTV);
        mClassroomTV = findViewById(R.id.classroomTV);
        mWeekdayTV = findViewById(R.id.weekdayTV);
        mClasstimeTV = findViewById(R.id.classtimeTV);
        mBeaconIdTV = findViewById(R.id.beaconIdTV);
    }

    private void ShowNewClassDialog(){
        //Log.d(TAG,"show dialog add class");
        AlertDialog.Builder builder = new AlertDialog.Builder(TeacherActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_class, null);
        mEditTextName = (TextView) view.findViewById(R.id.editTextName);
        mEditTextCourse = (TextView) view.findViewById(R.id.editTextCourse);
        mEditTextSemester = (TextView) view.findViewById(R.id.editTextSemester);
        Button btnOK = (Button) view.findViewById(R.id.buttonOK);
        TextView textView = (TextView) view.findViewById(R.id.textView);

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addClassOfTeacher();
                mAddClassDlg.cancel();
            }
        });

        builder.setView(view);
        mAddClassDlg = builder.create();
        mAddClassDlg.show();
    }

    private void ShowNewMeetingDialog(final boolean bUpdate){
        AlertDialog.Builder builder = new AlertDialog.Builder(TeacherActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialgo_add_meeting, null);
        mTVBeaconID = (TextView) view.findViewById(R.id.etBeaconID);
        mTVClassRoom = (TextView) view.findViewById(R.id.etClassroom);
        mTVClasssTime = (TextView) view.findViewById(R.id.etClasstime);
        mTVWeekday = (TextView) view.findViewById(R.id.etWeekday);
        Button btnOK = (Button) view.findViewById(R.id.buttonOK);
        TextView textView = (TextView) view.findViewById(R.id.textView);

        if(!bUpdate) {
            Long index = mClassSpinner.getSelectedItemId();
            if (index < 0) {
                Toast.makeText(getApplicationContext(), "please select a class first.", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            Long index = mClassSpinner.getSelectedItemId();
            if (index < 0) {
                Toast.makeText(getApplicationContext(), "please select a meeting first.", Toast.LENGTH_LONG).show();
                return;
            }
            MeetingOfClass meetingOfClass = mMeetingList.get(index.intValue());
            mTVBeaconID.setText(meetingOfClass.beaconID);
            mTVClassRoom.setText(meetingOfClass.classroom);
            mTVClasssTime.setText(meetingOfClass.classtime);
            mTVWeekday.setText(meetingOfClass.weekday);
            textView.setText("Update meeting");
            btnOK.setText("Update");
        }
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bUpdate)
                    UpdateSelectedMeeting();
                else
                    addMeetingOfClass();
                mAddMeetingDlg.cancel();
            }
        });
        builder.setView(view);
        mAddMeetingDlg = builder.create();
        mAddMeetingDlg.show();
    }

    private void DeleteSelectedMeeting(){
        Long index = mMeetingSpinner.getSelectedItemId();
        if(index<0){
            Toast.makeText(getApplicationContext(), "please select a meeting first.", Toast.LENGTH_LONG).show();
            return;
        }
        MeetingOfClass meetingOfClass = mMeetingList.get(index.intValue());
        Log.d(TAG,"meeting id: "+meetingOfClass.meetingID);
        FirebaseFirestore database = MyGlobal.getInstance().gDB;
        database.collection("meeting").document(meetingOfClass.meetingID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "meeting successfully deleted!");
                        getMeetingOfClass();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    private void UpdateSelectedMeeting(){
        Long index = mMeetingSpinner.getSelectedItemId();
        if(index<0){
            Toast.makeText(getApplicationContext(), "please select a meeting first.", Toast.LENGTH_LONG).show();
            return;
        }
        MeetingOfClass meetingOfClass = mMeetingList.get(index.intValue());
        Log.d(TAG,"meeting id: " + meetingOfClass.meetingID);
        FirebaseFirestore database = MyGlobal.getInstance().gDB;
        DocumentReference meeting = database.collection("meeting").document(meetingOfClass.meetingID);
        meeting.update("beaconID",mTVBeaconID.getText().toString());
        meeting.update("classroom",mTVClassRoom.getText().toString());
        meeting.update("classtime",mTVClasssTime.getText().toString());
        meeting.update("weekday", mTVWeekday.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "meeting successfully updated!");
                        getMeetingOfClass();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating meeting", e);
                    }
                });
    }
    private void addMeetingOfClass(){
        String str = mTVClassRoom.getText().toString();
        if(str.length() == 0){
            Log.d(TAG,"class room can't be empty");
            return;
        }
        FirebaseFirestore database = MyGlobal.getInstance().gDB;
        Long index = mClassSpinner.getSelectedItemId();
        ClassOfTeacher classOfTeacher = mClassesList.get(index.intValue());
        // Create a new user
        Map<String, Object> mapMeeting = new HashMap<>();
        mapMeeting.put("beaconID", mTVBeaconID.getText().toString());
        mapMeeting.put("classroom", mTVClassRoom.getText().toString());
        mapMeeting.put("classtime", mTVClasssTime.getText().toString());
        mapMeeting.put("weekday", mTVWeekday.getText().toString());
        mapMeeting.put("classID", classOfTeacher.classID);

        // Add a new document with a generated ID
        database.collection("meeting")
                .add(mapMeeting)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG,"added a meeting.");
                        getMeetingOfClass();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error: fail to adding class", e);
                    }
                });
    }

    private void addClassOfTeacher(){
        String str = mEditTextName.getText().toString();
        if(str.length() == 0){
            Log.d(TAG,"class name can't be empty");
            return;
        }
        FirebaseFirestore database = MyGlobal.getInstance().gDB;
        String userId = MyGlobal.getInstance().gUser.getID();
        // Create a new user
        Map<String, Object> mapClass = new HashMap<>();
        mapClass.put("class", mEditTextName.getText().toString());
        mapClass.put("course", mEditTextCourse.getText().toString());
        mapClass.put("semester", mEditTextSemester.getText().toString());
        mapClass.put("teachID", userId);

        //generate random class code
        final int min = 100000;
        final int max = 999999;
        final int random = new Random().nextInt((max - min) + 1) + min;
        mapClass.put("code", random);

        // Add a new document with a generated ID
        database.collection("classes")
                .add(mapClass)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG,"added a class.");
                        getClassOfTeacher();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error: fail to adding class", e);
                    }
                });


    }
    private void getClassOfTeacher(){
        String userId = MyGlobal.getInstance().gUser.getID();
        FirebaseFirestore database = MyGlobal.getInstance().gDB;
        database.collection("classes")
                .whereEqualTo("teachID", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        String classID = "", className = "", course = "", semester = "";
                        ClassOfTeacher classOfTeacher;
                        mClassesList.clear();
                        List<String> list = new ArrayList<String>();
                        for (QueryDocumentSnapshot snap : documentSnapshots) {
                            Log.d(TAG, snap.getId() + " => " + snap.getData());
                            className = snap.getData().get("class").toString();
                            course = snap.getData().get("course").toString();
                            semester = snap.getData().get("semester").toString();
                            classID = snap.getId();
                            Log.d(TAG, "class id: " + classID);
                            list.add(className);
                            classOfTeacher = new ClassOfTeacher();
                            classOfTeacher.classID = classID;
                            classOfTeacher.className = className;
                            classOfTeacher.course = course;
                            classOfTeacher.semester = semester;
                            mClassesList.add(classOfTeacher);
                        }

                        if (mClassesList.size() > 0) {
                            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(),
                                    android.R.layout.simple_spinner_item, list);
                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            mClassSpinner.setAdapter(null);
                            mClassSpinner.setAdapter(dataAdapter);
                        } else
                            Toast.makeText(getApplicationContext(), "You don't have class yet.", Toast.LENGTH_LONG).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Failed to authenticate user", e);
                        Toast.makeText(getApplicationContext(), "Failed to connect server.", Toast.LENGTH_LONG).show();
                    }
                });


    }

    private void getMeetingOfClass(){
        Long index = mClassSpinner.getSelectedItemId();
        ClassOfTeacher classOfTeacher = mClassesList.get(index.intValue());
        Log.d(TAG, classOfTeacher.classID + "-" +classOfTeacher.className);

        FirebaseFirestore database = MyGlobal.getInstance().gDB;
        database.collection("meeting")
                .whereEqualTo("classID", classOfTeacher.classID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        String meetingID ="", beaconID = "", classroom = "", classtime = "", weekday = "";
                        String classID = "";
                        MeetingOfClass meetingOfClass;
                        mMeetingList.clear();
                        List<String> list = new ArrayList<String>();
                        for (QueryDocumentSnapshot snap : documentSnapshots) {
                           Log.d(TAG, snap.getId() + " => " + snap.getData());
                            beaconID = snap.getData().get("beaconID").toString();
                            classroom = snap.getData().get("classroom").toString();
                            classtime = snap.getData().get("classtime").toString();
                            weekday = snap.getData().get("weekday").toString();
                            classID = snap.getData().get("classID").toString();
                            meetingID = snap.getId();
                            Log.d(TAG, "meeting id: " + meetingID);
                            list.add(classroom + ", " + classtime);
                            meetingOfClass = new MeetingOfClass();
                            meetingOfClass.beaconID = beaconID;
                            meetingOfClass.classroom = classroom;
                            meetingOfClass.classtime = classtime;
                            meetingOfClass.weekday = weekday;
                            meetingOfClass.classID = classID;
                            meetingOfClass.meetingID = meetingID;
                            mMeetingList.add(meetingOfClass);
                        }
                        mMeetingSpinner.setAdapter(null);
                        if (mMeetingList.size() > 0) {
                            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(),
                                    android.R.layout.simple_spinner_item, list);
                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            mMeetingSpinner.setAdapter(dataAdapter);
                        } else
                            Toast.makeText(getApplicationContext(), "this class don't have meeting yet.", Toast.LENGTH_LONG).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Failed to authenticate user", e);
                        Toast.makeText(getApplicationContext(), "Failed to connect server.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void ConnectToBeacon(){
        Toast.makeText(getApplicationContext(), "test..", Toast.LENGTH_LONG).show();
    }

    private void ShowMeetingDetail(){
        Long index = mClassSpinner.getSelectedItemId();
        ClassOfTeacher classOfTeacher = mClassesList.get(index.intValue());
        index = mMeetingSpinner.getSelectedItemId();
        MeetingOfClass meetingOfClass = mMeetingList.get(index.intValue());

        mSemesterTV.setText(classOfTeacher.semester);
        mCourseTV.setText(classOfTeacher.course);
        mClassNameTV.setText(classOfTeacher.className);
        mClassroomTV.setText(meetingOfClass.classroom);
        mWeekdayTV.setText(meetingOfClass.weekday);
        mClasstimeTV.setText(meetingOfClass.classtime);
        mBeaconIdTV.setText(meetingOfClass.beaconID);
        mBeaconBtn.setEnabled(true);
    }

    private void hideMeetingDetail(){
        mSemesterTV.setText("");
        mCourseTV.setText("");
        mClassNameTV.setText("");
        mClassroomTV.setText("");
        mWeekdayTV.setText("");
        mClasstimeTV.setText("");
        mBeaconIdTV.setText("");
        mBeaconBtn.setEnabled(false);
    }
    // for spinner
    public class ClassOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            //Toast.makeText(parent.getContext(),
            //        "OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
            //        Toast.LENGTH_SHORT).show();
            hideMeetingDetail();
            getMeetingOfClass();
            //Log.d(TAG, parent.getItemAtPosition(pos).toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }

    public class MeetingOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            //Toast.makeText(parent.getContext(),
            //        "OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
            //        Toast.LENGTH_SHORT).show();
            ShowMeetingDetail();
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }

    // for classes list
    public class ClassOfTeacher{
        public String classID;
        public String className;
        public String course;
        public String semester;
    }

    public class MeetingOfClass{
        public String meetingID;
        public String beaconID;
        public String classID;
        public String classroom;
        public String classtime;
        public String weekday;
    }
}
