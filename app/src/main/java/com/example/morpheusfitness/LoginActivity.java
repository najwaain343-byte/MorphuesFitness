package com.example.morpheusfitness;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    // =========================================================
    // UI
    // =========================================================

    private EditText etEmail;
    private EditText etPassword;

    private Button btnLogin;
    private TextView tvRegister;


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

        setContentView(R.layout.activity_login);


        // =====================================================
        // FIREBASE AUTH
        // =====================================================

        firebaseAuth =
                FirebaseAuth.getInstance();


        // =====================================================
        // FIREBASE REALTIME DATABASE
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

        etEmail =
                findViewById(R.id.etEmail);

        etPassword =
                findViewById(R.id.etPassword);

        btnLogin =
                findViewById(R.id.btnLogin);

        tvRegister =
                findViewById(R.id.tvRegister);


        // =====================================================
        // LOGIN
        // =====================================================

        btnLogin.setOnClickListener(
                v -> loginUser()
        );


        // =====================================================
        // REGISTER
        // =====================================================

        tvRegister.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            LoginActivity.this,
                            RegisterActivity.class
                    );

            startActivity(intent);
        });
    }


    // =========================================================
    // LOGIN USER
    // =========================================================

    private void loginUser() {

        String email =
                etEmail
                        .getText()
                        .toString()
                        .trim();

        String password =
                etPassword
                        .getText()
                        .toString();


        // Clear previous errors
        etEmail.setError(null);
        etPassword.setError(null);


        // =====================================================
        // EMAIL VALIDATION
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
                    "Enter a valid email address"
            );

            etEmail.requestFocus();

            return;
        }


        // =====================================================
        // PASSWORD VALIDATION
        // =====================================================

        if (TextUtils.isEmpty(password)) {

            etPassword.setError(
                    "Password is required"
            );

            etPassword.requestFocus();

            return;
        }


        // =====================================================
        // LOADING STATE
        // =====================================================

        btnLogin.setEnabled(false);

        btnLogin.setText(
                "LOGGING IN..."
        );


        // Remove previous session before new attempt
        firebaseAuth.signOut();


        // =====================================================
        // STEP 1 — FIREBASE AUTHENTICATION
        // =====================================================

        firebaseAuth
                .signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(task -> {

                    // -------------------------------------------------
                    // AUTHENTICATION FAILED
                    // -------------------------------------------------

                    if (!task.isSuccessful()) {

                        firebaseAuth.signOut();

                        resetLoginButton();

                        etPassword.setText("");

                        etPassword.requestFocus();

                        Toast.makeText(
                                LoginActivity.this,
                                "Invalid email or password",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }


                    // -------------------------------------------------
                    // GET AUTHENTICATED USER
                    // -------------------------------------------------

                    FirebaseUser currentUser =
                            firebaseAuth.getCurrentUser();


                    if (currentUser == null) {

                        firebaseAuth.signOut();

                        resetLoginButton();

                        Toast.makeText(
                                LoginActivity.this,
                                "Unable to verify user",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }


                    // =================================================
                    // STEP 2 — CHECK USER PROFILE EXISTS
                    // =================================================

                    String uid =
                            currentUser.getUid();


                    usersReference
                            .child(uid)
                            .addListenerForSingleValueEvent(

                                    new ValueEventListener() {

                                        @Override
                                        public void onDataChange(
                                                @NonNull DataSnapshot snapshot) {


                                            // =====================================
                                            // REGISTERED PROFILE EXISTS
                                            // =====================================

                                            if (snapshot.exists()) {

                                                openHome();
                                            }


                                            // =====================================
                                            // AUTH ACCOUNT EXISTS,
                                            // BUT APP PROFILE DOES NOT EXIST
                                            // =====================================

                                            else {

                                                firebaseAuth.signOut();

                                                resetLoginButton();

                                                etPassword.setText("");

                                                Toast.makeText(
                                                        LoginActivity.this,
                                                        "Account profile not found. Please register first.",
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                        }


                                        @Override
                                        public void onCancelled(
                                                @NonNull DatabaseError error) {

                                            firebaseAuth.signOut();

                                            resetLoginButton();

                                            Toast.makeText(
                                                    LoginActivity.this,
                                                    "Unable to verify account. Please try again.",
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        }
                                    }
                            );
                });
    }


    // =========================================================
    // OPEN HOME
    // =========================================================

    private void openHome() {

        resetLoginButton();


        Toast.makeText(
                LoginActivity.this,
                "Login successful",
                Toast.LENGTH_SHORT
        ).show();


        Intent intent =
                new Intent(
                        LoginActivity.this,
                        HomeActivity.class
                );


        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(intent);

        finish();
    }


    // =========================================================
    // RESET LOGIN BUTTON
    // =========================================================

    private void resetLoginButton() {

        btnLogin.setEnabled(true);

        btnLogin.setText("START YOUR WORKOUT  →");
    }
}