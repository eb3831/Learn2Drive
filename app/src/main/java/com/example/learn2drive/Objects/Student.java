package com.example.learn2drive.Objects;

/**
 * Represents a student in the system, extending the basic User profile.
 * This class includes specific driving-related data such as the number of lessons
 * completed and the assigned driving instructor.
 */
public class Student extends User
{
    private int lessonsCompleted;
    private String teacherUid;
    private String teacherName;

    /**
     * Default constructor for Student.
     * Calls the super constructor of the User class and initializes student-specific fields to default values.
     */
    public Student()
    {
        super();
        this.lessonsCompleted = 0;
        this.teacherUid = "";
        this.teacherName = "";
    }

    /**
     * Parameterized constructor to create a full Student profile.
     * @param uid The unique Firebase ID.
     * @param idNumber The student's Israeli ID number.
     * @param fullName The student's full name.
     * @param BirthDate The student's date of birth.
     * @param phoneNumber The student's phone number.
     * @param status The student's account status.
     * @param role The user's role index.
     * @param lessonsCompleted Total number of lessons the student has finished.
     * @param teacherUid The unique ID of the assigned teacher.
     * @param teacherName The name of the assigned teacher.
     */
    public Student(String uid, String idNumber, String fullName, String BirthDate, String phoneNumber,
                   String status, int role, int lessonsCompleted, String teacherUid,
                   String teacherName)
    {
        super(uid, idNumber, fullName, BirthDate, phoneNumber, status, role);
        this.lessonsCompleted = lessonsCompleted;
        this.teacherUid = teacherUid;
        this.teacherName = teacherName;
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

    public void setTeacherName(String teacherName)
    {
        this.teacherName = teacherName;
    }

    public String getTeacherName()
    {
        return teacherName;
    }
}