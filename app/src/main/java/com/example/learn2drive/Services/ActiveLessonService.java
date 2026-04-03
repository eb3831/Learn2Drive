package com.example.learn2drive.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.learn2drive.Helpers.AudioRecorderHelper;
import com.example.learn2drive.Helpers.LocationTrackingHelper;
import com.example.learn2drive.Objects.ScheduledLesson;
import com.example.learn2drive.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Foreground service that manages the active driving lesson in the background.
 * Handles the lesson timer, audio recording, and location tracking.
 */
public class ActiveLessonService extends Service implements LocationTrackingHelper.LocationUpdateListener
{
    private static final String CHANNEL_ID = "ActiveLessonChannel";
    private static final int NOTIFICATION_ID = 1;

    private boolean isAudioEnabled = false;
    private boolean isLocationEnabled = false;

    private AudioRecorderHelper audioRecorderHelper;
    private File audioFile;

    private Handler timerHandler;
    private Runnable timerRunnable;
    private long startTimeMillis;
    private long pausedTimeMillis = 0;
    private boolean isTimerRunning = false;
    private long currentElapsedMillis = 0;

    private LocationTrackingHelper locationHelper;
    private ArrayList<Location> recordedLocations;
    private ScheduledLesson currentLesson;

    private final IBinder binder = new LocalBinder();
    private LessonServiceListener listener;

    /**
     * Interface for communicating updates from the ActiveLessonService back to the bound UI.
     */
    public interface LessonServiceListener
    {
        /**
         * Called every second when the timer updates.
         *
         * @param formattedTime The formatted time string (e.g., "00:15:30").
         */
        void onTimerTick(String formattedTime);

        /**
         * Called whenever a new location is recorded.
         *
         * @param location The new Location object.
         */
        void onLocationUpdate(Location location);
    }

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends android.os.Binder
    {
        /**
         * Returns the instance of the service so clients can call public methods.
         *
         * @return The current instance of ActiveLessonService.
         */
        public ActiveLessonService getService()
        {
            return ActiveLessonService.this;
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        recordedLocations = new ArrayList<>();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null && intent.hasExtra("lesson_data"))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            {
                currentLesson = intent.getSerializableExtra("lesson_data", ScheduledLesson.class);
            }
            else
            {
                currentLesson = (ScheduledLesson) intent.getSerializableExtra("lesson_data");
            }
        }

        Notification notification = createNotification("Driving lesson is active");
        startForeground(NOTIFICATION_ID, notification);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    /**
     * Creates the notification channel required for foreground services on Android O and above.
     */
    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Active Lesson Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null)
            {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification createNotification(String contentText)
    {
        Intent notificationIntent = new Intent(this, com.example.learn2drive.Activities.TeacherMainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        notificationIntent.putExtra("RETURN_TO_ACTIVE_LESSON", true);

        if (currentLesson != null)
        {
            notificationIntent.putExtra("lesson_data", currentLesson);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                android.app.PendingIntent.FLAG_IMMUTABLE | android.app.PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Learn2Drive")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    public void initializeLessonCapabilities(boolean audioEnabled, boolean locationEnabled, String filePath, com.example.learn2drive.Objects.ScheduledLesson lesson)
    {
        this.isAudioEnabled = audioEnabled;
        this.isLocationEnabled = locationEnabled;
        this.currentLesson = lesson;

        if (isAudioEnabled && filePath != null)
        {
            this.audioFile = new File(filePath);
            this.audioRecorderHelper = new AudioRecorderHelper(this.audioFile.getAbsolutePath());
        }

        if (isLocationEnabled)
        {
            this.locationHelper = new LocationTrackingHelper(this, this);
        }
    }

    /**
     * Starts the lesson functionalities including timer, audio recording, and location tracking.
     */
    public void startLesson()
    {
        if (isAudioEnabled && audioRecorderHelper != null)
        {
            try
            {
                if (audioFile.exists())
                {
                    audioFile.delete();
                }
                audioRecorderHelper.startRecording();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                isAudioEnabled = false;
            }
        }

        if (isLocationEnabled && locationHelper != null)
        {
            locationHelper.startTracking();
        }

        startTimer();
    }

    /**
     * Pauses the lesson functionalities.
     */
    public void pauseLesson()
    {
        if (isAudioEnabled && audioRecorderHelper != null && audioRecorderHelper.isRecording())
        {
            audioRecorderHelper.pauseRecording();
        }
        pauseTimer();
    }

    /**
     * Resumes the lesson functionalities after being paused.
     */
    public void resumeLesson()
    {
        if (isAudioEnabled && audioRecorderHelper != null && audioRecorderHelper.isPaused())
        {
            audioRecorderHelper.resumeRecording();
        }
        resumeTimer();
    }

    /**
     * Stops all lesson functionalities completely.
     */
    public void stopLesson()
    {
        pauseTimer();
        if (isAudioEnabled && audioRecorderHelper != null)
        {
            audioRecorderHelper.stopRecording();
        }
        if (isLocationEnabled && locationHelper != null)
        {
            locationHelper.stopTracking();
        }
        stopForeground(true);
        stopSelf();
    }

    /**
     * Initializes and starts the internal timer.
     */
    private void startTimer()
    {
        timerHandler = new Handler(Looper.getMainLooper());
        startTimeMillis = System.currentTimeMillis();
        isTimerRunning = true;

        timerRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                if (isTimerRunning)
                {
                    currentElapsedMillis = System.currentTimeMillis() - startTimeMillis + pausedTimeMillis;

                    if (listener != null)
                    {
                        listener.onTimerTick(formatTime(currentElapsedMillis));
                    }

                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void pauseTimer()
    {
        if (isTimerRunning)
        {
            isTimerRunning = false;
            timerHandler.removeCallbacks(timerRunnable);
            pausedTimeMillis += System.currentTimeMillis() - startTimeMillis;
        }
    }

    private void resumeTimer()
    {
        if (!isTimerRunning)
        {
            isTimerRunning = true;
            startTimeMillis = System.currentTimeMillis();
            timerHandler.post(timerRunnable);
        }
    }

    @Override
    public void onLocationUpdated(Location location)
    {
        if (isTimerRunning)
        {
            recordedLocations.add(location);

            if (listener != null)
            {
                listener.onLocationUpdate(location);
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        stopLesson();

        if (timerHandler != null)
        {
            timerHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * Sets the listener that will receive UI updates from the service.
     *
     * @param listener The listener instance, or null to clear it.
     */
    public void setListener(LessonServiceListener listener)
    {
        this.listener = listener;
    }

    /**
     * Formats milliseconds into a standard HH:MM:SS string.
     *
     * @param millis The time in milliseconds.
     * @return A formatted time string.
     */
    private String formatTime(long millis)
    {
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;
        return String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    // --- Getters for the Fragment to use later ---

    public ArrayList<Location> getRecordedLocations()
    {
        return recordedLocations;
    }

    public File getAudioFile()
    {
        return audioFile;
    }

    public long getCurrentElapsedMillis()
    {
        return currentElapsedMillis;
    }

    public boolean isLessonTimerRunning()
    {
        return isTimerRunning;
    }
}