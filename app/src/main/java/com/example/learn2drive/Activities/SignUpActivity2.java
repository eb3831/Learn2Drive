package com.example.learn2drive.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.learn2drive.Helpers.GeminiCallBack;
import com.example.learn2drive.R;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.gms.tasks.Continuation;

public class SignUpActivity2 extends AppCompatActivity implements GeminiCallBack
{

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2000;

    Bitmap capturedBitmap;
    Intent gi;
    String signUpEmail, signUpPassword, signUpUsername;
    boolean SignUpRole;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up2);

        initViews();
    }

    private void openCamera()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null)
        {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }

        else
        {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkPermissionAndOpenCamera(View view)
    {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        else
        {
            openCamera();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getExtras() != null)
        {

            capturedBitmap = (Bitmap) data.getExtras().get("data");

            if (capturedBitmap == null)
            {
                Toast.makeText(this, "No image captured", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION)
        {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                openCamera();
            }

            else
            {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void initViews()
    {
        gi = getIntent();
        signUpEmail = gi.getStringExtra("email");
        signUpPassword = gi.getStringExtra("password");
        signUpUsername = gi.getStringExtra("username");
        SignUpRole = gi.getBooleanExtra("role", true);
    }


    public void backToSignUp1(View view)
    {
        gi = new Intent(this, SignUpActivity.class);
        startActivity(gi);
    }

    public void submit(View view)
    {

    }

    @Override
    public void onSuccess(String result) {

    }

    @Override
    public void onFailure(Throwable error) {

    }
}