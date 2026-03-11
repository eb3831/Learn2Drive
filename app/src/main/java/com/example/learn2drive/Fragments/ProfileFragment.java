package com.example.learn2drive.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.Student;
import com.example.learn2drive.Objects.Teacher;
import com.example.learn2drive.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment
{
    private static final String ARG_IS_STUDENT = "isStudent";
    private boolean isStudent;

    // View declarations
    private TextView tvProfileSubtitle, tvProfileName, tvProfileId, tvProfileBirthDate, tvProfileTeacher;
    private LinearLayout teacherContainer;
    private FrameLayout loadingOverlay;

    public ProfileFragment()
    {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(boolean isStudent) 
    {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
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
            isStudent = getArguments().getBoolean(ARG_IS_STUDENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) 
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) 
    {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupUI();
        loadProfileData();
    }

    private void initViews(View view) 
    {
        tvProfileSubtitle = view.findViewById(R.id.tvProfileSubtitle);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileId = view.findViewById(R.id.tvProfileId);
        tvProfileBirthDate = view.findViewById(R.id.tvProfileBirthDate);
        tvProfileTeacher = view.findViewById(R.id.tvProfileTeacher);
        teacherContainer = view.findViewById(R.id.profileTeacherContainer);
        loadingOverlay = view.findViewById(R.id.profileLoadingOverlay);
    }

    private void setupUI() {
        if (isStudent) {
            tvProfileSubtitle.setText("Student information");
            teacherContainer.setVisibility(View.VISIBLE);
        } else {
            tvProfileSubtitle.setText("Teacher information");
            teacherContainer.setVisibility(View.GONE);
        }
    }

    private void loadProfileData() {
        if (FBRef.uid == null || FBRef.uid.isEmpty()) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // הצגת אנימציית הטעינה לפני הקריאה למסד הנתונים
        loadingOverlay.setVisibility(View.VISIBLE);

        if (isStudent) {
            FBRef.refStudents.child(FBRef.uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Student student = snapshot.getValue(Student.class);
                        if (student != null) {
                            tvProfileName.setText(student.getFullName());
                            tvProfileId.setText(student.getIdNumber());
                            tvProfileBirthDate.setText(student.getBirthDate());
                            tvProfileTeacher.setText(student.getTeacherName());
                        }
                    }
                    // הסתרת הטעינה בסיום
                    loadingOverlay.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load profile data.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            FBRef.refTeachers.child(FBRef.uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Teacher teacher = snapshot.getValue(Teacher.class);
                        if (teacher != null) {
                            tvProfileName.setText(teacher.getFullName());
                            tvProfileId.setText(teacher.getIdNumber());
                            tvProfileBirthDate.setText(teacher.getBirthDate());
                        }
                    }
                    // הסתרת הטעינה בסיום
                    loadingOverlay.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load profile data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}