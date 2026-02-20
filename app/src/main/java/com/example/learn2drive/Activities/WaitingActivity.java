package com.example.learn2drive.Activities;

import static com.example.learn2drive.Helpers.FBRef.refClasses;
import static com.example.learn2drive.Helpers.FBRef.refStudents;
import static com.example.learn2drive.Helpers.FBRef.refTeachers;
import static com.example.learn2drive.Helpers.FBRef.refTeachersRequests;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

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
        initViews();
    }

    private void initViews()
    {
        tvWaitingForApproval = findViewById(R.id.tvWaitingForApproval);
        sp = getSharedPreferences("login_prefs", MODE_PRIVATE);

        // Student waiting for approval
        if(gi.getBooleanExtra("isStudent", true))
        {
            tvWaitingForApproval.setText("Waiting to be approved by Teacher");

            String teacherUid = sp.getString("teacher_uid", "");

            refClasses.child(teacherUid).child("Pending Students").child(FBRef.uid).
                    addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.getValue(Boolean.class) != null && Boolean.TRUE.equals(snapshot.getValue(Boolean.class)))
                    {
                        // Removes the request and moves to home screen
                        refClasses.child(teacherUid).child("Pending Students").
                                child(FBRef.uid).setValue(null);
                        refStudents.child(FBRef.uid).child("approved").setValue(true);

                        updateSharedPrefs();

                        startActivity(new Intent(WaitingActivity.this, MainActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        // Teacher waiting for approval
        else
        {
            tvWaitingForApproval.setText("Waiting to be approved by Admin");

            refTeachersRequests.child(FBRef.uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.getValue(Boolean.class) != null && Boolean.TRUE.equals(snapshot.getValue(Boolean.class)))
                    {
                        // Removes the request and moves to home screen
                        refTeachersRequests.child(FBRef.uid).setValue(null);
                        refTeachers.child(FBRef.uid).child("approved").setValue(true);

                        updateSharedPrefs();

                        startActivity(new Intent(WaitingActivity.this, MainActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

    private void updateSharedPrefs()
    {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("is_approved", true);
        editor.apply();
    }
}