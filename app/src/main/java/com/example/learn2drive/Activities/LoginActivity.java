package com.example.learn2drive.Activities;

import static com.example.learn2drive.Helpers.FBRef.refAuth;
import static com.example.learn2drive.Helpers.Utilities.isValidEmail;

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

import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Helpers.Utilities;
import com.example.learn2drive.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

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

        loadSavedCredentials();
    }

    /**
     * Checks if user is remembered and already signed in, redirects to MainActivity if so.
     */
    @Override
    protected void onStart()
    {
        super.onStart();
        boolean userConnected = sp.getBoolean("is_remembered", false);
        if (refAuth.getCurrentUser() != null && userConnected)
        {
            FBRef.saveCurrentUser(refAuth.getCurrentUser());
            gi = new Intent(this, MainActivity.class);
            startActivity(gi);
            finish();
        }
    }

    public void initViews()
    {
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);

        containerEmail = findViewById(R.id.containerEmail);
        containerPassword = findViewById(R.id.containerPassword);

        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);

        cbRememberMe = findViewById(R.id.cbRememberMe);
        sp = getSharedPreferences("login_prefs", MODE_PRIVATE);
        cbRememberMe = findViewById(R.id.cbRememberMe);
    }

    private void loadSavedCredentials()
    {
        boolean isRemembered = sp.getBoolean("is_remembered", false);
        if (isRemembered) {
            String savedEmail = sp.getString("email", "");
            etLoginEmail.setText(savedEmail);
            cbRememberMe.setChecked(true);
        }
    }

    public void moveToSignUp(View view)
    {
        gi = new Intent(this, SignUpActivity.class);
        startActivity(gi);
    }

    public void login(View view)
    {
        String email = etLoginEmail.getText().toString();
        String password = etLoginPassword.getText().toString();

        resetErrors();

        if (!Utilities.isValidEmail(email))
        {
            containerEmail.setBackgroundResource(R.drawable.bg_input_error);
            tvEmailError.setText("please enter vaild email");
            tvEmailError.setVisibility(View.VISIBLE);
        }

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

    private void resetErrors()
    {
        containerEmail.setBackgroundResource(R.drawable.bg_input_default);
        tvEmailError.setVisibility(View.GONE);

        containerPassword.setBackgroundResource(R.drawable.bg_input_default);
        tvPasswordError.setVisibility(View.GONE);
    }

    private void loginUser(String email, String password)
    {
        refAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task ->
        {
            if (task.isSuccessful())
            {
                // SAVE TO SHARED PREFERENCES
                SharedPreferences.Editor editor = sp.edit();
                if (cbRememberMe.isChecked())
                {
                    editor.putString("email", email);
                    editor.putBoolean("is_remembered", true);
                }

                else
                {
                    editor.clear(); // Clear data if user unchecked the box
                }
                editor.apply();

                Toast.makeText(LoginActivity.this, "LOGIN SUCCESSFULLY!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }

            else
            {
                Toast.makeText(LoginActivity.this, "LOGIN FAILED: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}