package com.example.learn2drive;

import java.util.Date;

public class Teacher extends User
{
    private boolean isApprovedByAdmin;
    private String classCode;
    private int defaultLessonDurationMin;
    private int hourlyRate;

    public Teacher()
    {
        super();
        this.isApprovedByAdmin = false;
        this.classCode = "";
        this.defaultLessonDurationMin = 0;
        this.hourlyRate = 0;
    }

    public Teacher(String UserID, String fullName, Date BirthDate, String phoneNumber, String Role,
                   boolean active, boolean isApprovedByAdmin, String classCode, int defaultLessonDurationMin, int hourlyRate)
    {
        super(UserID, fullName, BirthDate, phoneNumber, Role, active);
        this.isApprovedByAdmin = isApprovedByAdmin;
        this.classCode = classCode;
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

    public void setClassCode(String classCode)
    {
        this.classCode = classCode;
    }

    public String getClassCode()
    {
        return classCode;
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
