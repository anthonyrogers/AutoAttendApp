package com.example.autoattendapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import android.app.NotificationChannel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class ServiceForBeacon extends Service implements RangeNotifier, BeaconConsumer {

    //if you have questions on service contact Anthony
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static final String ACTIVE_TITLE = "You're currently active in ";
    public static final String ACTIVE_CONTENT = "Notification will cancel when class is over";
    public static final int NOTIFICATION_ID = 23;

    BeaconManager mBeaconManager;
    Context context;
    String classID;
    String className;
    List<String> classbeaconlist;

    public ServiceForBeacon(){}

    public ServiceForBeacon(Context applicationContext) {
        super();
        this.context = applicationContext;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        classID = intent.getExtras().getString("ClassID");
        String action = intent.getAction();
        if(action.equals("stop")){
            Log.i("SERVICE ==>", "ENDED");
            // this is where the code will be to submit the student info to the db
            stopSelf();
        }else if(action.equals("start")){
            Log.i("SERVICE ==>", "STARTED");
            getClassList();
            //sets up the beacon manager for use with eddystone beacons
            mBeaconManager = BeaconManager.getInstanceForApplication(this);
            mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
            // Binds this activity to the BeaconService
            mBeaconManager.bind(this);

            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,0, notificationIntent, 0);

            Notification notification  = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Searching for class beacon")
                    .setContentText("Notification will update when you're in class")
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setAutoCancel(true)
                    .build();
                startForeground(NOTIFICATION_ID, notification);
        }else{
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    //Updates notification and sets title and content when the app notices the beacon id associated with the class.
    private void updateNotification(String title, String content){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification  = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .build();

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    //just did a database call in here for testing purposes and we can access it without the app running
    //will eventually move to DBManager. This grabs the name of the class to display to the user in the notification.
    public void getClassList() {
        final FirebaseAuth firebaseUser = FirebaseAuth.getInstance();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        if(firebaseUser != null) {
            DocumentReference userRef = database.collection("classes").document(classID);
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    Log.i("CLASS DB CALL", "SUCCESS");
                    className = task.getResult().getString("course");
                    getBeaconIds(task.getResult().getString("teachID"));
                }
            });
        }

    }
    //gets the beacon id associated with current class
    public void getBeaconIds(String teacherId){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users").document(teacherId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    classbeaconlist = new ArrayList<>();
                    if (document.exists()) {
                            classbeaconlist.add(task.getResult().getString("beacon"));
                            Log.i("BEACON ID FROM TEACHER", task.getResult().getString("beacon"));
                    }
                }
            }
        });
    }

    //notification channels are needed in newer android OS. It allows the user to go under the settings in an
    //app and change what notifications they want from our app. In this case, we just have one.
    //if the user were to turn off notifications from us, the service should still stay running.
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            serviceChannel.setSound(null,null);
            serviceChannel.setLightColor(Color.GREEN);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "===> SERVICE DESTROYED!");
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
                Log.i("BEACON", collection.iterator().next().getIdentifier(0).toString());
                for(String beacon : classbeaconlist) {
                    if (collection.iterator().next().getIdentifier(0).toString().equals(beacon)) {
                        try {
                            //when it notices the beacon and it matches the beacon id with the professor
                            //it will update the notification saying the student is in class and then stop
                            //the beacon calls
                            updateNotification(ACTIVE_TITLE + className, ACTIVE_CONTENT);
                            mBeaconManager.stopRangingBeaconsInRegion(region);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        Log.i("BEACON ID", "BEACON NOTICED " + beacon);
                    }else{
                        Log.i("BEACON ID", "BEACON NOT NOTICED");
                    }
                }
        }
    }
}