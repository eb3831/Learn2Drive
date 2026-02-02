package com.example.learn2drive.Helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utilities
{
    /**
     * פעולה המקבלת מחרוזת ומחזירה אמת אם היא מייצגת כתובת אימייל תקינה
     *
     * @param email המחרוזת לבדיקה
     * @return true אם תקין, false אחרת
     */
    public static boolean isValidEmail(String email)
    {

        // בדיקה שהמחרוזת לא ריקה או null
        if (email == null || email.length() == 0)
        {
            return false;
        }

        // בדיקה שאין רווחים בכתובת
        if (email.indexOf(' ') != -1)
        {
            return false;
        }

        // בדיקת קיום התו '@' ומיקומו
        int atIndex = email.indexOf('@');
        int lastAtIndex = email.lastIndexOf('@');

        // אם אין @ בכלל, או שיש יותר מ-@ אחד (המיקום הראשון שונה מהאחרון)
        if (atIndex == -1 || atIndex != lastAtIndex)
        {
            return false;
        }

        // בדיקה שה-@ לא נמצא בהתחלה
        if (atIndex == 0)
        {
            return false;
        }

        // בדיקת קיום נקודה ומיקומה ביחס ל-@
        // אנו מחפשים את הנקודה האחרונה במחרוזת
        int dotIndex = email.lastIndexOf('.');

        // אם אין נקודה, או שהנקודה מופיעה לפני ה-@
        if (dotIndex == -1 || dotIndex < atIndex)
        {
            return false;
        }

        // בדיקה שהנקודה לא נמצאת מיד אחרי ה-@ (למשל name@.com)
        if (dotIndex == atIndex + 1)
        {
            return false;
        }

        // בדיקה שהנקודה אינה התו האחרון במחרוזת
        if (dotIndex == email.length() - 1)
        {
            return false;
        }

        // אם עברנו את כל הבדיקות - הכתובת נחשבת תקינה
        return true;
    }

    public static boolean isValidPassword(String password)
    {
        return password.length() >= 6;
    }

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
