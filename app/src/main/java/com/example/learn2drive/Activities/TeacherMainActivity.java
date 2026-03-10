package com.example.learn2drive.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.learn2drive.Fragments.TeacherHomeFragment;
import com.example.learn2drive.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TeacherMainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener
{
    private BottomNavigationView teacherBottomNav;

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
            // selectedFragment = new HomeFragment();
        }
        else if (itemId == R.id.menu_teacher_students)
        {
            // selectedFragment = new HomeFragment();
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
}