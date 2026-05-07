package com.example.learn2drive.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.learn2drive.Fragments.ActiveLessonFragment;
import com.example.learn2drive.Fragments.ManageStudentsFragment;
import com.example.learn2drive.Fragments.ProfileFragment;
import com.example.learn2drive.Fragments.TeacherHomeFragment;
import com.example.learn2drive.Objects.ScheduledLesson;
import com.example.learn2drive.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

/**
 * Main activity for the teacher side.
 * Handles the bottom navigation view routing and incoming intents from background services.
 */
public class TeacherMainActivity extends MasterActivity implements
        NavigationBarView.OnItemSelectedListener
{

    /**
     * Called when the activity is starting.
     * Initializes the activity layout and handles any incoming intent data,
     * specifically checking if it needs to resume an active lesson.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     * then this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_main);

        initViews();

        if (savedInstanceState == null)
        {
            Intent intent = getIntent();

            if (intent != null && intent.getBooleanExtra("RETURN_TO_ACTIVE_LESSON", false))
            {
                checkAndNavigateToActiveLesson(intent);
            }
            else
            {
                bottomNavigationView.setSelectedItemId(R.id.menu_teacher_home);
            }
        }
    }

    /**
     * Initializes the UI components of the activity.
     * Specifically sets up the bottom navigation view and attaches its item selection listener.
     */
    private void initViews()
    {
        bottomNavigationView = findViewById(R.id.teacherBottomNav);
        bottomNavigationView.setOnItemSelectedListener(this);
    }

    /**
     * Called when an item in the bottom navigation menu is selected.
     * Determines which fragment to display based on the selected menu item ID.
     *
     * @param item The selected menu item.
     * @return true to display the item as the selected item.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        Fragment selectedFragment = null;
        String tag = "";
        int itemId = item.getItemId();

        if (itemId == R.id.menu_teacher_home)
        {
            selectedFragment = new TeacherHomeFragment();
            tag = "TEACHER_HOME";
        }
        else if (itemId == R.id.menu_teacher_profile)
        {
            selectedFragment = ProfileFragment.newInstance(false);
            tag = "TEACHER_PROFILE";
        }
        else if (itemId == R.id.menu_teacher_students)
        {
            selectedFragment = ManageStudentsFragment.newInstance();
            tag = "TEACHER_MANAGE_STUDENTS";
        }
        else if (itemId == R.id.menu_teacher_payment)
        {
            // selectedFragment = new PaymentFragment();
            // tag = "PAYMENT";
        }

        if (selectedFragment != null)
        {
            replaceFragment(selectedFragment, false, tag);
        }

        return true;
    }

    /**
     * Replaces the current fragment in the main container.
     * Overrides the method from MasterActivity to target the teacher-specific fragment container.
     *
     * @param fragment       The new fragment to display.
     * @param addToBackStack Whether the transaction should be added to the back stack.
     * @param tag            A tag name for the fragment.
     */
    public void replaceFragment(Fragment fragment, boolean addToBackStack, String tag)
    {
        super.replaceFragment(fragment, addToBackStack, tag, R.id.teacherFragmentContainer);
    }

    /**
     * Called when the activity receives a new intent while it is already running at the top of the history stack.
     * Updates the intent and checks if navigation to the active lesson is required.
     *
     * @param intent The new intent that was started for the activity.
     */
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
        checkAndNavigateToActiveLesson(intent);
    }

    /**
     * Checks if the intent contains a flag to return to the active lesson.
     * Parses the lesson data and navigates to the ActiveLessonFragment safely,
     * ensuring backward compatibility for different Android API levels.
     *
     * @param intent The intent containing potential lesson data.
     */
    public void checkAndNavigateToActiveLesson(Intent intent)
    {
        if (intent != null && intent.getBooleanExtra("RETURN_TO_ACTIVE_LESSON", false))
        {
            intent.setExtrasClassLoader(ScheduledLesson.class.getClassLoader());

            ScheduledLesson lesson = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            {
                lesson = intent.getSerializableExtra("lesson_data", ScheduledLesson.class);
            }
            else
            {
                lesson = (ScheduledLesson) intent.getSerializableExtra("lesson_data");
            }

            if (lesson != null)
            {
                ActiveLessonFragment fragment = ActiveLessonFragment.newInstance(lesson);
                replaceFragment(fragment, false, "ActiveLessonFragment");
                intent.removeExtra("RETURN_TO_ACTIVE_LESSON");
            }
            else
            {
                intent.removeExtra("RETURN_TO_ACTIVE_LESSON");
                bottomNavigationView.setSelectedItemId(R.id.menu_teacher_home);
            }
        }
    }
}