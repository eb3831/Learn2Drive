package com.example.learn2drive.Fragments;

import static com.example.learn2drive.Helpers.FBRef.refLessonsDetails;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.learn2drive.Helpers.GpxHelpers;
import com.example.learn2drive.Objects.DoneLesson;
import com.example.learn2drive.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

/**
 * Fragment responsible for displaying the route of a completed driving lesson on a map.
 * It receives a DoneLesson object, loads the corresponding GPX track from Firebase Storage,
 * and draws it using polylines.
 */
public class LessonTrackFragment extends Fragment implements OnMapReadyCallback
{

    private TextView tvLessonDateTime;
    private TextView tvTrackDuration;
    private TextView tvTrackDistance;
    private ImageView btnBack;
    private FrameLayout trackLoadingOverlay;

    private GoogleMap mMap;
    private DoneLesson currentLesson;

    public LessonTrackFragment()
    {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance of this fragment.
     *
     * @param lesson The completed lesson object containing the necessary data to fetch the track.
     * @return A new instance of fragment LessonTrackFragment.
     */
    public static LessonTrackFragment newInstance(DoneLesson lesson)
    {
        LessonTrackFragment fragment = new LessonTrackFragment();
        Bundle args = new Bundle();
        args.putSerializable("lesson_data", lesson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_lesson_track, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        extractArguments();
        setupMap();
        setupListeners();
    }

    /**
     * Initializes all UI components from the inflated view.
     *
     * @param view The root view of the fragment.
     */
    private void initViews(View view)
    {
        tvLessonDateTime = view.findViewById(R.id.tvLessonDateTime);
        tvTrackDuration = view.findViewById(R.id.tvTrackDuration);
        tvTrackDistance = view.findViewById(R.id.tvTrackDistance);
        btnBack = view.findViewById(R.id.btnBack);
        trackLoadingOverlay = view.findViewById(R.id.trackLoadingOverlay);
    }

    /**
     * Extracts the DoneLesson object passed via arguments and populates initial UI data.
     */
    private void extractArguments()
    {
        if (getArguments() != null)
        {
            currentLesson = (DoneLesson) getArguments().getSerializable("lesson_data");

            if (currentLesson != null)
            {
                tvLessonDateTime.setText(currentLesson.getDateAndTime());
                tvTrackDuration.setText(currentLesson.getDuration() + " Min");
                tvTrackDistance.setText("Calculating...");
            }
        }
    }

    /**
     * Sets up the Google Maps fragment and requests asynchronous initialization.
     */
    private void setupMap()
    {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapFragment);

        if (mapFragment != null)
        {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Sets up click listeners for the UI components.
     */
    private void setupListeners()
    {
        btnBack.setOnClickListener(v ->
        {
            if (getActivity() != null)
            {
                getActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap)
    {
        mMap = googleMap;
        showLoading(true);

        downloadAndParseGpxTrack();
    }

    /**
     * Toggles the visibility of the loading overlay.
     *
     * @param isLoading True to show the loading screen, false to hide it.
     */
    private void showLoading(boolean isLoading)
    {
        if (isLoading)
        {
            trackLoadingOverlay.setVisibility(View.VISIBLE);
        }

        else
        {
            trackLoadingOverlay.setVisibility(View.GONE);
        }
    }

    /**
     * Downloads the GPX track file from Firebase Storage based on the lesson's details,
     * parses the XML data, and proceeds to draw the route on the map.
     */
    private void downloadAndParseGpxTrack()
    {
        if (currentLesson == null)
        {
            showLoading(false);
            Toast.makeText(getContext(), "Error: Lesson data is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        String storagePath = currentLesson.getTeacherUID() + "/" + currentLesson.getStudentUID() +
                "/" + currentLesson.getDateAndTime() + "/track.gpx";

        StorageReference trackRef = refLessonsDetails.child(storagePath);

        final long MAX_DOWNLOAD_SIZE = 5 * 1024 * 1024;

        trackRef.getBytes(MAX_DOWNLOAD_SIZE).addOnSuccessListener(bytes ->
        {
            try
            {
                InputStream inputStream = new ByteArrayInputStream(bytes);
                GpxHelpers.GpxData gpxData = GpxHelpers.parseGpx(inputStream);
                drawTrackOnMap(gpxData);

            }
            catch (Exception e)
            {
                e.printStackTrace();
                showLoading(false);
                Toast.makeText(getContext(), "Error parsing track data.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(exception ->
        {
            exception.printStackTrace();
            showLoading(false);
            Toast.makeText(getContext(), "Error downloading track: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Draws the parsed GPX route on the Google Map, adjusts the camera to fit the route,
     * updates the distance UI, and hides the loading overlay.
     *
     * @param gpxData The object containing the list of coordinates and the total calculated distance.
     */
    private void drawTrackOnMap(GpxHelpers.GpxData gpxData)
    {
        if (mMap == null)
        {
            showLoading(false);
            return;
        }

        List<LatLng> points = gpxData.getPoints();

        if (points == null || points.isEmpty())
        {
            showLoading(false);
            Toast.makeText(getContext(), "No valid track points found.", Toast.LENGTH_SHORT).show();
            return;
        }

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(points)
                .color(Color.parseColor("#3B82F6"))
                .width(12f)
                .geodesic(true);

        mMap.addPolyline(polylineOptions);

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng point : points)
        {
            boundsBuilder.include(point);
        }
        LatLngBounds bounds = boundsBuilder.build();

        int padding = 120;
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

        String formattedDistance = String.format(Locale.getDefault(), "%.1f km",
                gpxData.getDistanceInKm());
        tvTrackDistance.setText(formattedDistance);

        showLoading(false);
    }
}