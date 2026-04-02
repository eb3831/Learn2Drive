package com.example.learn2drive.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learn2drive.Adapters.TeacherLessonAdapter;
import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.DoneLesson;
import com.example.learn2drive.Objects.ScheduledLesson;
import com.example.learn2drive.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Fragment responsible for displaying the history of completed lessons for a teacher.
 * Allows filtering the lessons by student or by a specific date.
 */
public class TeacherHistoryFragment extends Fragment
{

    private ImageView btnBack;
    private Spinner spinnerFilterType;
    private LinearLayout layoutStudentFilter;
    private Spinner spinnerStudentName;
    private LinearLayout layoutDateFilter;
    private TextView tvDatePicker;
    private TextView tvLessonCount;
    private RecyclerView rvLessons;
    private LinearLayout layoutEmptyState;
    private ProgressBar progressBar;

    private ArrayList<ScheduledLesson> allLessons;
    private ArrayList<ScheduledLesson> displayedLessons;
    private TeacherLessonAdapter adapter;

    private DatabaseReference databaseReference;
    private ValueEventListener lessonsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_teacher_history, container, false);

        initViews(view);
        setupRecyclerView();
        setupFilterSpinner();
        fetchLessonsFromFirebase();

        return view;
    }

    /**
     * Initializes all UI components from the XML layout and sets basic listeners.
     *
     * @param view The root view of the fragment.
     */
    private void initViews(View view)
    {
        btnBack = view.findViewById(R.id.btnBack);
        spinnerFilterType = view.findViewById(R.id.spinnerFilterType);
        layoutStudentFilter = view.findViewById(R.id.layoutStudentFilter);
        spinnerStudentName = view.findViewById(R.id.spinnerStudentName);
        layoutDateFilter = view.findViewById(R.id.layoutDateFilter);
        tvDatePicker = view.findViewById(R.id.tvDatePicker);
        tvLessonCount = view.findViewById(R.id.tvLessonCount);
        rvLessons = view.findViewById(R.id.rvLessons);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        progressBar = view.findViewById(R.id.progressBar);

        btnBack.setOnClickListener(v ->
        {
            if (getActivity() != null)
            {
                getActivity().onBackPressed();
            }
        });
    }

    /**
     * Sets up the RecyclerView, assigns its LayoutManager, and initializes the adapter
     * with empty data structures.
     */
    private void setupRecyclerView()
    {
        allLessons = new ArrayList<>();
        displayedLessons = new ArrayList<>();

        rvLessons.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TeacherLessonAdapter(displayedLessons, new TeacherLessonAdapter.OnLessonClickListener()
        {
            @Override
            public void onLessonClicked(ScheduledLesson lesson)
            {
                // Future implementation: handle click on a specific completed lesson
            }
        });

        rvLessons.setAdapter(adapter);
    }

    /**
     * Populates the main filter type spinner with programmatic string options.
     */
    private void setupFilterSpinner()
    {
        String[] filterOptions = {"All Lessons", "By Student", "By Date"};

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                filterOptions
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterType.setAdapter(spinnerAdapter);
    }

    /**
     * Fetches completed lessons from Firebase for the currently authenticated teacher.
     * Iterates through the nested structure to populate the lessons list and extracts
     * unique student names for the filtering spinner.
     */
    private void fetchLessonsFromFirebase()
    {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        rvLessons.setVisibility(View.GONE);

        databaseReference = FBRef.refDoneLessons.child(FBRef.uid);

        lessonsListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allLessons.clear();
                ArrayList<String> uniqueStudentNames = new ArrayList<>();

                if (snapshot.exists())
                {
                    // Goes over Students UIDS
                    for (DataSnapshot studentSnapshot : snapshot.getChildren())
                    {
                        // Goes over dates
                        for (DataSnapshot dateSnapshot : studentSnapshot.getChildren())
                        {
                            DoneLesson lesson = dateSnapshot.getValue(DoneLesson.class);

                            if (lesson != null)
                            {
                                allLessons.add(lesson);

                                String studentName = lesson.getStudentName();
                                if (studentName != null && !uniqueStudentNames.contains(studentName))
                                {
                                    uniqueStudentNames.add(studentName);
                                }
                            }
                        }
                    }
                }

                progressBar.setVisibility(View.GONE);
                setupStudentSpinner(uniqueStudentNames);

                // Initially, display all lessons before any specific filter is applied
                displayedLessons.clear();
                displayedLessons.addAll(allLessons);
                adapter.updateList(displayedLessons);
                updateUIState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                progressBar.setVisibility(View.GONE);

            }
        };

        databaseReference.addValueEventListener(lessonsListener);
    }

    /**
     * Populates the student filter spinner with dynamically fetched unique student names.
     *
     * @param studentNames The list of unique student names extracted from completed lessons.
     */
    private void setupStudentSpinner(ArrayList<String> studentNames)
    {
        ArrayAdapter<String> studentAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                studentNames
        );
        studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudentName.setAdapter(studentAdapter);
    }

    /**
     * Updates the UI state depending on whether there are lessons to display or not.
     * Toggles between the RecyclerView and the Empty State layout.
     */
    private void updateUIState()
    {
        tvLessonCount.setText(displayedLessons.size() + " Lessons");

        if (displayedLessons.isEmpty())
        {
            rvLessons.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        }

        else
        {
            rvLessons.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }
}