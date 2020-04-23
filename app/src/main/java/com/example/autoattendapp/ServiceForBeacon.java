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
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import android.app.NotificationChannel;
import android.widget.Toast;

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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.altbeacon.beacon.Identifier.fromUuid;


public class ServiceForBeacon extends Service implements RangeNotifier, BeaconConsumer, MonitorNotifier {

    //if you have questions on service contact Anthony
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static final String ACTIVE_TITLE = "You're currently active in ";
    public static final String ACTIVE_CONTENT = "Notification will cancel when class is over";
    public static final int NOTIFICATION_ID = 23;

    final FirebaseAuth firebaseUser = FirebaseAuth.getInstance();
    final FirebaseFirestore database = FirebaseFirestore.getInstance();

    BeaconManager mBeaconManager;
    String classID, className, beaconID;
    List<String> classbeaconlist;
    Boolean verifiedBeacon;
    ArrayList<Map<String, String>> totalTime;
    Map<String, String> sessionTime;
    Region userRegion;

    public ServiceForBeacon(){}

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
            stopSelf();
        }else if(action.equals("start")){
            Log.i("SERVICE ==>", "STARTED");
            getClassList();
            //sets up the beacon manager for use with eddystone beacons
            mBeaconManager = BeaconManager.getInstanceForApplication(this);
            mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
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
            doesDateExistForClass();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            getStudentInformation(auth.getCurrentUser().getUid());
            totalTime = new ArrayList<>();
            verifiedBeacon = false;
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
        database.collection("users").document(teacherId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    classbeaconlist = new ArrayList<>();
                    if (document.exists()) {
                        classbeaconlist.add(task.getResult().getString("beacon"));
                        beaconID = task.getResult().getString("beacon");

                        Log.i("BEACON ID FROM TEACHER", task.getResult().getString("beacon"));
                    }
                }
            }
        });
    }

    public void doesDateExistForClass(){
        database.collection("classes").document(classID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<String> classes;
                    classes = (ArrayList<String>) task.getResult().get("pastMeetings");
                    if(!classes.contains(getCurrentDate())){
                        addDateToPastClasses(getCurrentDate());
                    }
                }
            }
        });
    }

    public void getStudentInformation(final String studentid){
        database.collection("users").document(studentid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    addStudentToAttendance(getCurrentDate(), task.getResult().getString("firstname"),
                            task.getResult().getString("lastname"));
                }
            }
        });
    }

    private String getCurrentTime(){
        DateFormat df = new SimpleDateFormat("HH:mm");
        String date = df.format(Calendar.getInstance().getTime());
        return date;
    }
    private String getCurrentDate(){
        DateFormat df = new SimpleDateFormat("EEE, MMM dd, ''yyyy");
        String date = df.format(Calendar.getInstance().getTime());
        Log.i("CURRENT DATE", date);
        return date;
    }

    public void addDateToPastClasses(final String date){
        DocumentReference userRef = database.collection("classes").document(classID);
        userRef.update(
                "pastMeetings", FieldValue.arrayUnion(date)
        ).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Log.i("UPDATED CLASS", date);
                }
            }
        });
    }
    // marks the students attendance when they first hit the beacon
    // if they never hit the beacon, timeIn will be null
    public void addStudentToAttendance(String date, String firstName, String lastName) {
        if(firebaseUser == null)
            return;
        final String studentID = firebaseUser.getUid();

        Map<String, Object> attendance = new HashMap<>();
        attendance.put("classID", classID);
        attendance.put("date", date);
        attendance.put("studentID", studentID);
        attendance.put("firstName", firstName);
        attendance.put("lastName", lastName);
        attendance.put("times", new ArrayList<Map<String,String>>());

        database.collection("attendance")
                .add(attendance)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("addAttendance ==>","added an attendance.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("addAttendance ==>", "Error: fail to adding attendance", e);
                    }
                });
    }

    public void markTimeIn(String date) {
        if(firebaseUser == null)
            return;
        database.collection("attendance")
                .whereEqualTo("studentID", firebaseUser.getCurrentUser().getUid())
                .whereEqualTo("classID", classID)
                .whereEqualTo("date", date)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String attendanceID = document.getId();
                                DocumentReference attendanceRef = database.collection("attendance").document(attendanceID);
                                attendanceRef
                                        .update("times", totalTime)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("time in", "logged");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("time in", "could not be logged", e);
                                            }
                                        });
                            }
                        } else {
                            Log.d("database", "Error getting documents: ", task.getException());
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
        //session time gets created only when the beacon is noticed
      if(sessionTime != null){
          //the session key gets recreated every time the re-enter into the beacon zone. if for some reason
          //the class ended early (before service ends) or they never show back up, this will verify that the
          //student has a timeout. if not, it will add the current timeout of when the service ends.
          //meaning they were there the whole time.
          if (sessionTime.containsKey("timeOut")) {
              markTimeIn(getCurrentDate());
          }else{
              sessionTime.put("timeOut", getCurrentTime());
              totalTime.add(sessionTime);
              markTimeIn(getCurrentDate());
          }
          try {
              mBeaconManager.stopMonitoringBeaconsInRegion(userRegion);
          } catch (RemoteException e) {
              e.printStackTrace();
          }
      }else{
          sessionTime = new HashMap<>();
          sessionTime.put("timeIn", null);
          sessionTime.put("timeOut", null);
          totalTime.add(sessionTime);
          markTimeIn(getCurrentDate());
          try {
              mBeaconManager.stopRangingBeaconsInRegion(userRegion);
          } catch (RemoteException e) {
              e.printStackTrace();
          }
      }
        Log.i("EXIT", "===> SERVICE DESTROYED!");
        super.onDestroy();
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
            mBeaconManager.startMonitoringBeaconsInRegion(region);
            userRegion = region;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        // Specifies a class that should be called each time the BeaconService gets ranging data, once per second by default
        mBeaconManager.addRangeNotifier(this);
        mBeaconManager.addMonitorNotifier(this);
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
                        verifiedBeacon = true;
                        mBeaconManager.stopRangingBeaconsInRegion(region);
                        sessionTime = new HashMap<>();
                        sessionTime.put("timeIn", getCurrentTime());
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

    @Override
    public void didEnterRegion(Region region) {
        if(verifiedBeacon) {
            sessionTime = new HashMap<>();
            sessionTime.put("timeIn", getCurrentTime());
            Log.i("BEACON ====>", "ENTERED REGION" + getCurrentTime());
        }
    }

    @Override
    public void didExitRegion(Region region) {
        if(verifiedBeacon) {
           sessionTime.put("timeOut", getCurrentTime());
           totalTime.add(sessionTime);
            Log.i("BEACON ====>", "EXITED REGION" + getCurrentTime());
        }
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
    //required override but not used in our app
    }
}