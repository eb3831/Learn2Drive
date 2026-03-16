package com.example.learn2drive.Activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.learn2drive.Fragments.ManageStudentsFragment;
import com.example.learn2drive.Fragments.ProfileFragment;
import com.example.learn2drive.Fragments.TeacherHomeFragment;
import com.example.learn2drive.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TeacherMainActivity extends MasterActivity implements
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

        // Set default fragment
        teacherBottomNav.setSelectedItemId(R.id.menu_teacher_home);
    }

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

    public void replaceFragment(Fragment fragment, boolean addToBackStack, String tag)
    {
        super.replaceFragment(fragment, addToBackStack, tag, R.id.teacherFragmentContainer);

    }
}