package com.example.learn2drive.Activities;

import static com.example.learn2drive.Helpers.FBRef.refUsers;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.learn2drive.Helpers.GeminiCallBack;
import com.example.learn2drive.Helpers.GeminiManager;
import com.example.learn2drive.Helpers.Utilities;
import com.example.learn2drive.Objects.User;
import com.example.learn2drive.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Activity class responsible for the first step of the user registration process.
 * It handles input validation, checks for existing users in Firebase, and utilizes
 * the Gemini API to scan and extract data from an ID card image.
 */
public class SignUpActivity extends AppCompatActivity implements GeminiCallBack
{
    Intent gi;

    EditText etSignUpEmail, etSignUpPassword, etBirthDate, etIDNumber, etFullName, etPhone;

    LinearLayout containerFullName, containerEmail, containerPhone,
            containerPassword, containerBirthDate, containerIdNumber;

    TextView tvFullNameError, tvEmailError, tvPhoneError,
            tvPasswordError, tvBirthDateError, tvIdError;

    String signUpEmail, signUpPassword, id, dob, fullName, phone;

    private static final String TAG = "SignUpActivity";
    private static final int REQUEST_FULL_IMAGE_CAPTURE = 202;
    private static final int REQUEST_CAMERA_PERMISSION = 101;

    private String currentPath;
    private Bitmap imageBitmap;
    private GeminiManager geminiManager;
    private ProgressDialog pD;

    private ArrayList<String> existingIds;

    /**
     * Called when the activity is starting.
     * Initializes the activity, sets the layout, and calls the method to initialize views.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     * then this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initViews();
    }

    /**
     * Initializes all UI components, variables, and managers used in the activity.
     * Also triggers the retrieval of existing users from the database.
     */
    public void initViews()
    {
        etSignUpEmail = findViewById(R.id.etSignUpEmail);
        etSignUpPassword = findViewById(R.id.etSignUpPassword);
        etBirthDate = findViewById(R.id.etBirthDate);
        etIDNumber = findViewById(R.id.etIDNumber);
        etFullName = findViewById(R.id.etSignUpFullName);
        etPhone = findViewById(R.id.etSignUpPhone);

        containerFullName = findViewById(R.id.containerFullName);
        containerEmail = findViewById(R.id.containerEmail);
        containerPhone = findViewById(R.id.containerPhone);
        containerPassword = findViewById(R.id.containerPassword);
        containerBirthDate = findViewById(R.id.containerBirthDate);
        containerIdNumber = findViewById(R.id.containerIdNumber);

        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        tvBirthDateError = findViewById(R.id.tvBirthDateError);
        tvIdError = findViewById(R.id.tvIdError);
        tvFullNameError = findViewById(R.id.tvFullNameError);
        tvPhoneError = findViewById(R.id.tvPhoneError);

        geminiManager = GeminiManager.getInstance();

        existingIds = new ArrayList<>();

        readExistingUsers();
    }

    /**
     * Reads all existing users from the Firebase Realtime Database and stores their ID numbers
     * in an ArrayList to prevent duplicate registrations.
     */
    private void readExistingUsers()
    {
        existingIds.clear();

        refUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot specificUserRef : snapshot.getChildren()) {
                    for (DataSnapshot user : specificUserRef.getChildren()) {
                        User userObj = user.getValue(User.class);
                        if (userObj != null) {
                            existingIds.add(userObj.getIdNumber());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * Validates all user inputs. If all fields are valid, it proceeds to the second step
     * of the sign-up process (SignUpActivity2) by passing the collected data via Intent.
     * Displays appropriate error messages if validation fails.
     *
     * @param view The view that was clicked to trigger this method.
     */
    public void moveToSignUp2(View view)
    {
        fullName = etFullName.getText().toString().trim();
        phone = etPhone.getText().toString().trim();
        signUpEmail = etSignUpEmail.getText().toString().trim();
        signUpPassword = etSignUpPassword.getText().toString().trim();
        id = etIDNumber.getText().toString().trim();
        dob = etBirthDate.getText().toString().trim();

        resetErrors();

        if(fullName.isEmpty())
        {
            containerFullName.setBackgroundResource(R.drawable.bg_input_error);
            tvFullNameError.setText("Please enter a valid name");
            tvFullNameError.setVisibility(View.VISIBLE);
        }

        // Email Validation
        else if (!Utilities.isValidEmail(signUpEmail))
        {
            containerEmail.setBackgroundResource(R.drawable.bg_input_error);
            tvEmailError.setText("Please enter a valid email");
            tvEmailError.setVisibility(View.VISIBLE);
        }

        else if(!(phone.length() == 10 && phone.startsWith("05")))
        {
            containerPhone.setBackgroundResource(R.drawable.bg_input_error);
            tvPhoneError.setText("Please enter a valid phone number");
            tvPhoneError.setVisibility(View.VISIBLE);
        }

        // Password Validation
        else if (!Utilities.isValidPassword(signUpPassword))
        {
            containerPassword.setBackgroundResource(R.drawable.bg_input_error);
            tvPasswordError.setText("Password must be at least 6 characters");
            tvPasswordError.setVisibility(View.VISIBLE);
        }

        // Birth Date Validation
        else if (dob.isEmpty())
        {
            containerBirthDate.setBackgroundResource(R.drawable.bg_input_error);
            tvBirthDateError.setText("Birth date is required");
            tvBirthDateError.setVisibility(View.VISIBLE);
        }

        else if (!Utilities.isUserOldEnough(dob))
        {
            containerBirthDate.setBackgroundResource(R.drawable.bg_input_error);
            tvBirthDateError.setText("You must be at least 16.5 years old");
            tvBirthDateError.setVisibility(View.VISIBLE);
        }

        // ID Validation
        else if (id.length() != 9)
        {
            containerIdNumber.setBackgroundResource(R.drawable.bg_input_error);
            tvIdError.setText("ID must be exactly 9 digits");
            tvIdError.setVisibility(View.VISIBLE);
        }

        else if (existingIds.contains(id))
        {
            containerIdNumber.setBackgroundResource(R.drawable.bg_input_error);
            tvIdError.setText("ID already exists");
            tvIdError.setVisibility(View.VISIBLE);
        }

        else
        {
            gi = new Intent(this, SignUpActivity2.class);
            gi.putExtra("email", signUpEmail);
            gi.putExtra("password", signUpPassword);
            gi.putExtra("birthDate", dob);
            gi.putExtra("id", id);
            gi.putExtra("username", fullName);
            gi.putExtra("phone", phone);

            startActivity(gi);
        }
    }

    // --- Error Handling Methods --- c

    /**
     * Resets all input container backgrounds to their default state and hides any
     * visible error text views.
     */
    private void resetErrors()
    {
        // Reset backgrounds
        containerFullName.setBackgroundResource(R.drawable.bg_input_default);
        containerPhone.setBackgroundResource(R.drawable.bg_input_default);
        containerEmail.setBackgroundResource(R.drawable.bg_input_default);
        containerPassword.setBackgroundResource(R.drawable.bg_input_default);
        containerBirthDate.setBackgroundResource(R.drawable.bg_input_default);
        containerIdNumber.setBackgroundResource(R.drawable.bg_input_default);

        // Hide error texts
        tvFullNameError.setVisibility(View.GONE);
        tvPhoneError.setVisibility(View.GONE);
        tvEmailError.setVisibility(View.GONE);
        tvPasswordError.setVisibility(View.GONE);
        tvBirthDateError.setVisibility(View.GONE);
        tvIdError.setVisibility(View.GONE);
    }

    // --- Existing Functionality ---

    /**
     * Opens a DatePickerDialog allowing the user to select their date of birth.
     * The maximum selectable date is constrained to the current system date.
     *
     * @param view The view that was clicked to trigger this method.
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
                    String date = String.format("%02d.%02d.%d", selectedDay, selectedMonth + 1, selectedYear);
                    etBirthDate.setText(date);
                },
                year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    /**
     * Navigates the user back to the LoginActivity and finishes the current activity.
     *
     * @param view The view that was clicked to trigger this method.
     */
    public void moveToLogin(View view)
    {
        gi = new Intent(this, LoginActivity.class);
        startActivity(gi);
        finish();
    }

    /**
     * Initiates the ID card scanning process. Requests camera permission if not already granted,
     * otherwise launches the camera directly.
     *
     * @param view The view that was clicked to trigger this method.
     */
    public void scanID(View view)
    {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        else
        {
            launchCamera();
        }
    }

    /**
     * Prepares a temporary file to store the captured image and launches the
     * default camera application to take a full-resolution picture.
     */
    private void launchCamera()
    {
        String filename = "tempfile_id_card";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try
        {
            File imgFile = File.createTempFile(filename, ".jpg", storageDir);
            currentPath = imgFile.getAbsolutePath();

            String authority = getPackageName() + ".fileprovider";
            Uri imageUri = FileProvider.getUriForFile(SignUpActivity.this, authority, imgFile);

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
     * Callback for the result from requesting permissions.
     * If the camera permission is granted, it proceeds to launch the camera.
     *
     * @param requestCode  The request code passed in requestPermissions().
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
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

    /**
     * Callback for the result from the camera intent.
     * Decodes the captured image and sends it to the Gemini API for processing.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult().
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param data_back   An Intent, which can return result data to the caller.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data_back)
    {
        super.onActivityResult(requestCode, resultCode, data_back);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_FULL_IMAGE_CAPTURE)
        {
            imageBitmap = BitmapFactory.decodeFile(currentPath);
            pD = new ProgressDialog(this);
            pD.setTitle("Processing ID");
            pD.setMessage("Analyzing with AI...");
            pD.setCancelable(false);
            pD.show();

            String finalPrompt = PHOTO_PROMPT + "\n\nReturn the data strictly according to this JSON schema:\n" + ID_CARD_SCHEMA;
            geminiManager.sendTextWithPhotoPrompt(finalPrompt, imageBitmap, this);
        }
    }

    /**
     * Callback triggered when the Gemini API successfully returns a response.
     * Parses the JSON result and populates the ID number and Date of Birth fields in the UI.
     *
     * @param result The generated text response from the Gemini model.
     */
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
                if (!idNumber.isEmpty()) etIDNumber.setText(idNumber);
                if (!birthDate.isEmpty()) etBirthDate.setText(birthDate);

                // Clear errors if AI filled them correctly
                if(!idNumber.isEmpty() || !birthDate.isEmpty()) resetErrors();
            });
        }

        catch (JSONException e)
        {
            Log.e(TAG, "JSON Parsing error: " + e.getMessage());
            runOnUiThread(() -> Toast.makeText(SignUpActivity.this, "Failed to parse data. Try again.", Toast.LENGTH_LONG).show());
        }
    }

    /**
     * Callback triggered when the Gemini API request fails.
     * Dismisses the progress dialog and displays an error message to the user.
     *
     * @param error The Throwable object containing error details.
     */
    @Override
    public void onFailure(Throwable error)
    {
        if (pD != null && pD.isShowing())
        {
            pD.dismiss();
        }
        Log.e(TAG, "Gemini Error: " + error.getMessage());
        runOnUiThread(() -> Toast.makeText(SignUpActivity.this, "Scan failed: " + error.getMessage(), Toast.LENGTH_SHORT).show());
    }
}