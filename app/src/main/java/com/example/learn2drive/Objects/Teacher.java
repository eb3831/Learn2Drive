package com.example.learn2drive.Objects;

import java.util.Date;

public class Teacher extends User
{
    private boolean isApprovedByAdmin;
    private int defaultLessonDurationMin;
    private int hourlyRate;

    public Teacher()
    {
        super();
        this.isApprovedByAdmin = false;
        this.defaultLessonDurationMin = 0;
        this.hourlyRate = 0;
    }

    public Teacher(String uid, String idNumber, String fullName, String BirthDate, String phoneNumber,
                   boolean active, boolean isApprovedByAdmin, int defaultLessonDurationMin, int hourlyRate)
    {
        super(uid, idNumber, fullName, BirthDate, phoneNumber, active);
        this.isApprovedByAdmin = isApprovedByAdmin;
        this.defaultLessonDurationMin = defaultLessonDurationMin;
        this.hourlyRate = hourlyRate;
    }

    public void setApprovedByAdmin(boolean isApprovedByAdmin)
    {
        this.isApprovedByAdmin = isApprovedByAdmin;
    }

    public boolean isApprovedByAdmin()
    {
        return isApprovedByAdmin;
    }

    public int getDefaultLessonDurationMin()
    {
        return defaultLessonDurationMin;
    }

    public void setDefaultLessonDurationMin(int defaultLessonDurationMin)
    {
        this.defaultLessonDurationMin = defaultLessonDurationMin;
    }

    public int getHourlyRate()
    {
        return hourlyRate;
    }

    public void setHourlyRate(int hourlyRate)
    {
        this.hourlyRate = hourlyRate;
    }
}
