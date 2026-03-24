package com.example.learn2drive.Helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/**
 * Helper class responsible for managing GPS location tracking.
 * Utilizes FusedLocationProviderClient to request and receive high-accuracy location updates.
 */
public class LocationTrackingHelper
{
    /**
     * Interface definition for a callback to be invoked when a new location is received.
     */
    public interface LocationUpdateListener
    {
        /**
         * Called when the location has changed.
         *
         * @param location The new location object containing latitude, longitude, and metadata.
         */
        void onLocationUpdated(Location location);
    }

    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationCallback locationCallback;
    private final LocationUpdateListener listener;
    private boolean isTracking = false;

    /**
     * Initializes the LocationTrackingHelper.
     *
     * @param context  The context used to obtain the FusedLocationProviderClient.
     * @param listener The listener that will receive location updates.
     */
    public LocationTrackingHelper(Context context, LocationUpdateListener listener)
    {
        this.listener = listener;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        this.locationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult)
            {
                if (locationResult == null)
                {
                    return;
                }
                for (Location location : locationResult.getLocations())
                {
                    if (LocationTrackingHelper.this.listener != null)
                    {
                        LocationTrackingHelper.this.listener.onLocationUpdated(location);
                    }
                }
            }
        };
    }

    /**
     * Starts requesting continuous location updates.
     * Requires ACCESS_FINE_LOCATION permission to be granted beforehand.
     */
    @SuppressLint("MissingPermission")
    public void startTracking()
    {
        if (isTracking)
        {
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        isTracking = true;
    }

    /**
     * Stops requesting location updates to save battery and prevent memory leaks.
     */
    public void stopTracking()
    {
        if (!isTracking)
        {
            return;
        }
        fusedLocationClient.removeLocationUpdates(locationCallback);
        isTracking = false;
    }
}