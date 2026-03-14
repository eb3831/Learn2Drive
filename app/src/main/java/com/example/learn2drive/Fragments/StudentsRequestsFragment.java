package com.example.learn2drive.Fragments;

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_students_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        rvStudentRequests = view.findViewById(R.id.rvStudentRequests);
        progressBar = view.findViewById(R.id.progressBar);
        btnBack = view.findViewById(R.id.btnBack);

        // Setup RecyclerView
        rvStudentRequests.setHasFixedSize(true);
        rvStudentRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the list
        studentsList = new ArrayList<>();

        // Initialize the adapter and connect the interface
        adapter = new StudentRequestsAdapter(getContext(), studentsList, new StudentRequestsAdapter.OnRequestClickListener()
        {
            @Override
            public void onAcceptClick(Student student, int position)
            {
                // Show confirmation dialog for accepting
                new android.app.AlertDialog.Builder(getContext())
                        .setTitle("Accept Request")
                        .setMessage("Are you sure you want to accept " + student.getFullName() + "'s request?")
                        .setCancelable(false)

                        .setPositiveButton("Accept",
                                new android.content.DialogInterface.OnClickListener()
                                {
                            @Override
                            public void onClick(android.content.DialogInterface dialog, int which)
                            {
                                // Change the status from "PENDING" to "ACCEPTED"
                                FBRef.refClasses.child(FBRef.uid).child("students")
                                        .child(student.getUid()).setValue(User.ACTIVE);

                                FBRef.refStudents.child(student.getUid()).child("status")
                                        .setValue(User.ACTIVE);

                                // Remove the item locally to refresh the UI with a smooth animation
                                studentsList.remove(position);
                                adapter.notifyItemRemoved(position);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onRejectClick(Student student, int position)
            {
                // Show confirmation dialog for rejecting
                new android.app.AlertDialog.Builder(getContext())
                        .setTitle("Reject Request")
                        .setMessage("Are you sure you want to reject " + student.getFullName() + "'s request?")
                        .setCancelable(false)

                        .setPositiveButton("Reject", new android.content.DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                String teacherUid = FBRef.uid != null ? FBRef.uid : FBRef.refAuth.getCurrentUser().getUid();

                                // Completely remove the request from the teacher's list
                                FBRef.refClasses.child(teacherUid).child("students")
                                        .child(student.getUid()).removeValue();

                                FBRef.refStudents.child(student.getUid()).child("status")
                                        .setValue(User.REJECTED);

                                // Remove the item locally to refresh the UI with a smooth animation
                                studentsList.remove(position);
                                adapter.notifyItemRemoved(position);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        rvStudentRequests.setAdapter(adapter);

        // Handle the back button click
        btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Load data from Firebase
        loadPendingRequests();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // Hide the Bottom Navigation View when this fragment is visible
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.teacherBottomNav);
        if (bottomNav != null)
        {
            bottomNav.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();

        // Show the Bottom Navigation View again when leaving this fragment
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.teacherBottomNav);
        if (bottomNav != null)
        {
            bottomNav.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Step 1: Find all student UIDs with "PENDING" status under this teacher
     */
    private void loadPendingRequests()
    {
        // Show loading animation
        progressBar.setVisibility(View.VISIBLE);
        studentsList.clear();

        // Ensure we have the teacher's UID (from your FBRef helper)
        String teacherUid = FBRef.uid;
        if (teacherUid == null && FBRef.refAuth.getCurrentUser() != null)
        {
            teacherUid = FBRef.refAuth.getCurrentUser().getUid();
        }

        if (teacherUid == null)
        {
            progressBar.setVisibility(View.GONE);
            return; // Error: No user connected
        }

        // Go to Classes -> TeacherUID
        FBRef.refClasses.child(teacherUid).child("students").addValueEventListener(
                new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                ArrayList<String> pendingStudentUids = new ArrayList<>();

                // Iterate over all students under this teacher
                for (DataSnapshot studentSnapshot : snapshot.getChildren())
                {
                    String studentUid = studentSnapshot.getKey();
                    String status = studentSnapshot.getValue(String.class);

                    if ("PENDING".equals(status))
                    {
                        pendingStudentUids.add(studentUid);
                    }
                }

                // If no pending requests, stop loading and update adapter
                if (pendingStudentUids.isEmpty())
                {
                    progressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    return;
                }

                // Step 2: Fetch the full Student objects
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
     * Step 2: Fetch the full Student object for each PENDING UID
     */
    private void fetchStudentsData(List<String> pendingStudentUids)
    {
        studentsList.clear();
        adapter.notifyDataSetChanged();

        // We use an array of size 1 so we can modify it inside the inner class
        final int[] loadedCount = {0};

        for (String studentUid : pendingStudentUids)
        {
            // Go to Users -> Students -> StudentUID
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

                    loadedCount[0]++;

                    // If we checked all students, hide progress bar and refresh list
                    if (loadedCount[0] == pendingStudentUids.size())
                    {
                        progressBar.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    loadedCount[0]++;
                    if (loadedCount[0] == pendingStudentUids.size())
                    {
                        progressBar.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }
}