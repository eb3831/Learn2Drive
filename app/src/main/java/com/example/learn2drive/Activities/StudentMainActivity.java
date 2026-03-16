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

public class StudentMainActivity extends MasterActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener
{

    private BottomNavigationView studentBottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        initViews();
    }

    private void initViews()
    {
        studentBottomNav = findViewById(R.id.studentBottomNav);
        studentBottomNav.setOnNavigationItemSelectedListener(this);

        // Set default fragment
        studentBottomNav.setSelectedItemId(R.id.menu_student_home);
    }

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

    public void replaceFragment(Fragment fragment, boolean addToBackStack, String tag)
    {
        super.replaceFragment(fragment, addToBackStack, tag, R.id.studentFragmentContainer);
    }
}