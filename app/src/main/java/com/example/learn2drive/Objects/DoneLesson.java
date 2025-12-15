package com.example.learn2drive.Objects;

public class DoneLesson extends ScheduledLesson
{
    private Payment payment;

    public DoneLesson()
    {
        super();
        this.payment = new Payment();
    }

    public DoneLesson(int lessonNumber, String teacherID, String studentID, String DateAndTime,
                      int duration, int lessonStatus, Payment payment)
    {
        super(lessonNumber, teacherID, studentID, DateAndTime, duration, lessonStatus);
        this.payment = payment;
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
