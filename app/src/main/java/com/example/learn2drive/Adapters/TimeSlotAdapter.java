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

/**
 * Adapter class for displaying and managing time slots in a RecyclerView.
 * This adapter adapts its UI and interaction logic based on whether it is being viewed
 * by a student (can select slots) or a teacher (can manage availability).
 */
public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>
{
    private List<TimeSlot> timeSlotList;
    private Context context;
    private final OnTimeSlotStatusChangeListener listener;
    private boolean isStudentView;

    /**
     * Interface to handle events in the adapter.
     */
    public interface OnTimeSlotStatusChangeListener
    {
        /**
         * Called when a teacher changes the availability status of a time slot.
         *
         * @param timeSlot  The time slot that was updated.
         * @param newStatus The new status of the time slot (e.g., AVAILABLE, UNAVAILABLE).
         * @param position  The position of the updated item in the adapter's data set.
         */
        void onStatusChanged(TimeSlot timeSlot, String newStatus, int position);

        /**
         * Called when a student clicks on a time slot to request a lesson.
         *
         * @param timeSlot The time slot selected by the student.
         */
        void onStudentTimeSlotClicked(TimeSlot timeSlot);
    }

    /**
     * Constructor for the TimeSlotAdapter.
     *
     * @param context       The context of the calling fragment/activity.
     * @param timeSlotList  The list of time slots to display.
     * @param isStudentView A boolean flag indicating if the current user is a student (true) or teacher (false).
     * @param listener      The callback interface for handling clicks and status changes.
     */
    public TimeSlotAdapter(Context context, List<TimeSlot> timeSlotList, boolean isStudentView, OnTimeSlotStatusChangeListener listener)
    {
        this.context = context;
        this.timeSlotList = timeSlotList;
        this.isStudentView = isStudentView;
        this.listener = listener;
    }

    /**
     * Called when RecyclerView needs a new {@link TimeSlotViewHolder} of the given type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new TimeSlotViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_time_slot, parent, false);
        return new TimeSlotViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * Customizes the UI elements based on the slot's status and whether the user is a student or teacher.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position)
    {
        TimeSlot currentSlot = timeSlotList.get(position);

        // Always display the time, regardless of whether it's a student or teacher
        String timeText = currentSlot.getStartTime() + " - " + currentSlot.getEndTime();
        holder.tvTimeRange.setText(timeText);

        if (isStudentView)
        {
            // Student Mode: Hide controls and make the row clickable
            holder.switchAvailability.setVisibility(View.GONE);
            holder.tvStatusBadge.setVisibility(View.GONE);
            setNormalStyle(holder);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null)
                {
                    listener.onStudentTimeSlotClicked(currentSlot);
                }
            });
        }
        else
        {
            // Teacher Mode: Show controls based on status
            switch (currentSlot.getStatus())
            {
                case TimeSlot.STATUS_BOOKED:
                    holder.tvStatusBadge.setVisibility(View.VISIBLE);
                    holder.tvStatusBadge.setText("Booked");
                    holder.switchAvailability.setVisibility(View.GONE);
                    setNormalStyle(holder);
                    break;

                case TimeSlot.STATUS_REQUESTED:
                    holder.tvStatusBadge.setVisibility(View.VISIBLE);
                    holder.tvStatusBadge.setText("Requested");
                    holder.switchAvailability.setVisibility(View.GONE);
                    setNormalStyle(holder);
                    break;

                case TimeSlot.STATUS_UNAVAILABLE:
                    holder.tvStatusBadge.setVisibility(View.GONE);
                    holder.switchAvailability.setVisibility(View.VISIBLE);
                    holder.switchAvailability.setEnabled(true);
                    holder.switchAvailability.setChecked(false);
                    setDisabledStyle(holder);
                    break;

                case TimeSlot.STATUS_AVAILABLE:
                default:
                    holder.tvStatusBadge.setVisibility(View.GONE);
                    holder.switchAvailability.setVisibility(View.VISIBLE);
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
                            String newStatus = isCheckedNow ? TimeSlot.STATUS_AVAILABLE : TimeSlot.STATUS_UNAVAILABLE;
                            currentSlot.setStatus(newStatus);

                            if (isCheckedNow)
                            {
                                setNormalStyle(holder);
                            } else
                            {
                                setDisabledStyle(holder);
                            }

                            if (listener != null)
                            {
                                listener.onStatusChanged(currentSlot, newStatus, position);
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> holder.switchAvailability.setChecked(previousState))
                        .setOnCancelListener(dialog -> holder.switchAvailability.setChecked(previousState))
                        .show();
            });
        }
    }

    /**
     * Returns the total number of time slots in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount()
    {
        return timeSlotList.size();
    }

    /**
     * Helper method to style the row as active/available.
     * Clears any strike-through text flags and restores full opacity to the icon.
     *
     * @param holder The ViewHolder containing the UI elements to style.
     */
    private void setNormalStyle(TimeSlotViewHolder holder)
    {
        holder.tvTimeRange.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        holder.tvTimeRange.setPaintFlags(holder.tvTimeRange.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        holder.ivClockIcon.setAlpha(1.0f);
    }

    /**
     * Helper method to style the row as inactive/canceled.
     * Applies a strike-through text flag and reduces the opacity of the icon.
     *
     * @param holder The ViewHolder containing the UI elements to style.
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

        /**
         * Constructor for the TimeSlotViewHolder.
         * Initializes the UI components by finding them within the provided item view.
         *
         * @param itemView The view containing the layout for a single time slot item.
         */
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