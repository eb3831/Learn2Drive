package com.example.learn2drive.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.learn2drive.Helpers.NotificationHelper;

/**
 * BroadcastReceiver triggered by the AlarmManager to show a lesson reminder notification.
 */
public class LessonReminderReceiver extends BroadcastReceiver
{
    private static final String TAG = "LessonReminderReceiver";

    /**
     * Called when the BroadcastReceiver is receiving an Intent broadcast.
     * Extracts lesson details from the intent and triggers a notification via NotificationHelper.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received, containing lesson time, notification ID, and reminder type.
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "Alarm triggered! Showing notification.");

        String lessonTime = intent.getStringExtra("LESSON_TIME");
        int notificationId = intent.getIntExtra("NOTIFICATION_ID", 0);
        String reminderType = intent.getStringExtra("REMINDER_TYPE");

        if (lessonTime == null)
        {
            lessonTime = "your scheduled time";
        }

        if (reminderType == null)
        {
            reminderType = "24H";
        }

        NotificationHelper.showLessonReminderNotification(context, notificationId, lessonTime, reminderType);
    }
}