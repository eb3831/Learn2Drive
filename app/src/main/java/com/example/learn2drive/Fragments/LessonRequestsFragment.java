package com.example.learn2drive.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learn2drive.Adapters.LessonRequestsAdapter;
import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.LessonRequestModel;
import com.example.learn2drive.Objects.ScheduledLesson;
import com.example.learn2drive.Objects.Student;
import com.example.learn2drive.Objects.TimeSlot;
import com.example.learn2drive.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for displaying and managing pending lesson requests for the connected teacher.
 */
public class LessonRequestsFragment extends Fragment implements LessonRequestsAdapter.OnRequestClickListener
{

    private ImageButton btnBack;
    private TextView tvPendingCount;
    private RecyclerView rvLessonRequests;

    private LessonRequestsAdapter adapter;
    private List<LessonRequestModel> requestList;

    private ValueEventListener timetableListener;

    public LessonRequestsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_lesson_requests, container, false);

        initViews(view);
        setupRecyclerView();
        loadLessonRequests();

        return view;
    }

    /**
     * Initializes all UI components from the layout.
     *
     * @param view The root view of the fragment.
     */
    private void initViews(View view)
    {
        btnBack = view.findViewById(R.id.btnBack);
        tvPendingCount = view.findViewById(R.id.tvPendingCount);
        rvLessonRequests = view.findViewById(R.id.rvLessonRequests);

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    /**
     * Sets up the RecyclerView with a LinearLayoutManager and the adapter.
     */
    private void setupRecyclerView()
    {
        requestList = new ArrayList<>();
        adapter = new LessonRequestsAdapter(getContext(), requestList, this);
        rvLessonRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLessonRequests.setAdapter(adapter);
    }

    /**
     * Loads lesson requests from the Firebase Realtime Database.
     * Listens to the teacher's timetable and fetches student details for requested slots.
     */
    private void loadLessonRequests()
    {
        if (FBRef.uid == null || FBRef.uid.isEmpty()) return;

        timetableListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                requestList.clear();
                adapter.notifyDataSetChanged();
                updatePendingCount();

                // Iterate through all dates in the timetable
                for (DataSnapshot dateSnapshot : snapshot.getChildren())
                {
                    String date = dateSnapshot.getKey();

                    for (DataSnapshot timeSnapshot : dateSnapshot.getChildren())
                    {
                        TimeSlot slot = timeSnapshot.getValue(TimeSlot.class);

                        if (slot != null && slot.isRequested() && slot.getStudentUid() != null)
                        {
                            fetchStudentDataAndAddToList(slot, date);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Log.e("LessonRequests", "Error loading timetable: " + error.getMessage());
            }
        };

        FBRef.refTeachersTimeTable.child(FBRef.uid).addValueEventListener(timetableListener);
    }

    /**
     * Fetches the student details from Firebase and adds the combined request to the list.
     *
     * @param slot The requested time slot.
     * @param date The date of the request.
     */
    private void fetchStudentDataAndAddToList(TimeSlot slot, String date)
    {
        FBRef.refStudents.child(slot.getStudentUid()).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                Student student = snapshot.getValue(Student.class);
                if (student != null)
                {
                    LessonRequestModel requestModel = new LessonRequestModel(slot, student, date);
                    requestList.add(requestModel);
                    adapter.notifyDataSetChanged();
                    updatePendingCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Log.e("LessonRequests", "Error loading student: " + error.getMessage());
            }
        });
    }

    /**
     * Updates the pending requests counter text view.
     */
    private void updatePendingCount()
    {
        int count = requestList.size();
        tvPendingCount.setText(count + " pending requests");
    }

    @Override
    public void onAcceptClick(LessonRequestModel request)
    {
        if (getContext() == null || FBRef.uid == null) return;

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Accept Lesson Request")
                .setMessage("Are you sure you want to schedule this lesson?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    String teacherId = FBRef.uid;
                    String date = request.getDate();
                    TimeSlot slot = request.getTimeSlot();
                    Student student = request.getStudent();

                    // Update the TimeSlot status to BOOKED
                    slot.setStatus(TimeSlot.STATUS_BOOKED);
                    FBRef.refTeachersTimeTable.child(teacherId).child(date).child(slot.getStartTime()).setValue(slot);

                    // Query Firebase to count existing scheduled lessons for this student
                    FBRef.refScheduledLessons.child(teacherId).child(student.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot)
                                {
                                    long currentlyScheduledCount = snapshot.getChildrenCount();

                                    int lessonNum = student.getLessonsCompleted() + (int) currentlyScheduledCount + 1;

                                    String dateAndTime = date + " " + slot.getStartTime();

                                    ScheduledLesson newLesson = new ScheduledLesson(
                                            lessonNum,
                                            teacherId,
                                            student.getUid(),
                                            student.getIdNumber(),
                                            dateAndTime,
                                            slot.getDuration(),
                                            ScheduledLesson.ACCEPTED,
                                            student.getFullName()
                                    );

                                    // Save the new lesson using the DateAndTime as the unique key
                                    FBRef.refScheduledLessons.child(teacherId).
                                            child(student.getUid()).child(dateAndTime).
                                            setValue(newLesson);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error)
                                {
                                }
                            });
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDeclineClick(LessonRequestModel request)
    {
        if (getContext() == null || FBRef.uid == null) return;

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Decline Lesson Request")
                .setMessage("Are you sure you want to decline this request?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    String teacherId = FBRef.uid;
                    String date = request.getDate();
                    TimeSlot slot = request.getTimeSlot();

                    slot.setStatus(TimeSlot.STATUS_AVAILABLE);
                    slot.setStudentUid("");

                    FBRef.refTeachersTimeTable.child(teacherId).child(date).child(slot.getStartTime()).setValue(slot);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if (timetableListener != null && FBRef.uid != null)
        {
            FBRef.refTeachersTimeTable.child(FBRef.uid).removeEventListener(timetableListener);
        }
    }
}