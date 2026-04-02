package com.example.learn2drive.Fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learn2drive.Activities.MasterActivity;
import com.example.learn2drive.Activities.TeacherMainActivity;
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
import java.util.Calendar;
import java.util.Locale;

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

    private String selectedDateFilter = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_teacher_history, container, false);

        initViews(view);
        setupRecyclerView();
        setupFilterSpinner();
        setupListeners();

        return view;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment TeacherHistoryFragment.
     */
    public static TeacherHistoryFragment newInstance()
    {
        TeacherHistoryFragment fragment = new TeacherHistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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
                if (lesson instanceof DoneLesson)
                {
                    DoneLesson clickedLesson = (DoneLesson) lesson;
                    ((TeacherMainActivity)requireActivity()).replaceFragment(
                            DoneLessonDetailsFragment.newInstance(clickedLesson, false),
                            true,
                            "LessonDetailsFragment");
                }
                else
                {
                    Toast.makeText(getContext(), "Cannot open details for this lesson", Toast.LENGTH_SHORT).show();
                }
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

    /**
     * Sets up listeners for the filtering UI components (Spinners and DatePicker).
     */
    private void setupListeners()
    {
        spinnerFilterType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // Position 0: All, 1: By Student, 2: By Date
                if (position == 0)
                {
                    layoutStudentFilter.setVisibility(View.GONE);
                    layoutDateFilter.setVisibility(View.GONE);
                }

                else if (position == 1)
                {
                    layoutStudentFilter.setVisibility(View.VISIBLE);
                    layoutDateFilter.setVisibility(View.GONE);
                }

                else if (position == 2)
                {
                    layoutStudentFilter.setVisibility(View.GONE);
                    layoutDateFilter.setVisibility(View.VISIBLE);
                }
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerStudentName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        tvDatePicker.setOnClickListener(v -> showDatePicker());
    }

    /**
     * Displays a DatePickerDialog to allow the teacher to select a date.
     * Formats the selected date to match the "dd/MM/yyyy" format and applies the filter.
     */
    private void showDatePicker()
    {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, selectedYear, selectedMonth, selectedDay) ->
        {
            String formattedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear);
            tvDatePicker.setText(formattedDate);
            selectedDateFilter = formattedDate;
            applyFilter();
        }, year, month, day);

        datePickerDialog.show();
    }

    /**
     * Applies the selected filter (All, By Student, By Date) to the master list of lessons
     * and updates the RecyclerView via the adapter.
     */
    private void applyFilter()
    {
        displayedLessons.clear();
        int filterType = spinnerFilterType.getSelectedItemPosition();

        if (filterType == 0)
        {
            // No filter, show all
            displayedLessons.addAll(allLessons);

        }

        else if (filterType == 1)
        {
            // Filter by selected student
            Object selectedItem = spinnerStudentName.getSelectedItem();
            if (selectedItem != null)
            {
                String studentName = selectedItem.toString();
                for (ScheduledLesson lesson : allLessons)
                {
                    if (lesson.getStudentName() != null && lesson.getStudentName().equals(studentName))
                    {
                        displayedLessons.add(lesson);
                    }
                }
            }

        }
        else if (filterType == 2)
        {
            // Filter by selected date
            if (!selectedDateFilter.isEmpty())
            {
                for (ScheduledLesson lesson : allLessons)
                {
                    String lessonDate = lesson.getDateAndTime();

                    // Using startsWith because dateAndTime contains "dd/MM/yyyy HH:mm"
                    if (lessonDate != null && lessonDate.startsWith(selectedDateFilter))
                    {
                        displayedLessons.add(lesson);
                    }
                }
            }
        }

        adapter.updateList(displayedLessons);
        updateUIState(); // Updates the counter and Empty State visibility
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * Re-attaches the Firebase listener to fetch the most up-to-date lessons,
     * clears previous data to avoid duplicates, and hides the bottom navigation bar.
     */
    @Override
    public void onResume()
    {
        super.onResume();

        ((MasterActivity) getActivity()).hideBottomNav();

        if (adapter != null)
        {
            displayedLessons.clear();
            adapter.updateList(displayedLessons);
        }

        fetchLessonsFromFirebase();
    }

    /**
     * Called when the fragment is no longer visible to the user.
     * Removes the Firebase database listener to prevent memory leaks
     * and restores the bottom navigation bar.
     */
    @Override
    public void onStop()
    {
        super.onStop();

        if (databaseReference != null && lessonsListener != null)
        {
            databaseReference.removeEventListener(lessonsListener);
        }

        ((MasterActivity) getActivity()).showBottomNav();
    }
}