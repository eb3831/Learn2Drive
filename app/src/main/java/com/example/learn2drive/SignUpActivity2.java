package com.example.learn2drive;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignUpActivity2 extends AppCompatActivity
{
    Intent gi;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up2);
    }

    public void backToSignUp1(View view)
    {
        gi = new Intent(this, SignUpActivity.class);
        startActivity(gi);
    }

    public void submit(View view) {}
}