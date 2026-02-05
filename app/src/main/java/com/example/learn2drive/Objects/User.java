package com.example.learn2drive.Objects;

import java.util.Date;

public class User
{
    private String userId;
    private String fullName;
    private String birthDate;
    private String phoneNumber;
    private boolean active;

    public User()
    {
        this.userId = "";
        this.fullName = "";
        this.birthDate = "";
        this.phoneNumber = "";
        this.active = true;
    }

    public User(String userId, String fullName, String birthDate, String phoneNumber, boolean active)
    {
        this.userId = userId;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.active = active;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
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
