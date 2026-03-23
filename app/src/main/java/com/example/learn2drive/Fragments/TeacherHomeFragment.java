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

import com.example.learn2drive.Activities.TeacherMainActivity;
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

/**
 * Fragment displaying the teacher's scheduled lessons on their home screen.
 */
public class TeacherHomeFragment extends Fragment
{

    private RecyclerView teacherRvScheduledLessons;
    private TeacherLessonAdapter adapter;
    private ArrayList<ScheduledLesson> lessonList;
    private SimpleDateFormat dateTimeFormatter;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmptyState;
    private LinearLayout btnHoursManager, btnLessonRequests;

    private ValueEventListener lessonsListener;

    public TeacherHomeFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_teacher_home, container, false);

        initViews(view);

        dateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

        loadScheduledLessons();

        return view;
    }

    /**
     * Links the UI components and sets up the RecyclerView with its adapter and click listeners.
     *
     * @param view The root view of the fragment.
     */
    private void initViews(View view)
    {
        teacherRvScheduledLessons = view.findViewById(R.id.teacherRvScheduledLessons);
        pbLoading = view.findViewById(R.id.teacherHomePb);
        layoutEmptyState = view.findViewById(R.id.teacherHomeEmptyStateLayout);
        btnHoursManager = view.findViewById(R.id.btnHoursManager);
        btnLessonRequests = view.findViewById(R.id.btnLessonRequests);

        teacherRvScheduledLessons.setLayoutManager(new LinearLayoutManager(getContext()));
        lessonList = new ArrayList<>();

        adapter = new TeacherLessonAdapter(lessonList, lesson ->
        {
            TeacherLessonDetailsFragment fragment = TeacherLessonDetailsFragment.newInstance(lesson);

            if (getActivity() instanceof TeacherMainActivity)
            {
                ((TeacherMainActivity) getActivity()).replaceFragment(
                        fragment,
                        true,
                        "TeacherLessonDetailsFragment"
                );
            }
        });

        teacherRvScheduledLessons.setAdapter(adapter);

        btnHoursManager.setOnClickListener(v -> ((TeacherMainActivity) requireActivity()).
                replaceFragment(HoursManagerFragment.newInstance(), true, "HoursManagerFragment"));

        btnLessonRequests.setOnClickListener(v -> ((TeacherMainActivity) requireActivity()).
                replaceFragment(LessonRequestsFragment.newInstance(), true, "LessonRequestsFragment"));
    }

    /**
     * Fetches lessons from Firebase Realtime DB and attaches a listener.
     */
    private void loadScheduledLessons()
    {
        if (FBRef.uid == null) return;

        pbLoading.setVisibility(View.VISIBLE);
        teacherRvScheduledLessons.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);

        lessonsListener = new ValueEventListener()
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
                        if (lesson != null)
                        {
                            lessonList.add(lesson);
                        }
                    }
                }

                sortLessonsByDate();
                updateUIState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                if (pbLoading != null)
                {
                    pbLoading.setVisibility(View.GONE);
                }
            }
        };

        FBRef.refScheduledLessons.child(FBRef.uid).addValueEventListener(lessonsListener);
    }

    /**
     * Toggles visibility between the list, the loading spinner, and the empty state.
     */
    private void updateUIState()
    {
        pbLoading.setVisibility(View.GONE);

        if (lessonList.isEmpty())
        {
            teacherRvScheduledLessons.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        }
        else
        {
            teacherRvScheduledLessons.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Sorts the lessonList based on date and time.
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

    /**
     * Removes the Firebase listener when the fragment is stopped to prevent memory leaks.
     */
    @Override
    public void onStop()
    {
        super.onStop();
        if (lessonsListener != null && FBRef.uid != null)
        {
            FBRef.refScheduledLessons.child(FBRef.uid).removeEventListener(lessonsListener);
        }
    }
}