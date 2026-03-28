package com.example.learn2drive.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learn2drive.Activities.StudentMainActivity;
import com.example.learn2drive.Adapters.StudentLessonAdapter;
import com.example.learn2drive.Helpers.AlarmHelper;
import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.DoneLesson;
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
 * Fragment representing the student's home dashboard.
 * Displays upcoming lessons, handles notification permissions (legacy style), and manages database listeners.
 */
public class StudentHomeFragment extends Fragment
{
    private static final int REQUEST_NOTIFICATION_PERMISSION = 102;

    private RecyclerView studentRvScheduledLessons;
    private StudentLessonAdapter adapter;
    private ArrayList<ScheduledLesson> lessonList;
    private SimpleDateFormat dateTimeFormatter;
    private ProgressBar pbLoading;
    private LinearLayout layoutEmptyState, btnHistoryStudent;
    private LinearLayout btnSchedule;
    private String teacherUid;

    private ValueEventListener scheduledLessonsListener;

    /**
     * Required empty public constructor.
     */
    public StudentHomeFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_student_home, container, false);

        initViews(view);
        setupListeners();
        checkNotificationPermission();

        dateTimeFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

        fetchTeacherUidAndLessons();

        return view;
    }

    /**
     * Sets up the RecyclerView and initializes UI components.
     *
     * @param view The root view of the fragment.
     */
    private void initViews(View view)
    {
        studentRvScheduledLessons = view.findViewById(R.id.studentRvScheduledLessons);
        pbLoading = view.findViewById(R.id.studentHomePb);
        layoutEmptyState = view.findViewById(R.id.studentHomeEmptyStateLayout);
        btnSchedule = view.findViewById(R.id.btnSchedule);
        btnHistoryStudent = view.findViewById(R.id.btnHistoryStudent);

        btnHistoryStudent.setOnClickListener(v ->( (StudentMainActivity) requireActivity()).
                replaceFragment(StudentHistoryFragment.newInstance(), true,
                        "STUDENT_HISTORY"));

        studentRvScheduledLessons.setLayoutManager(new LinearLayoutManager(getContext()));
        lessonList = new ArrayList<>();
        adapter = new StudentLessonAdapter(lessonList, lesson -> {});
        studentRvScheduledLessons.setAdapter(adapter);
    }

    /**
     * Sets up click listeners for the interactive UI elements.
     */
    private void setupListeners()
    {
        btnSchedule.setOnClickListener(v -> {
            if (teacherUid != null && !teacherUid.isEmpty())
            {
                if (getActivity() instanceof StudentMainActivity)
                {
                    ((StudentMainActivity) getActivity()).replaceFragment(
                            ScheduleLessonFragment.newInstance(teacherUid),
                            true,
                            "SCHEDULE_LESSON"
                    );
                }
            }
            else
            {
                Toast.makeText(getContext(), "You don't have a teacher assigned yet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Checks if the app has permission to post notifications (Android 13+).
     * Requests permission using the legacy approach to maintain project consistency.
     */
    private void checkNotificationPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // Permission granted, alarms will be shown properly
            }
            else
            {
                Toast.makeText(getContext(), "Notifications disabled. You won't receive lesson reminders.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Fetches the teacher's UID and loads scheduled lessons.
     */
    private void fetchTeacherUidAndLessons()
    {
        if (FBRef.uid == null) return;

        pbLoading.setVisibility(View.VISIBLE);
        studentRvScheduledLessons.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);

        FBRef.refStudents.child(FBRef.uid).child("teacherUid").
                addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        teacherUid = snapshot.getValue(String.class);

                        if (teacherUid != null && !teacherUid.isEmpty())
                        {
                            loadScheduledLessons(teacherUid);
                        }
                        else
                        {
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
     * Loads the student's scheduled lessons from Firebase and manages the listener.
     * Path: Lessons -> Scheduled -> TeacherUID -> StudentUID -> DateAndTime
     *
     * @param teacherUid The UID of the assigned teacher.
     */
    private void loadScheduledLessons(String teacherUid)
    {
        scheduledLessonsListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                lessonList.clear();

                for (DataSnapshot lessonSnapshot : snapshot.getChildren())
                {
                    ScheduledLesson lesson = lessonSnapshot.getValue(ScheduledLesson.class);

                    if (lesson != null)
                    {
                        lessonList.add(lesson);
                    }
                }

                sortLessonsByDate();
                updateUIState();

                // Schedule alarms for all future lessons
                for (ScheduledLesson lesson : lessonList)
                {
                    AlarmHelper.scheduleLessonReminder(requireContext(), lesson);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                pbLoading.setVisibility(View.GONE);
            }
        };

        FBRef.refScheduledLessons.child(teacherUid).child(FBRef.uid).addValueEventListener(scheduledLessonsListener);
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

    @Override
    public void onStop()
    {
        super.onStop();

        if (scheduledLessonsListener != null && teacherUid != null && FBRef.uid != null)
        {
            FBRef.refScheduledLessons.child(teacherUid).child(FBRef.uid).removeEventListener(scheduledLessonsListener);
        }
    }
}