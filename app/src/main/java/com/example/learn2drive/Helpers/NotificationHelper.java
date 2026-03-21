package com.example.learn2drive.Helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.learn2drive.R;

/**
 * Helper class for managing and displaying system notifications.
 */
public class NotificationHelper
{
    private static final String REMINDER_CHANNEL_ID = "Lesson_Reminder_ID";
    private static final String REMINDER_CHANNEL_NAME = "Lesson Reminders";

    /**
     * Displays a notification reminding the student of an upcoming driving lesson.
     */
    public static void showLessonReminderNotification(Context context, int notificationId, String lessonTime, String reminderType)
    {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(
                    REMINDER_CHANNEL_ID,
                    REMINDER_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }

        String title = "";
        String text = "";

        if (reminderType.equals("1H"))
        {
            title = "Driving Lesson in 1 Hour!";
            text = "Get ready! Your driving lesson starts at " + lessonTime + ".";
        }

        else
        {
            title = "Driving Lesson Tomorrow!";
            text = "You have a driving lesson scheduled tomorrow at " + lessonTime + ". Be ready!";
        }

        NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true);

        notificationManager.notify(notificationId, notiBuilder.build());
    }
}