package com.example.learn2drive.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.learn2drive.R;

import java.util.ArrayList;

public class SignUpActivity2 extends AppCompatActivity
{
    private EditText etUsername;
    private AppCompatButton btnTypeStudent, btnTypeTeacher;
    private LinearLayout layoutTeacherSelection;
    private ListView lvTeachers;

    private boolean isStudent = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up2);
        initializeViews();
        updateUserTypeUI();
        setupEmptyList();
    }

    private void initializeViews()
    {
        etUsername = findViewById(R.id.etUsername);
        layoutTeacherSelection = findViewById(R.id.layoutTeacherSelection);
        lvTeachers = findViewById(R.id.lvTeachers);
    }

    // --- פונקציות ה-OnClick ---

    public void onBackClicked(View v)
    {
        finish();
    }

    public void onStudentClicked(View v)
    {
        if (!isStudent)
        {
            isStudent = true;
            updateUserTypeUI();
        }
    }

    public void onTeacherClicked(View v)
    {
        if (isStudent)
        {
            isStudent = false;
            updateUserTypeUI();
        }
    }

    public void onSubmitClicked(View v)
    {
        String username = etUsername.getText().toString().trim();

        if (username.isEmpty())
        {
            etUsername.setError("Username is required");
            return;
        }

        String type = isStudent ? "Student" : "Teacher";
        Toast.makeText(this, "Signing up as " + type + ": " + username, Toast.LENGTH_SHORT).show();

        // כאן תוסיפי מעבר מסך (Intent)
    }

    // --- פונקציות עזר (לוגיקה ועיצוב) ---

    private void updateUserTypeUI()
    {
        if (isStudent)
        {
            // מצב תלמיד
            btnTypeStudent.setBackgroundResource(R.drawable.bg_button_black);
            btnTypeStudent.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnTypeTeacher.setBackgroundResource(R.drawable.bg_input_default);
            btnTypeTeacher.setTextColor(ContextCompat.getColor(this, R.color.gray_700));

            layoutTeacherSelection.setVisibility(View.VISIBLE);
        }

        else
        {
            // מצב מורה
            btnTypeTeacher.setBackgroundResource(R.drawable.bg_button_black);
            btnTypeTeacher.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnTypeStudent.setBackgroundResource(R.drawable.bg_input_default);
            btnTypeStudent.setTextColor(ContextCompat.getColor(this, R.color.gray_700));

            layoutTeacherSelection.setVisibility(View.GONE);
        }
    }

    private void setupEmptyList()
    {
        ArrayList<String> emptyList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, emptyList);
        lvTeachers.setAdapter(adapter);
    }
}