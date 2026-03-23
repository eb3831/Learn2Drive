package com.example.learn2drive.Fragments;

import static com.example.learn2drive.Helpers.FBRef.refProfilePics;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.learn2drive.Activities.TeacherMainActivity;
import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.ScheduledLesson;
import com.example.learn2drive.Objects.TimeSlot;
import com.example.learn2drive.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.StorageReference;

/**
 * Fragment responsible for displaying the details of a specific scheduled lesson for a teacher.
 * Handles starting the lesson or cancelling it with timetable updates using the FBRef helper class.
 */
public class TeacherLessonDetailsFragment extends Fragment
{

    private static final String ARG_LESSON = "scheduledLesson";

    private ScheduledLesson currentLesson;
    private String date = "";
    private String time = "";

    private ImageView ivBack;
    private ImageView ivStudentProfile;
    private ProgressBar pbProfilePicLoading;
    private TextView tvStudentName;
    private TextView tvLessonDate;
    private TextView tvLessonTime;
    private TextView tvLessonDuration;
    private MaterialButton btnStartLesson;
    private MaterialButton btnCancelLesson;
    private FrameLayout lessonLoadingOverlay;

    /**
     * Required empty public constructor.
     */
    public TeacherLessonDetailsFragment()
    {
    }

    /**
     * Factory method to create a new instance of this fragment using a ScheduledLesson object.
     *
     * @param lesson The complete ScheduledLesson object.
     * @return A new instance of fragment TeacherLessonDetailsFragment.
     */
    public static TeacherLessonDetailsFragment newInstance(ScheduledLesson lesson)
    {
        TeacherLessonDetailsFragment fragment = new TeacherLessonDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LESSON, lesson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            currentLesson = (ScheduledLesson) getArguments().getSerializable(ARG_LESSON);

            if (currentLesson != null && currentLesson.getDateAndTime() != null)
            {
                String fullDateTime = currentLesson.getDateAndTime();
                if (fullDateTime.contains(" "))
                {
                    String[] parts = fullDateTime.split(" ");
                    date = parts[0];
                    time = parts[1];
                } else
                {
                    date = fullDateTime;
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_teacher_lesson_details, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if (currentLesson != null)
        {
            populateUI();
        }
        setupClickListeners();
    }

    /**
     * Initializes all UI components by finding their references from the layout.
     *
     * @param view The root view of the fragment containing the UI elements.
     */
    private void initViews(View view)
    {
        ivBack = view.findViewById(R.id.ivBack);
        ivStudentProfile = view.findViewById(R.id.ivStudentProfile);
        pbProfilePicLoading = view.findViewById(R.id.pbProfilePicLoading);
        tvStudentName = view.findViewById(R.id.tvStudentName);
        tvLessonDate = view.findViewById(R.id.tvLessonDate);
        tvLessonTime = view.findViewById(R.id.tvLessonTime);
        tvLessonDuration = view.findViewById(R.id.tvLessonDuration);
        btnStartLesson = view.findViewById(R.id.btnStartLesson);
        btnCancelLesson = view.findViewById(R.id.btnCancelLesson);
        lessonLoadingOverlay = view.findViewById(R.id.lessonLoadingOverlay);
    }

    /**
     * Populates the UI fields directly from the passed ScheduledLesson object.
     */
    private void populateUI()
    {
        tvStudentName.setText(currentLesson.getStudentName());
        tvLessonDate.setText(date);
        tvLessonTime.setText(time);
        tvLessonDuration.setText(currentLesson.getDuration() + " minutes");

        loadStudentProfilePicture();
    }

    /**
     * Fetches the student's profile picture download URL from Firebase Storage
     * and loads it into the ImageView using Glide.
     */
    private void loadStudentProfilePicture()
    {
        if (currentLesson == null || currentLesson.getStudentUID() == null)
        {
            pbProfilePicLoading.setVisibility(View.GONE);
            return;
        }

        pbProfilePicLoading.setVisibility(View.VISIBLE);

        StorageReference profilePicRef = refProfilePics.child(currentLesson.getStudentUID())
                .child("profile.jpg");

        profilePicRef.getDownloadUrl().addOnSuccessListener(uri ->
        {
            if (isAdded() && getContext() != null)
            {
                ivStudentProfile.setImageTintList(null);
                ivStudentProfile.setPadding(0, 0, 0, 0);

                Glide.with(getContext())
                        .load(uri)
                        .placeholder(R.drawable.user)
                        .error(R.drawable.user)
                        .circleCrop()
                        .into(ivStudentProfile);

                pbProfilePicLoading.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e ->
        {
            pbProfilePicLoading.setVisibility(View.GONE);
            ivStudentProfile.setImageResource(R.drawable.user);
        });
    }

    /**
     * Sets up click listeners for the buttons and interactive views.
     */
    private void setupClickListeners()
    {
        ivBack.setOnClickListener(v ->
        {
            if (getActivity() != null)
            {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        btnStartLesson.setOnClickListener(v ->
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Start Lesson");
            builder.setMessage("Are you sure you want to start this lesson?");

            builder.setPositiveButton("Start", (dialog, which) ->
            {
                Fragment activeLessonFragment = new ActiveLessonFragment();
                ((TeacherMainActivity) requireActivity()).replaceFragment(activeLessonFragment, true, "ActiveLessonFragment");
            });

            builder.setNegativeButton("Cancel", (dialog, which) ->
            {
                dialog.dismiss();
            });

            builder.show();
        });

        btnCancelLesson.setOnClickListener(v -> showCancelLessonDialog());
    }

    /**
     * Displays a standard AlertDialog with cancellation options for the scheduled lesson.
     * Uses standard dialog buttons instead of a list of items.
     */
    private void showCancelLessonDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Cancel Lesson");
        builder.setMessage("How would you like to cancel this lesson?");

        builder.setPositiveButton("Cancel & Make Available", (dialog, which) ->
        {
            processLessonCancellation(true);
        });

        builder.setNegativeButton("Cancel & Mark Unavailable", (dialog, which) ->
        {
            processLessonCancellation(false);
        });

        builder.setNeutralButton("Back", (dialog, which) ->
        {
            dialog.dismiss();
        });

        builder.show();
    }

    /**
     * Removes the scheduled lesson using FBRef and updates the teacher's timetable status.
     *
     * @param makeAvailable True if the time slot should be marked as AVAILABLE, false for UNAVAILABLE.
     */
    private void processLessonCancellation(boolean makeAvailable)
    {
        if (FBRef.uid == null || currentLesson == null) return;

        lessonLoadingOverlay.setVisibility(View.VISIBLE);
        String dateTimeKey = currentLesson.getDateAndTime();

        // Removes the scheduled lesson
        FBRef.refScheduledLessons.child(FBRef.uid).child(currentLesson.getStudentUID())
                .child(dateTimeKey).removeValue().addOnSuccessListener(aVoid ->
                {

                    String newStatus = makeAvailable ? TimeSlot.STATUS_AVAILABLE : TimeSlot.STATUS_UNAVAILABLE;

                    // Updates timetable status and clears studentUid
                    FBRef.refTeachersTimeTable.child(FBRef.uid).child(date).child(time)
                            .child("status").setValue(newStatus).addOnSuccessListener(aVoid1 ->
                            {
                                FBRef.refTeachersTimeTable.child(FBRef.uid).child(date).child(time)
                                        .child("studentUid").setValue("").addOnSuccessListener(aVoid2 ->
                                        {
                                            lessonLoadingOverlay.setVisibility(View.GONE);
                                            Toast.makeText(getContext(), "Lesson cancelled successfully", Toast.LENGTH_SHORT).show();

                                            getActivity().getSupportFragmentManager().popBackStack();

                                        }).addOnFailureListener(e ->
                                        {
                                            lessonLoadingOverlay.setVisibility(View.GONE);
                                            Toast.makeText(getContext(), "Failed to clear student UID", Toast.LENGTH_SHORT).show();
                                        });

                            }).addOnFailureListener(e ->
                            {
                                lessonLoadingOverlay.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "Failed to update timetable", Toast.LENGTH_SHORT).show();
                            });

                }).addOnFailureListener(e ->
                {
                    lessonLoadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to cancel lesson", Toast.LENGTH_SHORT).show();
                });
    }
}