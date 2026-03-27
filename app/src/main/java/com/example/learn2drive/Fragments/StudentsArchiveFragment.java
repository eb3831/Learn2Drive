package com.example.learn2drive.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.learn2drive.Adapters.StudentAdapter;
import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.Student;
import com.example.learn2drive.Objects.User;
import com.example.learn2drive.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StudentsArchiveFragment extends Fragment
{
    private RecyclerView rvArchivedStudents;
    private LinearLayout layoutEmptyState;
    private ImageView btnBack;
    private ProgressBar pbLoading;

    private StudentAdapter studentAdapter;
    private List<Student> archivedStudentList;

    public StudentsArchiveFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_students_archive, container, false);

        initViews(view);

        btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        loadArchivedStudents();

        return view;
    }

    private void initViews(View view)
    {
        rvArchivedStudents = view.findViewById(R.id.rvArchivedStudents);
        layoutEmptyState = view.findViewById(R.id.studentsArchiveEmptyLayout);
        btnBack = view.findViewById(R.id.studentsArchiveBtnBack);
        pbLoading = view.findViewById(R.id.pbStudentsArchive);

        archivedStudentList = new ArrayList<>();
        rvArchivedStudents.setLayoutManager(new LinearLayoutManager(getContext()));

        studentAdapter = new StudentAdapter(getContext(), archivedStudentList);
        rvArchivedStudents.setAdapter(studentAdapter);

        studentAdapter.setOnItemClickListener(new StudentAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(Student student)
            {
                new android.app.AlertDialog.Builder(getContext())
                        .setTitle("Restore Student")
                        .setMessage("Are you sure you want to restore " + student.getFullName() + " to active status?")
                        .setPositiveButton("Yes, Restore", (dialog, which) ->
                        {
                            restoreStudent(student);
                        })
                        .setNegativeButton("Cancel", null)
                        .setIcon(R.drawable.ic_user)
                        .show();
            }
        });
    }

    /**
     * Restores an archived student by changing their status back to ACTIVE in the Firebase database.
     * Upon success, it reloads the archived students list to reflect the changes.
     *
     * @param student The Student object to be restored.
     */
    private void restoreStudent(Student student)
    {
        if (FBRef.uid == null) return;

        FBRef.refClasses.child(FBRef.uid).child("students").child(student.getUid())
                .setValue(User.ACTIVE)
                .addOnSuccessListener(aVoid ->
                {
                    Toast.makeText(getContext(), student.getFullName() + " restored successfully.", Toast.LENGTH_SHORT).show();
                    loadArchivedStudents();
                })
                .addOnFailureListener(e ->
                {
                    Toast.makeText(getContext(), "Error restoring student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadArchivedStudents()
    {
        if (FBRef.uid == null) return;

        archivedStudentList.clear();
        studentAdapter.notifyDataSetChanged();

        pbLoading.setVisibility(View.VISIBLE);
        rvArchivedStudents.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);

        FBRef.refClasses.child(FBRef.uid).child("students")
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        archivedStudentList.clear();
                        studentAdapter.notifyDataSetChanged();

                        ArrayList<String> archivedUids = new ArrayList<>();

                        for (DataSnapshot studentSnapshot : snapshot.getChildren())
                        {
                            String status = studentSnapshot.getValue(String.class);

                            if (status != null && status.equals("ARCHIVED"))
                            {
                                archivedUids.add(studentSnapshot.getKey());
                            }
                        }

                        if (archivedUids.isEmpty())
                        {
                            pbLoading.setVisibility(View.GONE);
                            rvArchivedStudents.setVisibility(View.GONE);
                            layoutEmptyState.setVisibility(View.VISIBLE);
                            return;
                        }

                        pbLoading.setVisibility(View.GONE);
                        layoutEmptyState.setVisibility(View.GONE);
                        rvArchivedStudents.setVisibility(View.VISIBLE);

                        for (String studentUid : archivedUids)
                        {
                            FBRef.refStudents.child(studentUid)
                                    .addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot studentData)
                                        {
                                            Student student = studentData.getValue(Student.class);

                                            if (student != null)
                                            {
                                                archivedStudentList.add(student);
                                                studentAdapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error)
                                        {
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        pbLoading.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error loading archived students", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}