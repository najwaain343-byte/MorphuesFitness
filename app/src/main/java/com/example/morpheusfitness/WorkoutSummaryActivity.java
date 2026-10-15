package com.example.morpheusfitness;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class WorkoutSummaryActivity extends AppCompatActivity {

    private static final String DATABASE_URL =
            "https://morpheusfitness-f1650-default-rtdb.asia-southeast1.firebasedatabase.app";

    // =========================================================
    // UI
    // =========================================================

    private TextView tvSummaryExercise;
    private TextView tvSummaryPerformance;
    private TextView tvSummaryDuration;
    private TextView tvSummaryCalories;

    private Button btnSaveWorkout;
    private Button btnBackToExercises;


    // =========================================================
    // WORKOUT DATA
    // =========================================================

    private String exerciseType;

    private int reps;

    private long holdSeconds;

    private long durationSeconds;

    private double calories;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth firebaseAuth;

    private DatabaseReference workoutsReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_workout_summary);


        // =====================================================
        // CONNECT XML
        // =====================================================

        tvSummaryExercise =
                findViewById(R.id.tvSummaryExercise);

        tvSummaryPerformance =
                findViewById(R.id.tvSummaryPerformance);

        tvSummaryDuration =
                findViewById(R.id.tvSummaryDuration);

        tvSummaryCalories =
                findViewById(R.id.tvSummaryCalories);

        btnSaveWorkout =
                findViewById(R.id.btnSaveWorkout);

        btnBackToExercises =
                findViewById(R.id.btnBackToExercises);


        // =====================================================
        // FIREBASE
        // =====================================================

        firebaseAuth =
                FirebaseAuth.getInstance();

        workoutsReference =
                FirebaseDatabase
                        .getInstance(DATABASE_URL)
                        .getReference("workouts");


        // =====================================================
        // GET WORKOUT DATA
        // =====================================================

        exerciseType =
                getIntent()
                        .getStringExtra("EXERCISE_TYPE");

        reps =
                getIntent()
                        .getIntExtra(
                                "REPS",
                                0
                        );

        holdSeconds =
                getIntent()
                        .getLongExtra(
                                "HOLD_SECONDS",
                                0
                        );

        durationSeconds =
                getIntent()
                        .getLongExtra(
                                "DURATION_SECONDS",
                                0
                        );

        calories =
                getIntent()
                        .getDoubleExtra(
                                "CALORIES",
                                0.0
                        );


        // =====================================================
        // DISPLAY SUMMARY
        // =====================================================

        displayWorkoutSummary();


        // =====================================================
        // SAVE WORKOUT
        // =====================================================

        btnSaveWorkout.setOnClickListener(v -> {

            saveWorkout();

        });


        // =====================================================
        // BACK TO EXERCISES
        // =====================================================

        btnBackToExercises.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            WorkoutSummaryActivity.this,
                            ExerciseSelectionActivity.class
                    );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            );

            startActivity(intent);

            finish();

        });
    }


    // =========================================================
    // DISPLAY WORKOUT SUMMARY
    // =========================================================

    private void displayWorkoutSummary() {

        String exerciseName;


        if ("PUSH_UP".equals(exerciseType)) {

            exerciseName = "PUSH-UP";

        } else if ("PLANK".equals(exerciseType)) {

            exerciseName = "PLANK";

        } else {

            exerciseName = "SQUAT";

        }


        tvSummaryExercise.setText(
                exerciseName
        );


        // =====================================================
        // REPS / HOLD TIME
        // =====================================================

        if ("PLANK".equals(exerciseType)) {

            tvSummaryPerformance.setText(
                    "Hold Time: "
                            + holdSeconds
                            + " seconds"
            );

        } else {

            tvSummaryPerformance.setText(
                    "Repetitions: "
                            + reps
            );

        }


        // =====================================================
        // DURATION
        // =====================================================

        long minutes =
                durationSeconds / 60;

        long seconds =
                durationSeconds % 60;


        String durationText =
                String.format(
                        Locale.US,
                        "Duration: %02d:%02d",
                        minutes,
                        seconds
                );


        tvSummaryDuration.setText(
                durationText
        );


        // =====================================================
        // CALORIES
        // =====================================================

        String caloriesText =
                String.format(
                        Locale.US,
                        "Calories Burned: %.2f kcal",
                        calories
                );


        tvSummaryCalories.setText(
                caloriesText
        );
    }


    // =========================================================
    // SAVE WORKOUT TO FIREBASE
    // =========================================================

    private void saveWorkout() {

        FirebaseUser currentUser =
                firebaseAuth.getCurrentUser();


        // =====================================================
        // CHECK LOGIN
        // =====================================================

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "User not logged in.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // Prevent multiple taps while saving
        btnSaveWorkout.setEnabled(false);

        btnSaveWorkout.setText(
                "SAVING..."
        );


        String uid =
                currentUser.getUid();


        // =====================================================
        // CREATE UNIQUE WORKOUT ID
        // =====================================================

        String workoutId =
                workoutsReference
                        .child(uid)
                        .push()
                        .getKey();


        if (workoutId == null) {

            btnSaveWorkout.setEnabled(true);

            btnSaveWorkout.setText(
                    "SAVE WORKOUT"
            );

            Toast.makeText(
                    this,
                    "Unable to create workout.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // CREATE WORKOUT OBJECT
        // =====================================================

        Workout workout =
                new Workout(
                        exerciseType,
                        reps,
                        holdSeconds,
                        durationSeconds,
                        calories,
                        System.currentTimeMillis()
                );


        // =====================================================
        // SAVE
        // =====================================================

        workoutsReference
                .child(uid)
                .child(workoutId)
                .setValue(workout)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                WorkoutSummaryActivity.this,
                                "Workout saved successfully!",
                                Toast.LENGTH_SHORT
                        ).show();


                        btnSaveWorkout.setText(
                                "SAVED"
                        );

                        // Keep disabled so same workout
                        // cannot accidentally be saved twice.
                        btnSaveWorkout.setEnabled(false);


                    } else {

                        btnSaveWorkout.setEnabled(true);

                        btnSaveWorkout.setText(
                                "SAVE WORKOUT"
                        );


                        String errorMessage =
                                "Failed to save workout.";

                        if (task.getException() != null) {

                            errorMessage =
                                    task
                                            .getException()
                                            .getMessage();

                        }


                        Toast.makeText(
                                WorkoutSummaryActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();

                    }

                });

    }

}