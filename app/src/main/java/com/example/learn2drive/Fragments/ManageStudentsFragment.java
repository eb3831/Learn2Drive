package com.example.learn2drive.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.learn2drive.Activities.TeacherMainActivity;
import com.example.learn2drive.Adapters.StudentAdapter;
import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.Student;
import com.example.learn2drive.Objects.User;
import com.example.learn2drive.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for displaying and managing active students of a teacher.
 * Allows teachers to view their students, navigate to requests or archive,
 * and move active students to the archive.
 */
public class ManageStudentsFragment extends Fragment
{
    private RecyclerView rvManageStudents;
    private LinearLayout btnRequests, btnArchive;
    private ProgressBar pbLoading;
    private LinearLayout llEmptyState;

    private StudentAdapter studentAdapter;
    private List<Student> studentList;

    private DatabaseReference studentsRef;
    private ValueEventListener studentsListener;

    public ManageStudentsFragment()
    {
    }

    public static ManageStudentsFragment newInstance()
    {
        ManageStudentsFragment fragment = new ManageStudentsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_manage_students, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadActiveStudents();
    }

    /**
     * Initializes the UI components from the layout.
     *
     * @param view The root view of the fragment.
     */
    private void initViews(View view)
    {
        rvManageStudents = view.findViewById(R.id.rvManageStudents);
        btnRequests = view.findViewById(R.id.btnRequests);
        btnArchive = view.findViewById(R.id.btnArchive);
        pbLoading = view.findViewById(R.id.pbManageStudents);
        llEmptyState = view.findViewById(R.id.llMangeStudentsEmptyState);

        studentList = new ArrayList<>();
    }

    /**
     * Configures the RecyclerView with its layout manager and adapter.
     */
    private void setupRecyclerView()
    {
        rvManageStudents.setHasFixedSize(true);
        rvManageStudents.setLayoutManager(new LinearLayoutManager(getContext()));

        studentAdapter = new StudentAdapter(getContext(), studentList);
        rvManageStudents.setAdapter(studentAdapter);

        studentAdapter.setOnItemClickListener(new StudentAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(Student student)
            {
                showArchiveConfirmation(student);
            }
        });
    }

    /**
     * Sets up click listeners for the navigation buttons.
     */
    private void setupListeners()
    {
        btnRequests.setOnClickListener(v -> {
            ((TeacherMainActivity) requireActivity()).replaceFragment(
                    new StudentsRequestsFragment(), true,
                    "StudentsRequestsFragment");
        });

        btnArchive.setOnClickListener(v -> {
            ((TeacherMainActivity) requireActivity()).replaceFragment(
                    new StudentsArchiveFragment(), true,
                    "StudentsArchiveFragment");
        });
    }

    /**
     * Shows a confirmation dialog before moving a student to the archive.
     *
     * @param student The student object to be archived.
     */
    private void showArchiveConfirmation(Student student)
    {
        new AlertDialog.Builder(getContext())
                .setTitle("Archive Student")
                .setMessage("Are you sure you want to move " + student.getFullName() + " to the archive?")
                .setCancelable(false)
                .setPositiveButton("Yes, Archive", (dialog, which) -> {
                    archiveStudent(student);
                })
                .setNegativeButton("Cancel", null)
                .setIcon(R.drawable.book)
                .show();
    }

    /**
     * Updates Firebase database to change the student's status to ARCHIVED.
     *
     * @param student The student to archive.
     */
    private void archiveStudent(Student student)
    {
        if (FBRef.uid == null) return;

        FBRef.refClasses.child(FBRef.uid).child("students").child(student.getUid())
                .setValue("ARCHIVED")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), student.getFullName() + " moved to archive.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error archiving student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Fetches all student UIDs with "ACTIVE" status from the database.
     */
    private void loadActiveStudents()
    {
        if (FBRef.uid == null) return;

        pbLoading.setVisibility(View.VISIBLE);
        rvManageStudents.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);

        studentsRef = FBRef.refClasses.child(FBRef.uid).child("students");

        studentsListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                ArrayList<String> activeUids = new ArrayList<>();

                for (DataSnapshot studentSnapshot : snapshot.getChildren())
                {
                    String status = studentSnapshot.getValue(String.class);

                    if (status != null && status.equals(User.ACTIVE))
                    {
                        activeUids.add(studentSnapshot.getKey());
                    }
                }

                if (activeUids.isEmpty())
                {
                    pbLoading.setVisibility(View.GONE);
                    studentList.clear();
                    studentAdapter.notifyDataSetChanged();
                    updateUI();
                    return;
                }

                fetchStudentsData(activeUids);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error loading students", Toast.LENGTH_SHORT).show();
            }
        };

        studentsRef.addValueEventListener(studentsListener);
    }

    /**
     * Fetches full Student objects for a list of UIDs and updates the UI.
     *
     * @param activeUids List of active student UIDs to fetch.
     */
    private void fetchStudentsData(List<String> activeUids)
    {
        studentList.clear();
        final int[] loadedCount = {0};

        for (String studentUid : activeUids)
        {
            FBRef.refStudents.child(studentUid).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    Student student = snapshot.getValue(Student.class);
                    if (student != null)
                    {
                        studentList.add(student);
                    }
                    checkLoadingComplete(++loadedCount[0], activeUids.size());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    checkLoadingComplete(++loadedCount[0], activeUids.size());
                }
            });
        }
    }

    /**
     * Checks if all asynchronous database calls are finished to hide the progress bar.
     *
     * @param current The number of students loaded so far.
     * @param total   The total number of students to load.
     */
    private void checkLoadingComplete(int current, int total)
    {
        if (current == total)
        {
            pbLoading.setVisibility(View.GONE);
            studentAdapter.notifyDataSetChanged();
            updateUI();
        }
    }

    /**
     * Toggles visibility between the RecyclerView and the Empty State placeholder
     * based on the size of the students list.
     */
    private void updateUI()
    {
        if (studentList.isEmpty())
        {
            rvManageStudents.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        }

        else
        {
            rvManageStudents.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        // Remove the listener to avoid memory leaks and duplicate data bugs
        if (studentsRef != null && studentsListener != null)
        {
            studentsRef.removeEventListener(studentsListener);
        }
    }
}