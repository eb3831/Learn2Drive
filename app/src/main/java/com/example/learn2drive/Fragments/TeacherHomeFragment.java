package com.example.learn2drive.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learn2drive.Adapters.TeacherLessonAdapter;
import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.ScheduledLesson;
import com.example.learn2drive.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class TeacherHomeFragment extends Fragment
{
    private RecyclerView teacherRvScheduledLessons;
    private TeacherLessonAdapter adapter;
    private ArrayList<ScheduledLesson> lessonList;
    private SimpleDateFormat dateTimeFormatter;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmptyState;

    public TeacherHomeFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_teacher_home, container, false);

        // Initialize UI components
        initViews(view);

        // Initialize Date Formatter for sorting (Format: DD.MM.YYYY HH:mm)
        dateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

        // Start listening to Firebase data
        loadScheduledLessons();

        return view;
    }

    /**
     * Links the UI components and sets up the RecyclerView with its adapter.
     */
    private void initViews(View view)
    {
        teacherRvScheduledLessons = view.findViewById(R.id.teacherRvScheduledLessons);
        pbLoading = view.findViewById(R.id.teacherHomePb);
        layoutEmptyState = view.findViewById(R.id.teacherHomeEmptyStateLayout);

        teacherRvScheduledLessons.setLayoutManager(new LinearLayoutManager(getContext()));
        lessonList = new ArrayList<>();
        adapter = new TeacherLessonAdapter(lessonList);
        teacherRvScheduledLessons.setAdapter(adapter);
    }

    /**
     * Fetches lessons from Firebase Realtime DB.
     * Path: Lessons -> Scheduled -> TeacherUID -> StudentUID -> LessonNumber
     */
    private void loadScheduledLessons()
    {
        if (FBRef.uid == null) return;

        // STEP A: Before starting the request, show the loader and hide everything else
        pbLoading.setVisibility(View.VISIBLE);
        teacherRvScheduledLessons.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);

        FBRef.refScheduledLessons.child(FBRef.uid).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                lessonList.clear();

                for (DataSnapshot studentSnapshot : snapshot.getChildren())
                {
                    for (DataSnapshot lessonSnapshot : studentSnapshot.getChildren())
                    {
                        ScheduledLesson lesson = lessonSnapshot.getValue(ScheduledLesson.class);

                        if (lesson != null && lesson.getLessonStatus() == ScheduledLesson.ACCEPTED)
                        {
                            lessonList.add(lesson);
                        }
                    }
                }

                sortLessonsByDate();

                // STEP B: Update the UI based on the results
                updateUIState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                // Hide loader even if there is an error
                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Toggles visibility between the list, the loading spinner, and the empty state.
     */
    private void updateUIState()
    {
        // Data has arrived, so always hide the loading spinner
        pbLoading.setVisibility(View.GONE);

        // No lessons found: Show Empty State, Hide List
        if (lessonList.isEmpty())
        {
            teacherRvScheduledLessons.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        }

        // Lessons found: Show List, Hide Empty State
        else
        {
            teacherRvScheduledLessons.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);

            // Refresh the adapter with the new data
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Sorts the lessonList based on date and time.
     * The nearest lesson will appear at the top of the list.
     */
    private void sortLessonsByDate()
    {
        Collections.sort(lessonList, new Comparator<ScheduledLesson>()
        {
            @Override
            public int compare(ScheduledLesson l1, ScheduledLesson l2)
            {
                try
                {
                    return dateTimeFormatter.parse(l1.getDateAndTime())
                            .compareTo(dateTimeFormatter.parse(l2.getDateAndTime()));
                }

                catch (ParseException e)
                {
                    return 0; // If parsing fails, keep the original order
                }
            }
        });
    }
}