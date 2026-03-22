package com.example.learn2drive.Objects;

import java.io.Serializable;

public class ScheduledLesson implements Serializable
{
    private int lessonNumber;
    private String teacherUID;
    private String studentUID;
    private String studentID;
    private String dateAndTime;
    private int duration;
    private String studentName;

    public ScheduledLesson()
    {
        this.lessonNumber = 0;
        this.teacherUID = "";
        this.studentUID = "";
        this.studentID = "";
        this.dateAndTime = "";
        this.duration = 0;
        this.studentName = "";
    }


    public ScheduledLesson(int lessonNumber, String teacherUID, String studentUID, String studentID,
                           String dateAndTime, int duration, String studentName)
    {
        this.lessonNumber = lessonNumber;
        this.teacherUID = teacherUID;
        this.studentUID = studentUID;
        this.studentID = studentID;
        this.dateAndTime = dateAndTime;
        this.duration = duration;
        this.studentName = studentName;
    }

    public void setLessonNumber(int lessonNumber)
    {
        this.lessonNumber = lessonNumber;
    }

    public int getLessonNumber()
    {
        return lessonNumber;
    }

    public void setTeacherUID(String teacherUID)
    {
        this.teacherUID = teacherUID;
    }

    public String getTeacherUID()
    {
        return teacherUID;
    }

    public void setStudentUID(String studentUID) {
        this.studentUID = studentUID;
    }

    public String getStudentUID()
    {
        return studentUID;
    }

    public void setStudentID(String studentID)
    {
        this.studentID = studentID;
    }

    public String getStudentID()
    {
        return studentID;
    }

    public void setDateAndTime(String dateAndTime)
    {
        this.dateAndTime = dateAndTime;
    }

    public String getDateAndTime()
    {
        return dateAndTime;
    }

    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    public int getDuration()
    {
        return duration;
    }

    public void setStudentName(String studentName)
    {
        this.studentName = studentName;
    }

    public String getStudentName()
    {
        return studentName;
    }
}
