package com.example.learn2drive.Objects;

public class User
{
    public static final int STUDENT = 0;
    public static final int TEACHER = 1;
    public static final int ADMIN = 2;

    public static final String ACTIVE = "ACTIVE";
    public static final String PENDING = "PENDING";
    public static final String ARCHIVED = "ARCHIVED";

    protected int role;  // 0 - Student, 1 - Teacher, 2 - Admin
    protected String uid;
    protected String idNumber;
    protected String fullName;
    protected String birthDate;
    protected String phoneNumber;
    protected String status;

    public User()
    {
        this.role = 0;
        this.uid = "";
        this.idNumber = "";
        this.fullName = "";
        this.birthDate = "";
        this.phoneNumber = "";
        this.status = "";
    }

    public User(String uid, String idNumber, String fullName, String birthDate, String phoneNumber,
                String status, int role)
    {
        this.role = role;
        this.uid = uid;
        this.idNumber = idNumber;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public int getRole()
    {
        return role;
    }

    public void setRole(int role)
    {
        this.role = role;
    }

    public String getUid()
    {
        return uid;
    }

    public void setUid(String uid)
    {
        this.uid = uid;
    }

    public String getIdNumber()
    {
        return idNumber;
    }

    public void setIdNumber(String idNumber)
    {
        this.idNumber = idNumber;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setBirthDate(String BirthDate)
    {
        this.birthDate = BirthDate;
    }

    public String getBirthDate()
    {
        return birthDate;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

}
