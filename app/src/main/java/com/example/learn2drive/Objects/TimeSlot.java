package com.example.learn2drive.Objects;

import com.google.firebase.database.Exclude; // IMPORTANT: Add this import

/**
 * Represents a single time slot for a teacher's schedule.
 */
public class TimeSlot
{
    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_UNAVAILABLE = "UNAVAILABLE";
    public static final String STATUS_BOOKED = "BOOKED";
    public static final String STATUS_REQUESTED = "REQUESTED";

    private int duration;
    private String status;
    private String studentUid;
    private String startTime;
    private String endTime;

    public TimeSlot()
    {
        this.duration = 0;
        this.status = STATUS_AVAILABLE;
        this.studentUid = "";
        this.startTime = "";
        this.endTime = "";
    }

    /**
     * Constructor for creating a new available time slot.
     *
     * @param duration  The duration of the lesson in minutes.
     * @param startTime The start time of the slot (e.g., "09:00").
     * @param endTime   The end time of the slot (e.g., "10:00").
     */
    public TimeSlot(int duration, String startTime, String endTime)
    {
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = STATUS_AVAILABLE;
        this.studentUid = "";
    }

    /**
     * Full constructor for creating a time slot with all specific details.
     *
     * @param duration   The duration of the lesson in minutes.
     * @param status     The current status of the slot.
     * @param studentUid The UID of the student (if booked), otherwise empty.
     * @param startTime  The start time of the slot.
     * @param endTime    The end time of the slot.
     */
    public TimeSlot(int duration, String status, String studentUid, String startTime, String endTime)
    {
        this.duration = duration;
        this.status = status;
        this.studentUid = studentUid;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public String getStartTime()
    {
        return startTime;
    }

    public void setStartTime(String startTime)
    {
        this.startTime = startTime;
    }

    public String getEndTime()
    {
        return endTime;
    }

    public void setEndTime(String endTime)
    {
        this.endTime = endTime;
    }

    /**
     * Checks if the time slot is currently pending approval.
     * Excluded from Firebase serialization to prevent creating redundant database fields.
     *
     * @return true if status is REQUESTED, false otherwise.
     */
    @Exclude
    public boolean isRequested()
    {
        return STATUS_REQUESTED.equals(this.status);
    }

    /**
     * Checks if the time slot is available for booking.
     * Excluded from Firebase serialization to prevent creating redundant database fields.
     *
     * @return true if status is AVAILABLE, false otherwise.
     */
    @Exclude
    public boolean isAvailable()
    {
        return STATUS_AVAILABLE.equals(this.status);
    }
}