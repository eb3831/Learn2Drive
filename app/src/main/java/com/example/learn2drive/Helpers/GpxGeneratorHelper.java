package com.example.learn2drive.Helpers;

import android.location.Location;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Helper class responsible for generating a GPX (GPS Exchange Format) file
 * from a list of recorded Location objects.
 */
public class GpxGeneratorHelper
{
    /**
     * Converts a list of Location objects into a standard GPX XML format
     * and writes it to the specified file.
     *
     * @param locations The list of locations recorded during the driving lesson.
     * @param gpxFile   The destination File where the GPX data will be saved.
     * @throws IOException If an error occurs during the file writing process.
     */
    public static void generateGpxFile(List<Location> locations, File gpxFile) throws IOException
    {
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<gpx version=\"1.1\" creator=\"Learn2Drive\">\n" +
                "  <trk>\n" +
                "    <name>Driving Lesson Track</name>\n" +
                "    <trkseg>\n";

        String footer = "    </trkseg>\n" +
                "  </trk>\n" +
                "</gpx>";

        // GPX files require time to be in ISO 8601 format (UTC time)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try (FileWriter writer = new FileWriter(gpxFile))
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
}