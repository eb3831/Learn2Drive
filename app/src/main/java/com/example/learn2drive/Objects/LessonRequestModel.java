package com.example.learn2drive.Objects;

/**
 * A wrapper model class that aggregates all the necessary data to display and handle a single lesson request.
 * It combines the specific time slot, the requested date, and the student's full information.
 */
public class LessonRequestModel
{
    private TimeSlot timeSlot;
    private Student student;
    private String date;

    /**
     * Constructor to initialize the lesson request model.
     *
     * @param timeSlot The specific time slot requested.
     * @param student  The student who made the request.
     * @param date     The requested date in "dd-MM-yyyy" format.
     */
    public LessonRequestModel(TimeSlot timeSlot, Student student, String date)
    {
        this.timeSlot = timeSlot;
        this.student = student;
        this.date = date;
    }

    public TimeSlot getTimeSlot()
    {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot)
    {
        this.timeSlot = timeSlot;
    }

    public Student getStudent()
    {
        return student;
    }

    public void setStudent(Student student)
    {
        this.student = student;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }
}