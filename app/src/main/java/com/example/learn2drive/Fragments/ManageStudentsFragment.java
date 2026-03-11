package com.example.learn2drive.Fragments;

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

public class ManageStudentsFragment extends Fragment
{

    private RecyclerView rvManageStudents;
    private LinearLayout btnRequests, btnArchive;

    private StudentAdapter studentAdapter;
    private List<Student> studentList;

    public ManageStudentsFragment()
    {
        // Required empty public constructor
    }

    public static ManageStudentsFragment newInstance()
    {
        ManageStudentsFragment fragment = new ManageStudentsFragment();
        Bundle args = new Bundle();
        // You can add parameters here later using args.putString("KEY", value);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_manage_students, container, false);

        initViews(view);

        btnRequests.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Requests clicked - To be implemented", Toast.LENGTH_SHORT).show();
            }
        });

        btnArchive.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Archive clicked - To be implemented", Toast.LENGTH_SHORT).show();
            }
        });

        loadActiveStudents();

        return view;
    }

    private void initViews(View view)
    {
        rvManageStudents = view.findViewById(R.id.rvManageStudents);
        btnRequests = view.findViewById(R.id.btnRequests);
        btnArchive = view.findViewById(R.id.btnArchive);

        studentList = new ArrayList<>();
        rvManageStudents.setLayoutManager(new LinearLayoutManager(getContext()));

        studentAdapter = new StudentAdapter(getContext(), studentList);
        rvManageStudents.setAdapter(studentAdapter);

        studentAdapter.setOnItemClickListener(new StudentAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(Student student)
            {
                Toast.makeText(getContext(), "Selected: " + student.getFullName(), Toast.LENGTH_SHORT).show();

                // TODO: Add options for student profile
            }
        });
    }

    private void loadActiveStudents()
    {
        if (FBRef.uid == null) return;

        FBRef.refClasses.child(FBRef.uid).child("students")
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        studentList.clear();
                        studentAdapter.notifyDataSetChanged();

                        for (DataSnapshot studentSnapshot : snapshot.getChildren())
                        {
                            String studentUid = studentSnapshot.getKey();
                            String status = studentSnapshot.getValue(String.class);

                            if (status != null && status.equals(User.ACTIVE))
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
                                                    studentList.add(student);
                                                    studentAdapter.notifyDataSetChanged();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error)
                                            {
                                                Toast.makeText(getContext(), "Error loading student details", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Toast.makeText(getContext(), "Error loading students list", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}