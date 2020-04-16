package com.example.autoattendapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StudentAddClassActivity extends AppCompatActivity {

    EditText classCode;
    Button addButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_add_class);
        setTitle("Add Class");

        addButton = findViewById(R.id.studentAddClassButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                classCode = findViewById(R.id.classCodeText);
                String code = classCode.getText().toString();

                if(code.equals("")) {
                    Toast.makeText(StudentAddClassActivity.this, "Please enter class code", Toast.LENGTH_SHORT).show();
                } else {
                    DBManager dbManager = DBManager.getInstance();
                    dbManager.checkClassCode(code, StudentAddClassActivity.this.getApplicationContext());

                    final Intent intent = new Intent(StudentAddClassActivity.this, CourseListActivity.class);
                    intent.putExtra("userType", "Student");

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            startActivity(intent);
                        }
                    }, 1000);

                }
            }
        });

        //This is the code where we would inject the students end time for the class and set a pending intent with
        //the alarm manager. when the intent is received by the service, it will read the intent action I set
        //currently im using the calendar object to fake a time
        //all this code will be moved into the user activity and it will parse the database for the time that a class ends
        //-Anthony
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 22);
        calendar.set(Calendar.SECOND, 0); // set the time when youre supposed to stop
        AlarmManager am =( AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, ServiceForBeacon.class);
        i.setAction("stop");
        PendingIntent pi = PendingIntent.getForegroundService(this, 0, i, 0);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);

        //will convert 11:00pm to 13:00 this is needed to create a calendar object which we will inject
        //into the alarm manager to let it know when to launch the pending intent. -Anthony
        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mma");
       // Date date = parseFormat.parse(meeting.endTime.replaceAll("\\s",""));



        String datee = "Tue Apr 23 16:08:28 GMT+05:30 2013";
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE HH:mm   ", Locale.ENGLISH);
        // LocalDateTime localDate = LocalDateTime.parse(date, formatter);
        //long timeInMilliseconds = localDate.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli();
        //Log.d("DATE", "Date in milli :: FOR API >= 26 >>> " + timeInMilliseconds);
       // Log.i("MEETING TIMES list ==>", displayFormat.format(date));
    }
}
