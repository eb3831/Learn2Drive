package com.example.learn2drive.Activities;

import static com.example.learn2drive.Helpers.Prompts.ID_CARD_SCHEMA;
import static com.example.learn2drive.Helpers.Prompts.PHOTO_PROMPT;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.learn2drive.Helpers.GeminiCallBack;
import com.example.learn2drive.Helpers.GeminiManager;
import com.example.learn2drive.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class SignUpActivity2 extends AppCompatActivity implements GeminiCallBack
{
    private static final String TAG = "SignUpActivity2";
    private static final int REQUEST_FULL_IMAGE_CAPTURE = 202;
    private static final int REQUEST_CAMERA_PERMISSION = 101;

    private String currentPath;
    private Bitmap imageBitmap;
    private GeminiManager geminiManager;
    private ProgressDialog pD;

    String signUpEmail, signUpPassword, signUpUsername;
    boolean signUpRole;

    EditText etIDNumber, etBirthDate;
    CheckBox cbRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up2);

        initViews();
    }

    /**
     * Initializes the views and sets up the GeminiManager.
     */
    public void initViews()
    {
        Intent gi = getIntent();
        signUpEmail = gi.getStringExtra("email");
        signUpPassword = gi.getStringExtra("password");
        signUpUsername = gi.getStringExtra("username");
        signUpRole = gi.getBooleanExtra("role", true);

        etBirthDate = findViewById(R.id.etBirthDate);
        etIDNumber = findViewById(R.id.etUserID);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        geminiManager = GeminiManager.getInstance();
    }

    /**
     * Launches the camera app to capture an image.
     * @param view The view that triggered this method.
     */
    public void scanID(View view)
    {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
        else
        {
            // Permission already granted
            launchCamera();
        }
    }

    /**
     * Helper method to actually create the file and start the camera intent.
     * Only called after permission is confirmed.
     */
    private void launchCamera()
    {
        String filename = "tempfile_id_card";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try
        {
            File imgFile = File.createTempFile(filename, ".jpg", storageDir);
            currentPath = imgFile.getAbsolutePath();

            // --- תיקון קריטי: שימוש בשם החבילה הדינמי ---
            String authority = getPackageName() + ".fileprovider";
            Uri imageUri = FileProvider.getUriForFile(SignUpActivity2.this, authority, imgFile);

            Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            if (takePicIntent.resolveActivity(getPackageManager()) != null)
            {
                startActivityForResult(takePicIntent, REQUEST_FULL_IMAGE_CAPTURE);
            }
        }
        catch (IOException e)
        {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "launchCamera error: " + e.getMessage());
        }
    }

    /**
     * Called when the user responds to the permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                launchCamera();
            }
            else
            {
                Toast.makeText(this, "Camera permission is required to scan ID", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data_back)
    {
        super.onActivityResult(requestCode, resultCode, data_back);

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_FULL_IMAGE_CAPTURE)
        {
            // Decode the image
            imageBitmap = BitmapFactory.decodeFile(currentPath);

            // Show Progress Dialog
            pD = new ProgressDialog(this);
            pD.setTitle("Processing ID");
            pD.setMessage("Analyzing with AI...");
            pD.setCancelable(false);
            pD.show();

            // Prepare Prompt with Schema
            String finalPrompt = PHOTO_PROMPT + "\n\nReturn the data strictly according to this JSON schema:\n" + ID_CARD_SCHEMA;

            // Send to Gemini
            geminiManager.sendTextWithPhotoPrompt(finalPrompt, imageBitmap, this);
        }
    }

    // --- Gemini CallBack Methods ---

    @Override
    public void onSuccess(String result)
    {
        if (pD != null && pD.isShowing())
        {
            pD.dismiss();
        }

        try
        {
            String cleanResult = result.replace("```json", "").replace("```", "").trim();
            JSONObject jsonResponse = new JSONObject(cleanResult);

            String idNumber = jsonResponse.optString("id_number", "");
            String birthDate = jsonResponse.optString("date_of_birth", "");

            runOnUiThread(() ->
            {
                if (!idNumber.isEmpty())
                {
                    etIDNumber.setText(idNumber);
                }

                if (!birthDate.isEmpty())
                {
                    etBirthDate.setText(birthDate);
                }
            });
        }
        catch (JSONException e)
        {
            Log.e(TAG, "JSON Parsing error: " + e.getMessage());

            runOnUiThread(() ->
                    Toast.makeText(SignUpActivity2.this, "Failed to parse data. Try again.", Toast.LENGTH_LONG).show()
            );
        }
    }

    @Override
    public void onFailure(Throwable error)
    {
        if (pD != null && pD.isShowing())
        {
            pD.dismiss();
        }

        Log.e(TAG, "Gemini Error: " + error.getMessage());

        runOnUiThread(() ->
                Toast.makeText(SignUpActivity2.this, "Scan failed: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    // --- Navigation & Submission ---

    public void backToSignUp1(View view)
    {
        finish();
    }

    public void submit(View view)
    {
        String id = etIDNumber.getText().toString().trim();
        String dob = etBirthDate.getText().toString().trim();

        if (id.isEmpty() || dob.isEmpty())
        {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (id.length() != 9)
        {
            etIDNumber.setError("ID must be 9 digits");
            return;
        }

        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Opens a calendar dialog for selecting the birth date.
     * PUBLIC method linked via android:onClick in XML.
     * @param view The view that triggered this method.
     */
    public void openDatePicker(View view)
    {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (v, selectedYear, selectedMonth, selectedDay) ->
                {
                    // Format the date as DD/MM/YYYY
                    String date = String.format("%02d.%02d.%d", selectedDay, selectedMonth + 1, selectedYear);
                    etBirthDate.setText(date);
                },
                year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
}
