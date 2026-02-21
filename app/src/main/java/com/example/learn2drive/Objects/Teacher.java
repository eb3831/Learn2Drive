package com.example.learn2drive.Objects;

import java.util.Date;

public class Teacher extends User
{
    private int defaultLessonDurationMin;
    private int hourlyRate;

    public Teacher()
    {
        super();
        this.defaultLessonDurationMin = 0;
        this.hourlyRate = 0;
    }

    public Teacher(String uid, String idNumber, String fullName, String BirthDate,
                   String phoneNumber,String status , int role,
                   int defaultLessonDurationMin, int hourlyRate)
    {
        super(uid, idNumber, fullName, BirthDate, phoneNumber, status, role);
        this.defaultLessonDurationMin = defaultLessonDurationMin;
        this.hourlyRate = hourlyRate;
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
