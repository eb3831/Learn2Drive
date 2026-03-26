package com.example.learn2drive.Activities;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.learn2drive.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MasterActivity extends AppCompatActivity
{
    private FragmentManager fragmentManager;
    protected BottomNavigationView bottomNavigationView;

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

    /**
     * Replaces the current fragment with a new one in the specified container.
     *
     * @param fragment       The new fragment to display.
     * @param addToBackStack Whether to add this transaction to the back stack.
     * @param tag            The tag for the fragment.
     * @param containerId    The ID of the layout container where the fragment should be placed.
     */
    public void replaceFragment(Fragment fragment, boolean addToBackStack, String tag, int containerId)
    {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Fix: Use the dynamic containerId instead of the hardcoded one
        transaction.replace(containerId, fragment, tag);

        if (addToBackStack)
        {
            transaction.addToBackStack(tag);
        }

        transaction.commit();
        invalidateOptionsMenu();
    }

    public void clearStack()
    {
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    /**
     * Hides the bottom navigation bar across any child activity.
     */
    public void hideBottomNav()
    {
        if (bottomNavigationView != null)
        {
            bottomNavigationView.setVisibility(View.GONE);
        }
    }

    /**
     * Shows the bottom navigation bar across any child activity.
     */
    public void showBottomNav()
    {
        if (bottomNavigationView != null)
        {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }
}