package com.example.learn2drive.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.learn2drive.R;

public class AdminActivity extends AppCompatActivity
{
    private AppCompatButton btnNewRequests, btnActiveTeachers;
    private TextView tvListTitle;
    private ListView lvAdminTeachers;
    private boolean showingRequests = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        initViews();
        updateUI();
    }

    private void initViews()
    {
        btnNewRequests = findViewById(R.id.btnNewRequests);
        btnActiveTeachers = findViewById(R.id.btnActiveTeachers);
        tvListTitle = findViewById(R.id.tvListTitle);
        lvAdminTeachers = findViewById(R.id.lvAdminTeachers);
    }

    public void onNewRequestsClicked(View v)
    {
        if (!showingRequests) {
            showingRequests = true;
            updateUI();
            // כאן תטען את רשימת הבקשות החדשות
        }
    }

    public void onActiveTeachersClicked(View v)
    {
        if (showingRequests) {
            showingRequests = false;
            updateUI();
            // כאן תטען את רשימת המורים הקיימים
        }
    }

    private void updateUI()
    {
        if (showingRequests)
        {
            btnNewRequests.setBackgroundResource(R.drawable.bg_button_black);
            btnNewRequests.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnActiveTeachers.setBackgroundResource(R.drawable.bg_input_default);
            btnActiveTeachers.setTextColor(ContextCompat.getColor(this, R.color.gray_700));

            tvListTitle.setText("Pending Approvals");
        }
        else
        {
            btnActiveTeachers.setBackgroundResource(R.drawable.bg_button_black);
            btnActiveTeachers.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnNewRequests.setBackgroundResource(R.drawable.bg_input_default);
            btnNewRequests.setTextColor(ContextCompat.getColor(this, R.color.gray_700));

            tvListTitle.setText("Registered Teachers");
        }
    }
}