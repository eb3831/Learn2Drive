package com.example.learn2drive.Fragments;

import static com.example.learn2drive.Helpers.FBRef.refDoneLessons;
import static com.example.learn2drive.Helpers.FBRef.refStudents;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.learn2drive.Activities.MasterActivity;
import com.example.learn2drive.Activities.StudentMainActivity;
import com.example.learn2drive.Adapters.StudentLessonAdapter;
import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.DoneLesson;
import com.example.learn2drive.Objects.ScheduledLesson;
import com.example.learn2drive.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment responsible for displaying the student's completed lessons history.
 * Fetches data from Firebase, calculates total lessons and hours, and displays them in a RecyclerView.
 */
public class StudentHistoryFragment extends Fragment
{
    private ImageView ivBack;
    private RecyclerView rvLessonsHistory;
    private LinearLayout layoutEmptyState, btnHistoryStudent;
    private LinearLayout layoutStatsSummary;
    private TextView tvTotalLessons;
    private TextView tvTotalHours;
    private ProgressBar progressBar;

    private StudentLessonAdapter adapter;
    private ArrayList<ScheduledLesson> lessonsList;

    private DatabaseReference lessonsRef;
    private ValueEventListener lessonsListener;

    private String teacherUID = "TEACHER_UID_PLACEHOLDER";

    public StudentHistoryFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment. You can add parameters here in the future if needed.
     *
     * @return A new instance of fragment StudentHistoryFragment.
     */
    public static StudentHistoryFragment newInstance()
    {
        StudentHistoryFragment fragment = new StudentHistoryFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_student_history, container, false);

        initViews(view);
        setupRecyclerView();
        fetchTeacherUIDAndLoadHistory();

        return view;
    }

    /**
     * Initializes all UI components from the inflated view.
     *
     * @param view The root view of the fragment.
     */
    private void initViews(View view)
    {
        ivBack = view.findViewById(R.id.ivBack);
        rvLessonsHistory = view.findViewById(R.id.rvLessonsHistory);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        layoutStatsSummary = view.findViewById(R.id.layoutStatsSummary);
        tvTotalLessons = view.findViewById(R.id.tvTotalLessons);
        tvTotalHours = view.findViewById(R.id.tvTotalHours);
        progressBar = view.findViewById(R.id.progressBar);

        ivBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    /**
     * Sets up the RecyclerView with a LinearLayoutManager and attaches the adapter.
     */
    private void setupRecyclerView()
    {
        lessonsList = new ArrayList<>();
        rvLessonsHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter with the click listener interface
        adapter = new StudentLessonAdapter(lessonsList, lesson ->
        {
            if (lesson instanceof DoneLesson)
            {
                DoneLesson clickedLesson = (DoneLesson) lesson;
                ((StudentMainActivity)requireActivity()).replaceFragment(
                        DoneLessonDetailsFragment.newInstance(clickedLesson, true),
                        true,
                        "LessonDetailsFragment");
            }
            else
            {
                Toast.makeText(getContext(), "Cannot open details for this lesson", Toast.LENGTH_SHORT).show();
            }
        });

        rvLessonsHistory.setAdapter(adapter);
    }

    /**
     * Fetches the current student's data from Firebase to retrieve their teacher's UID.
     * Once the teacherUID is retrieved, it triggers the loading of the lesson history.
     */
    private void fetchTeacherUIDAndLoadHistory()
    {
        progressBar.setVisibility(View.VISIBLE);

        refStudents.child(FBRef.uid).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (isAdded() && snapshot.exists())
                {
                    teacherUID = snapshot.child("teacherUid").getValue(String.class);

                    if (teacherUID != null && !teacherUID.isEmpty())
                    {
                        loadHistoryData();
                    }

                    else
                    {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "You don't have an assigned teacher yet",
                                Toast.LENGTH_SHORT).show();
                        layoutEmptyState.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                if (isAdded())
                {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error loading student data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Fetches the completed lessons from Firebase Realtime Database.
     * Calculates statistics and sorts the list locally by date.
     */
    private void loadHistoryData()
    {
        progressBar.setVisibility(View.VISIBLE);
        layoutStatsSummary.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
        rvLessonsHistory.setVisibility(View.GONE);

        lessonsRef = refDoneLessons.child(teacherUID).child(FBRef.uid);

        lessonsListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                lessonsList.clear();
                int totalDurationMinutes = 0;

                for (DataSnapshot lessonSnapshot : snapshot.getChildren())
                {
                    DoneLesson doneLesson = lessonSnapshot.getValue(DoneLesson.class);
                    if (doneLesson != null)
                    {
                        lessonsList.add(doneLesson);
                        totalDurationMinutes += doneLesson.getDuration();
                    }
                }

                sortLessonsByDateDesc();
                updateUI(totalDurationMinutes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                progressBar.setVisibility(View.GONE);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load history", Toast.LENGTH_SHORT).show();
                }
            }
        };

        lessonsRef.addValueEventListener(lessonsListener);
    }

    /**
     * Sorts the lessonsList by date and time in descending order (newest first).
     */
    private void sortLessonsByDateDesc()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

        Collections.sort(lessonsList, (lesson1, lesson2) -> {
            try
            {
                Date date1 = sdf.parse(lesson1.getDateAndTime());
                Date date2 = sdf.parse(lesson2.getDateAndTime());

                if (date1 != null && date2 != null)
                {
                    return date2.compareTo(date1); // Descending order
                }
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
            return 0;
        });
    }

    /**
     * Updates the UI components based on the fetched data and calculates hours/minutes.
     *
     * @param totalMinutes The total duration of all completed lessons combined.
     */
    private void updateUI(int totalMinutes)
    {
        progressBar.setVisibility(View.GONE);

        if (lessonsList.isEmpty())
        {
            layoutEmptyState.setVisibility(View.VISIBLE);
        }
        else
        {
            rvLessonsHistory.setVisibility(View.VISIBLE);
            layoutStatsSummary.setVisibility(View.VISIBLE);

            tvTotalLessons.setText(String.valueOf(lessonsList.size()));

            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;
            tvTotalHours.setText(hours + "h " + minutes + "m");

            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (lessonsRef != null && lessonsListener != null)
        {
            lessonsRef.removeEventListener(lessonsListener);
        }
        ((MasterActivity) getActivity()).showBottomNav();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        ((MasterActivity) getActivity()).hideBottomNav();
    }
}