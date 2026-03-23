package com.example.learn2drive.Fragments;

import static com.example.learn2drive.Helpers.Prompts.ID_CARD_SCHEMA;
import static com.example.learn2drive.Helpers.Prompts.LESSON_SUMMARY_SCHEMA;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.learn2drive.Helpers.AudioRecorderHelper;
import com.example.learn2drive.Helpers.GeminiCallBack;
import com.example.learn2drive.Helpers.GeminiManager;
import com.example.learn2drive.Helpers.Prompts;
import com.example.learn2drive.Objects.ScheduledLesson;
import com.example.learn2drive.R;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Fragment responsible for managing an active driving lesson.
 * Handles UI updates, timer, audio recording, and ending the lesson.
 */
public class ActiveLessonFragment extends Fragment
{
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 300;

    private ImageView btnBack;
    private View vRecordingIndicator;
    private TextView tvRecordingStatus;
    private TextView tvTimer;
    private TextView tvActiveLessonStudent;
    private MaterialButton btnPauseRecording;
    private MaterialButton btnEndLesson;
    private FrameLayout activeLessonLoadingOverlay;

    private ScheduledLesson currentLesson;
    private AudioRecorderHelper audioRecorderHelper;
    private File audioFile;

    private Handler timerHandler;
    private Runnable timerRunnable;
    private long startTimeMillis;
    private long pausedTimeMillis = 0;
    private boolean isTimerRunning = false;

    public ActiveLessonFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param lesson The scheduled lesson object to be passed to the fragment.
     * @return A new instance of fragment ActiveLessonFragment.
     */
    public static ActiveLessonFragment newInstance(ScheduledLesson lesson)
    {
        ActiveLessonFragment fragment = new ActiveLessonFragment();
        Bundle args = new Bundle();
        args.putSerializable("lesson_data", lesson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_active_lesson, container, false);
        initializeViews(view);
        extractArguments();
        setupAudioRecorder();
        setupClickListeners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        checkPermissionsAndStart();
    }

    /**
     * Initializes all UI components from the inflated view.
     *
     * @param view The root view of the fragment.
     */
    private void initializeViews(View view)
    {
        btnBack = view.findViewById(R.id.btnBack);
        vRecordingIndicator = view.findViewById(R.id.vRecordingIndicator);
        tvRecordingStatus = view.findViewById(R.id.tvRecordingStatus);
        tvTimer = view.findViewById(R.id.tvTimer);
        tvActiveLessonStudent = view.findViewById(R.id.tvActiveLessonStudent);
        btnPauseRecording = view.findViewById(R.id.btnPauseRecording);
        btnEndLesson = view.findViewById(R.id.btnEndLesson);
        activeLessonLoadingOverlay = view.findViewById(R.id.activeLessonLoadingOverlay);
    }

    /**
     * Extracts the ScheduledLesson object passed as an argument to this fragment.
     */
    private void extractArguments()
    {
        if (getArguments() != null)
        {
            currentLesson = (ScheduledLesson) getArguments().getSerializable("lesson_data");
            if (currentLesson != null)
            {
                tvActiveLessonStudent.setText("Lesson with " + currentLesson.getStudentName());
            }
        }
    }

    /**
     * Sets up the AudioRecorderHelper and defines the output file path.
     */
    private void setupAudioRecorder()
    {
        audioFile = new File(requireContext().getCacheDir(), "lesson_recording.m4a");
        audioRecorderHelper = new AudioRecorderHelper(audioFile.getAbsolutePath());
    }

    /**
     * Sets up click listeners for the buttons in the fragment.
     */
    private void setupClickListeners()
    {
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        btnPauseRecording.setOnClickListener(v -> handlePauseResumeClick());

        btnEndLesson.setOnClickListener(v -> showEndLessonDialog());
    }

    /**
     * Checks if the RECORD_AUDIO permission is granted.
     * Starts the lesson immediately if granted, otherwise requests the permission.
     */
    private void checkPermissionsAndStart()
    {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }
        else
        {
            startLesson();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                startLesson();
            }
            else
            {
                Toast.makeText(requireContext(), "Microphone permission is required to record the lesson.", Toast.LENGTH_LONG).show();
                requireActivity().onBackPressed();
            }
        }
    }

    /**
     * Starts the lesson timer and the audio recording.
     */
    private void startLesson()
    {
        if (audioFile != null && audioFile.exists())
        {
            audioFile.delete();
        }

        startTimer();
        try
        {
            audioRecorderHelper.startRecording();
            updateRecordingUI(true);
        }
        catch (IOException e)
        {
            Toast.makeText(requireContext(), "Failed to start recording", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the logic when the pause/resume button is clicked.
     */
    private void handlePauseResumeClick()
    {
        if (audioRecorderHelper.isRecording())
        {
            if (audioRecorderHelper.isPaused())
            {
                audioRecorderHelper.resumeRecording();
                resumeTimer();
                btnPauseRecording.setText("Pause Recording");
                updateRecordingUI(true);
            }
            else
            {
                audioRecorderHelper.pauseRecording();
                pauseTimer();
                btnPauseRecording.setText("Resume Recording");
                updateRecordingUI(false);
            }
        }
    }

    /**
     * Updates the UI elements related to the recording status (indicator and text).
     *
     * @param isRecording Active state of the recording.
     */
    private void updateRecordingUI(boolean isRecording)
    {
        if (isRecording)
        {
            vRecordingIndicator.setBackgroundResource(R.drawable.bg_circle_red); // Assuming this exists
            tvRecordingStatus.setText("Recording");
            tvRecordingStatus.setTextColor(Color.parseColor("#D1D5DB"));
        }
        else
        {
            vRecordingIndicator.setBackgroundColor(Color.parseColor("#808080")); // Gray out when paused
            tvRecordingStatus.setText("Paused");
            tvRecordingStatus.setTextColor(Color.parseColor("#808080"));
        }
    }

    /**
     * Displays a confirmation dialog before ending the lesson.
     */
    private void showEndLessonDialog()
    {
        new AlertDialog.Builder(requireContext())
                .setTitle("End Lesson")
                .setMessage("Are you sure you want to end this lesson?")
                .setPositiveButton("Yes", (dialog, which) -> processLessonEnd())
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Handles the final steps of ending a lesson: stopping timer and recording,
     * displaying the loading overlay, and sending the audio to Gemini for summarization.
     */
    private void processLessonEnd()
    {
        pauseTimer();
        audioRecorderHelper.stopRecording();
        updateRecordingUI(false);
        tvRecordingStatus.setText("Processing...");

        // Show loading screen so the user can't click anything else
        activeLessonLoadingOverlay.setVisibility(View.VISIBLE);

        String finalPrompt = Prompts.LESSON_SUMMARY_PROMPT +
                "\n\nReturn the data strictly according to this JSON schema:\n" +
                LESSON_SUMMARY_SCHEMA;

        if (!audioFile.exists() || audioFile.length() == 0)
        {
            activeLessonLoadingOverlay.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Error: Audio file was not saved properly.", Toast.LENGTH_LONG).show();
            return;
        }

        // Send to Gemini
        GeminiManager.getInstance().sendAudioPrompt(finalPrompt, audioFile, new GeminiCallBack()
        {
            @Override
            public void onSuccess(String result)
            {
                requireActivity().runOnUiThread(() ->
                {
                    activeLessonLoadingOverlay.setVisibility(View.GONE);

                    new AlertDialog.Builder(requireContext())
                            .setTitle("Lesson Summary")
                            .setMessage(result)
                            .setPositiveButton("Save to Firebase", (dialog, which) -> {
                                // TODO: Step 6 - Save 'result' and 'audioFile' to Firebase
                                Toast.makeText(requireContext(), "Preparing to save...", Toast.LENGTH_SHORT).show();
                            })
                            .setCancelable(false)
                            .show();
                });
            }

            @Override
            public void onFailure(Throwable error)
            {
                requireActivity().runOnUiThread(() ->
                {
                    activeLessonLoadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Failed to summarize lesson: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Initializes and starts the timer.
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
                    long elapsedMillis = System.currentTimeMillis() - startTimeMillis + pausedTimeMillis;
                    int seconds = (int) (elapsedMillis / 1000);
                    int minutes = seconds / 60;
                    int hours = minutes / 60;
                    seconds = seconds % 60;
                    minutes = minutes % 60;

                    tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        timerHandler.post(timerRunnable);
    }

    /**
     * Pauses the timer by stopping the handler callbacks.
     */
    private void pauseTimer()
    {
        if (isTimerRunning)
        {
            isTimerRunning = false;
            timerHandler.removeCallbacks(timerRunnable);
            pausedTimeMillis += System.currentTimeMillis() - startTimeMillis;
        }
    }

    /**
     * Resumes the timer.
     */
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
    public void onDestroyView()
    {
        super.onDestroyView();
        if (timerHandler != null)
        {
            timerHandler.removeCallbacks(timerRunnable);
        }
        if (audioRecorderHelper != null)
        {
            audioRecorderHelper.stopRecording();
        }
    }
}