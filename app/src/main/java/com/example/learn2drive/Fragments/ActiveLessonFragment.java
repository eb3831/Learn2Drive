package com.example.learn2drive.Fragments;

import static com.example.learn2drive.Helpers.Prompts.LESSON_SUMMARY_SCHEMA;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
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
import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Helpers.GeminiCallBack;
import com.example.learn2drive.Helpers.GeminiManager;
import com.example.learn2drive.Helpers.GpxHelpers;
import com.example.learn2drive.Helpers.Prompts;
import com.example.learn2drive.Objects.DoneLesson;
import com.example.learn2drive.Objects.LessonSummary;
import com.example.learn2drive.Objects.Payment;
import com.example.learn2drive.Objects.ScheduledLesson;
import com.example.learn2drive.R;
import com.example.learn2drive.Services.ActiveLessonService;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for managing an active driving lesson UI.
 * Binds to ActiveLessonService to display real-time timer and location updates.
 */
public class ActiveLessonFragment extends Fragment implements OnMapReadyCallback,
        ActiveLessonService.LessonServiceListener
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
    private GoogleMap mMap;
    private Polyline routePolyline;
    private FusedLocationProviderClient fusedLocationClient;

    private boolean isAudioEnabledForLesson = false;
    private boolean isLocationEnabledForLesson = false;

    // Service connection variables
    private ActiveLessonService activeLessonService;
    private boolean isBound = false;
    private boolean pendingLessonStart = false;

    /**
     * Handles the connection state between this fragment and the ActiveLessonService.
     */
    private final ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            ActiveLessonService.LocalBinder binder = (ActiveLessonService.LocalBinder) service;
            activeLessonService = binder.getService();
            isBound = true;
            activeLessonService.setListener(ActiveLessonFragment.this);

            if (activeLessonService.isLessonTimerRunning())
            {
                syncUIWithService();
            }
            else if (pendingLessonStart)
            {
                startServiceLogic();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            isBound = false;
            activeLessonService = null;
        }
    };

    private void startServiceLogic()
    {
        if (activeLessonService != null && !activeLessonService.isLessonTimerRunning())
        {
            File audioFile = new File(requireContext().getCacheDir(), "lesson_recording.m4a");
            activeLessonService.initializeLessonCapabilities(
                    isAudioEnabledForLesson,
                    isLocationEnabledForLesson,
                    audioFile.getAbsolutePath(),
                    currentLesson
            );
            activeLessonService.startLesson();
            pendingLessonStart = false;
            syncUIWithService();
        }
    }

    public ActiveLessonFragment()
    {
        // Required empty public constructor
    }

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
    }

    @Override
    public void onStart()
    {
        super.onStart();
        // Bind to the service when the fragment becomes visible
        Intent intent = new Intent(requireContext(), ActiveLessonService.class);
        requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        // Unbind from the service when the fragment is hidden
        if (isBound && activeLessonService != null)
        {
            activeLessonService.setListener(null);
            requireActivity().unbindService(serviceConnection);
            isBound = false;
        }
        ((MasterActivity) getActivity()).showBottomNav();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        ((MasterActivity) getActivity()).hideBottomNav();
    }

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

    private void setupClickListeners()
    {
        btnBack.setOnClickListener(v -> Toast.makeText(requireContext(),
                "You can navigate away, the lesson continues in the background!",
                Toast.LENGTH_SHORT).show());

        btnPauseRecording.setOnClickListener(v -> handlePauseResumeClick());
        btnEndLesson.setOnClickListener(v -> showEndLessonDialog());
    }

    // --- Permissions Logic remains largely the same ---
    private void checkPermissionsAndStart()
    {
        boolean hasAudioPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean hasLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        List<String> permissionsToRequest = new ArrayList<>();
        if (!hasAudioPermission) permissionsToRequest.add(Manifest.permission.RECORD_AUDIO);
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

    private void evaluatePermissionsAndStart(boolean hasAudio, boolean hasLocation)
    {
        if (hasLocation)
        {
            if (!isLocationEnabled())
            {
                promptEnableLocation(hasAudio);
                return;
            }
        }

        startLessonWithCapabilities(hasAudio, hasLocation);
    }

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

    private void promptEnableLocation(boolean hasAudio)
    {
        new AlertDialog.Builder(requireContext())
                .setTitle("Enable Location Services")
                .setMessage("GPS is required to track the driving route. Would you like to enable it in the settings?")
                .setPositiveButton("Settings", (dialog, which) ->
                {
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
     * Configures the UI and tells the Service to begin the lesson.
     */
    private void startLessonWithCapabilities(boolean audioEnabled, boolean locationEnabled)
    {
        isAudioEnabledForLesson = audioEnabled;
        isLocationEnabledForLesson = locationEnabled;

        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        llAudioBadge.setVisibility(audioEnabled ? View.VISIBLE : View.GONE);
        llLocationBadge.setVisibility(locationEnabled ? View.VISIBLE : View.GONE);

        if (!audioEnabled)
        {
            vRecordingIndicator.setBackgroundColor(Color.parseColor("#808080"));
            tvRecordingStatus.setText("No Audio");
            tvRecordingStatus.setTextColor(Color.parseColor("#808080"));
            btnPauseRecording.setEnabled(false);
        }

        if (locationEnabled)
        {
            setupMap();
        }

        Intent serviceIntent = new Intent(requireContext(), ActiveLessonService.class);
        serviceIntent.putExtra("lesson_data", currentLesson);
        androidx.core.content.ContextCompat.startForegroundService(requireContext(), serviceIntent);

        if (isBound && activeLessonService != null)
        {
            startServiceLogic();
        }
        else
        {
            pendingLessonStart = true;
        }
    }

    private void setupMap()
    {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null)
        {
            mapFragment.getMapAsync(this);
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

        // If we re-binded and there are existing points, draw them
        syncMapWithService();
    }

    /**
     * Synchronizes the UI elements with the current state of the bound Service.
     */
    private void syncUIWithService()
    {
        if (activeLessonService != null && activeLessonService.isLessonTimerRunning())
        {
            updateRecordingUI(true);
            syncMapWithService();
        }
    }

    /**
     * Redraws the polyline with all points collected by the service so far.
     */
    private void syncMapWithService()
    {
        if (mMap != null && routePolyline != null && activeLessonService != null)
        {
            ArrayList<Location> history = activeLessonService.getRecordedLocations();
            List<LatLng> points = new ArrayList<>();
            for (Location loc : history)
            {
                points.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
            }
            routePolyline.setPoints(points);
        }
    }

    private void handlePauseResumeClick()
    {
        if (isBound && activeLessonService != null)
        {
            if (activeLessonService.isLessonTimerRunning())
            {
                activeLessonService.pauseLesson();
                btnPauseRecording.setText("Resume Recording");
                updateRecordingUI(false);
            }
            else
            {
                activeLessonService.resumeLesson();
                btnPauseRecording.setText("Pause Recording");
                updateRecordingUI(true);
            }
        }
    }

    private void updateRecordingUI(boolean isRecording)
    {
        if (isRecording)
        {
            vRecordingIndicator.setBackgroundResource(R.drawable.bg_circle_red);
            tvRecordingStatus.setText("Recording");
            tvRecordingStatus.setTextColor(Color.parseColor("#D1D5DB"));
        }
        else
        {
            vRecordingIndicator.setBackgroundColor(Color.parseColor("#808080"));
            tvRecordingStatus.setText("Paused");
            tvRecordingStatus.setTextColor(Color.parseColor("#808080"));
        }
    }

    private void showEndLessonDialog()
    {
        new AlertDialog.Builder(requireContext())
                .setTitle("End Lesson")
                .setMessage("Are you sure you want to end this lesson?")
                .setPositiveButton("Yes", (dialog, which) -> processLessonEnd())
                .setNegativeButton("No", null)
                .show();
    }

    private void processLessonEnd()
    {
        if (!isBound || activeLessonService == null) return;

        activeLessonLoadingOverlay.setVisibility(View.VISIBLE);

        // 1. Pull the data from the Service before stopping it
        File audioFile = activeLessonService.getAudioFile();
        ArrayList<Location> finalLocations = new ArrayList<>(activeLessonService.getRecordedLocations());

        // 2. Stop the Service and unbind
        activeLessonService.stopLesson();
        requireActivity().unbindService(serviceConnection);
        isBound = false;

        // 3. Process Audio with Gemini (if enabled and recorded)
        if (isAudioEnabledForLesson && audioFile != null && audioFile.exists() && audioFile.length() > 0)
        {
            tvRecordingStatus.setText("Processing...");

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
                        uploadGpxAndSaveLesson(result, finalLocations);
                    });
                }

                @Override
                public void onFailure(Throwable error)
                {
                    requireActivity().runOnUiThread(() ->
                    {
                        Toast.makeText(requireContext(), "Failed to summarize lesson: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        uploadGpxAndSaveLesson("{}", finalLocations);
                    });
                }
            });
        }
        else
        {
            Toast.makeText(requireContext(), "No audio recorded. Saving lesson...", Toast.LENGTH_SHORT).show();
            uploadGpxAndSaveLesson("{}", finalLocations);
        }
    }

    private void uploadGpxAndSaveLesson(String geminiSummary, ArrayList<Location> finalLocations)
    {
        LessonSummary tempSummary;
        try
        {
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
            android.util.Log.e("ActiveLesson", "Gson parsing error: " + e.getMessage());
            tempSummary = new LessonSummary();
        }

        final LessonSummary finalParsedSummary = tempSummary;

        if (!isLocationEnabledForLesson || finalLocations == null || finalLocations.isEmpty())
        {
            saveLessonDataToDatabase(finalParsedSummary);
            return;
        }

        File gpxFile = new File(requireContext().getCacheDir(), "track.gpx");
        try
        {
            GpxHelpers.generateGpxFile(finalLocations, gpxFile);
        }
        catch (java.io.IOException e)
        {
            Toast.makeText(requireContext(), "Failed to generate track file. Saving lesson...", Toast.LENGTH_SHORT).show();
            saveLessonDataToDatabase(finalParsedSummary);
            return;
        }

        String teacherUid = currentLesson.getTeacherUID();
        String studentUid = currentLesson.getStudentUID();
        String dateTime = currentLesson.getDateAndTime();

        StorageReference trackRef = FBRef.refLessonsDetails
                .child(teacherUid).child(studentUid).child(dateTime).child("track.gpx");

        Uri fileUri = Uri.fromFile(gpxFile);

        trackRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> saveLessonDataToDatabase(finalParsedSummary))
                .addOnFailureListener(e ->
                {
                    Toast.makeText(requireContext(), "Failed to upload track, but saving lesson...", Toast.LENGTH_SHORT).show();
                    saveLessonDataToDatabase(finalParsedSummary);
                });
    }

    private void saveLessonDataToDatabase(LessonSummary parsedSummary)
    {
        String teacherUid = currentLesson.getTeacherUID();
        String studentUid = currentLesson.getStudentUID();
        String dateTime = currentLesson.getDateAndTime();

        DoneLesson doneLesson = new DoneLesson(
                currentLesson, new Payment(), parsedSummary,
                isLocationEnabledForLesson, isAudioEnabledForLesson);

        FBRef.refDoneLessons.child(teacherUid).child(studentUid).child(dateTime).setValue(doneLesson)
                .addOnSuccessListener(aVoid ->
                {
                    FBRef.refScheduledLessons.child(teacherUid).child(studentUid).child(dateTime).removeValue()
                            .addOnSuccessListener(aVoid1 -> incrementStudentLessonCount(studentUid))
                            .addOnFailureListener(e ->
                            {
                                if (!isAdded() || getContext() == null) return;
                                Toast.makeText(getContext(), "Lesson saved, but failed to remove from schedule.", Toast.LENGTH_LONG).show();
                                returnToHome();
                            });
                })
                .addOnFailureListener(e ->
                {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "Failed to save done lesson.", Toast.LENGTH_LONG).show();
                    returnToHome();
                });
    }

    private void incrementStudentLessonCount(String studentUid)
    {
        FBRef.refStudents.child(studentUid).child("lessonsCompleted")
                .setValue(com.google.firebase.database.ServerValue.increment(1))
                .addOnSuccessListener(aVoid ->
                {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "Lesson saved and student count updated successfully!", Toast.LENGTH_SHORT).show();
                    returnToHome();
                })
                .addOnFailureListener(e ->
                {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "Failed to update student's count.", Toast.LENGTH_LONG).show();
                    returnToHome();
                });
    }

    private void returnToHome()
    {
        TeacherMainActivity activity = (TeacherMainActivity) requireActivity();
        activity.clearStack();
        activity.replaceFragment(com.example.learn2drive.Fragments.TeacherHomeFragment.newInstance(), false, "TeacherHomeFragment");
    }

    // --- ActiveLessonService.LessonServiceListener Implementation ---

    @Override
    public void onTimerTick(String formattedTime)
    {
        // UI updates must happen on the main thread
        requireActivity().runOnUiThread(() -> tvTimer.setText(formattedTime));
    }

    @Override
    public void onLocationUpdate(Location location)
    {
        requireActivity().runOnUiThread(() ->
        {
            if (mMap != null && routePolyline != null)
            {
                List<LatLng> points = routePolyline.getPoints();
                LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
                points.add(newPoint);
                routePolyline.setPoints(points);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(newPoint));
            }
        });
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if (getActivity() != null)
        {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}