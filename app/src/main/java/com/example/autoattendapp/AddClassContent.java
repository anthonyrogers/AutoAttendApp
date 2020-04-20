package com.example.autoattendapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddClassContent extends AppCompatActivity implements View.OnClickListener,TimePickerDialog.OnTimeSetListener
{

    DatePickerDialog mDatePickerDlg;
    TimePickerDialog mTimePickerDlg;

    private EditText mCourseEText;
    private EditText mClassroomEText;
    private EditText mStartDayEText;
    private EditText mEndDayEText;

    private int iWhichTime;
    private EditText etStartTime1;
    private EditText etEndTime1;
    private EditText etStartTime2;
    private EditText etEndTime2;
    private EditText etStartTime3;
    private EditText etEndTime3;
    private EditText etStartTime4;
    private EditText etEndTime4;
    private EditText etStartTime5;
    private EditText etEndTime5;

    private Spinner spinWeekDay1;
    private Spinner spinWeekDay2;
    private Spinner spinWeekDay3;
    private Spinner spinWeekDay4;
    private Spinner spinWeekDay5;

    Button addClassgBtn;

    private String mClassId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_add_class);

        addClassgBtn = findViewById(R.id.addClassBtn);
        if(getIntent().getExtras() == null) {
            setTitle("Add Class");
        }
        else {
            mClassId = getIntent().getExtras().getString("classId-view");
            if(mClassId != null){
                setTitle("View Class");
                addClassgBtn.setText("Start Class");
                DBManager db = DBManager.getInstance();
                db.getClassInfoById(this, mClassId);
            }else {
                mClassId = getIntent().getExtras().getString("classId-modify");
                if (mClassId != null) {
                    setTitle("Modify Class");
                    addClassgBtn.setText("Modify");
                    DBManager db = DBManager.getInstance();
                    db.getClassInfoById(this, mClassId);
                }
            }
        }

        mStartDayEText = findViewById(R.id.editTextStartDay);
        mStartDayEText.setFocusable(false);
        mStartDayEText.setClickable(true);

        mEndDayEText = findViewById(R.id.editTextEndDay);
        mEndDayEText.setFocusable(false);
        mEndDayEText.setClickable(true);

        mCourseEText = findViewById(R.id.editTextCourse);
        mClassroomEText = findViewById(R.id.editTextLocation);
        spinWeekDay1 = findViewById(R.id.spinWeekDay1);
        spinWeekDay2 = findViewById(R.id.spinWeekDay2);
        spinWeekDay3 = findViewById(R.id.spinWeekDay3);
        spinWeekDay4 = findViewById(R.id.spinWeekDay4);
        spinWeekDay5 = findViewById(R.id.spinWeekDay5);
        //spinWeekDay1.setOnItemSelectedListener(new ClassOnItemSelectedListener());

        iWhichTime = 0;
        etStartTime1 = findViewById(R.id.etStartTime1);
        etStartTime1.setFocusable(false);
        etStartTime1.setClickable(true);
        etStartTime1.setOnClickListener(this);

        etEndTime1 = findViewById(R.id.etEndTime1);
        etEndTime1.setFocusable(false);
        etEndTime1.setClickable(true);
        etEndTime1.setOnClickListener(this);

        etStartTime2 = findViewById(R.id.etStartTime2);
        etStartTime2.setFocusable(false);
        etStartTime2.setClickable(true);
        etStartTime2.setOnClickListener(this);

        etEndTime2 = findViewById(R.id.etEndTime2);
        etEndTime2.setFocusable(false);
        etEndTime2.setClickable(true);
        etEndTime2.setOnClickListener(this);

        etStartTime3 = findViewById(R.id.etStartTime3);
        etStartTime3.setFocusable(false);
        etStartTime3.setClickable(true);
        etStartTime3.setOnClickListener(this);

        etEndTime3 = findViewById(R.id.etEndTime3);
        etEndTime3.setFocusable(false);
        etEndTime3.setClickable(true);
        etEndTime3.setOnClickListener(this);

        etStartTime4 = findViewById(R.id.etStartTime4);
        etStartTime4.setFocusable(false);
        etStartTime4.setClickable(true);
        etStartTime4.setOnClickListener(this);

        etEndTime4 = findViewById(R.id.etEndTime4);
        etEndTime4.setFocusable(false);
        etEndTime4.setClickable(true);
        etEndTime4.setOnClickListener(this);

        etStartTime5 = findViewById(R.id.etStartTime5);
        etStartTime5.setFocusable(false);
        etStartTime5.setClickable(true);
        etStartTime5.setOnClickListener(this);

        etEndTime5 = findViewById(R.id.etEndTime5);
        etEndTime5.setFocusable(false);
        etEndTime5.setClickable(true);
        etEndTime5.setOnClickListener(this);

        List<String> list = new ArrayList<String>();
        list.add("");
        list.add(MeetingOfClass.MONDAY);
        list.add(MeetingOfClass.TUESDAY);
        list.add(MeetingOfClass.WEDNESDAY);
        list.add(MeetingOfClass.THURSDAY);
        list.add(MeetingOfClass.FRIDAY);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinWeekDay1.setAdapter(dataAdapter);
        spinWeekDay2.setAdapter(dataAdapter);
        spinWeekDay3.setAdapter(dataAdapter);
        spinWeekDay4.setAdapter(dataAdapter);
        spinWeekDay5.setAdapter(dataAdapter);


        mStartDayEText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                mDatePickerDlg = new DatePickerDialog(AddClassContent.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                mStartDayEText.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
                            }
                        }, year, month, day);
                mDatePickerDlg.show();
            }
        });

        mEndDayEText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                mDatePickerDlg = new DatePickerDialog(AddClassContent.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                mEndDayEText.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
                            }
                        }, year, month, day);
                mDatePickerDlg.show();
            }
        });

        addClassgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                if(mClassId == null)
                    addClass_onClick();
                else{
                    if(addClassgBtn.getText().toString().equals("Modify"))
                        addClass_onClick();
                    else
                        startClass_onClick();
                }
                /*
                final Intent intent = new Intent(AddClassContent.this, CourseListActivity.class);
                intent.putExtra("userType", "Teacher");

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        startActivity(intent);
                    }
                }, 1000);*/
            }
        });

        if(getTitle().toString().equals("View Class")){
            makeControlsReadOnly();
        }
    }

    private void makeControlsReadOnly(){
        //.setEnabled(false);
        mCourseEText.setEnabled(false);
        mClassroomEText.setEnabled(false);
        mStartDayEText.setEnabled(false);
        mEndDayEText.setEnabled(false);

        spinWeekDay1.setEnabled(false);
        spinWeekDay2.setEnabled(false);
        spinWeekDay3.setEnabled(false);
        spinWeekDay4.setEnabled(false);
        spinWeekDay5.setEnabled(false);

        etStartTime1.setEnabled(false);
        etEndTime1.setEnabled(false);
        etStartTime2.setEnabled(false);
        etEndTime2.setEnabled(false);
        etStartTime3.setEnabled(false);
        etEndTime3.setEnabled(false);
        etStartTime4.setEnabled(false);
        etEndTime4.setEnabled(false);
        etStartTime5.setEnabled(false);
        etEndTime5.setEnabled(false);
    }

    private void startClass_onClick(){
        Snackbar.make(getCurrentFocus(), "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        //Turn on the Beacon
        //connect to the beacon
        //show the attendances
    }

    // return class info from DBManager
    public void getClassInfoFromDB(boolean success, String name, String classroom, String startDay,
                                   String endDay, ArrayList<MeetingInfo> list){
        if(!success) {
            Log.d("AddClassContent ==>", "fail to get class");
            return;
        }
        //Log.d("course list ==>", "class id: "+ id+"   "+name);
        mCourseEText.setText(name);
        mClassroomEText.setText(classroom);
        mStartDayEText.setText(startDay);
        mEndDayEText.setText(endDay);

        for(int i=0; i<list.size(); i++){
            if(i==0){
                spinWeekDay1.setSelection(MeetingOfClass.getIndexOfWeekDay(list.get(i).weekday));
                etStartTime1.setText(list.get(i).startTime);
                etEndTime1.setText(list.get(i).endTime);
            }
            else if(i==1){
                spinWeekDay2.setSelection(MeetingOfClass.getIndexOfWeekDay(list.get(i).weekday));
                etStartTime2.setText(list.get(i).startTime);
                etEndTime2.setText(list.get(i).endTime);
            }
            else if(i==2){
                spinWeekDay3.setSelection(MeetingOfClass.getIndexOfWeekDay(list.get(i).weekday));
                etStartTime3.setText(list.get(i).startTime);
                etEndTime3.setText(list.get(i).endTime);
            }
            else if(i==3){
                spinWeekDay4.setSelection(MeetingOfClass.getIndexOfWeekDay(list.get(i).weekday));
                etStartTime4.setText(list.get(i).startTime);
                etEndTime4.setText(list.get(i).endTime);
            }
            else if(i==4){
                spinWeekDay5.setSelection(MeetingOfClass.getIndexOfWeekDay(list.get(i).weekday));
                etStartTime5.setText(list.get(i).startTime);
                etEndTime5.setText(list.get(i).endTime);
            }
        }
    }


    private  void addClass_onClick(){
        if(mCourseEText.getText().toString().length()==0) {
            Snackbar.make(getCurrentFocus(), "course name cannot be empty", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
            return;
        }
        if(mClassroomEText.getText().toString().length()==0) {
            Snackbar.make(getCurrentFocus(), "location cannot be empty", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        if(mStartDayEText.getText().toString().length()==0) {
            Snackbar.make(getCurrentFocus(), "start day cannot be empty", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        if(mEndDayEText.getText().toString().length()==0) {
            Snackbar.make(getCurrentFocus(), "end day cannot be empty", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        DBManager db = DBManager.getInstance();
        ArrayList<MeetingOfClass> meetingList = new ArrayList<>();
        MeetingOfClass meeting;
        String weekday = spinWeekDay1.getSelectedItem().toString();
        if(weekday.length() > 0) {
            String startTime = etStartTime1.getText().toString();
            if (startTime.length() == 0) {
                Snackbar.make(getCurrentFocus(), "start time cannot be empty", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            }
            String endTime = etEndTime1.getText().toString();
            if (endTime.length() == 0) {
                Snackbar.make(getCurrentFocus(), "end time cannot be empty", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            }
            meeting = new MeetingOfClass();
            meeting.weekday = weekday;
            meeting.startTime = startTime;
            meeting.endTime = endTime;
            meetingList.add(meeting);
        }
        weekday = spinWeekDay2.getSelectedItem().toString();
        if(weekday.length() > 0) {
            String startTime = etStartTime2.getText().toString();
            if (startTime.length() == 0) {
                Snackbar.make(getCurrentFocus(), "start time cannot be empty", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            }
            String endTime = etEndTime2.getText().toString();
            if (endTime.length() == 0) {
                Snackbar.make(getCurrentFocus(), "end time cannot be empty", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            }
            meeting = new MeetingOfClass();
            meeting.weekday = weekday;
            meeting.startTime = startTime;
            meeting.endTime = endTime;
            meetingList.add(meeting);
        }
        weekday = spinWeekDay3.getSelectedItem().toString();
        if(weekday.length() > 0) {
            String startTime = etStartTime3.getText().toString();
            if (startTime.length() == 0) {
                Snackbar.make(getCurrentFocus(), "start time cannot be empty", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            }
            String endTime = etEndTime3.getText().toString();
            if (endTime.length() == 0) {
                Snackbar.make(getCurrentFocus(), "end time cannot be empty", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            }
            meeting = new MeetingOfClass();
            meeting.weekday = weekday;
            meeting.startTime = startTime;
            meeting.endTime = endTime;
            meetingList.add(meeting);
        }
        weekday = spinWeekDay4.getSelectedItem().toString();
        if(weekday.length() > 0) {
            String startTime = etStartTime4.getText().toString();
            if (startTime.length() == 0) {
                Snackbar.make(getCurrentFocus(), "start time cannot be empty", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            }
            String endTime = etEndTime4.getText().toString();
            if (endTime.length() == 0) {
                Snackbar.make(getCurrentFocus(), "end time cannot be empty", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            }
            meeting = new MeetingOfClass();
            meeting.weekday = weekday;
            meeting.startTime = startTime;
            meeting.endTime = endTime;
            meetingList.add(meeting);
        }
        weekday = spinWeekDay5.getSelectedItem().toString();
        if(weekday.length() > 0) {
            String startTime = etStartTime5.getText().toString();
            if (startTime.length() == 0) {
                Snackbar.make(getCurrentFocus(), "start time cannot be empty", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            }
            String endTime = etEndTime5.getText().toString();
            if (endTime.length() == 0) {
                Snackbar.make(getCurrentFocus(), "end time cannot be empty", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            }
            meeting = new MeetingOfClass();
            meeting.weekday = weekday;
            meeting.startTime = startTime;
            meeting.endTime = endTime;
            meetingList.add(meeting);
        }
        if(meetingList.size()==0) {
            Snackbar.make(getCurrentFocus(), "meeting cannot be empty", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        //modify class
        if(addClassgBtn.getText().toString().equals("Modify")) {
            db.ModifyClassOfTeacher(mClassId, mCourseEText.getText().toString(),
                    mClassroomEText.getText().toString(),
                    mStartDayEText.getText().toString(),
                    mEndDayEText.getText().toString(),
                    meetingList);
        }// add a class
        else{
            db.addClassToTeacher(mCourseEText.getText().toString(),
                    mClassroomEText.getText().toString(),
                    mStartDayEText.getText().toString(),
                    mEndDayEText.getText().toString(),
                    meetingList);
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        final Calendar cldr = Calendar.getInstance();
        int hr = cldr.get(Calendar.HOUR_OF_DAY);
        int min = cldr.get(Calendar.MINUTE);
        mTimePickerDlg = new TimePickerDialog(AddClassContent.this, this, hr, min,false);
        switch (v.getId()) {
            case R.id.etStartTime1:
                iWhichTime = 10;
                break;
            case R.id.etEndTime1:
                iWhichTime = 11;
                break;
            case R.id.etStartTime2:
                iWhichTime = 20;
                break;
            case R.id.etEndTime2:
                iWhichTime = 21;
                break;
            case R.id.etStartTime3:
                iWhichTime = 30;
                break;
            case R.id.etEndTime3:
                iWhichTime = 31;
                break;
            case R.id.etStartTime4:
                iWhichTime = 40;
                break;
            case R.id.etEndTime4:
                iWhichTime = 41;
                break;
            case R.id.etStartTime5:
                iWhichTime = 50;
                break;
            case R.id.etEndTime5:
                iWhichTime = 51;
                break;
            default:
                break;
        }
        mTimePickerDlg.show();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        String strTime = "";
        if (hour >= 12) {
            if (hour > 12)
                hour -= 12;
            strTime = String.format("%02d:%02dPM", hour, minute);
        } else
            strTime = String.format("%02d:%02dAM", hour, minute);
        switch (iWhichTime) {
            case 10:
                etStartTime1.setText(strTime);
                break;
            case 11:
                etEndTime1.setText(strTime);
                break;
            case 20:
                etStartTime2.setText(strTime);
                break;
            case 21:
                etEndTime2.setText(strTime);
                break;
            case 30:
                etStartTime3.setText(strTime);
                break;
            case 31:
                etEndTime3.setText(strTime);
                break;
            case 40:
                etStartTime4.setText(strTime);
                break;
            case 41:
                etEndTime4.setText(strTime);
                break;
            case 50:
                etStartTime5.setText(strTime);
                break;
            case 51:
                etEndTime5.setText(strTime);
                break;
            default:
                break;
        }
    }

    public static class MeetingInfo{
        public  String weekday;
        public String startTime;
        public String endTime;
        public MeetingInfo(String day, String start, String end){
            weekday = day;
            startTime = start;
            endTime = end;
        }
    }
}

