package com.example.learn2drive.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.learn2drive.Fragments.ProfileFragment;
import com.example.learn2drive.Fragments.StudentHomeFragment;
import com.example.learn2drive.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StudentMainActivity extends AppCompatActivity implements
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
        studentBottomNav.setSelectedItemId(R.id.menu_student_home);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.menu_student_home)
        {
            selectedFragment = new StudentHomeFragment();
        }
        else if (itemId == R.id.menu_student_profile)
        {
            selectedFragment = ProfileFragment.newInstance(true);
        }
        else if (itemId == R.id.menu_student_payment)
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
}