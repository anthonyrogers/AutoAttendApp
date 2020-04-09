package com.example.autoattendapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class ServiceForBeacon extends Service {

    public int counter = 0;
    public ServiceForBeacon(){}

    public ServiceForBeacon(Context applicationContext) {
        super();
        Log.i("Service", "==> is created");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "===> on destroy!");
        //sooo on destroy it will send a boadcastIntent that will launch my beacon restart class which then
        //will re-run the service class
        if(counter < 20) {
            stopSelf();
            Intent broadcastIntent = new Intent(this, BeaconRestarterBroadcastReceiver.class);
            sendBroadcast(broadcastIntent);
        }

    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  "+ (counter++));
            }
        };
    }


}
