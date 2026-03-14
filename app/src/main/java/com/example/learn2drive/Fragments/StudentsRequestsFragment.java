package com.example.learn2drive.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learn2drive.Adapters.StudentRequestsAdapter;
import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.Student;
import com.example.learn2drive.Objects.User;
import com.example.learn2drive.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for displaying and managing pending student registration requests.
 * Allows teachers to accept or reject students who wish to join their classes.
 */
public class StudentsRequestsFragment extends Fragment
{
    private RecyclerView rvStudentRequests;
    private ProgressBar progressBar;
    private ImageView btnBack;

    private StudentRequestsAdapter adapter;
    private List<Student> studentsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_students_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadPendingRequests();
    }

    /**
     * Initializes the UI components from the layout.
     * @param view The root view of the fragment.
     */
    private void initViews(View view)
    {
        rvStudentRequests = view.findViewById(R.id.rvStudentRequests);
        progressBar = view.findViewById(R.id.progressBar);
        btnBack = view.findViewById(R.id.btnBack);
        studentsList = new ArrayList<>();
    }

    /**
     * Configures the RecyclerView with its layout manager and adapter.
     */
    private void setupRecyclerView()
    {
        rvStudentRequests.setHasFixedSize(true);
        rvStudentRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new StudentRequestsAdapter(getContext(), studentsList, new StudentRequestsAdapter.OnRequestClickListener()
        {
            @Override
            public void onAcceptClick(Student student, int position)
            {
                showAcceptConfirmation(student, position);
            }

            @Override
            public void onRejectClick(Student student, int position)
            {
                showRejectConfirmation(student, position);
            }
        });

        rvStudentRequests.setAdapter(adapter);
    }

    /**
     * Sets up click listeners for static UI elements.
     */
    private void setupListeners()
    {
        btnBack.setOnClickListener(v ->
        {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    /**
     * Shows a confirmation dialog to accept a student request.
     * @param student The student object to be accepted.
     * @param position The position of the student in the list.
     */
    private void showAcceptConfirmation(Student student, int position)
    {
        new AlertDialog.Builder(getContext())
                .setTitle("Accept Request")
                .setMessage("Are you sure you want to accept " + student.getFullName() + "'s request?")
                .setCancelable(false)
                .setPositiveButton("Accept", (dialog, which) ->
                {
                    handleAcceptRequest(student, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Updates Firebase and the local list when a request is accepted.
     * @param student The student to accept.
     * @param position The position in the adapter.
     */
    private void handleAcceptRequest(Student student, int position)
    {
        String teacherUid = getTeacherUid();
        if (teacherUid == null)
        {
            return;
        }

        FBRef.refClasses.child(teacherUid).child("students")
                .child(student.getUid()).setValue(User.ACTIVE);

        FBRef.refStudents.child(student.getUid()).child("status")
                .setValue(User.ACTIVE);

        removeStudentFromList(position);
    }

    /**
     * Shows a confirmation dialog to reject a student request.
     * @param student The student object to be rejected.
     * @param position The position of the student in the list.
     */
    private void showRejectConfirmation(Student student, int position)
    {
        new AlertDialog.Builder(getContext())
                .setTitle("Reject Request")
                .setMessage("Are you sure you want to reject " + student.getFullName() + "'s request?")
                .setCancelable(false)
                .setPositiveButton("Reject", (dialog, which) ->
                {
                    handleRejectRequest(student, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Updates Firebase and the local list when a request is rejected.
     * @param student The student to reject.
     * @param position The position in the adapter.
     */
    private void handleRejectRequest(Student student, int position)
    {
        String teacherUid = getTeacherUid();
        if (teacherUid == null)
        {
            return;
        }

        FBRef.refClasses.child(teacherUid).child("students")
                .child(student.getUid()).removeValue();

        FBRef.refStudents.child(student.getUid()).child("status")
                .setValue(User.REJECTED);

        removeStudentFromList(position);
    }

    /**
     * Removes a student from the local list and updates the adapter with animation.
     * @param position The index to remove.
     */
    private void removeStudentFromList(int position)
    {
        studentsList.remove(position);
        adapter.notifyItemRemoved(position);
    }

    /**
     * Retrieves the current teacher's UID from FBRef or FirebaseAuth.
     * @return The UID string or null if not authenticated.
     */
    private String getTeacherUid()
    {
        if (FBRef.uid != null)
        {
            return FBRef.uid;
        }
        if (FBRef.refAuth.getCurrentUser() != null)
        {
            return FBRef.refAuth.getCurrentUser().getUid();
        }
        return null;
    }

    /**
     * Fetches all student UIDs with "PENDING" status from the database.
     */
    private void loadPendingRequests()
    {
        progressBar.setVisibility(View.VISIBLE);
        String teacherUid = getTeacherUid();

        if (teacherUid == null)
        {
            progressBar.setVisibility(View.GONE);
            return;
        }

        FBRef.refClasses.child(teacherUid).child("students").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                ArrayList<String> pendingStudentUids = new ArrayList<>();

                for (DataSnapshot studentSnapshot : snapshot.getChildren())
                {
                    String status = studentSnapshot.getValue(String.class);
                    if ("PENDING".equals(status))
                    {
                        pendingStudentUids.add(studentSnapshot.getKey());
                    }
                }

                if (pendingStudentUids.isEmpty())
                {
                    progressBar.setVisibility(View.GONE);
                    studentsList.clear();
                    adapter.notifyDataSetChanged();
                    return;
                }

                fetchStudentsData(pendingStudentUids);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Fetches full Student objects for a list of UIDs and updates the UI.
     * @param pendingStudentUids List of UIDs to fetch.
     */
    private void fetchStudentsData(List<String> pendingStudentUids)
    {
        studentsList.clear();
        final int[] loadedCount = {0};

        for (String studentUid : pendingStudentUids)
        {
            FBRef.refStudents.child(studentUid).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    Student student = snapshot.getValue(Student.class);
                    if (student != null)
                    {
                        studentsList.add(student);
                    }
                    checkLoadingComplete(++loadedCount[0], pendingStudentUids.size());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    checkLoadingComplete(++loadedCount[0], pendingStudentUids.size());
                }
            });
        }
    }

    /**
     * Checks if all asynchronous database calls are finished to hide the progress bar.
     * @param current The number of students loaded so far.
     * @param total The total number of students to load.
     */
    private void checkLoadingComplete(int current, int total)
    {
        if (current == total)
        {
            progressBar.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        toggleBottomNavigation(false);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        toggleBottomNavigation(true);
    }

    /**
     * Shows or hides the BottomNavigationView.
     * @param isVisible True to show, false to hide.
     */
    private void toggleBottomNavigation(boolean isVisible)
    {
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.teacherBottomNav);
        if (bottomNav != null)
        {
            bottomNav.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }
}