package com.example.morpheusfitness;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";


    // =========================================================
    // UI
    // =========================================================

    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etAge;
    private EditText etHeight;
    private EditText etWeight;

    private Spinner spinnerGender;

    private Button btnRegister;
    private TextView tvLogin;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersReference;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);


        // =====================================================
        // FIREBASE AUTHENTICATION
        // =====================================================

        firebaseAuth =
                FirebaseAuth.getInstance();


        // =====================================================
        // FIREBASE REALTIME DATABASE
        // IMPORTANT:
        // Database is located in asia-southeast1
        // =====================================================

        usersReference =
                FirebaseDatabase
                        .getInstance(
                                "https://morpheusfitness-f1650-default-rtdb.asia-southeast1.firebasedatabase.app"
                        )
                        .getReference("users");


        // =====================================================
        // CONNECT XML
        // =====================================================

        etName =
                findViewById(R.id.etName);

        etEmail =
                findViewById(R.id.etEmail);

        etPassword =
                findViewById(R.id.etPassword);

        etAge =
                findViewById(R.id.etAge);

        etHeight =
                findViewById(R.id.etHeight);

        etWeight =
                findViewById(R.id.etWeight);

        spinnerGender =
                findViewById(R.id.spinnerGender);

        btnRegister =
                findViewById(R.id.btnRegister);

        tvLogin =
                findViewById(R.id.tvLogin);


        // =====================================================
        // GENDER SPINNER
        // =====================================================

        String[] genders = {
                "Select Gender",
                "Male",
                "Female"
        };


        String[] gender = {"Male", "Female"};

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_gender_selected,
                genders
        );

        genderAdapter.setDropDownViewResource(
                R.layout.spinner_gender_dropdown
        );

        spinnerGender.setAdapter(genderAdapter);


        // =====================================================
        // REGISTER BUTTON
        // =====================================================

        btnRegister.setOnClickListener(
                v -> registerUser()
        );


        // =====================================================
        // ALREADY HAVE ACCOUNT → LOGIN
        // =====================================================

        tvLogin.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            RegisterActivity.this,
                            LoginActivity.class
                    );

            startActivity(intent);

            finish();
        });
    }


    // =========================================================
    // REGISTER USER
    // =========================================================

    private void registerUser() {


        // =====================================================
        // GET USER INPUT
        // =====================================================

        String name =
                etName
                        .getText()
                        .toString()
                        .trim();


        String email =
                etEmail
                        .getText()
                        .toString()
                        .trim();


        String password =
                etPassword
                        .getText()
                        .toString()
                        .trim();


        String ageText =
                etAge
                        .getText()
                        .toString()
                        .trim();


        String heightText =
                etHeight
                        .getText()
                        .toString()
                        .trim();


        String weightText =
                etWeight
                        .getText()
                        .toString()
                        .trim();


        String gender =
                spinnerGender
                        .getSelectedItem()
                        .toString();


        // =====================================================
        // VALIDATION - NAME
        // =====================================================

        if (TextUtils.isEmpty(name)) {

            etName.setError(
                    "Name is required"
            );

            etName.requestFocus();

            return;
        }


        // =====================================================
        // VALIDATION - EMAIL
        // =====================================================

        if (TextUtils.isEmpty(email)) {

            etEmail.setError(
                    "Email is required"
            );

            etEmail.requestFocus();

            return;
        }


        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etEmail.setError(
                    "Enter a valid email"
            );

            etEmail.requestFocus();

            return;
        }


        // =====================================================
        // VALIDATION - PASSWORD
        // =====================================================

        if (TextUtils.isEmpty(password)) {

            etPassword.setError(
                    "Password is required"
            );

            etPassword.requestFocus();

            return;
        }


        if (password.length() < 6) {

            etPassword.setError(
                    "Password must be at least 6 characters"
            );

            etPassword.requestFocus();

            return;
        }


        // =====================================================
        // VALIDATION - AGE
        // =====================================================

        if (TextUtils.isEmpty(ageText)) {

            etAge.setError(
                    "Age is required"
            );

            etAge.requestFocus();

            return;
        }


        // =====================================================
        // VALIDATION - GENDER
        // =====================================================

        if (gender.equals("Select Gender")) {

            Toast.makeText(
                    RegisterActivity.this,
                    "Please select gender",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // VALIDATION - HEIGHT
        // =====================================================

        if (TextUtils.isEmpty(heightText)) {

            etHeight.setError(
                    "Height is required"
            );

            etHeight.requestFocus();

            return;
        }


        // =====================================================
        // VALIDATION - WEIGHT
        // =====================================================

        if (TextUtils.isEmpty(weightText)) {

            etWeight.setError(
                    "Weight is required"
            );

            etWeight.requestFocus();

            return;
        }


        // =====================================================
        // CONVERT NUMBER VALUES
        // =====================================================

        int age;
        double height;
        double weight;


        try {

            age =
                    Integer.parseInt(
                            ageText
                    );


            height =
                    Double.parseDouble(
                            heightText
                    );


            weight =
                    Double.parseDouble(
                            weightText
                    );


        } catch (NumberFormatException e) {

            Toast.makeText(
                    RegisterActivity.this,
                    "Please enter valid profile information",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // AGE VALIDATION
        // =====================================================

        if (age <= 0) {

            etAge.setError(
                    "Enter a valid age"
            );

            etAge.requestFocus();

            return;
        }


        // =====================================================
        // HEIGHT VALIDATION
        // =====================================================

        if (height <= 0) {

            etHeight.setError(
                    "Enter a valid height"
            );

            etHeight.requestFocus();

            return;
        }


        // =====================================================
        // WEIGHT VALIDATION
        // =====================================================

        if (weight <= 0) {

            etWeight.setError(
                    "Enter a valid weight"
            );

            etWeight.requestFocus();

            return;
        }


        // =====================================================
        // DISABLE REGISTER BUTTON
        // =====================================================

        btnRegister.setEnabled(false);

        btnRegister.setText(
                "CREATING ACCOUNT..."
        );


        Log.d(
                TAG,
                "Starting Firebase registration..."
        );


        // =====================================================
        // CREATE FIREBASE AUTHENTICATION ACCOUNT
        // =====================================================

        firebaseAuth
                .createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(
                        RegisterActivity.this,
                        authTask -> {


                            // =============================================
                            // AUTHENTICATION FAILED
                            // =============================================

                            if (!authTask.isSuccessful()) {

                                enableRegisterButton();


                                String errorMessage =
                                        "Registration failed";


                                if (authTask.getException() != null) {

                                    Log.e(
                                            TAG,
                                            "Firebase Authentication failed",
                                            authTask.getException()
                                    );


                                    if (authTask
                                            .getException()
                                            .getMessage() != null) {

                                        errorMessage =
                                                authTask
                                                        .getException()
                                                        .getMessage();
                                    }
                                }


                                Toast.makeText(
                                        RegisterActivity.this,
                                        errorMessage,
                                        Toast.LENGTH_LONG
                                ).show();


                                return;
                            }


                            // =============================================
                            // AUTHENTICATION SUCCESS
                            // =============================================

                            Log.d(
                                    TAG,
                                    "Firebase Authentication successful"
                            );


                            // =============================================
                            // GET CREATED FIREBASE USER
                            // =============================================

                            FirebaseUser firebaseUser =
                                    firebaseAuth
                                            .getCurrentUser();


                            if (firebaseUser == null) {

                                enableRegisterButton();


                                Log.e(
                                        TAG,
                                        "Firebase user is null"
                                );


                                Toast.makeText(
                                        RegisterActivity.this,
                                        "Unable to retrieve user account",
                                        Toast.LENGTH_LONG
                                ).show();


                                return;
                            }


                            // =============================================
                            // GET USER UID
                            // =============================================

                            String uid =
                                    firebaseUser
                                            .getUid();


                            Log.d(
                                    TAG,
                                    "Firebase account created. UID: "
                                            + uid
                            );


                            // =============================================
                            // CREATE USER PROFILE OBJECT
                            // =============================================

                            User user =
                                    new User(
                                            name,
                                            email,
                                            age,
                                            gender,
                                            height,
                                            weight
                                    );


                            Log.d(
                                    TAG,
                                    "Saving profile to Realtime Database..."
                            );


                            // =============================================
                            // SAVE USER PROFILE TO REALTIME DATABASE
                            //
                            // Structure:
                            //
                            // users
                            //   └── UID
                            //        ├── name
                            //        ├── email
                            //        ├── age
                            //        ├── gender
                            //        ├── height
                            //        └── weight
                            // =============================================

                            usersReference
                                    .child(uid)
                                    .setValue(user)
                                    .addOnCompleteListener(
                                            databaseTask -> {


                                                // =================================
                                                // DATABASE SAVE SUCCESS
                                                // =================================

                                                if (databaseTask.isSuccessful()) {


                                                    Log.d(
                                                            TAG,
                                                            "User profile saved successfully"
                                                    );


                                                    Toast.makeText(
                                                            RegisterActivity.this,
                                                            "Registration successful! Please login.",
                                                            Toast.LENGTH_SHORT
                                                    ).show();


                                                    /*
                                                     * IMPORTANT:
                                                     *
                                                     * Firebase automatically
                                                     * signs in a newly created
                                                     * account.
                                                     *
                                                     * Sign out ONLY AFTER
                                                     * Realtime Database confirms
                                                     * that the profile was saved.
                                                     */

                                                    firebaseAuth.signOut();


                                                    Log.d(
                                                            TAG,
                                                            "New user signed out"
                                                    );


                                                    // =============================
                                                    // GO TO LOGIN PAGE
                                                    // =============================

                                                    Intent intent =
                                                            new Intent(
                                                                    RegisterActivity.this,
                                                                    LoginActivity.class
                                                            );


                                                    /*
                                                     * Clear previous activities
                                                     * so pressing Back doesn't
                                                     * return to Register.
                                                     */

                                                    intent.setFlags(
                                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    );


                                                    startActivity(intent);

                                                    finish();


                                                } else {


                                                    // =================================
                                                    // DATABASE SAVE FAILED
                                                    // =================================

                                                    enableRegisterButton();


                                                    String errorMessage =
                                                            "Failed to save profile";


                                                    if (databaseTask
                                                            .getException() != null) {


                                                        Log.e(
                                                                TAG,
                                                                "Realtime Database save failed",
                                                                databaseTask
                                                                        .getException()
                                                        );


                                                        if (databaseTask
                                                                .getException()
                                                                .getMessage() != null) {


                                                            errorMessage =
                                                                    databaseTask
                                                                            .getException()
                                                                            .getMessage();
                                                        }
                                                    }


                                                    Toast.makeText(
                                                            RegisterActivity.this,
                                                            "Profile save failed: "
                                                                    + errorMessage,
                                                            Toast.LENGTH_LONG
                                                    ).show();
                                                }
                                            }
                                    );
                        }
                );
    }


    // =========================================================
    // ENABLE REGISTER BUTTON AGAIN
    // =========================================================

    private void enableRegisterButton() {

        btnRegister.setEnabled(true);

        btnRegister.setText(
                "REGISTER"
        );
    }
}