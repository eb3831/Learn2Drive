package com.example.learn2drive.Helpers;

import android.media.MediaRecorder;
import android.os.Build;

import java.io.IOException;

/**
 * Helper class to manage audio recording functionality using MediaRecorder.
 */
public class AudioRecorderHelper
{
    private MediaRecorder mediaRecorder;
    private boolean isRecording;
    private boolean isPaused;
    private String outputFilePath;

    /**
     * Initializes the AudioRecorderHelper with a specific output file path.
     *
     * @param outputFilePath The absolute path where the audio file will be saved.
     */
    public AudioRecorderHelper(String outputFilePath)
    {
        this.outputFilePath = outputFilePath;
        this.isRecording = false;
        this.isPaused = false;
    }

    /**
     * Starts the audio recording.
     *
     * @throws IOException If the MediaRecorder fails to prepare.
     */
    public void startRecording() throws IOException
    {
        if (isRecording)
        {
            return;
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(outputFilePath);

        mediaRecorder.prepare();
        mediaRecorder.start();

        isRecording = true;
        isPaused = false;
    }

    /**
     * Pauses the audio recording (Requires API level 24+).
     */
    public void pauseRecording()
    {
        if (isRecording && !isPaused && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            mediaRecorder.pause();
            isPaused = true;
        }
    }

    /**
     * Resumes the paused audio recording (Requires API level 24+).
     */
    public void resumeRecording()
    {
        if (isRecording && isPaused && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            mediaRecorder.resume();
            isPaused = false;
        }
    }

    /**
     * Stops and releases the audio recording.
     */
    public void stopRecording()
    {
        if (isRecording)
        {
            try
            {
                mediaRecorder.stop();
            }
            catch (RuntimeException e)
            {
                // Handle exception if stop is called immediately after start
            }
            finally
            {
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
                isPaused = false;
            }
        }
    }

    /**
     * Checks if the recorder is currently recording.
     *
     * @return True if recording, false otherwise.
     */
    public boolean isRecording()
    {
        return isRecording;
    }

    /**
     * Checks if the recorder is currently paused.
     *
     * @return True if paused, false otherwise.
     */
    public boolean isPaused()
    {
        return isPaused;
    }
}