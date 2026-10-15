package com.example.morpheusfitness;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    // =========================================================
    // UI
    // =========================================================

    private TextView tvWelcomeName;

    private Button btnStartWorkout;
    private Button btnWorkoutHistory;
    private Button btnProfile;
    private Button btnLogout;


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

        setContentView(R.layout.activity_home);


        // =====================================================
        // CONNECT XML
        // =====================================================

        tvWelcomeName =
                findViewById(R.id.tvWelcomeName);

        btnStartWorkout =
                findViewById(R.id.btnStartWorkout);

        btnWorkoutHistory =
                findViewById(R.id.btnWorkoutHistory);

        btnProfile =
                findViewById(R.id.btnProfile);

        btnLogout =
                findViewById(R.id.btnLogout);


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
        // LOAD USER NAME
        // =====================================================

        loadUserName();


        // =====================================================
        // START WORKOUT
        // =====================================================

        btnStartWorkout.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            ExerciseSelectionActivity.class
                    );

            startActivity(intent);

        });


        // =====================================================
        // WORKOUT HISTORY
        // =====================================================

        btnWorkoutHistory.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            WorkoutHistoryActivity.class
                    );

            startActivity(intent);

        });


        // =====================================================
        // PROFILE
        // =====================================================

        btnProfile.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            ProfileActivity.class
                    );

            startActivity(intent);

        });


        // =====================================================
        // LOGOUT
        // =====================================================

        btnLogout.setOnClickListener(v -> {

            firebaseAuth.signOut();


            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            LoginActivity.class
                    );


            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );


            startActivity(intent);

            finish();

        });
    }


    // =========================================================
    // LOAD USER NAME FROM FIREBASE
    // =========================================================

    private void loadUserName() {

        FirebaseUser currentUser =
                firebaseAuth.getCurrentUser();


        // =====================================================
        // USER NOT LOGGED IN
        // =====================================================

        if (currentUser == null) {

            tvWelcomeName.setText(
                    "Welcome Back! 💪"
            );

            return;
        }


        // =====================================================
        // GET CURRENT USER UID
        // =====================================================

        String uid =
                currentUser.getUid();


        // =====================================================
        // READ NAME FROM:
        //
        // users
        //    └── UID
        //         └── name
        // =====================================================

        usersReference
                .child(uid)
                .child("name")
                .addListenerForSingleValueEvent(

                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                String name =
                                        snapshot.getValue(String.class);


                                // =================================
                                // NAME EXISTS
                                // =================================

                                if (name != null
                                        && !name.trim().isEmpty()) {

                                    String cleanName =
                                            formatName(name);


                                    tvWelcomeName.setText(
                                            "Hi, "
                                                    + cleanName
                                                    + "! 💪"
                                    );
                                }


                                // =================================
                                // NAME NOT FOUND
                                // =================================

                                else {

                                    tvWelcomeName.setText(
                                            "Welcome Back! 💪"
                                    );
                                }
                            }


                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                                // Fallback if Firebase cannot load name

                                tvWelcomeName.setText(
                                        "Welcome Back! 💪"
                                );
                            }
                        }
                );
    }


    // =========================================================
    // FORMAT USER NAME
    // =========================================================

    private String formatName(String name) {

        name = name.trim();


        if (name.isEmpty()) {

            return "User";
        }


        // Example:
        // aryana -> Aryana

        return name.substring(0, 1)
                .toUpperCase()
                + name.substring(1);
    }


    // =========================================================
    // REFRESH NAME WHEN RETURNING FROM PROFILE
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();


        /*
         * If user changes their name inside ProfileActivity
         * and returns to HomeActivity,
         * dashboard will automatically reload the latest name.
         */

        if (firebaseAuth != null
                && tvWelcomeName != null
                && usersReference != null) {

            loadUserName();
        }
    }
}