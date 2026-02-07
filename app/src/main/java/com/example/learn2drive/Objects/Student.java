package com.example.learn2drive.Objects;

import java.util.Date;

public class Student extends User
{
    private boolean isApprovedByTeacher;
    private int lessonsCompleted;
    private String teacherUid;

    public Student()
    {
        super();
        this.isApprovedByTeacher = false;
        this.lessonsCompleted = 0;
        this.teacherUid = "";
    }

    public Student(String uid, String idNumber, String fullName, String BirthDate, String phoneNumber,
                   boolean active, boolean isApprovedByTeacher, int lessonsCompleted, String teacherUid)
    {
        super(uid, idNumber, fullName, BirthDate, phoneNumber, active);
        this.isApprovedByTeacher = isApprovedByTeacher;
        this.lessonsCompleted = lessonsCompleted;
        this.teacherUid = teacherUid;
    }

    public void setApprovedByTeacher(boolean isApprovedByTeacher)
    {
        this.isApprovedByTeacher = isApprovedByTeacher;
    }

    public boolean isApprovedByTeacher()
    {
        return isApprovedByTeacher;
    }

    public void setLessonsCompleted(int lessonsCompleted)
    {
        this.lessonsCompleted = lessonsCompleted;
    }

    public int getLessonsCompleted()
    {
        return lessonsCompleted;
    }

    public void setTeacherUid(String teacherUid)
    {
        this.teacherUid = teacherUid;
    }

    public String getTeacherUid()
    {
        return teacherUid;
    }
}
