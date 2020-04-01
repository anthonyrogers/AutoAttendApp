package com.example.autoattendapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Course implements Parcelable {

    private Teacher teacher;
    private ArrayList<Student> students;
    private String classCode;
    private String courseName;
    private HashMap<Student, List<StudentRecord>> studentRecords;

    public Course(Teacher teacher, ArrayList<Student> students, String classCode, String courseName) {
        this.teacher = teacher;
        this.students = students;
        this.classCode = classCode;
        this.courseName = courseName;
    }

    protected Course(Parcel in){
        teacher = (Teacher) in.readParcelable(User.class.getClassLoader());
        students = in.readArrayList(Student.class.getClassLoader());
        classCode = in.readString();
        courseName = in.readString();
        studentRecords = (HashMap<Student, List<StudentRecord>>) in.readSerializable();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((User) teacher, flags);
        dest.writeList(students);
        dest.writeString(classCode);
        dest.writeString(courseName);
        dest.writeSerializable(studentRecords);
    }

    public static final Creator<Course> CREATOR = new Creator<Course>() {
        @Override
        public Course createFromParcel(Parcel source) {
            return new Course(source);
        }

        @Override
        public Course[] newArray(int size) {
            return new Course[size];
        }
    };


    public String getCourseName() {
        return courseName;
    }

    public boolean addStudent(Student student) {
        students.add(student);
        // database calls
        return false;
    }

    public boolean removeStudent(Student toRemove) {
        if(students == null || students.size() == 0) {
            return false;
        }
        for(Student student : students) {
            if(student.equals(toRemove)) {
                students.remove(student);
                // database calls
                break;
            }
        }
        return false;
    }

    public boolean setTeacher(Teacher newTeacher) {
        this.teacher = newTeacher;
        //database calls
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
