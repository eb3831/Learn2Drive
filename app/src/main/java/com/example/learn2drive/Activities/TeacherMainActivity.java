package com.example.learn2drive.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.learn2drive.Fragments.ManageStudentsFragment;
import com.example.learn2drive.Fragments.ProfileFragment;
import com.example.learn2drive.Fragments.TeacherHomeFragment;
import com.example.learn2drive.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TeacherMainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener
{
    private BottomNavigationView teacherBottomNav;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_main);

        initViews();
    }

    private void initViews()
    {
        teacherBottomNav = findViewById(R.id.teacherBottomNav);
        teacherBottomNav.setOnNavigationItemSelectedListener(this);
        teacherBottomNav.setSelectedItemId(R.id.menu_teacher_home);

        fragmentManager = getSupportFragmentManager();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.menu_teacher_home)
        {
            selectedFragment = new TeacherHomeFragment();
        }
        else if (itemId == R.id.menu_teacher_profile)
        {
            selectedFragment = ProfileFragment.newInstance(false);
        }
        else if (itemId == R.id.menu_teacher_students)
        {
            selectedFragment = ManageStudentsFragment.newInstance();
        }
        else if (itemId == R.id.menu_teacher_payment)
        {
            // selectedFragment = new HomeFragment();
        }

        if (selectedFragment != null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
        }

        return true;
    }

    /**
     * This method replaces the current fragment displayed on the screen.
     * @param fragment The fragment to replace to.
     * @param backStack Whether to add the fragment to back stack, or not.
     * @param fragmentName The name of the fragment.
     */
    public void replaceFragment(Fragment fragment, boolean backStack, String fragmentName)
    {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, fragmentName);

        if (backStack)
        {
            transaction.addToBackStack(fragmentName);
        }

        else
        {
            transaction.runOnCommit(this::invalidateOptionsMenu);
        }

        transaction.commit();

        // If added to back stack, call invalidateOptionsMenu manually after commit
        if (backStack)
        {
            fragmentManager.executePendingTransactions(); // ensures commit completes
            invalidateOptionsMenu();
        }
    }
}