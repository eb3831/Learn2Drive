package com.example.learn2drive.Fragments;

import android.app.AlertDialog;
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

import com.example.learn2drive.Activities.StudentMainActivity;
import com.example.learn2drive.Activities.TeacherMainActivity;
import com.example.learn2drive.Objects.DoneLesson;
import com.example.learn2drive.Objects.LessonSummary;
import com.example.learn2drive.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment responsible for displaying the details of a completed driving lesson.
 * Receives a DoneLesson object and adapts the UI based on whether the user is a student or a teacher.
 */
public class DoneLessonDetailsFragment extends Fragment
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

    public DoneLessonDetailsFragment()
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
    public static DoneLessonDetailsFragment newInstance(DoneLesson lesson, boolean isStudent)
    {
        DoneLessonDetailsFragment fragment = new DoneLessonDetailsFragment();
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
        return inflater.inflate(R.layout.fragment_done_lesson_details, container, false);
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
     * Displays a custom AlertDialog containing the AI-generated lesson summary.
     * Inflates a custom layout and populates it without using dynamic views.
     */
    private void showSummaryDialog()
    {
        if (getContext() == null || currentLesson.getSummary() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_summary, null);

        TextView tvOverview = dialogView.findViewById(R.id.tvOverviewContent);
        TextView tvTopics = dialogView.findViewById(R.id.tvTopicsContent);
        TextView tvStrengths = dialogView.findViewById(R.id.tvStrengthsContent);
        TextView tvImprovements = dialogView.findViewById(R.id.tvImprovementsContent);
        TextView tvRecommendations = dialogView.findViewById(R.id.tvRecommendationsContent);
        LinearLayout btnClose = dialogView.findViewById(R.id.btnCloseDialog);

        LessonSummary summary = currentLesson.getSummary();

        tvOverview.setText(summary.getLessonSummary() != null ? summary.getLessonSummary() : "No overview available.");
        tvTopics.setText(formatListToBullets(summary.getTopicsCovered()));
        tvStrengths.setText(formatListToBullets(summary.getStrengths()));
        tvImprovements.setText(formatListToBullets(summary.getAreasForImprovement()));
        tvRecommendations.setText(formatListToBullets(summary.getRecommendations()));

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();

        btnClose.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    /**
     * Helper method to convert a List of Strings into a single bulleted String.
     *
     * @param list The list of strings to format.
     * @return A single formatted string with bullet points and newlines.
     */
    private String formatListToBullets(List<String> list)
    {
        if (list == null || list.isEmpty())
        {

            return "None";
        }

        StringBuilder builder = new StringBuilder();
        for (String item : list)
        {
            builder.append("• ").append(item).append("\n");
        }

        // Remove the very last newline character to avoid extra empty space at the bottom
        return builder.toString().trim();
    }

    /**
     * Replaces the current fragment with the map tracking fragment.
     */
    private void navigateToTrackMap()
    {
        if(isStudent)
        {
            ((StudentMainActivity)requireActivity()).replaceFragment(
                    LessonTrackFragment.newInstance(currentLesson),
                    true, "LESSON_TRACK_FRAGMENT");
        }

        else
        {
            ((TeacherMainActivity)requireActivity()).replaceFragment(
                    LessonTrackFragment.newInstance(currentLesson),
                    true, "LESSON_TRACK_FRAGMENT");
        }
    }
}