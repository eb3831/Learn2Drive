package com.example.learn2drive.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.learn2drive.Helpers.Utilities;
import com.example.learn2drive.R;

public class SignUpActivity extends AppCompatActivity
{
    Intent gi;
    EditText etSignUpEmail, etSignUpPassword, etSignUpUsername;
    RadioGroup rgRole;
    String signUpEmail, signUpPassword, signUpUsername;
    boolean SignUpRole;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initViews();
    }

    public void initViews()
    {
        etSignUpEmail = findViewById(R.id.etLoginEmail);
        etSignUpPassword = findViewById(R.id.etLoginPassword);
        etSignUpUsername = findViewById(R.id.etUsername);
        rgRole = findViewById(R.id.rgRole);
    }

    public void moveToSignUp2(View view)
    {
        signUpEmail = etSignUpEmail.getText().toString();
        signUpPassword = etSignUpPassword.getText().toString();
        signUpUsername = etSignUpUsername.getText().toString();

        if (!Utilities.isValidEmail(signUpEmail))
        {
            etSignUpEmail.setError("invalid email address");
        }

        else if (!Utilities.isValidPassword(signUpPassword))
        {
            etSignUpPassword.setError("password must be at least 6 characters");
        }

        else if (signUpUsername.isEmpty()) //TO DO!!!
        {
            etSignUpUsername.setError("please write your username");
        }

        else if (rgRole.getCheckedRadioButtonId() == -1)
        {
            Toast.makeText(this, "please choose your role", Toast.LENGTH_SHORT).show();
        }

        else
        {
            SignUpRole = true;
            if (rgRole.getCheckedRadioButtonId() == R.id.rbStudent)
            {
                SignUpRole = false;
            }

            gi = new Intent(this, SignUpActivity2.class);
            gi.putExtra("email", signUpEmail);
            gi.putExtra("password", signUpPassword);
            gi.putExtra("username", signUpUsername);
            gi.putExtra("role", SignUpRole);
            startActivity(gi);
        }
    }

    public void moveToLogin(View view)
    {
        gi = new Intent(this, LoginActivity.class);
        startActivity(gi);
    }
}