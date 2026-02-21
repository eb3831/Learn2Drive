package com.example.learn2drive.Activities;

import static com.example.learn2drive.Helpers.FBRef.refAuth;
import static com.example.learn2drive.Helpers.FBRef.refClasses;
import static com.example.learn2drive.Helpers.FBRef.refStudents;
import static com.example.learn2drive.Helpers.FBRef.refTeachers;

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
import com.example.learn2drive.Objects.User;
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

/**
 * SignUpActivity2 handles the second phase of user registration.
 * It allows the user to choose a role (Student/Teacher), search for a teacher
 * if they are a student, and finalize the account creation process.
 */
public class SignUpActivity2 extends AppCompatActivity
{
    private final Context context = this;

    private EditText etSearchTeacher;
    private AppCompatButton btnTypeStudent, btnTypeTeacher;
    private LinearLayout layoutTeacherSelection;
    private ListView lvTeachers;
    private CheckBox cbRememberMe;

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

    /**
     * Initializes UI components, retrieves data from the previous Intent,
     * and sets up SharedPreferences.
     */
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

    /**
     * Fetches the list of all teachers from the database to allow student selection.
     */
    private void setupTeacherData() 
    {
        allTeachersNames = new ArrayList<>();
        displayedTeachersNames = new ArrayList<>();
        allTeachersObjects = new ArrayList<>();
        displayedTeachersObjects = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayedTeachersNames);
        lvTeachers.setAdapter(adapter);

        ProgressDialog pd = ProgressDialog.show(this, "Loading Data",
                "Looking for teachers...", true);

        refTeachers.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                allTeachersNames.clear();
                for (DataSnapshot data : snapshot.getChildren())
                {
                    Teacher teacher = data.getValue(Teacher.class);

                    if (teacher != null && teacher.getStatus().equals(Teacher.ACTIVE))
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
                Toast.makeText(context, "Error while loading teachers data", Toast.LENGTH_SHORT).show();
            }
        });

        lvTeachers.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedTeacher = displayedTeachersObjects.get(position);
                etSearchTeacher.setText(selectedTeacher.getFullName());
                etSearchTeacher.setSelection(etSearchTeacher.getText().length());

                Toast.makeText(SignUpActivity2.this, "You chose: " +
                        selectedTeacher.getFullName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets up a TextWatcher on the search field to filter the teacher list in real-time.
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

    /**
     * Filters the teachers list based on the user's search query.
     * @param query The search string entered by the user.
     */
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

    /**
     * Handles the Student role selection button click.
     * @param v The view triggered the click.
     */
    public void onStudentClicked(View v)
    {
        if (!isStudent)
        {
            isStudent = true;
            updateUserTypeUI();
        }
    }

    /**
     * Handles the Teacher role selection button click.
     * @param v The view triggered the click.
     */
    public void onTeacherClicked(View v)
    {
        if (isStudent)
        {
            isStudent = false;
            updateUserTypeUI();
        }
    }

    /**
     * Updates the UI styling and visibility based on the selected user role.
     */
    private void updateUserTypeUI()
    {
        if (btnTypeStudent == null || btnTypeTeacher == null) return;

        if (isStudent)
        {
            btnTypeStudent.setBackgroundResource(R.drawable.bg_button_black);
            btnTypeStudent.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnTypeTeacher.setBackgroundResource(R.drawable.bg_input_default);
            btnTypeTeacher.setTextColor(ContextCompat.getColor(this, R.color.gray_700));

            layoutTeacherSelection.setVisibility(View.VISIBLE);
        }

        else
        {
            btnTypeTeacher.setBackgroundResource(R.drawable.bg_button_black);
            btnTypeTeacher.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnTypeStudent.setBackgroundResource(R.drawable.bg_input_default);
            btnTypeStudent.setTextColor(ContextCompat.getColor(this, R.color.gray_700));

            layoutTeacherSelection.setVisibility(View.GONE);
        }
    }

    /**
     * Closes the current activity and returns to the previous one.
     * @param v The view triggered the click.
     */
    public void onBackClicked(View v)
    {
        finish();
    }

    /**
     * Finalizes the registration by creating a user in Firebase Auth and saving details to Database.
     * @param v The view triggered the click.
     */
    public void onSubmitClicked(View v)
    {
        if (isStudent && selectedTeacher == null)
        {
            Toast.makeText(this, "Please select a teacher", Toast.LENGTH_SHORT).show();
            return;
        }

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
                            int role = isStudent ? 0 : 1;

                            saveRememberMe(role, isStudent ? selectedTeacher.getUid(): "");
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

    /**
     * Saves session preferences including role and approval status.
     * @param role The user role (0 for Student, 1 for Teacher).
     */
    private void saveRememberMe(int role, String teacherUid)
    {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("is_remembered", cbRememberMe.isChecked());
        editor.putInt("user_role", role);
        editor.putBoolean("is_approved", false);

        if(role == 0)
        {
            editor.putString("teacher_uid", teacherUid);
        }

        editor.apply();
    }

    /**
     * Maps the user data to the relevant object and uploads it to Firebase.
     * @param isStudent Boolean indicating the user's selected role.
     */
    private void saveUserToFB(boolean isStudent)
    {
        if (isStudent)
        {
            Student student = new Student(FBRef.uid, id, username,
                    birthDate, phone, User.PENDING, 0,
                    0, selectedTeacher.getUid());
            refStudents.child(FBRef.uid).setValue(student);

            refClasses.child(selectedTeacher.getUid()).child("students").
                    child(FBRef.uid).setValue(User.PENDING);        }

        else
        {
            Teacher teacher = new Teacher(FBRef.uid, id, username,
                    birthDate, phone, User.PENDING, 1, 60, 200);
            refTeachers.child(FBRef.uid).setValue(teacher);
        }
    }

    /**
     * Handles Firebase Authentication exceptions and provides user feedback.
     * @param exp The exception to handle.
     */
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