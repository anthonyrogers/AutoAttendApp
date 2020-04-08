package com.example.autoattendapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.internal.$Gson$Types;

import java.io.Serializable;

public class LoginInfo implements Parcelable, Serializable {
    private String mEmail;
    private String mPassword;

    public LoginInfo(String email, String password){
        mEmail = email;
        mPassword = password;
    }

    protected LoginInfo(Parcel in) {
        mEmail = in.readString();
        mPassword = in.readString();
    }

    public static final Creator<LoginInfo> CREATOR = new Creator<LoginInfo>() {
        @Override
        public LoginInfo createFromParcel(Parcel in) {
            return new LoginInfo(in);
        }

        @Override
        public LoginInfo[] newArray(int size) {
            return new LoginInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mEmail);
        parcel.writeString(mPassword);
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }
    public String getEmail(){
        return mEmail;
    }

    public void setPassword(String password){
        this.mPassword = password;
    }
    public String getPassword(){
        return  mPassword;
    }
}
