package com.example.morpheusfitness;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String DATABASE_URL =
            "https://morpheusfitness-f1650-default-rtdb.asia-southeast1.firebasedatabase.app";

    private EditText etProfileName;
    private EditText etProfileEmail;
    private EditText etProfileAge;
    private EditText etProfileHeight;
    private EditText etProfileWeight;

    private Spinner spinnerProfileGender;

    private Button btnUpdateProfile;
    private Button btnProfileBack;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersReference;

    private String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);


        // =====================================================
        // CONNECT XML
        // =====================================================

        etProfileName =
                findViewById(R.id.etProfileName);

        etProfileEmail =
                findViewById(R.id.etProfileEmail);

        etProfileAge =
                findViewById(R.id.etProfileAge);

        spinnerProfileGender =
                findViewById(R.id.spinnerGender);

        etProfileHeight =
                findViewById(R.id.etProfileHeight);

        etProfileWeight =
                findViewById(R.id.etProfileWeight);

        btnUpdateProfile =
                findViewById(R.id.btnUpdateProfile);

        btnProfileBack =
                findViewById(R.id.btnProfileBack);


        // =====================================================
        // GENDER SPINNER
        // =====================================================

        // =========================================================
// GENDER SPINNER - MORPHEUS DARK THEME
// =========================================================

        String[] genders = {"Male", "Female"};

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_gender_selected,
                genders
        );

        genderAdapter.setDropDownViewResource(
                R.layout.spinner_gender_dropdown
        );

        spinnerProfileGender.setAdapter(genderAdapter);


        // =====================================================
        // FIREBASE
        // =====================================================

        firebaseAuth =
                FirebaseAuth.getInstance();

        FirebaseUser currentUser =
                firebaseAuth.getCurrentUser();


        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "User not logged in.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        uid = currentUser.getUid();


        usersReference =
                FirebaseDatabase
                        .getInstance(DATABASE_URL)
                        .getReference("users");


        // =====================================================
        // LOAD PROFILE
        // =====================================================

        loadUserProfile();


        // =====================================================
        // UPDATE PROFILE
        // =====================================================

        btnUpdateProfile.setOnClickListener(v -> {

            updateUserProfile();

        });


        // =====================================================
        // BACK
        // =====================================================

        btnProfileBack.setOnClickListener(v -> {

            finish();

        });
    }


    // =========================================================
    // LOAD USER PROFILE
    // =========================================================

    private void loadUserProfile() {

        usersReference
                .child(uid)
                .addListenerForSingleValueEvent(

                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot
                            ) {

                                if (!snapshot.exists()) {

                                    Toast.makeText(
                                            ProfileActivity.this,
                                            "Profile not found.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }


                                // NAME
                                Object name =
                                        snapshot
                                                .child("name")
                                                .getValue();

                                if (name != null) {

                                    etProfileName.setText(
                                            name.toString()
                                    );
                                }


                                // EMAIL
                                Object email =
                                        snapshot
                                                .child("email")
                                                .getValue();

                                if (email != null) {

                                    etProfileEmail.setText(
                                            email.toString()
                                    );
                                }


                                // AGE
                                Object age =
                                        snapshot
                                                .child("age")
                                                .getValue();

                                if (age != null) {

                                    etProfileAge.setText(
                                            age.toString()
                                    );
                                }


                                // HEIGHT
                                Object height =
                                        snapshot
                                                .child("height")
                                                .getValue();

                                if (height != null) {

                                    etProfileHeight.setText(
                                            height.toString()
                                    );
                                }


                                // WEIGHT
                                Object weight =
                                        snapshot
                                                .child("weight")
                                                .getValue();

                                if (weight != null) {

                                    etProfileWeight.setText(
                                            weight.toString()
                                    );
                                }


                                // GENDER
                                Object gender =
                                        snapshot
                                                .child("gender")
                                                .getValue();

                                if (gender != null) {

                                    String genderValue =
                                            gender.toString();


                                    if (genderValue.equalsIgnoreCase(
                                            "Female"
                                    )) {

                                        spinnerProfileGender
                                                .setSelection(1);

                                    } else {

                                        spinnerProfileGender
                                                .setSelection(0);
                                    }
                                }
                            }


                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error
                            ) {

                                Toast.makeText(
                                        ProfileActivity.this,
                                        "Unable to load profile.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }


    // =========================================================
    // UPDATE USER PROFILE
    // =========================================================

    private void updateUserProfile() {

        String name =
                etProfileName
                        .getText()
                        .toString()
                        .trim();

        String ageText =
                etProfileAge
                        .getText()
                        .toString()
                        .trim();

        String heightText =
                etProfileHeight
                        .getText()
                        .toString()
                        .trim();

        String weightText =
                etProfileWeight
                        .getText()
                        .toString()
                        .trim();

        String gender =
                spinnerProfileGender
                        .getSelectedItem()
                        .toString();


        // =====================================================
        // EMPTY VALIDATION
        // =====================================================

        if (name.isEmpty()
                || ageText.isEmpty()
                || heightText.isEmpty()
                || weightText.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please complete all fields.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        int age;
        double height;
        double weight;


        // =====================================================
        // NUMBER VALIDATION
        // =====================================================

        try {

            age =
                    Integer.parseInt(ageText);

            height =
                    Double.parseDouble(heightText);

            weight =
                    Double.parseDouble(weightText);

        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "Please enter valid numbers.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (age <= 0
                || height <= 0
                || weight <= 0) {

            Toast.makeText(
                    this,
                    "Age, height and weight must be greater than 0.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // DATA TO UPDATE
        // =====================================================

        Map<String, Object> updates =
                new HashMap<>();

        updates.put(
                "name",
                name
        );

        updates.put(
                "age",
                age
        );

        updates.put(
                "gender",
                gender
        );

        updates.put(
                "height",
                height
        );

        updates.put(
                "weight",
                weight
        );


        // =====================================================
        // UPDATE FIREBASE
        // =====================================================

        btnUpdateProfile.setEnabled(false);

        btnUpdateProfile.setText(
                "UPDATING..."
        );


        usersReference
                .child(uid)
                .updateChildren(updates)
                .addOnCompleteListener(task -> {

                    btnUpdateProfile.setEnabled(true);

                    btnUpdateProfile.setText(
                            "UPDATE PROFILE"
                    );


                    if (task.isSuccessful()) {

                        Toast.makeText(
                                ProfileActivity.this,
                                "Profile updated successfully!",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        String message =
                                "Unable to update profile.";

                        if (task.getException() != null
                                && task.getException().getMessage() != null) {

                            message =
                                    task
                                            .getException()
                                            .getMessage();
                        }


                        Toast.makeText(
                                ProfileActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}