package com.example.autoattendapp;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class ServiceForBeacon extends Service implements RangeNotifier, BeaconConsumer {

    //if you have questions on service contact Anthony
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public int counter = 0;
    BeaconManager mBeaconManager;
    Context context;
    String classID;
    String className;
    List<String> list;
    DBManager db = DBManager.getInstance();

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

            // this is where the code will be to submit the student info to the db
            stopSelf();
        }else if(action.equals("start")){
            getBeaconIds();
            //for the beacon
            mBeaconManager = BeaconManager.getInstanceForApplication(this);
            mBeaconManager.getBeaconParsers().add(new BeaconParser().
                    setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
            // Binds this activity to the BeaconService
            mBeaconManager.bind(this);
            getClassList();


            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("You're currently active in " + className)
                    .setContentText("This will dismiss when class is over")
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .build();
            startForeground(1, notification);
        }else{
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    //just did a database call in here for testing purposes and we can access it without the app running
    //will eventually move to DBManager. This grabs the name of the class to display to the user in the notification.
    public void getClassList() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        if(firebaseUser != null) {
            DocumentReference userRef = database.collection("classes").document(classID);
            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    className = documentSnapshot.getString("course");
                    Log.i("SERVICE DB CALL", "SUCCESS");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    className = "class";
                    Log.i("SERVICE DB CALL", "FAILED");
                }
            });
        }
    }

    public void getBeaconIds(){
        FirebaseFirestore database = MyGlobal.getInstance().gDB;
        database.collection("users")
                .whereEqualTo("classes", classID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        list = new ArrayList<>();
                        for (QueryDocumentSnapshot snap : documentSnapshots) {
                            list.add(snap.getString("beacon"));
                            Log.i("BEACON IDS", snap.getString("beacon"));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
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
                Log.i("BEACON", collection.iterator().next().getIdentifier(0).toString());
        }else{
           //stopSelf();
        }
    }
}

