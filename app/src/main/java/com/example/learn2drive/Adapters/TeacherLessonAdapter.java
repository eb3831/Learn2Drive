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

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_lesson, parent, false);
        return new LessonViewHolder(view);
    }

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