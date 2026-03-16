package com.example.learn2drive.Activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.learn2drive.R;

public class MasterActivity extends AppCompatActivity
{
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);

        initViews();
    }

    private void initViews()
    {
        fragmentManager = getSupportFragmentManager();
    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack, String tag, int containerId)
    {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.teacherFragmentContainer, fragment, tag);

        if (addToBackStack)
        {
            transaction.addToBackStack(tag);
        }

        transaction.commit();
        invalidateOptionsMenu();
    }
}