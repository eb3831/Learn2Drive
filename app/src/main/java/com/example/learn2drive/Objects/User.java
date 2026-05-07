package com.example.learn2drive.Objects;

/**
 * Base class representing a generic user in the system.
 * This class serves as a parent for Student, Teacher, and Admin classes,
 * centralizing common identity and contact information.
 */
public class User
{
    public static final int STUDENT = 0;
    public static final int TEACHER = 1;
    public static final int ADMIN = 2;

    public static final String ACTIVE = "ACTIVE";
    public static final String PENDING = "PENDING";
    public static final String ARCHIVED = "ARCHIVED";
    public static final String REJECTED = "REJECTED";

    protected int role;  // 0 - Student, 1 - Teacher, 2 - Admin
    protected String uid;
    protected String idNumber;
    protected String fullName;
    protected String birthDate;
    protected String phoneNumber;
    protected String status;

    /**
     * Default constructor for User.
     * Initializes a student role with empty strings for all identity fields.
     * Required for Firebase Realtime Database operations.
     */
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

    /**
     * Parameterized constructor to create a complete User profile.
     * @param uid The unique Firebase Authentication UID.
     * @param idNumber The user's Israeli ID number.
     * @param fullName The user's full name.
     * @param birthDate The user's date of birth in dd.MM.yyyy format.
     * @param phoneNumber The user's contact phone number.
     * @param status The current status of the account (e.g., ACTIVE).
     * @param role The numeric role identifier (0, 1, or 2).
     */
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
