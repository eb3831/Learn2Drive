package com.example.learn2drive.Adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learn2drive.Objects.TimeSlot;
import com.example.learn2drive.R;

import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>
{
    private List<TimeSlot> timeSlotList;
    private Context context;
    private final OnTimeSlotStatusChangeListener listener;

    /**
     * Interface to handle availability switch toggle events.
     */
    public interface OnTimeSlotStatusChangeListener
    {
        void onStatusChanged(TimeSlot timeSlot, String newStatus, int position);
    }

    public TimeSlotAdapter(Context context, List<TimeSlot> timeSlotList, OnTimeSlotStatusChangeListener listener)
    {
        this.context = context;
        this.timeSlotList = timeSlotList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_time_slot, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position)
    {
        TimeSlot currentSlot = timeSlotList.get(position);

        String timeText = currentSlot.getStartTime() + " - " + currentSlot.getEndTime();
        holder.tvTimeRange.setText(timeText);

        switch (currentSlot.getStatus())
        {
            case TimeSlot.STATUS_BOOKED:
                holder.tvStatusBadge.setVisibility(View.VISIBLE);
                holder.switchAvailability.setChecked(true);
                holder.switchAvailability.setEnabled(false); // Locked - teacher cannot cancel a booked lesson here
                setNormalStyle(holder);
                break;

            case TimeSlot.STATUS_UNAVAILABLE:
                holder.tvStatusBadge.setVisibility(View.GONE);
                holder.switchAvailability.setEnabled(true);
                holder.switchAvailability.setChecked(false);
                setDisabledStyle(holder);
                break;

            case TimeSlot.STATUS_AVAILABLE:
            default:
                holder.tvStatusBadge.setVisibility(View.GONE);
                holder.switchAvailability.setEnabled(true);
                holder.switchAvailability.setChecked(true);
                setNormalStyle(holder);
                break;
        }

        holder.switchAvailability.setOnClickListener(v ->
        {
            boolean isCheckedNow = holder.switchAvailability.isChecked();

            boolean previousState = !isCheckedNow;

            String dialogTitle = "Change Time Slot Status";
            String dialogMessage = isCheckedNow
                    ? "Do you want to mark this time slot as available for lessons?"
                    : "Do you want to cancel this time slot? Students will not be able to book a lesson during this time.";

            new AlertDialog.Builder(context)
                    .setTitle(dialogTitle)
                    .setMessage(dialogMessage)
                    .setPositiveButton("Yes, Confirm", (dialog, which) ->
                    {
                        // User confirmed! Update the object and the UI
                        String newStatus = isCheckedNow ? TimeSlot.STATUS_AVAILABLE : TimeSlot.STATUS_UNAVAILABLE;
                        currentSlot.setStatus(newStatus);

                        if (isCheckedNow)
                        {
                            setNormalStyle(holder);
                        } else
                        {
                            setDisabledStyle(holder);
                        }

                        // Notify the fragment to update Firebase
                        if (listener != null)
                        {
                            listener.onStatusChanged(currentSlot, newStatus, position);
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) ->
                    {
                        holder.switchAvailability.setChecked(previousState);
                    })
                    .setOnCancelListener(dialog ->
                    {
                        holder.switchAvailability.setChecked(previousState);
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount()
    {
        return timeSlotList.size();
    }

    /**
     * Helper method to style the row as active/available.
     */
    private void setNormalStyle(TimeSlotViewHolder holder)
    {
        holder.tvTimeRange.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        holder.tvTimeRange.setPaintFlags(holder.tvTimeRange.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        holder.ivClockIcon.setAlpha(1.0f);
    }

    /**
     * Helper method to style the row as inactive/canceled.
     */
    private void setDisabledStyle(TimeSlotViewHolder holder)
    {
        holder.tvTimeRange.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
        holder.tvTimeRange.setPaintFlags(holder.tvTimeRange.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.ivClockIcon.setAlpha(0.4f);
    }

    /**
     * ViewHolder class holding the UI elements for a single time slot.
     */
    public static class TimeSlotViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvTimeRange;
        TextView tvStatusBadge;
        ImageView ivClockIcon;
        SwitchCompat switchAvailability;

        public TimeSlotViewHolder(@NonNull View itemView)
        {
            super(itemView);
            tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            ivClockIcon = itemView.findViewById(R.id.ivClockIcon);
            switchAvailability = itemView.findViewById(R.id.switchAvailability);
        }
    }
}