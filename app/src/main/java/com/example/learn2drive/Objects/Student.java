package com.example.learn2drive.Objects;

import java.util.Date;

public class Student extends User
{
    private boolean isApprovedByTeacher;
    private int lessonsCompleted;
    private String teacherID;

    public Student()
    {
        super();
        this.isApprovedByTeacher = false;
        this.lessonsCompleted = 0;
        this.teacherID = "";
    }

    public Student(String UserID, String fullName, String BirthDate, String phoneNumber,
                   boolean active, boolean isApprovedByTeacher, int lessonsCompleted, String teacherID)
    {
        super(UserID, fullName, BirthDate, phoneNumber, active);
        this.isApprovedByTeacher = isApprovedByTeacher;
        this.lessonsCompleted = lessonsCompleted;
        this.teacherID = teacherID;
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

    public void setTeacherID(String teacherID)
    {
        this.teacherID = teacherID;
    }

    public String getTeacherID()
    {
        return teacherID;
    }
}
