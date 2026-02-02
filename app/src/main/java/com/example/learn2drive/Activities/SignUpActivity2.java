package com.example.learn2drive.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
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

    // רכיבי התצוגה
    private EditText etUsername, etSearchTeacher;
    private AppCompatButton btnTypeStudent, btnTypeTeacher;
    private LinearLayout layoutTeacherSelection;
    private ListView lvTeachers;
    private AppCompatButton btnSubmit;

    // משתנים לניהול נתונים
    private ArrayList<String> allTeachers;
    private ArrayList<String> displayedTeachers;

    private ArrayAdapter<String> adapter;

    private boolean isStudent = true;
    private String selectedTeacher = "";

    Intent gi;
    String email, password, birthDate, id;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up2);

        initViews();
        setupTeacherData();
        setupCustomSearch();
        updateUserTypeUI();
    }

    private void initViews()
    {
        gi = getIntent();
        email = gi.getStringExtra("email");
        password = gi.getStringExtra("password");
        birthDate = gi.getStringExtra("birthDate");
        id = gi.getStringExtra("id");

        etUsername = findViewById(R.id.etUsername);
        etSearchTeacher = findViewById(R.id.etSearchTeacher);
        layoutTeacherSelection = findViewById(R.id.layoutTeacherSelection);
        lvTeachers = findViewById(R.id.lvTeachers);

        btnTypeStudent = findViewById(R.id.btnTypeStudent);
        btnTypeTeacher = findViewById(R.id.btnTypeTeacher);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    /**
     *
     */
    private void setupTeacherData()
    {
        allTeachers = new ArrayList<>();
        allTeachers.add("Yanir Aton");
        allTeachers.add("Ori Roitzaid");
        allTeachers.add("Eliya Bitton");
        allTeachers.add("Albert Levi");
        allTeachers.add("Moshe Levi");
        allTeachers.add("Itai Hadar");

        displayedTeachers = new ArrayList<>(allTeachers);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayedTeachers);
        lvTeachers.setAdapter(adapter);

        lvTeachers.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                selectedTeacher = displayedTeachers.get(position);

                etSearchTeacher.setText(selectedTeacher);

                etSearchTeacher.setSelection(etSearchTeacher.getText().length());

                Toast.makeText(SignUpActivity2.this, "בחרת ב: " + selectedTeacher, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     *
     */
    private void setupCustomSearch()
    {
        etSearchTeacher.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                filterTeachers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterTeachers(String query)
    {
        displayedTeachers.clear();

        if (query.isEmpty())
        {
            displayedTeachers.addAll(allTeachers);
        }

        else
        {
            query = query.toLowerCase();

            for (String teacher : allTeachers)
            {
                if (teacher.toLowerCase().contains(query))
                {
                    displayedTeachers.add(teacher);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    // --- לוגיקה ועיצוב (Student/Teacher) ---

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

    private void updateUserTypeUI()
    {
        if (btnTypeStudent == null || btnTypeTeacher == null) return;

        if (isStudent)
        {
            // עיצוב למצב תלמיד
            btnTypeStudent.setBackgroundResource(R.drawable.bg_button_black);
            btnTypeStudent.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnTypeTeacher.setBackgroundResource(R.drawable.bg_input_default);
            btnTypeTeacher.setTextColor(ContextCompat.getColor(this, R.color.gray_700));

            layoutTeacherSelection.setVisibility(View.VISIBLE);
        }

        else
        {
            // עיצוב למצב מורה
            btnTypeTeacher.setBackgroundResource(R.drawable.bg_button_black);
            btnTypeTeacher.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnTypeStudent.setBackgroundResource(R.drawable.bg_input_default);
            btnTypeStudent.setTextColor(ContextCompat.getColor(this, R.color.gray_700));

            layoutTeacherSelection.setVisibility(View.GONE);
        }
    }

    // --- כפתורים כלליים ---

    public void onBackClicked(View v)
    {
        finish();
    }

    public void onSubmitClicked(View v)
    {
        String username = etUsername.getText().toString().trim();

        if (username.isEmpty())
        {
            etUsername.setError("Username is required");
            return;
        }

        if (isStudent && selectedTeacher.isEmpty())
        {
            Toast.makeText(this, "Please select a teacher", Toast.LENGTH_SHORT).show();
        }

        String type = isStudent ? "Student" : "Teacher";
        String msg = "Signed up: " + username + " (" + type + ")";
        if (isStudent)
            msg += " with teacher: " + etSearchTeacher.getText().toString();

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        // Intent intent = new Intent(this, NextActivity.class);
        // startActivity(intent);
    }
}