package com.example.autoattendapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
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

    private Button mAddClassgBtn;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_add_class);
        setTitle("Add Class");
        mAddClassgBtn = findViewById(R.id.addClassBtn);
        mStartDayEText = findViewById(R.id.editTextStartDay);
        mStartDayEText.setFocusable(false);
        mStartDayEText.setClickable(true);

        mEndDayEText = findViewById(R.id.editTextEndDay);
        mEndDayEText.setFocusable(false);
        mEndDayEText.setClickable(true);

        Spinner spinWeekDay1 = findViewById(R.id.spinWeekDay1);
        Spinner spinWeekDay2 = findViewById(R.id.spinWeekDay2);
        Spinner spinWeekDay3 = findViewById(R.id.spinWeekDay3);
        Spinner spinWeekDay4 = findViewById(R.id.spinWeekDay4);
        Spinner spinWeekDay5 = findViewById(R.id.spinWeekDay5);
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
        list.add("Monday");
        list.add("Tuesday");
        list.add("Wednesday");
        list.add("Thursday");
        list.add("Friday");
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

        mAddClassgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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
            strTime = String.format("%02d : %02d PM", hour, minute);
        } else
            strTime = String.format("%02d : %02d AM", hour, minute);
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
}

