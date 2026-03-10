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

import com.example.learn2drive.Adapters.StudentLessonAdapter;
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

public class StudentHomeFragment extends Fragment
{
    private RecyclerView studentRvScheduledLessons;
    private StudentLessonAdapter adapter;
    private ArrayList<ScheduledLesson> lessonList;
    private SimpleDateFormat dateTimeFormatter;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmptyState;

    public StudentHomeFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student_home, container, false);

        // Initialize UI components
        initViews(view);

        // Initialize Date Formatter for sorting (Format: DD.MM.YYYY HH:mm)
        dateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

        // Fetches from FB
        fetchTeacherUidAndLessons();

        return view;
    }

    /**
     * Sets up the RecyclerView and its adapter.
     */
    private void initViews(View view)
    {
        studentRvScheduledLessons = view.findViewById(R.id.studentRvScheduledLessons);
        pbLoading = view.findViewById(R.id.studentHomePb);
        layoutEmptyState = view.findViewById(R.id.studentHomeEmptyStateLayout);

        studentRvScheduledLessons.setLayoutManager(new LinearLayoutManager(getContext()));
        lessonList = new ArrayList<>();
        adapter = new StudentLessonAdapter(lessonList);
        studentRvScheduledLessons.setAdapter(adapter);
    }

    /**
     * Fetches the teacher's UID and loads scheduled lessons.
     */
    private void fetchTeacherUidAndLessons()
    {
        if (FBRef.uid == null) return;

        // Displays loading bar, and hides other elements
        pbLoading.setVisibility(View.VISIBLE);
        studentRvScheduledLessons.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);

        // Reaching the teacherUid field of the student
        FBRef.refStudents.child(FBRef.uid).child("teacherUid").
                addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                String teacherUid = snapshot.getValue(String.class);

                // If the student has a teacher, continues to fetch the lessons
                if (teacherUid != null && !teacherUid.isEmpty())
                {
                    loadScheduledLessons(teacherUid);
                }
                else
                {
                    // If there is no teacher, shows the empty state
                    updateUIState();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Loads the student's lessons from FB
     * Path: Lessons -> Scheduled -> TeacherUID -> StudentUID -> LessonNumber
     */
    private void loadScheduledLessons(String teacherUid)
    {
        FBRef.refScheduledLessons.child(teacherUid).child(FBRef.uid).addValueEventListener(
                new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                lessonList.clear();

                // Goes over the lessons
                for (DataSnapshot lessonSnapshot : snapshot.getChildren())
                {
                    ScheduledLesson lesson = lessonSnapshot.getValue(ScheduledLesson.class);

                    if (lesson != null && lesson.getLessonStatus() == ScheduledLesson.ACCEPTED)
                    {
                        lessonList.add(lesson);
                    }
                }

                sortLessonsByDate();
                updateUIState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                pbLoading.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Toggles visibility between the list, the loading spinner, and the empty state.
     */
    private void updateUIState()
    {
        pbLoading.setVisibility(View.GONE);

        if (lessonList.isEmpty())
        {
            studentRvScheduledLessons.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        }
        else
        {
            studentRvScheduledLessons.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
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
                    return 0;
                }
            }
        });
    }
}