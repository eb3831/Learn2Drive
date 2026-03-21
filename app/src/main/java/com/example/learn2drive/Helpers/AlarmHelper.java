package com.example.learn2drive.Helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.learn2drive.Objects.ScheduledLesson;
import com.example.learn2drive.Receivers.LessonReminderReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Helper class responsible for calculating and scheduling alarms for upcoming lessons.
 */
public class AlarmHelper
{
    private static final String TAG = "AlarmHelper";

    /**
     * Schedules local alarms to trigger 24 hours and 1 hour before the specified lesson.
     */
    public static void scheduleLessonReminder(Context context, ScheduledLesson lesson)
    {
        try
        {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            Date lessonDate = formatter.parse(lesson.getDateAndTime());

            if (lessonDate == null) return;

            long lessonTimeMillis = lessonDate.getTime();
            long currentTimeMillis = System.currentTimeMillis();

            long time24HoursBefore = lessonTimeMillis - (24 * 60 * 60 * 1000);
            long time1HourBefore = lessonTimeMillis - (60 * 60 * 1000);

            String fullDateAndTime = lesson.getDateAndTime();
            String timeOnly = fullDateAndTime;
            if (fullDateAndTime != null && fullDateAndTime.contains(" "))
            {
                String[] parts = fullDateAndTime.split(" ");
                if (parts.length > 1)
                {
                    timeOnly = parts[1];
                }
            }

            int baseNotificationId = lesson.getDateAndTime().hashCode();

            if (currentTimeMillis < time24HoursBefore)
            {
                setAlarm(context, time24HoursBefore, timeOnly, baseNotificationId, "24H");
            }

            if (currentTimeMillis < time1HourBefore)
            {
                setAlarm(context, time1HourBefore, timeOnly, baseNotificationId + 1, "1H");
            }
        }
        catch (ParseException e)
        {
            Log.e(TAG, "Error parsing lesson date: " + e.getMessage());
        }
    }

    /**
     * Private helper method to actually schedule the alarm.
     */
    private static void setAlarm(Context context, long triggerTime, String timeOnly, int notificationId, String reminderType)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, LessonReminderReceiver.class);
        intent.putExtra("LESSON_TIME", timeOnly);
        intent.putExtra("NOTIFICATION_ID", notificationId);
        intent.putExtra("REMINDER_TYPE", reminderType);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                flags
        );

        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        Log.d(TAG, "Alarm scheduled successfully for type " + reminderType + " at " + new Date(triggerTime).toString());
    }
}