package com.example.learn2drive.Activities;

import static com.example.learn2drive.Helpers.FBRef.refAuth;
import static com.example.learn2drive.Helpers.Utilities.isValidEmail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

        if (!isValidEmail(email))
        {
            etLoginEmail.setError("invalid email address");
            return;
        }

        if (password.isEmpty())
        {
            etLoginPassword.setError("please write your password");
            return;
        }

        loginUser(email, password);
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

                    // gi = new Intent(this, ); //to home screen
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

