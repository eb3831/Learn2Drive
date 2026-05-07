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
 * Fragment responsible for displaying the teacher's scheduled lessons on their home screen.
 * It provides a quick overview of upcoming lessons and navigation buttons to other main features.
 */
public class TeacherHomeFragment extends Fragment
{

    private RecyclerView teacherRvScheduledLessons;
    private TeacherLessonAdapter adapter;
    private ArrayList<ScheduledLesson> lessonList;
    private SimpleDateFormat dateTimeFormatter;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmptyState;
    private LinearLayout btnHoursManager, btnLessonRequests, btnTeacherHistory;

    private ValueEventListener lessonsListener;

    /**
     * Required empty public constructor.
     */
    public TeacherHomeFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment TeacherHomeFragment.
     */
    public static TeacherHomeFragment newInstance()
    {
        TeacherHomeFragment fragment = new TeacherHomeFragment();
        Bundle args = new Bundle();
        // Add parameters to the bundle here if needed in the future
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initializes the fragment's user interface, sets up UI components,
     * configures the date formatter, and triggers the data fetching process from Firebase.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
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
     * Also sets up navigation listeners for the action buttons.
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
        btnTeacherHistory = view.findViewById(R.id.btnTeacherHistory);

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

        btnTeacherHistory.setOnClickListener(v -> ((TeacherMainActivity) requireActivity()).
                replaceFragment(TeacherHistoryFragment.newInstance(), true, "TeacherHistoryFragment"));
    }

    /**
     * Fetches scheduled lessons from Firebase Realtime Database for the current teacher
     * and attaches a ValueEventListener to keep the list updated in real-time.
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
     * Toggles visibility between the RecyclerView list, the loading spinner, and the empty state layout
     * based on whether there are scheduled lessons to display.
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
     * The nearest upcoming lessons will appear at the top of the list.
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
     * Called when the fragment is no longer visible to the user.
     * Removes the Firebase ValueEventListener to prevent memory leaks and unnecessary network calls.
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