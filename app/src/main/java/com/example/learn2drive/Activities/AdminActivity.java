package com.example.learn2drive.Activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.Teacher;
import com.example.learn2drive.Objects.User;
import com.example.learn2drive.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Activity for administrators to manage teacher registrations.
 * Handles viewing pending teacher requests, approving/rejecting them,
 * and viewing the list of currently active teachers.
 */
public class AdminActivity extends AppCompatActivity
{
    private AppCompatButton btnNewRequests, btnActiveTeachers;
    private TextView tvListTitle;
    private ListView lvAdminTeachers;
    private boolean showingRequests = true;

    private ArrayList<Teacher> pendingTeachersList;
    private ArrayList<Teacher> activeTeachersList;
    private ArrayList<String> displayNamesList;
    private ArrayAdapter<String> adapter;

    /**
     * Initializes the activity, sets up the user interface, configures the lists,
     * and fetches the initial data from Firebase.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        initViews();
        setupListView();
        fetchTeachersData();
        updateUI();
    }

    /**
     * Initializes the UI components by finding their views and instantiates
     * the ArrayLists required for managing teacher data.
     */
    private void initViews()
    {
        btnNewRequests = findViewById(R.id.btnNewRequests);
        btnActiveTeachers = findViewById(R.id.btnActiveTeachers);
        tvListTitle = findViewById(R.id.tvListTitle);
        lvAdminTeachers = findViewById(R.id.lvAdminTeachers);

        pendingTeachersList = new ArrayList<>();
        activeTeachersList = new ArrayList<>();
        displayNamesList = new ArrayList<>();
    }

    /**
     * Sets up the ListView with its adapter and defines the item click listener
     * to handle interactions with specific teachers in the list.
     */
    private void setupListView()
    {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayNamesList);
        lvAdminTeachers.setAdapter(adapter);

        lvAdminTeachers.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (showingRequests)
                {
                    Teacher selectedTeacher = pendingTeachersList.get(position);
                    showApprovalDialog(selectedTeacher);
                }
                else
                {
                    Teacher selectedTeacher = activeTeachersList.get(position);
                    // כאן תוכל להוסיף דיאלוג להצגת פרטי המורה או לחסימה (Suspend)
                    Toast.makeText(AdminActivity.this, "Active Teacher: " + selectedTeacher.getFullName(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Attaches a ValueEventListener to the Firebase Realtime Database to fetch
     * all teachers and categorizes them into active and pending lists.
     */
    private void fetchTeachersData()
    {
        FBRef.refTeachers.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                pendingTeachersList.clear();
                activeTeachersList.clear();

                for (DataSnapshot ds : snapshot.getChildren())
                {
                    Teacher teacher = ds.getValue(Teacher.class);
                    if (teacher != null)
                    {
                        if (User.PENDING.equals(teacher.getStatus()))
                        {
                            pendingTeachersList.add(teacher);
                        }
                        else if (User.ACTIVE.equals(teacher.getStatus()))
                        {
                            activeTeachersList.add(teacher);
                        }
                    }
                }
                refreshDisplayList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(AdminActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Click handler for the "New Requests" button.
     * Switches the current view to display pending teacher approvals.
     *
     * @param v The view that was clicked.
     */
    public void onNewRequestsClicked(View v)
    {
        if (!showingRequests)
        {
            showingRequests = true;
            updateUI();
            refreshDisplayList();
        }
    }

    /**
     * Click handler for the "Active Teachers" button.
     * Switches the current view to display registered and active teachers.
     *
     * @param v The view that was clicked.
     */
    public void onActiveTeachersClicked(View v)
    {
        if (showingRequests)

        {
            showingRequests = false;
            updateUI();
            refreshDisplayList();
        }
    }

    /**
     * Updates the visual styles of the toggle buttons and the title
     * based on the currently selected state (showing requests or active teachers).
     */
    private void updateUI()
    {
        if (showingRequests)
        {
            btnNewRequests.setBackgroundResource(R.drawable.bg_button_black);
            btnNewRequests.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnActiveTeachers.setBackgroundResource(R.drawable.bg_input_default);
            btnActiveTeachers.setTextColor(ContextCompat.getColor(this, R.color.gray_700));

            tvListTitle.setText("Pending Approvals");
        }
        else
        {
            btnActiveTeachers.setBackgroundResource(R.drawable.bg_button_black);
            btnActiveTeachers.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnNewRequests.setBackgroundResource(R.drawable.bg_input_default);
            btnNewRequests.setTextColor(ContextCompat.getColor(this, R.color.gray_700));

            tvListTitle.setText("Registered Teachers");
        }
    }

    /**
     * Refreshes the displayed list view data by populating the display list
     * from either the pending or active teachers list and notifying the adapter.
     */
    private void refreshDisplayList()
    {
        displayNamesList.clear();
        ArrayList<Teacher> currentList = showingRequests ? pendingTeachersList : activeTeachersList;

        for (Teacher t : currentList)
        {
            displayNamesList.add(t.getFullName() + "\nID: " + t.getIdNumber());
        }

        adapter.notifyDataSetChanged();
    }


    /**
     * Displays a dialog prompting the admin to either approve or reject
     * a pending teacher's registration request. Updates Firebase accordingly.
     *
     * @param teacher The Teacher object representing the pending user.
     */
    private void showApprovalDialog(Teacher teacher)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Approve Teacher");
        builder.setMessage("Do you want to approve " + teacher.getFullName() + "?\nID: " + teacher.getIdNumber());

        builder.setPositiveButton("Approve", (dialog, which) ->
        {
            // Changing the status to ACTIVE
            FBRef.refTeachers.child(teacher.getUid()).child("status").setValue(User.ACTIVE)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AdminActivity.this, "Teacher Approved!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        builder.setNegativeButton("Reject", (dialog, which) ->
        {
            // Changing the status to REJECTED
            FBRef.refTeachers.child(teacher.getUid()).child("status").setValue(User.REJECTED)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AdminActivity.this, "Teacher request rejected.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}