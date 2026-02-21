package com.example.learn2drive.Objects;

public class Student extends User
{
    private int lessonsCompleted;
    private String teacherUid;

    public Student()
    {
        super();
        this.lessonsCompleted = 0;
        this.teacherUid = "";
    }

    public Student(String uid, String idNumber, String fullName, String BirthDate, String phoneNumber,
                   String status, int role, int lessonsCompleted, String teacherUid)
    {
        super(uid, idNumber, fullName, BirthDate, phoneNumber, status, role);
        this.lessonsCompleted = lessonsCompleted;
        this.teacherUid = teacherUid;
    }

    public void setLessonsCompleted(int lessonsCompleted)
    {
        this.lessonsCompleted = lessonsCompleted;
    }

    public int getLessonsCompleted()
    {
        return lessonsCompleted;
    }

    public void setTeacherUid(String teacherUid)
    {
        this.teacherUid = teacherUid;
    }

    public String getTeacherUid()
    {
        return teacherUid;
    }
}
