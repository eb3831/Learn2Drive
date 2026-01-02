package com.example.learn2drive.Activities;

import static com.example.learn2drive.Helpers.FBRef.refAuth;
import static com.example.learn2drive.Helpers.Utilities.isValidEmail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.learn2drive.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class LoginActivity extends AppCompatActivity
{
    EditText etLoginEmail, etLoginPassword;
    LinearLayout containerEmail, containerPassword;
    TextView tvEmailError, tvPasswordError;

    Intent gi;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
    }

    public void initViews()
    {
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);

        containerEmail = findViewById(R.id.containerEmail);
        containerPassword = findViewById(R.id.containerPassword);

        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
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

        boolean hasError = false;

        if (email.isEmpty())
        {
            showEmailError("Email is required");
            hasError = true;
        }

        else if (!isValidEmail(email))
        {
            showEmailError("Invalid email address");
            hasError = true;
        }

        if (password.isEmpty())
        {
            showPasswordError("Password is required");
            hasError = true;
        }

        if (hasError)
        {
            return;
        }

        loginUser(email, password);
    }

    private void showEmailError(String message)
    {
        containerEmail.setBackgroundResource(R.drawable.bg_input_error);
        tvEmailError.setText(message);
        tvEmailError.setVisibility(View.VISIBLE);
    }

    private void showPasswordError(String message)
    {
        containerPassword.setBackgroundResource(R.drawable.bg_input_error);
        tvPasswordError.setText(message);
        tvPasswordError.setVisibility(View.VISIBLE);
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
        refAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(LoginActivity.this, "LOGIN SUCCESSFULLY!", Toast.LENGTH_SHORT).show();

                    // gi = new Intent(this, HomeScreenActivity.class); // שנה לשם המסך הבא שלך
                    // startActivity(gi);
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "LOGIN FAILED: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}