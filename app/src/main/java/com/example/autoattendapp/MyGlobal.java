package com.example.autoattendapp;

import com.google.firebase.firestore.FirebaseFirestore;

public class MyGlobal {
    private static MyGlobal mInstance= null;

    public User gUser;
    public LoginInfo gLoginInfo;
    public CourseListActivity courseListActivity;
    public FirebaseFirestore gDB = FirebaseFirestore.getInstance();;

    protected MyGlobal(){}

    public static synchronized MyGlobal getInstance() {
        if(null == mInstance){
            mInstance = new MyGlobal();
        }
        return mInstance;
    }
}
