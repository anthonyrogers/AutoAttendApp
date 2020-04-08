package com.example.autoattendapp;

public class Account {

    private static Student studentAccount = null;
    private static Teacher teacherAccount = null;

    private Account(){}

    public static void setStudentAccount(Student studentAccount) {
        Account.studentAccount = studentAccount;
        Account.teacherAccount = null;
    }

    public static void setTeacherAccount(Teacher teacherAccount) {
        Account.teacherAccount = teacherAccount;
        Account.studentAccount = null;
    }

    public static Student getStudentAccount(){
        return studentAccount;
    }

    public static Teacher getTeacherAccount(){
        return teacherAccount;
    }
}
