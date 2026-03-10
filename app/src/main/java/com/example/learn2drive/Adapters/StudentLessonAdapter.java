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

public class StudentLessonAdapter extends
        RecyclerView.Adapter<StudentLessonAdapter.LessonViewHolder>
{
    private ArrayList<ScheduledLesson> lessonList;

    public StudentLessonAdapter(ArrayList<ScheduledLesson> lessonList)
    {
        this.lessonList = lessonList;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position)
    {
        ScheduledLesson lesson = lessonList.get(position);

        // Handles date and time string, splits it
        String fullDateTime = lesson.getDateAndTime();

        if (fullDateTime != null && fullDateTime.contains(" "))
        {
            String[] parts = fullDateTime.split(" ");
            holder.tvDate.setText(parts[0]); // DD.MM.YYYY
            holder.tvTime.setText(parts[1]); // HH:mm
        }
        else
        {
            holder.tvDate.setText(fullDateTime);
            holder.tvTime.setText("--:--");
        }

        // Sets student and lesson details
        holder.tvDurationDetails.setText(lesson.getDuration() + " min");
        holder.tvDurationBadge.setText(lesson.getDuration() + "m");
    }

    @Override
    public int getItemCount()
    {
        return lessonList != null ? lessonList.size() : 0;
    }

    public static class LessonViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvDate, tvTime, tvDurationDetails, tvDurationBadge;

        public LessonViewHolder(@NonNull View itemView)
        {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDurationDetails = itemView.findViewById(R.id.tvDurationDetails);
            tvDurationBadge = itemView.findViewById(R.id.tvDurationBadge);
        }
    }
}