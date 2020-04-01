package com.example.autoattendapp;

import android.os.Parcel;
import android.os.Parcelable;

public class Beacon implements Parcelable {

    public String ID;

    public Beacon(String ID) {
        this.ID = ID;
    }

    protected Beacon(Parcel in) {
        ID = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ID);
    }

    public static final Creator<Beacon> CREATOR = new Creator<Beacon>() {
        @Override
        public Beacon createFromParcel(Parcel source) {
            return new Beacon(source);
        }

        @Override
        public Beacon[] newArray(int size) {
            return new Beacon[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }


}
