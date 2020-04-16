package com.example.autoattendapp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import android.app.NotificationChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;


public class ServiceForBeacon extends Service implements RangeNotifier, BeaconConsumer {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    public int counter = 0;
    BeaconManager mBeaconManager;
    Context context;
    DBManager db = DBManager.getInstance();

    public ServiceForBeacon(){}
    public ServiceForBeacon(Context applicationContext) {
        super();
        this.context = applicationContext;
        Log.i("Service", "==> is created");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        String action = intent.getAction();
        if(action.equals("stop")){

            // this is where the code will be to submit the student info to the db
            stopSelf();
        }

        //for the beacon
        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        // Binds this activity to the BeaconService
        mBeaconManager.bind(this);


        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("You're currently active in class")
                .setContentText("This will dismiss when class is over")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .build();
        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "===> on destroy!");
    }

   @Override
    public void onBeaconServiceConnect() {
        // Encapsulates a beacon identifier of arbitrary byte length
        ArrayList<Identifier> identifiers = new ArrayList<>();

        // Set null to indicate that we want to match beacons with any value
        identifiers.add(null);
        // Represents a criteria of fields used to match beacon
        Region region = new Region("BeaconID", identifiers);
        try {
            // Tells the BeaconService to start looking for beacons that match the passed Region object
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        // Specifies a class that should be called each time the BeaconService gets ranging data, once per second by default
        mBeaconManager.addRangeNotifier(this);
        Log.i("BEACON CONNECT SERVICE", "CALLED");
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
        if (collection.size() > 0) {
           // if(counter < 20) {
                Log.i("BEACON", collection.iterator().next().getIdentifier(0).toString());
                counter++;
          //  }else{
              // stopSelf();
           // }
        }else{
            //Since you guys dont have the beacon to connect to i just stopped the service so it wouldn't run in the background
            //for now. will set a condition to stop based off class time
           //stopSelf();
        }
    }
}

