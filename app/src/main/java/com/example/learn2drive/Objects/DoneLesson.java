package com.example.learn2drive.Objects;

import java.io.Serializable;

/**
 * Model class representing a completed driving lesson.
 * Extends ScheduledLesson and includes payment details and an AI-generated summary.
 */
public class DoneLesson extends ScheduledLesson implements Serializable
{
    private Payment payment;
    private LessonSummary summary;
    private boolean hasTrack;
    private boolean hasSummary;

    /**
     * Default constructor required for Firebase Realtime Database.
     */
    public DoneLesson()
    {
        super();
        this.payment = new Payment();
        this.summary = new LessonSummary();
        this.hasTrack = false;
        this.hasSummary = false;
    }

    /**
     * Parameterized constructor including payment and summary.
     *
     * @param lessonNumber  The lesson's sequence number.
     * @param teacherUID    The unique ID of the teacher.
     * @param studentUID    The unique ID of the student.
     * @param studentID     The student's national ID.
     * @param dateAndTime   The scheduled date and time.
     * @param duration      The duration of the lesson in minutes.
     * @param studentName   The full name of the student.
     * @param payment       The payment details for the lesson.
     * @param summary       The AI-generated lesson summary.
     */
    public DoneLesson(int lessonNumber, String teacherUID, String studentUID, String studentID,
                      String dateAndTime, int duration, String studentName,
                      Payment payment, LessonSummary summary, boolean hasTrack, boolean hasSummary)
    {
        super(lessonNumber, teacherUID, studentUID, studentID, dateAndTime, duration, studentName);
        this.payment = payment;
        this.summary = summary;
        this.hasTrack = hasTrack;
        this.hasSummary = hasSummary;
    }

    /**
     * Constructor mapping from an existing ScheduledLesson with existing payment and summary.
     *
     * @param lesson  The original scheduled lesson.
     * @param payment The payment details.
     * @param summary The AI-generated lesson summary.
     */
    public DoneLesson(ScheduledLesson lesson, Payment payment, LessonSummary summary,
                      boolean hasTrack, boolean hasSummary)
    {
        super(lesson.getLessonNumber(), lesson.getTeacherUID(), lesson.getStudentUID(),
                lesson.getStudentID(), lesson.getDateAndTime(), lesson.getDuration(),
                lesson.getStudentName());
        this.payment = payment;
        this.summary = summary;
        this.hasTrack = hasTrack;
        this.hasSummary = hasSummary;
    }

    /**
     * Constructor mapping from an existing ScheduledLesson.
     * Initializes payment and summary to their default empty states.
     *
     * @param lesson The original scheduled lesson.
     */
    public DoneLesson(ScheduledLesson lesson)
    {
        super(lesson.getLessonNumber(), lesson.getTeacherUID(), lesson.getStudentUID(),
                lesson.getStudentID(), lesson.getDateAndTime(), lesson.getDuration(),
                lesson.getStudentName());
        this.payment = new Payment();
        this.summary = new LessonSummary();
    }

    public void setPayment(Payment payment)
    {
        this.payment = payment;
    }

    public Payment getPayment()
    {
        return payment;
    }

    public LessonSummary getSummary()
    {
        return summary;
    }

    public void setSummary(LessonSummary summary)
    {
        this.summary = summary;
    }

    public void setHasTrack(boolean hasTrack)
    {
        this.hasTrack = hasTrack;
    }

    public boolean getHasTrack()
    {
        return hasTrack;
    }

    public void setHasSummary(boolean hasSummary)
    {
        this.hasSummary = hasSummary;
    }

    public boolean getHasSummary()
    {
        return hasSummary;
    }
}