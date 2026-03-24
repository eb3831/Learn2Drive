package com.example.learn2drive.Fragments;

import static com.example.learn2drive.Helpers.Prompts.ID_CARD_SCHEMA;
import static com.example.learn2drive.Helpers.Prompts.LESSON_SUMMARY_SCHEMA;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.learn2drive.Helpers.AudioRecorderHelper;
import com.example.learn2drive.Helpers.GeminiCallBack;
import com.example.learn2drive.Helpers.GeminiManager;
import com.example.learn2drive.Helpers.LocationTrackingHelper;
import com.example.learn2drive.Helpers.Prompts;
import com.example.learn2drive.Objects.ScheduledLesson;
import com.example.learn2drive.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment responsible for managing an active driving lesson.
 * Handles UI updates, timer, audio recording, and ending the lesson.
 */
public class ActiveLessonFragment extends Fragment implements OnMapReadyCallback,
        LocationTrackingHelper.LocationUpdateListener
{
    private static final int REQUEST_PERMISSIONS_CODE = 300;

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

    private GoogleMap mMap;

    private LocationTrackingHelper locationHelper;
    private ArrayList<Location> recordedLocations;
    private Polyline routePolyline;
    private FusedLocationProviderClient fusedLocationClient;

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
        setupMap();
        checkPermissionsAndStart();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        recordedLocations = new ArrayList<>();
    }

    /**
     * Initializes the Google Map fragment using the child fragment manager.
     */
    private void setupMap()
    {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null)
        {
            mapFragment.getMapAsync(this);
        }
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
     * Checks if both Audio and Location permissions are granted.
     * Starts the lesson immediately if granted, otherwise requests them.
     */
    private void checkPermissionsAndStart()
    {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        boolean allGranted = true;
        for (String permission : permissions)
        {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED)
            {
                allGranted = false;
                break;
            }
        }

        if (!allGranted)
        {
            requestPermissions(permissions, REQUEST_PERMISSIONS_CODE);
        } else
        {
            checkLocationEnabledAndStart();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE)
        {
            boolean allGranted = true;
            if (grantResults.length > 0)
            {
                for (int result : grantResults)
                {
                    if (result != PackageManager.PERMISSION_GRANTED)
                    {
                        allGranted = false;
                        break;
                    }
                }
            } else
            {
                allGranted = false;
            }

            if (allGranted)
            {
                checkLocationEnabledAndStart();
            } else
            {
                Toast.makeText(requireContext(), "Microphone and Location permissions are required for the lesson.", Toast.LENGTH_LONG).show();
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
        } catch (IOException e)
        {
            Toast.makeText(requireContext(), "Failed to start recording", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Prepares the environment for the active lesson: keeps the screen on,
     * enables the map's user location tracking, initializes tracking helper,
     * sets up the Polyline, and starts the timer/audio.
     */
    @SuppressLint("MissingPermission")
    private void startLessonAndMap()
    {
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (mMap != null)
        {
            mMap.setMyLocationEnabled(true);

            // Set up the Polyline (the red line that will be drawn on the map)
            com.google.android.gms.maps.model.PolylineOptions polylineOptions = new com.google.android.gms.maps.model.PolylineOptions()
                    .color(android.graphics.Color.RED)
                    .width(12f)
                    .geodesic(true);
            routePolyline = mMap.addPolyline(polylineOptions);

            // Get the last known location and zoom the camera to it
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location ->
            {
                if (location != null)
                {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f));
                }
            });
        }

        // Initialize and start our custom location helper
        locationHelper = new LocationTrackingHelper(requireContext(), this);
        locationHelper.startTracking();

        startLesson();
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
            } else
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
        } else
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

        if (locationHelper != null)
        {
            locationHelper.stopTracking();
        }

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
                            .setPositiveButton("Save to Firebase", (dialog, which) ->
                            {
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
        if (getActivity() != null)
        {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (locationHelper != null)
        {
            locationHelper.stopTracking();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap)
    {
        mMap = googleMap;
    }

    /**
     * Checks if the device's location services (GPS or Network) are currently enabled.
     *
     * @return true if enabled, false otherwise.
     */
    private boolean isLocationEnabled()
    {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null)
        {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        return false;
    }

    /**
     * Prompts the user to enable location services by directing them to the device settings.
     * If the user cancels, the lesson cannot proceed and the fragment is closed.
     */
    private void promptEnableLocation()
    {
        new AlertDialog.Builder(requireContext())
                .setTitle("Enable Location Services")
                .setMessage("GPS is required to track the driving route. Please enable it in the settings.")
                .setPositiveButton("Settings", (dialog, which) ->
                {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) ->
                {
                    Toast.makeText(requireContext(), "Location services are required to start the lesson.", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Verifies if the GPS is turned on before starting the lesson.
     * If off, prompts the user to turn it on.
     */
    private void checkLocationEnabledAndStart()
    {
        if (!isLocationEnabled())
        {
            promptEnableLocation();
        } else
        {
            startLessonAndMap();
        }
    }

    /**
     * Callback triggered by LocationTrackingHelper whenever a new location is available.
     * Records the location and updates the map polyline if the lesson is not paused.
     *
     * @param location The new location object.
     */
    @Override
    public void onLocationUpdated(Location location)
    {
        if (isTimerRunning)
        {
            recordedLocations.add(location);

            if (mMap != null && routePolyline != null)
            {
                // Get the current points of the polyline, add the new one, and update it
                List<LatLng> points = routePolyline.getPoints();
                LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
                points.add(newPoint);
                routePolyline.setPoints(points);

                // Smoothly move the camera to follow the car
                mMap.animateCamera(CameraUpdateFactory.newLatLng(newPoint));
            }
        }
    }
}