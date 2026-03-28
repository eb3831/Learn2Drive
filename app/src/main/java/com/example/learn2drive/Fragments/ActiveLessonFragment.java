package com.example.learn2drive.Fragments;

import static com.example.learn2drive.Helpers.FBRef.refDoneLessons;
import static com.example.learn2drive.Helpers.FBRef.refLessonsDetails;
import static com.example.learn2drive.Helpers.FBRef.refScheduledLessons;
import static com.example.learn2drive.Helpers.Prompts.LESSON_SUMMARY_SCHEMA;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.learn2drive.Activities.MasterActivity;
import com.example.learn2drive.Activities.TeacherMainActivity;
import com.example.learn2drive.Helpers.AudioRecorderHelper;
import com.example.learn2drive.Helpers.GeminiCallBack;
import com.example.learn2drive.Helpers.GeminiManager;
import com.example.learn2drive.Helpers.GpxGeneratorHelper;
import com.example.learn2drive.Helpers.LocationTrackingHelper;
import com.example.learn2drive.Helpers.Prompts;
import com.example.learn2drive.Objects.DoneLesson;
import com.example.learn2drive.Objects.LessonSummary;
import com.example.learn2drive.Objects.Payment;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

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
    private static final int REQUEST_ENABLE_GPS = 1001;

    private ImageView btnBack;
    private View vRecordingIndicator;
    private TextView tvRecordingStatus;
    private TextView tvTimer;
    private TextView tvActiveLessonStudent;
    private MaterialButton btnPauseRecording;
    private MaterialButton btnEndLesson;
    private FrameLayout activeLessonLoadingOverlay;
    private LinearLayout llAudioBadge;
    private LinearLayout llLocationBadge;

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

    private boolean isAudioEnabledForLesson = false;
    private boolean isLocationEnabledForLesson = false;

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
        llAudioBadge = view.findViewById(R.id.llAudioBadge);
        llLocationBadge = view.findViewById(R.id.llLocationBadge);
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
        btnBack.setOnClickListener(v -> Toast.makeText(requireContext(),
                "You can't leave this screen while a lesson is active, yet!",
                Toast.LENGTH_SHORT).show());

        btnPauseRecording.setOnClickListener(v -> handlePauseResumeClick());

        btnEndLesson.setOnClickListener(v -> showEndLessonDialog());
    }

    /**
     * Checks for required permissions (Audio and Location).
     * If permissions are missing, it requests them.
     * If they are already granted, it proceeds to evaluate the current permission state.
     */
    private void checkPermissionsAndStart()
    {
        boolean hasAudioPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean hasLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        List<String> permissionsToRequest = new ArrayList<>();

        if (!hasAudioPermission)
        {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO);
        }
        if (!hasLocationPermission)
        {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!permissionsToRequest.isEmpty())
        {
            requestPermissions(permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSIONS_CODE);
        }
        else
        {
            // All previously requested permissions are granted (or were already granted)
            evaluatePermissionsAndStart(true, true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS_CODE)
        {
            boolean audioGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
            boolean locationGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            evaluatePermissionsAndStart(audioGranted, locationGranted);
        }
    }

    /**
     * Evaluates the granted permissions and decides how to start the lesson.
     * It also checks if the GPS hardware is actually enabled if location permission is granted.
     *
     * @param hasAudio    True if audio recording permission is granted.
     * @param hasLocation True if location access permission is granted.
     */
    private void evaluatePermissionsAndStart(boolean hasAudio, boolean hasLocation)
    {
        if (hasLocation)
        {
            if (!isLocationEnabled())
            {
                // Has permission, but GPS is turned off. Prompt the user.
                // We pass the audio state so the prompt knows what fallback to use.
                promptEnableLocation(hasAudio);
                return;
            }
        }

        // Proceed to start the lesson with the finalized capabilities
        startLessonWithCapabilities(hasAudio, hasLocation);
    }

    /**
     * Starts the lesson timer and the audio recording.
     * This is the SINGLE point where the lesson actually starts to prevent race conditions.
     */
    private void startLesson()
    {
        if (isAudioEnabledForLesson)
        {
            if (audioFile != null && audioFile.exists())
            {
                audioFile.delete();
            }
            try
            {
                audioRecorderHelper.startRecording();
                updateRecordingUI(true);
            }
            catch (IOException e)
            {
                Log.e("ActiveLesson", "Error starting recording: ", e);
                Toast.makeText(requireContext(), "Failed to start recording", Toast.LENGTH_SHORT).show();
                isAudioEnabledForLesson = false;
                llAudioBadge.setVisibility(View.GONE);
                updateRecordingUI(false);
            }
        }

        startTimer();
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
     * Handles the final steps of ending a lesson.
     * Stops tracking, recording, and delegates to Gemini or directly saves depending on capabilities.
     */
    private void processLessonEnd()
    {
        pauseTimer();
        activeLessonLoadingOverlay.setVisibility(View.VISIBLE);

        if (isLocationEnabledForLesson && locationHelper != null)
        {
            locationHelper.stopTracking();
        }

        if (isAudioEnabledForLesson)
        {
            audioRecorderHelper.stopRecording();
            updateRecordingUI(false);
            tvRecordingStatus.setText("Processing...");

            if (!audioFile.exists() || audioFile.length() == 0)
            {
                activeLessonLoadingOverlay.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: Audio file was not saved properly.", Toast.LENGTH_LONG).show();
                uploadGpxAndSaveLesson("{}");
                return;
            }

            String finalPrompt = Prompts.LESSON_SUMMARY_PROMPT +
                    "\n\nReturn the data strictly according to this JSON schema:\n" +
                    LESSON_SUMMARY_SCHEMA;

            GeminiManager.getInstance().sendAudioPrompt(finalPrompt, audioFile, new GeminiCallBack()
            {
                @Override
                public void onSuccess(String result)
                {
                    requireActivity().runOnUiThread(() ->
                    {
                        Toast.makeText(requireContext(), "Processing complete. Saving lesson...", Toast.LENGTH_SHORT).show();
                        uploadGpxAndSaveLesson(result);
                    });
                }

                @Override
                public void onFailure(Throwable error)
                {
                    requireActivity().runOnUiThread(() ->
                    {
                        Toast.makeText(requireContext(), "Failed to summarize lesson: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        uploadGpxAndSaveLesson("{}");
                    });
                }
            });
        }
        else
        {
            // No Audio -> Skip Gemini completely
            Toast.makeText(requireContext(), "No audio recorded. Saving lesson...", Toast.LENGTH_SHORT).show();
            uploadGpxAndSaveLesson("{}");
        }
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

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap)
    {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);

        PolylineOptions polylineOptions = new PolylineOptions()
                .color(android.graphics.Color.RED)
                .width(12f)
                .geodesic(true);
        routePolyline = mMap.addPolyline(polylineOptions);

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location ->
        {
            if (location != null)
            {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f));
            }
        });

        startLessonLogic();
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
     * Prompts the user to enable location services if the hardware is off.
     *
     * @param hasAudio Indicates if audio permission was granted, to pass it along.
     */
    private void promptEnableLocation(boolean hasAudio)
    {
        new AlertDialog.Builder(requireContext())
                .setTitle("Enable Location Services")
                .setMessage("GPS is required to track the driving route. Would you like to enable it in the settings?")
                .setPositiveButton("Settings", (dialog, which) ->
                {
                    // Temporarily store audio state to use when returning from settings
                    isAudioEnabledForLesson = hasAudio;
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, REQUEST_ENABLE_GPS);
                })
                .setNegativeButton("Continue without GPS", (dialog, which) ->
                {
                    Toast.makeText(requireContext(), "Starting without location tracking.", Toast.LENGTH_SHORT).show();
                    startLessonWithCapabilities(hasAudio, false);
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_GPS)
        {
            if (isLocationEnabled())
            {
                startLessonWithCapabilities(isAudioEnabledForLesson, true);
            }
            else
            {
                Toast.makeText(requireContext(), "GPS still disabled. Starting without location tracking.", Toast.LENGTH_SHORT).show();
                startLessonWithCapabilities(isAudioEnabledForLesson, false);
            }
        }
    }

    /**
     * Starts the lesson based on the finalized capabilities.
     * Updates UI badges, and coordinates the start of location tracking or starts the lesson immediately.
     *
     * @param audioEnabled    true if audio should be recorded.
     * @param locationEnabled true if location should be tracked.
     */
    private void startLessonWithCapabilities(boolean audioEnabled, boolean locationEnabled)
    {
        isAudioEnabledForLesson = audioEnabled;
        isLocationEnabledForLesson = locationEnabled;

        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        llAudioBadge.setVisibility(audioEnabled ? View.VISIBLE : View.GONE);
        llLocationBadge.setVisibility(locationEnabled ? View.VISIBLE : View.GONE);

        // Updates UI for NO AUDIO
        if (!audioEnabled)
        {
            vRecordingIndicator.setBackgroundColor(Color.parseColor("#808080"));
            tvRecordingStatus.setText("No Audio");
            tvRecordingStatus.setTextColor(Color.parseColor("#808080"));
            btnPauseRecording.setEnabled(false);
        }

        // Handles Location and Lesson Start
        if (locationEnabled)
        {
            setupMap();
        }
        else
        {
            startLesson();
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

    /**
     * Prepares the environment for the active lesson when location is enabled.
     * Initializes tracking helper, and delegates to startLesson().
     */
    private void startLessonLogic()
    {
        locationHelper = new LocationTrackingHelper(requireContext(), this);
        locationHelper.startTracking();

        // Start timer and audio
        startLesson();
    }

    /**
     * Processes the generated summary and decides whether to upload a GPX track
     * based on the lesson's location capabilities, then saves the final lesson data.
     * Ensures that in all scenarios, the lesson is saved to the database.
     *
     * @param geminiSummary The JSON summary generated by Gemini, or "{}" if unavailable.
     */
    private void uploadGpxAndSaveLesson(String geminiSummary)
    {
        LessonSummary tempSummary;
        try
        {
            // Clean the string from Markdown formatting if Gemini added it
            String cleanJson = geminiSummary;
            if (cleanJson.startsWith("```json"))
            {
                cleanJson = cleanJson.replace("```json", "").replace("```", "").trim();
            }

            else if (cleanJson.startsWith("```"))
            {
                cleanJson = cleanJson.replace("```", "").trim();
            }

            Gson gson = new Gson();
            tempSummary = gson.fromJson(cleanJson, LessonSummary.class);
            if (tempSummary == null)
            {
                tempSummary = new LessonSummary();
            }
        }
        catch (Exception e)
        {
            Log.e("ActiveLesson", "Gson parsing error: " + e.getMessage());
            tempSummary = new LessonSummary();
        }

        final LessonSummary finalParsedSummary = tempSummary;

        // Skip Storage upload if location tracking was disabled or no locations were recorded
        if (!isLocationEnabledForLesson || recordedLocations == null || recordedLocations.isEmpty())
        {
            saveLessonDataToDatabase(finalParsedSummary);
            return;
        }

        // Location tracking was enabled, proceed to generate and upload the GPX file
        File gpxFile = new File(requireContext().getCacheDir(), "track.gpx");
        try
        {
            GpxGeneratorHelper.generateGpxFile(recordedLocations, gpxFile);
        }
        catch (IOException e)
        {
            Toast.makeText(requireContext(), "Failed to generate track file. Saving lesson...", Toast.LENGTH_SHORT).show();
            saveLessonDataToDatabase(finalParsedSummary);
            return;
        }

        String teacherUid = currentLesson.getTeacherUID();
        String studentUid = currentLesson.getStudentUID();
        String dateTime = currentLesson.getDateAndTime();

        StorageReference trackRef = refLessonsDetails.child(teacherUid).child(studentUid)
                .child(dateTime).child("track.gpx");

        Uri fileUri = Uri.fromFile(gpxFile);

        trackRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> saveLessonDataToDatabase(finalParsedSummary))
                .addOnFailureListener(e ->
                {
                    Toast.makeText(requireContext(), "Failed to upload track, but saving lesson...", Toast.LENGTH_SHORT).show();
                    saveLessonDataToDatabase(finalParsedSummary);
                });
    }

    /**
     * Helper method to save the DoneLesson object to Realtime Database
     * and remove the original ScheduledLesson.
     *
     * @param parsedSummary The AI-generated summary object.
     */
    private void saveLessonDataToDatabase(LessonSummary parsedSummary)
    {
        String teacherUid = currentLesson.getTeacherUID();
        String studentUid = currentLesson.getStudentUID();
        String dateTime = currentLesson.getDateAndTime();

        DoneLesson doneLesson = new DoneLesson(currentLesson, new Payment(), parsedSummary,
                isLocationEnabledForLesson, isAudioEnabledForLesson);

        refDoneLessons.child(teacherUid).child(studentUid).child(dateTime).setValue(doneLesson)
                .addOnSuccessListener(aVoid ->
                {
                    // Removes the existing ScheduledLesson
                    refScheduledLessons.child(teacherUid).child(studentUid).child(dateTime).removeValue()
                            .addOnSuccessListener(aVoid1 ->
                            {
                                Toast.makeText(requireContext(), "Lesson saved successfully!", Toast.LENGTH_SHORT).show();
                                returnToHome();
                            })
                            .addOnFailureListener(e ->
                            {
                                Toast.makeText(requireContext(), "Lesson saved, but failed to remove from schedule: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                returnToHome();

                            });
                })
                .addOnFailureListener(e ->
                {
                    Toast.makeText(requireContext(), "Failed to save done lesson: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    returnToHome();
                });
    }

    /**
     * Clears the fragment back stack and returns the user to the home screen.
     */
    private void returnToHome()
    {
        TeacherMainActivity activity = (TeacherMainActivity) requireActivity();

        activity.clearStack();
        activity.replaceFragment(TeacherHomeFragment.newInstance(), false, "TeacherHomeFragment");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        ((MasterActivity) getActivity()).hideBottomNav();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        ((MasterActivity) getActivity()).showBottomNav();
    }
}