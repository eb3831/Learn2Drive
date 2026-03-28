package com.example.learn2drive.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.learn2drive.Objects.DoneLesson;
import com.example.learn2drive.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment responsible for displaying the details of a completed driving lesson.
 * Receives a DoneLesson object and adapts the UI based on whether the user is a student or a teacher.
 */
public class LessonDetailsFragment extends Fragment
{

    private static final String ARG_LESSON = "arg_lesson";
    private static final String ARG_IS_STUDENT = "arg_is_student";

    private DoneLesson currentLesson;
    private boolean isStudent;

    private ImageView btnBack;
    private TextView tvStudentName;
    private TextView tvStudentId;
    private TextView tvLessonNumber;
    private TextView tvLessonDate;
    private TextView tvLessonTime;
    private LinearLayout llStudentDetails;
    private LinearLayout btnWatchTrack;
    private LinearLayout btnWatchSummary;

    public LessonDetailsFragment()
    {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance of this fragment.
     *
     * @param lesson    The completed lesson object containing all details.
     * @param isStudent Boolean indicating if the current logged-in user is a student.
     * @return A new instance of fragment LessonDetailsFragment.
     */
    public static LessonDetailsFragment newInstance(DoneLesson lesson, boolean isStudent)
    {
        LessonDetailsFragment fragment = new LessonDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LESSON, lesson);
        args.putBoolean(ARG_IS_STUDENT, isStudent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            currentLesson = (DoneLesson) getArguments().getSerializable(ARG_LESSON);
            isStudent = getArguments().getBoolean(ARG_IS_STUDENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_lesson_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupUI();
        setupListeners();
    }

    /**
     * Initializes all view components by finding their IDs in the layout.
     *
     * @param view The root view of the fragment.
     */
    private void initViews(View view)
    {
        btnBack = view.findViewById(R.id.btnBack);
        tvStudentName = view.findViewById(R.id.tvStudentName);
        tvStudentId = view.findViewById(R.id.tvStudentId);
        tvLessonNumber = view.findViewById(R.id.tvLessonNumber);
        tvLessonDate = view.findViewById(R.id.tvLessonDate);
        tvLessonTime = view.findViewById(R.id.tvLessonTime);
        llStudentDetails = view.findViewById(R.id.llStudentDetails);
        btnWatchTrack = view.findViewById(R.id.btnWatchTrack);
        btnWatchSummary = view.findViewById(R.id.btnWatchSummary);
    }

    /**
     * Populates the UI components with the data from the currentLesson object.
     * Handles the visibility logic for student/teacher views.
     */
    private void setupUI()
    {
        if (currentLesson == null) return;

        tvLessonNumber.setText("Lesson " + currentLesson.getLessonNumber());

        if (isStudent)
        {
            llStudentDetails.setVisibility(View.GONE);
        }
        else
        {
            llStudentDetails.setVisibility(View.VISIBLE);
            tvStudentName.setText(currentLesson.getStudentName());
            tvStudentId.setText(currentLesson.getStudentID());
        }

        formatDateAndTime(currentLesson.getDateAndTime(), currentLesson.getDuration());
    }

    /**
     * Parses the raw date and time string, calculates the end time based on duration,
     * and updates the corresponding TextViews.
     *
     * @param dateTimeStr The raw date string from the database (e.g., "25-03-2026 14:00").
     * @param duration    The duration of the lesson in minutes.
     */
    private void formatDateAndTime(String dateTimeStr, int duration)
    {
        try
        {
            SimpleDateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            Date date = originalFormat.parse(dateTimeStr);

            if (date != null)
            {
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);
                tvLessonDate.setText(dateFormat.format(date));

                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String startTime = timeFormat.format(date);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.MINUTE, duration);
                String endTime = timeFormat.format(calendar.getTime());

                tvLessonTime.setText(startTime + " - " + endTime);
            }
        }

        catch (ParseException e)
        {
            e.printStackTrace();
            tvLessonDate.setText(dateTimeStr);
            tvLessonTime.setText(String.valueOf(duration) + " minutes");
        }
    }

    /**
     * Sets up click listeners for the buttons in the fragment.
     */
    private void setupListeners()
    {
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnWatchSummary.setOnClickListener(v ->
        {
            if (currentLesson != null && currentLesson.getHasSummary())
            {
                showSummaryDialog();
            }

            else
            {
                Toast.makeText(getContext(), "Summary is not available for this lesson.", Toast.LENGTH_SHORT).show();
            }
        });

        btnWatchTrack.setOnClickListener(v ->
        {
            if (currentLesson != null && currentLesson.getHasTrack())
            {
                navigateToTrackMap();
            }

            else
            {
                Toast.makeText(getContext(), "No track recorded for this lesson.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Displays an AlertDialog containing the AI-generated lesson summary.
     */
    private void showSummaryDialog()
    {
        // TODO: Implement AlertDialog construction using currentLesson.getSummary()
        Toast.makeText(getContext(), "Opening Summary Dialog...", Toast.LENGTH_SHORT).show();
    }

    /**
     * Replaces the current fragment with the map tracking fragment.
     */
    private void navigateToTrackMap()
    {
        // TODO: Implement FragmentManager transaction to the map fragment
        Toast.makeText(getContext(), "Navigating to Track Map...", Toast.LENGTH_SHORT).show();
    }
}