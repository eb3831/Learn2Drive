package com.example.learn2drive.Objects;

public class User
{
    private String uid;
    private String idNumber;
    private String fullName;
    private String birthDate;
    private String phoneNumber;
    private boolean active;

    public User()
    {
        this.uid = "";
        this.idNumber = "";
        this.fullName = "";
        this.birthDate = "";
        this.phoneNumber = "";
        this.active = true;
    }

    public User(String uid, String idNumber, String fullName, String birthDate, String phoneNumber, boolean active)
    {
        this.uid = uid;
        this.idNumber = idNumber;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.active = active;
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

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public boolean isActive()
    {
        return active;
    }

}
