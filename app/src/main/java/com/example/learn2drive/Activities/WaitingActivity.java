package com.example.learn2drive.Activities;

import static com.example.learn2drive.Helpers.FBRef.refClasses;
import static com.example.learn2drive.Helpers.FBRef.refStudents;
import static com.example.learn2drive.Helpers.FBRef.refTeachers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.User;
import com.example.learn2drive.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * WaitingActivity handles the intermediate state where a user (Student or Teacher)
 * is waiting for admin or teacher approval.
 * * It listens to real-time updates from Firebase and redirects the user
 * to the main screen once the approval flag is updated.
 */
public class WaitingActivity extends AppCompatActivity
{
    TextView tvWaitingForApproval;
    Intent gi;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        gi = getIntent();
        initAll();
    }

    /**
     * Initializes the UI components and sets up Firebase listeners
     * based on the user type (Student or Teacher).
     */
    private void initAll()
    {
        tvWaitingForApproval = findViewById(R.id.tvWaitingForApproval);
        sp = getSharedPreferences("login_prefs", MODE_PRIVATE);

        // Student waiting for approval
        if(gi.getBooleanExtra("isStudent", true))
        {
            tvWaitingForApproval.setText("Waiting to be approved by Teacher");

            String teacherUid = sp.getString("teacher_uid", "");

            // Listen for changes in the specific teacher's pending students list
            refClasses.child(teacherUid).child(FBRef.uid).
                    addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    // Check if the student has been approved
                    if(snapshot.getValue(String.class) != null &&
                            snapshot.getValue(String.class).equals(User.ACTIVE))
                    {
                        // Removes the request from pending and updates student object in Firebase
                        refClasses.child(teacherUid).child("Pending Students").
                                child(FBRef.uid).setValue(null);
                        refStudents.child(FBRef.uid).child("status").setValue(User.ACTIVE);

                        updateSharedPrefs();

                        startActivity(new Intent(WaitingActivity.this, MainActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle potential database errors here
                }
            });
        }

        // Teacher waiting for approval
        else
        {
            tvWaitingForApproval.setText("Waiting to be approved by Admin");

            // Listen for changes in the status property of the teacher's object
            refTeachers.child(FBRef.uid).child("status").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    // Checks if the admin has approved the teacher
                    if(snapshot.getValue(String.class) != null &&
                            snapshot.getValue(String.class).equals(User.ACTIVE))
                    {
                        updateSharedPrefs();

                        startActivity(new Intent(WaitingActivity.this, MainActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle potential database errors here
                }
            });

        }
    }

    /**
     * Updates the local SharedPreferences to indicate that the user has been approved.
     * This makes sure the user is not shown the waiting screen again.
     */
    private void updateSharedPrefs()
    {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("is_approved", true);
        editor.apply();
    }
}