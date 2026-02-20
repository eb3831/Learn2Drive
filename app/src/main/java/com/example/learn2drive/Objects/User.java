package com.example.learn2drive.Objects;

public class User
{
    protected int role;  // 0 - Student, 1 - Teacher, 2 - Admin
    protected String uid;
    protected String idNumber;
    protected String fullName;
    protected String birthDate;
    protected String phoneNumber;
    protected boolean active;
    protected boolean approved;

    public User()
    {
        this.role = 0;
        this.uid = "";
        this.idNumber = "";
        this.fullName = "";
        this.birthDate = "";
        this.phoneNumber = "";
        this.active = true;
        this.approved = false;
    }

    public User(String uid, String idNumber, String fullName, String birthDate, String phoneNumber,
                boolean active, boolean approved, int role)
    {
        this.role = role;
        this.uid = uid;
        this.idNumber = idNumber;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.active = active;
        this.approved = approved;
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

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public boolean isActive()
    {
        return active;
    }

    public boolean isApproved()
    {
        return approved;
    }

    public void setApproved(boolean approved)
    {
        this.approved = approved;
    }

}
