package com.example.learn2drive.Activities;

import static com.example.learn2drive.Helpers.FBRef.refAuth;
import static com.example.learn2drive.Helpers.FBRef.refTeachers;
import static com.example.learn2drive.Helpers.FBRef.refUsers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.learn2drive.BuildConfig;
import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Helpers.Utilities;
import com.example.learn2drive.Objects.User;
import com.example.learn2drive.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * LoginActivity handles the user authentication process.
 * It supports email/password login, "Remember Me" functionality using SharedPreferences,
 * and role-based navigation (Admin, Student, or Teacher).
 */
public class LoginActivity extends AppCompatActivity
{
    EditText etLoginEmail, etLoginPassword;
    LinearLayout containerEmail, containerPassword;
    TextView tvEmailError, tvPasswordError;
    CheckBox cbRememberMe;
    SharedPreferences sp;

    Intent gi;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
    }

    /**
     * Checks if a user is already authenticated and has chosen to be remembered.
     * If so, redirects them to the appropriate screen based on their saved approval status.
     */
    @Override
    protected void onStart()
    {
        super.onStart();
        boolean userConnected = sp.getBoolean("is_remembered", false);

        if (refAuth.getCurrentUser() != null && userConnected)
        {
            FBRef.saveCurrentUser(refAuth.getCurrentUser());
            int role = sp.getInt("user_role", -1);
            boolean approved = sp.getBoolean("is_approved", false);

            // Admin check
            if(role == 2)
            {
                startActivity(new Intent(this, AdminActivity.class));
            }
            else if(approved)
            {
                startActivity(new Intent(this, MainActivity.class));
            }
            else
            {
                gi = new Intent(this, WaitingActivity.class);
                gi.putExtra("isStudent", role == 0);
                startActivity(gi);
            }
            finish();
        }
    }

    /**
     * Initializes the UI components and SharedPreferences.
     */
    public void initViews()
    {
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);

        containerEmail = findViewById(R.id.containerEmail);
        containerPassword = findViewById(R.id.containerPassword);

        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);

        cbRememberMe = findViewById(R.id.cbRemember);
        sp = getSharedPreferences("login_prefs", MODE_PRIVATE);
    }

    /**
     * Navigates to the SignUpActivity.
     * @param view The view that triggered the click event.
     */
    public void moveToSignUp(View view)
    {
        gi = new Intent(this, SignUpActivity.class);
        startActivity(gi);
    }

    /**
     * Validates input fields and initiates the login process if valid.
     * @param view The view that triggered the click event.
     */
    public void login(View view)
    {
        String email = etLoginEmail.getText().toString();
        String password = etLoginPassword.getText().toString();

        resetErrors();

        // Validate email format
        if (!Utilities.isValidEmail(email))
        {
            containerEmail.setBackgroundResource(R.drawable.bg_input_error);
            tvEmailError.setText("please enter vaild email");
            tvEmailError.setVisibility(View.VISIBLE);
        }

        // Validate password format
        if (!Utilities.isValidPassword(password))
        {
            containerPassword.setBackgroundResource(R.drawable.bg_input_error);
            tvPasswordError.setText("please enter vaild password");
            tvPasswordError.setVisibility(View.VISIBLE);
        }

        else
        {
            loginUser(email, password);
        }
    }

    /**
     * Resets the visual error indicators on the input fields.
     */
    private void resetErrors()
    {
        containerEmail.setBackgroundResource(R.drawable.bg_input_default);
        tvEmailError.setVisibility(View.GONE);

        containerPassword.setBackgroundResource(R.drawable.bg_input_default);
        tvPasswordError.setVisibility(View.GONE);
    }

    /**
     * Attempts to sign in the user via Firebase Authentication.
     * @param email The user's email.
     * @param password The user's password.
     */
    private void loginUser(String email, String password)
    {
        ProgressDialog pd = ProgressDialog.show(this, "Log In", "Loading...", true);

        refAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task ->
        {
            pd.dismiss();

            if (task.isSuccessful())
            {
                FBRef.saveCurrentUser(refAuth.getCurrentUser());
                checkUserRoleAndNavigate(FBRef.uid, email, password);
            }

            else
            {
                Toast.makeText(LoginActivity.this, "LOGIN FAILED: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Saves user details and session state to SharedPreferences.
     * @param user The User object containing role and approval status.
     * @param email The user's email.
     */
    private void saveToSharedPreferences(User user, String email)
    {
        SharedPreferences.Editor editor = sp.edit();
        if (cbRememberMe.isChecked())
        {
            editor.putBoolean("is_remembered", true);
        }

        else
        {
            editor.clear(); // Clear data if user unchecked the box
        }

        editor.putInt("user_role", user.getRole());
        editor.putBoolean("is_approved", user.getStatus().equals(User.ACTIVE));
        editor.apply();
    }

    /**
     * Checks the user's role in the database and navigates to the appropriate activity.
     * Also handles Admin credentials check.
     * @param uid The unique ID of the authenticated user.
     * @param email The email used for login.
     * @param password The password used for login.
     */
    private void checkUserRoleAndNavigate(String uid, String email, String password)
    {
        // Admin check
        if (email.equals(BuildConfig.ADMIN_EMAIL) && password.equals(BuildConfig.ADMIN_PASSWORD))
        {
            // Using SharedPreferences to save admin credentials
            saveToSharedPreferences(new User("", "", "", "",
                    "", User.ACTIVE, 2), email);
            startActivity(new Intent(this, AdminActivity.class));
            finish();
            return;
        }

        // Fetch user data from Realtime Database to determine role and approval status
        refUsers.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                boolean found = false;

                for (DataSnapshot roleSnapshot : snapshot.getChildren())
                {
                    if (roleSnapshot.hasChild(uid))
                    {
                        found = true;
                        User user = roleSnapshot.child(uid).getValue(User.class);

                        saveToSharedPreferences(user, email);

                        if (user.getStatus().equals(User.ACTIVE))
                        {
                            gi = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(gi);
                        }

                        else if (user.getStatus().equals(User.REJECTED))
                        {
                            Toast.makeText(LoginActivity.this,
                                    "Your account has been rejected", Toast.LENGTH_SHORT).show();
                        }

                        else
                        {
                            gi = new Intent(LoginActivity.this, WaitingActivity.class);
                            gi.putExtra("isStudent", user.getRole() == 0);
                            startActivity(gi);
                        }
                        finish();
                    }
                }

                if (!found)
                {
                    Toast.makeText(LoginActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}