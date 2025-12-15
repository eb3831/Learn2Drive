package com.example.learn2drive.Objects;

public class ScheduledLesson
{
    private int lessonNumber;
    private String teacherID;
    private String studentID;
    private String DateAndTime;
    private int duration;
    private int lessonStatus;

    public ScheduledLesson()
    {
        this.lessonNumber = 0;
        this.teacherID = "";
        this.studentID = "";
        this.DateAndTime = "";
        this.duration = 0;
        this.lessonStatus = 0;
    }


    public ScheduledLesson(int lessonNumber, String teacherID, String studentID, String DateAndTime,
                           int duration, int lessonStatus)
    {
        this.lessonNumber = lessonNumber;
        this.teacherID = teacherID;
        this.studentID = studentID;
        this.DateAndTime = DateAndTime;
        this.duration = duration;
        this.lessonStatus = lessonStatus;
    }

    public void setLessonNumber(int lessonNumber)
    {
        this.lessonNumber = lessonNumber;
    }

    public int getLessonNumber()
    {
        return lessonNumber;
    }

    public void setTeacherID(String teacherID)
    {
        this.teacherID = teacherID;
    }

    public String getTeacherID()
    {
        return teacherID;
    }

    public void setStudentID(String studentID)
    {
        this.studentID = studentID;
    }

    public String getStudentID()
    {
        return studentID;
    }

    public void setDateAndTime(String DateAndTime)
    {
        this.DateAndTime = DateAndTime;
    }

    public String getDateAndTime()
    {
        return DateAndTime;
    }

    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    public int getDuration()
    {
        return duration;
    }

    public void setLessonStatus(int lessonStatus)
    {
        this.lessonStatus = lessonStatus;
    }

    public int getLessonStatus()
    {
        return lessonStatus;
    }
}
