package com.example.learn2drive.Helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class providing helper methods for data validation and date calculations.
 * Contains logic for email verification, password strength, and age requirements.
 */
public class Utilities
{
    /**
     * Validates if a given string follows a standard email format.
     * Checks for the presence of '@' and '.', correct positioning, and absence of spaces.
     * @param email The email string to be validated.
     * @return true if the email format is valid, false otherwise.
     */
    public static boolean isValidEmail(String email)
    {

        if (email == null || email.length() == 0)
        {
            return false;
        }

        if (email.indexOf(' ') != -1)
        {
            return false;
        }

        int atIndex = email.indexOf('@');
        int lastAtIndex = email.lastIndexOf('@');

        if (atIndex == -1 || atIndex != lastAtIndex)
        {
            return false;
        }

        if (atIndex == 0)
        {
            return false;
        }

        int dotIndex = email.lastIndexOf('.');

        if (dotIndex == -1 || dotIndex < atIndex)
        {
            return false;
        }

        if (dotIndex == atIndex + 1)
        {
            return false;
        }

        if (dotIndex == email.length() - 1)
        {
            return false;
        }

        return true;
    }

    /**
     * Validates if the provided password meets the minimum length requirement.
     * @param password The password string to be checked.
     * @return true if the password is at least 6 characters long, false otherwise.
     */
    public static boolean isValidPassword(String password)
    {
        return password.length() >= 6;
    }

    /**
     * Checks if a user is old enough to start driving lessons (at least 16.5 years old).
     * Parses the date of birth string and compares it with the current date.
     * @param dateString The birth date in "dd.MM.yyyy" format.
     * @return true if the user's age is 16 years and 6 months or older, false otherwise.
     */
    public static boolean isUserOldEnough(String dateString)
    {
        String dateFormat = "dd.MM.yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        sdf.setLenient(false);

        try
        {
            Date birthDate = sdf.parse(dateString);
            Calendar dob = Calendar.getInstance();
            dob.setTime(birthDate);

            Calendar limitDate = Calendar.getInstance();
            limitDate.add(Calendar.YEAR, -16);
            limitDate.add(Calendar.MONTH, -6);

            return !dob.after(limitDate);
        }

        catch (Exception e)
        {
            return false;
        }
    }
}