package com.example.learn2drive;

import java.util.Date;

public class User
{
    private String UserID;
    private String fullName;
    private Date BirthDate;
    private String phoneNumber;
    private String Role;
    private boolean active;

    public User()
    {
        this.UserID = "";
        this.fullName = "";
        this.BirthDate = new Date();
        this.phoneNumber = "";
        this.Role = "";
        this.active = true;
    }

    public User(String UserID, String fullName, Date BirthDate, String phoneNumber, String Role,
                boolean active)
    {
        this.UserID = UserID;
        this.fullName = fullName;
        this.BirthDate = BirthDate;
        this.phoneNumber = phoneNumber;
        this.Role = Role;
        this.active = active;
    }

    public String getUserID()
    {
        return UserID;
    }

    public void setUserID(String userID)
    {
        UserID = userID;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setBirthDate(Date BirthDate)
    {
        this.BirthDate = BirthDate;
    }

    public Date getBirthDate()
    {
        return BirthDate;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setRole(String Role)
    {
        this.Role = Role;
    }

    public String getRole()
    {
        return Role;
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
