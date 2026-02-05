package com.example.learn2drive.Objects;

import java.util.Date;

public class User
{
    private String userID;
    private String fullName;
    private String birthDate;
    private String phoneNumber;
    private boolean active;

    public User()
    {
        this.userID = "";
        this.fullName = "";
        this.birthDate = "";
        this.phoneNumber = "";
        this.active = true;
    }

    public User(String UserID, String fullName, String BirthDate, String phoneNumber, boolean active)
    {
        this.userID = UserID;
        this.fullName = fullName;
        this.birthDate = BirthDate;
        this.phoneNumber = phoneNumber;
        this.active = active;
    }

    public String getUserID()
    {
        return userID;
    }

    public void setUserID(String userID)
    {
        userID = userID;
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
