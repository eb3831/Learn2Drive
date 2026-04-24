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

/**
 * Base activity class that provides utility methods for fragment management
 * and controlling the visibility of the bottom navigation view.
 * Other activities in the application should extend this class to inherit these behaviors.
 */
public class MasterActivity extends AppCompatActivity
{
    private FragmentManager fragmentManager;
    protected BottomNavigationView bottomNavigationView;

    /**
     * Initializes the activity when it is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initViews();
    }

    /**
     * Initializes the necessary components for the activity, specifically
     * the FragmentManager used for handling fragment transactions.
     */
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

        transaction.replace(containerId, fragment, tag);

        if (addToBackStack)
        {
            transaction.addToBackStack(tag);
        }

        transaction.commit();
        invalidateOptionsMenu();
    }

    /**
     * Clears all fragments currently in the back stack.
     * This is useful for resetting the navigation state to the root fragment.
     */
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