package com.example.learn2drive.Objects;

public class TimeSlot
{
    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_UNAVAILABLE = "UNAVAILABLE";
    public static final String STATUS_BOOKED = "BOOKED";

    private int duration;
    private String status;
    private String studentUid;

    public TimeSlot()
    {
    }

    public TimeSlot(int duration, String status, String studentUid)
    {
        this.duration = duration;
        this.status = status;
        this.studentUid = studentUid;
    }

    public TimeSlot(int duration)
    {
        this.duration = duration;
        this.status = STATUS_AVAILABLE;
        this.studentUid = "";
    }

    public int getDuration()
    {
        return duration;
    }

    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStudentUid()
    {
        return studentUid;
    }

    public void setStudentUid(String studentUid)
    {
        this.studentUid = studentUid;
    }
}