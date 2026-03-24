package com.example.learn2drive.Objects;

public class DoneLesson extends ScheduledLesson
{
    private Payment payment;

    public DoneLesson()
    {
        super();
        this.payment = new Payment();
    }

    public DoneLesson(int lessonNumber, String teacherUID, String studentUID, String studentID,
                      String dateAndTime, int duration, String studentName, Payment payment)
    {
        super(lessonNumber, teacherUID, studentUID, studentID, dateAndTime, duration,
                studentName);
        this.payment = payment;
    }

    public DoneLesson(ScheduledLesson lesson, Payment payment)
    {
        super(lesson.getLessonNumber(), lesson.getTeacherUID(), lesson.getStudentUID(),
                lesson.getStudentID(), lesson.getDateAndTime(), lesson.getDuration(),
                lesson.getStudentName());
        this.payment = payment;
    }

    public DoneLesson(ScheduledLesson lesson)
    {
        super(lesson.getLessonNumber(), lesson.getTeacherUID(), lesson.getStudentUID(),
                lesson.getStudentID(), lesson.getDateAndTime(), lesson.getDuration(),
                lesson.getStudentName());
        this.payment = new Payment();
    }

    public void setPayment(Payment payment)
    {
        this.payment = payment;
    }

    public Payment getPayment()
    {
        return payment;
    }
}
