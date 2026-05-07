package com.example.learn2drive.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learn2drive.Objects.ScheduledLesson;
import com.example.learn2drive.R;

import java.util.ArrayList;

/**
 * Adapter for displaying a list of scheduled lessons in a RecyclerView.
 */
public class TeacherLessonAdapter extends RecyclerView.Adapter<TeacherLessonAdapter.LessonViewHolder>
{

    private ArrayList<ScheduledLesson> lessonList;
    private OnLessonClickListener listener;

    /**
     * Interface for handling clicks on lesson items.
     */
    public interface OnLessonClickListener
    {
        /**
         * Called when a scheduled lesson item is clicked.
         *
         * @param lesson The scheduled lesson object that was clicked.
         */
        void onLessonClicked(ScheduledLesson lesson);
    }

    /**
     * Constructor for the TeacherLessonAdapter.
     *
     * @param lessonList The list of scheduled lessons to display.
     * @param listener   The listener to handle item click events.
     */
    public TeacherLessonAdapter(ArrayList<ScheduledLesson> lessonList, OnLessonClickListener listener)
    {
        this.lessonList = lessonList;
        this.listener = listener;
    }

    /**
     * Updates the current list of lessons and notifies the adapter of the data changes.
     *
     * @param newList The new filtered list of scheduled or completed lessons to display.
     */
    public void updateList(ArrayList<ScheduledLesson> newList)
    {
        this.lessonList = newList;
        notifyDataSetChanged();
    }

    /**
     * Called when RecyclerView needs a new {@link LessonViewHolder} of the given type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new LessonViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the {@link LessonViewHolder} to reflect the lesson at the given position.
     * It handles the splitting of the combined date and time string for proper display in separate text views.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position)
    {
        ScheduledLesson lesson = lessonList.get(position);

        String fullDateTime = lesson.getDateAndTime();

        if (fullDateTime != null && fullDateTime.contains(" "))
        {
            String[] parts = fullDateTime.split(" ");
            holder.tvDate.setText(parts[0]);
            holder.tvTime.setText(parts[1]);
        } else
        {
            holder.tvDate.setText(fullDateTime);
            holder.tvTime.setText("--:--");
        }

        holder.tvStudentName.setText(lesson.getStudentName());
        holder.tvIDNumber.setText(lesson.getStudentID());
        holder.tvDurationDetails.setText(lesson.getDuration() + " min");
        holder.tvDurationBadge.setText(lesson.getDuration() + "m");

        holder.itemView.setOnClickListener(v ->
        {
            if (listener != null)
            {
                listener.onLessonClicked(lesson);
            }
        });
    }

    /**
     * Returns the total number of lessons in the data set held by the adapter.
     * Handles null-safety by returning 0 if the list is null.
     *
     * @return The total number of items in this adapter, or 0 if the list is null.
     */
    @Override
    public int getItemCount()
    {
        return lessonList != null ? lessonList.size() : 0;
    }

    /**
     * ViewHolder class for lesson items.
     */
    public static class LessonViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvDate, tvTime, tvStudentName, tvIDNumber, tvDurationDetails, tvDurationBadge;

        /**
         * Constructor for the LessonViewHolder.
         * Initializes the UI components by finding them within the provided item view.
         *
         * @param itemView The view containing the layout for a single lesson item.
         */
        public LessonViewHolder(@NonNull View itemView)
        {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvIDNumber = itemView.findViewById(R.id.tvIDNumber);
            tvDurationDetails = itemView.findViewById(R.id.tvDurationDetails);
            tvDurationBadge = itemView.findViewById(R.id.tvDurationBadge);
        }
    }
}