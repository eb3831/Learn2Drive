package com.example.learn2drive.Activities;

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
import com.google.firebase.database.DatabaseReference;
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
            addListenerToFBBranch(true);
        }

        // Teacher waiting for approval
        else
        {
            tvWaitingForApproval.setText("Waiting to be approved by Admin");
            addListenerToFBBranch(false);
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

    private void addListenerToFBBranch(boolean isStudent)
    {
        DatabaseReference ref = isStudent ? refStudents : refTeachers;

        // Listen for changes in the status property of given branch (Teachers/Students)
        ref.child(FBRef.uid).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                // Checks if the user was approved
                if(snapshot.getValue(String.class) != null &&
                        snapshot.getValue(String.class).equals(User.ACTIVE))
                {
                    updateSharedPrefs();

                    startActivity(new Intent(WaitingActivity.this, StudentMainActivity.class));
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