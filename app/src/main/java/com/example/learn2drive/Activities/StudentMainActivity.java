package com.example.learn2drive.Activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.learn2drive.Fragments.ProfileFragment;
import com.example.learn2drive.Fragments.StudentHomeFragment;
import com.example.learn2drive.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main activity for the student user role.
 * Handles the bottom navigation and fragment transactions for the student's main interface.
 */
public class StudentMainActivity extends MasterActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener
{
    /**
     * Called when the activity is starting.
     * Sets the content view and calls the initialization method for the UI components.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     * then this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        initViews();
    }

    /**
     * Initializes the UI components of the activity.
     * Sets up the BottomNavigationView, attaches the selection listener,
     * and sets the default selected item to the student home screen.
     */
    private void initViews()
    {
        bottomNavigationView = findViewById(R.id.studentBottomNav);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.menu_student_home);
    }

    /**
     * Called when an item in the bottom navigation menu is selected.
     * Determines which fragment to instantiate based on the selected menu item ID
     * and triggers the fragment replacement.
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

        if (itemId == R.id.menu_student_home)
        {
            selectedFragment = new StudentHomeFragment();
            tag = "STUDENT_HOME";
        }

        else if (itemId == R.id.menu_student_profile)
        {
            selectedFragment = ProfileFragment.newInstance(true);
            tag = "STUDENT_PROFILE";
        }

        else if (itemId == R.id.menu_student_payment)
        {
            // selectedFragment = new StudentPaymentFragment();
            // tag = "STUDENT_PAYMENT";
        }

        if (selectedFragment != null)
        {
            replaceFragment(selectedFragment, false, tag);
        }

        return true;
    }

    /**
     * Replaces the current fragment within the student fragment container.
     * Overrides the method to explicitly target the specific container used in this activity.
     *
     * @param fragment       The new fragment to display.
     * @param addToBackStack True if the transaction should be added to the back stack, false otherwise.
     * @param tag            An optional tag name for the fragment.
     */
    public void replaceFragment(Fragment fragment, boolean addToBackStack, String tag)
    {
        super.replaceFragment(fragment, addToBackStack, tag, R.id.studentFragmentContainer);
    }
}