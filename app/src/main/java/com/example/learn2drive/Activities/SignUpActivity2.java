package com.example.learn2drive.Activities;

import static com.example.learn2drive.Helpers.FBRef.refAuth;
import static com.example.learn2drive.Helpers.FBRef.refClasses;
import static com.example.learn2drive.Helpers.FBRef.refStudents;
import static com.example.learn2drive.Helpers.FBRef.refTeachers;
import static com.example.learn2drive.Helpers.FBRef.refTeachersRequests;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.Student;
import com.example.learn2drive.Objects.Teacher;
import com.example.learn2drive.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SignUpActivity2 extends AppCompatActivity
{
    private final Context context = this;

    // רכיבי התצוגה
    private EditText etSearchTeacher;
    private AppCompatButton btnTypeStudent, btnTypeTeacher;
    private LinearLayout layoutTeacherSelection;
    private ListView lvTeachers;
    private CheckBox cbRememberMe;

    // משתנים לניהול נתונים
    private ArrayList<String> allTeachersNames;
    private ArrayList<String> displayedTeachersNames;
    private ArrayList<Teacher> allTeachersObjects, displayedTeachersObjects;

    private ArrayAdapter<String> adapter;

    private boolean isStudent = true;
    private Teacher selectedTeacher = null;

    Intent gi, si;
    String email, password, birthDate, id, username, phone;

    SharedPreferences sp;

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
        username = gi.getStringExtra("username");
        phone = gi.getStringExtra("phone");

        etSearchTeacher = findViewById(R.id.etSearchTeacher);
        layoutTeacherSelection = findViewById(R.id.layoutTeacherSelection);
        lvTeachers = findViewById(R.id.lvTeachers);

        btnTypeStudent = findViewById(R.id.btnTypeStudent);
        btnTypeTeacher = findViewById(R.id.btnTypeTeacher);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        sp = getSharedPreferences("login_prefs", MODE_PRIVATE);
    }

    private void setupTeacherData() 
    {
        allTeachersNames = new ArrayList<>();
        displayedTeachersNames = new ArrayList<>();
        allTeachersObjects = new ArrayList<>();
        displayedTeachersObjects = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayedTeachersNames);
        lvTeachers.setAdapter(adapter);

        ProgressDialog pd = ProgressDialog.show(this, "טוען נתונים", "מחפש מורים במערכת...", true);

        refTeachers.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                allTeachersNames.clear();
                for (DataSnapshot data : snapshot.getChildren())
                {
                    Teacher teacher = data.getValue(Teacher.class);

                    if (teacher != null)
                    {
                        allTeachersObjects.add(teacher);
                        allTeachersNames.add(teacher.getFullName() + "\n" + teacher.getIdNumber());
                    }
                }

                displayedTeachersObjects.clear();
                displayedTeachersNames.clear();

                displayedTeachersObjects.addAll(allTeachersObjects);
                displayedTeachersNames.addAll(allTeachersNames);
                adapter.notifyDataSetChanged();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pd.dismiss();
                Toast.makeText(context, "שגיאה בטעינת המורים", Toast.LENGTH_SHORT).show();
            }
        });

        lvTeachers.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedTeacher = displayedTeachersObjects.get(position);
                etSearchTeacher.setText(selectedTeacher.getFullName());
                etSearchTeacher.setSelection(etSearchTeacher.getText().length());

                Toast.makeText(SignUpActivity2.this, "בחרת ב: " + selectedTeacher.getFullName(), Toast.LENGTH_SHORT).show();
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
        displayedTeachersNames.clear();

        if (query.isEmpty())
        {
            displayedTeachersNames.addAll(allTeachersNames);
        }

        else
        {
            query = query.toLowerCase();

            for (String teacher : allTeachersNames)
            {
                if (teacher.toLowerCase().contains(query))
                {
                    displayedTeachersNames.add(teacher);
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
        if (isStudent && selectedTeacher == null)
        {
            Toast.makeText(this, "Please select a teacher", Toast.LENGTH_SHORT).show();
            return;
        }

        //
        ProgressDialog pd = ProgressDialog.show(this, "Sign Up", "Loading...", true);

        refAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity2.this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        pd.dismiss();
                        if (task.isSuccessful())
                        {
                            saveRememberMe();
                            FBRef.saveCurrentUser(refAuth.getCurrentUser());
                            saveUserToFB(isStudent);
                            Toast.makeText(context, "User created successfully", Toast.LENGTH_LONG).show();

                            si = new Intent(context, WaitingActivity.class);
                            si.putExtra("isStudent", isStudent);
                            startActivity(si);
                            finish();
                        }
                        else
                        {
                            handleAuthException(task.getException());
                        }
                    }
                });
    }

    private void saveRememberMe()
    {
        sp.edit().putBoolean("is_remembered", cbRememberMe.isChecked()).apply();
    }

    private void saveUserToFB(boolean isStudent)
    {
        if (isStudent)
        {
            Student student = new Student(FBRef.uid, id, username,
                    birthDate, phone, true, false,
                    0, selectedTeacher.getUid());
            refStudents.child(FBRef.uid).setValue(student);

            refClasses.child(selectedTeacher.getUid()).child("Pending Students").child(FBRef.uid).setValue(true);
        }

        else
        {
            Teacher teacher = new Teacher(FBRef.uid, id, username,
                    birthDate, phone, true, false, 60, 200);
            refTeachers.child(FBRef.uid).setValue(teacher);

            refTeachersRequests.child(FBRef.uid).setValue(true);
        }
    }

    private void handleAuthException(Exception exp)
    {
        if (exp instanceof FirebaseAuthUserCollisionException)
        {
            Toast.makeText(this, "User with this mail already exists", Toast.LENGTH_LONG).show();
        }

        else if (exp instanceof FirebaseNetworkException)
        {
            Toast.makeText(this, "Network error. Please check your connection", Toast.LENGTH_LONG).show();
        }

        else
        {
            Toast.makeText(this, "An error occurred, please try again later", Toast.LENGTH_LONG).show();
        }
    }

}