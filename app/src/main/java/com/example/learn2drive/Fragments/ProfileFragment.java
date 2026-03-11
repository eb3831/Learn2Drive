package com.example.learn2drive.Fragments;

import static com.example.learn2drive.Helpers.FBRef.refProfilePics;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.Student;
import com.example.learn2drive.Objects.Teacher;
import com.example.learn2drive.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class ProfileFragment extends Fragment
{
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_FULL_IMAGE_CAPTURE = 202;
    private String currentPath;
    private Uri imageUri;

    private static final String ARG_IS_STUDENT = "isStudent";
    private boolean isStudent;

    // View declarations
    private TextView tvProfileSubtitle, tvProfileName, tvProfileId, tvProfileBirthDate, tvProfileTeacher;
    private ImageView ivProfilePicture, ivEditProfilePic;
    private LinearLayout teacherContainer;
    private FrameLayout loadingOverlay;
    private ProgressBar pbProfilePicLoading;

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
        loadProfilePicture();
        ivEditProfilePic.setOnClickListener(v -> checkCameraPermission());
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
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        ivEditProfilePic = view.findViewById(R.id.ivEditProfilePic);
        pbProfilePicLoading = view.findViewById(R.id.pbProfilePicLoading);
    }

    private void setupUI() {
        if (isStudent)
        {
            tvProfileSubtitle.setText("Student information");
            teacherContainer.setVisibility(View.VISIBLE);
        }

        else
        {
            tvProfileSubtitle.setText("Teacher information");
            teacherContainer.setVisibility(View.GONE);
        }
    }

    private void loadProfileData()
    {
        if (FBRef.uid == null || FBRef.uid.isEmpty())
        {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

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

    private void loadProfilePicture()
    {
        if (FBRef.uid == null || FBRef.uid.isEmpty()) return;

        StorageReference picRef = refProfilePics.child(FBRef.uid).child("profile.jpg");

        pbProfilePicLoading.setVisibility(View.VISIBLE);

        picRef.getDownloadUrl().addOnSuccessListener(uri ->
        {
            if (isAdded() && getContext() != null)
            {
                Glide.with(requireContext())
                        .load(uri)
                        .listener(new RequestListener<Drawable>()
                        {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                        Target<Drawable> target,
                                                        boolean isFirstResource)
                            {
                                pbProfilePicLoading.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model,
                                                           Target<Drawable> target,
                                                           DataSource dataSource,
                                                           boolean isFirstResource)
                            {
                                pbProfilePicLoading.setVisibility(View.GONE);

                                // Reset the tint and padding to default
                                ivProfilePicture.setImageTintList(null);
                                ivProfilePicture.setPadding(0, 0, 0, 0);

                                return false;
                            }
                        })
                        .circleCrop()
                        .into(ivProfilePicture);
            }
        }).addOnFailureListener(e ->
        {
            // Hides the loading spinner
            pbProfilePicLoading.setVisibility(View.GONE);
        });
    }

    private void checkCameraPermission()
    {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        else
        {
            launchCamera();
        }
    }

    private void launchCamera()
    {
        String filename = "tempfile_profile";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try
        {
            File imgFile = File.createTempFile(filename, ".jpg", storageDir);
            currentPath = imgFile.getAbsolutePath();

            String authority = requireActivity().getPackageName() + ".fileprovider";
            imageUri = FileProvider.getUriForFile(requireContext(), authority, imgFile);

            Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            if (takePicIntent.resolveActivity(requireActivity().getPackageManager()) != null)
            {
                startActivityForResult(takePicIntent, REQUEST_FULL_IMAGE_CAPTURE);
            }
        }

        catch (IOException e)
        {
            Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
            Log.e("ProfileFragment", "launchCamera error: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                launchCamera();
            }

            else
            {
                Toast.makeText(requireContext(),
                        "Camera permission is required to update profile picture",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FULL_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK)
        {
            uploadProfilePicture();
        }
    }

    private void uploadProfilePicture()
    {
        if (imageUri == null || FBRef.uid == null || FBRef.uid.isEmpty()) return;

        // Displays the spinner
        pbProfilePicLoading.setVisibility(View.VISIBLE);

        StorageReference picRef = FBRef.refProfilePics.child(FBRef.uid).child("profile.jpg");

        picRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                {
                    picRef.getDownloadUrl().addOnSuccessListener(uri ->
                    {
                        if (isAdded() && getContext() != null)
                        {
                            Glide.with(requireContext())
                                    .load(uri)
                                    .listener(new RequestListener<Drawable>()
                                    {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e,
                                                                    Object model,
                                                                    Target<Drawable> target,
                                                                    boolean isFirstResource)
                                        {
                                            pbProfilePicLoading.setVisibility(View.GONE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource,
                                                                       Object model,
                                                                       Target<Drawable> target,
                                                                       DataSource dataSource,
                                                                       boolean isFirstResource)
                                        {
                                            pbProfilePicLoading.setVisibility(View.GONE);
                                            ivProfilePicture.setImageTintList(null);
                                            ivProfilePicture.setPadding(0, 0, 0, 0);
                                            return false;
                                        }
                                    })
                                    .circleCrop()
                                    .into(ivProfilePicture);

                            Toast.makeText(requireContext(),
                                    "Profile picture updated successfully!", Toast.LENGTH_SHORT).show();
                        }
                    });
                })

                .addOnFailureListener(e ->
                {
                    pbProfilePicLoading.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Failed to upload image.", Toast.LENGTH_SHORT).show();
                    Log.e("ProfileFragment", "Upload failed: " + e.getMessage());
                });
    }
}