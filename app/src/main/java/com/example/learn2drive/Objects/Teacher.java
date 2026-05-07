package com.example.learn2drive.Objects;

import java.util.Date;

/**
 * Represents a driving instructor in the system, extending the base User profile.
 * This class includes professional settings such as lesson duration and pricing.
 */
public class Teacher extends User
{
    private int defaultLessonDurationMin;
    private int hourlyRate;

    /**
     * Default constructor for Teacher.
     * Calls the super constructor of the User class and initializes teacher-specific fields to zero.
     */
    public Teacher()
    {
        super();
        this.defaultLessonDurationMin = 0;
        this.hourlyRate = 0;
    }

    /**
     * Parameterized constructor to create a full Teacher profile.
     * @param uid The unique Firebase ID.
     * @param idNumber The teacher's Israeli ID number.
     * @param fullName The teacher's full name.
     * @param BirthDate The teacher's date of birth.
     * @param phoneNumber The teacher's phone number.
     * @param status The teacher's account status (e.g., ACTIVE, PENDING).
     * @param role The user's role index (representing a teacher).
     * @param defaultLessonDurationMin The standard length of a single lesson in minutes.
     * @param hourlyRate The cost charged per hour of instruction.
     */
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