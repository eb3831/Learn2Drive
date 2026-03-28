package com.example.learn2drive.Helpers;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Helper class responsible for generating and parsing GPX (GPS Exchange Format) files.
 */
public class GpxHelpers
{

    /**
     * Data class to hold the parsed GPX results: the route points and total distance.
     */
    public static class GpxData
    {
        private final List<LatLng> points;
        private final float distanceInKm;

        public GpxData(List<LatLng> points, float distanceInKm)
        {
            this.points = points;
            this.distanceInKm = distanceInKm;
        }

        public List<LatLng> getPoints()
        {
            return points;
        }

        public float getDistanceInKm()
        {
            return distanceInKm;
        }
    }

    /**
     * Converts a list of Location objects into a standard GPX XML format
     * and writes it to the specified file.
     *
     * @param locations The list of locations recorded during the driving lesson.
     * @param gpxFile   The destination File where the GPX data will be saved.
     * @throws IOException If an error occurs during the file writing process or if the list is empty.
     */
    public static void generateGpxFile(List<Location> locations, File gpxFile) throws IOException
    {
        if (locations == null || locations.isEmpty())
        {
            throw new IOException("Location list is empty or null. Cannot generate GPX file.");
        }

        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<gpx version=\"1.1\" creator=\"Learn2Drive\">\n" +
                "  <trk>\n" +
                "    <name>Driving Lesson Track</name>\n" +
                "    <trkseg>\n";

        String footer = "    </trkseg>\n" +
                "  </trk>\n" +
                "</gpx>";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(gpxFile)))
        {
            writer.write(header);

            for (Location location : locations)
            {
                writer.write("      <trkpt lat=\"" + location.getLatitude() + "\" lon=\"" + location.getLongitude() + "\">\n");

                if (location.hasAltitude())
                {
                    writer.write("        <ele>" + location.getAltitude() + "</ele>\n");
                }

                String formattedTime = dateFormat.format(new Date(location.getTime()));
                writer.write("        <time>" + formattedTime + "</time>\n");

                writer.write("      </trkpt>\n");
            }

            writer.write(footer);
        }
    }

    /**
     * Parses a GPX file from an InputStream, extracting the route coordinates and calculating the total distance.
     *
     * @param inputStream The input stream containing the GPX XML data.
     * @return A GpxData object containing the list of LatLng points and the total distance in kilometers.
     * @throws Exception If an error occurs during XML parsing or stream reading.
     */
    public static GpxData parseGpx(InputStream inputStream) throws Exception
    {
        List<LatLng> points = new ArrayList<>();
        float totalDistanceMeters = 0.0f;
        Location lastLocation = null;

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(inputStream, null);

        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_TAG && parser.getName().equals("trkpt"))
            {
                String latStr = parser.getAttributeValue(null, "lat");
                String lonStr = parser.getAttributeValue(null, "lon");

                if (latStr != null && lonStr != null)
                {
                    double lat = Double.parseDouble(latStr);
                    double lon = Double.parseDouble(lonStr);
                    LatLng currentLatLng = new LatLng(lat, lon);
                    points.add(currentLatLng);

                    Location currentLocation = new Location("");
                    currentLocation.setLatitude(lat);
                    currentLocation.setLongitude(lon);

                    if (lastLocation != null)
                    {
                        totalDistanceMeters += lastLocation.distanceTo(currentLocation);
                    }
                    lastLocation = currentLocation;
                }
            }
            eventType = parser.next();
        }

        float distanceInKm = totalDistanceMeters / 1000.0f;
        return new GpxData(points, distanceInKm);
    }
}