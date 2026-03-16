package com.example.learn2drive.Objects;

public class TimeSlot
{
    private int duration;
    private boolean isAvailable;

    public TimeSlot()
    {
    }

    public TimeSlot(int duration, boolean isAvailable)
    {
        this.duration = duration;
        this.isAvailable = isAvailable;
    }

    public int getDuration()
    {
        return duration;
    }

    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    public boolean isAvailable()
    {
        return isAvailable;
    }

    public void setAvailable(boolean available)
    {
        isAvailable = available;
    }
}
