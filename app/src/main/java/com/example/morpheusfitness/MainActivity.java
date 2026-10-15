package com.example.morpheusfitness;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

// Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Intent;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    // =========================================================
    // CAMERA PERMISSION
    // =========================================================

    private static final int CAMERA_PERMISSION_CODE = 100;

    private static final String TAG = "MorpheusFitness";


    // =========================================================
    // UI COMPONENTS
    // =========================================================

    private PreviewView previewView;
    private OverlayView overlayView;

    private TextView tvExerciseName;
    private TextView tvKneeAngle;
    private TextView tvHipAngle;
    private TextView tvTorsoAngle;

    private TextView tvPhase;
    private TextView tvReps;
    private TextView tvFeedback;

    private TextView tvDuration;
    private TextView tvCalories;
    private Button btnResetWorkout;
    private Button btnFinishWorkout;

    // Final workout values
    private int currentReps = 0;
    private long currentHoldSeconds = 0;
    private long currentDurationSeconds = 0;
    private double currentCalories = 0.0;


    // =========================================================
    // EXERCISE ANALYZERS
    // =========================================================

    private SquatAnalyzer squatAnalyzer;
    private PushUpAnalyzer pushUpAnalyzer;
    private PlankAnalyzer plankAnalyzer;


    // =========================================================
    // EXERCISE INFORMATION
    // =========================================================

    private String exerciseType;


    // =========================================================
    // WORKOUT INFORMATION
    // =========================================================

    private long workoutStartTime;


    // =========================================================
    // USER WEIGHT
    // =========================================================

    private double userWeightKg = 0.0;

    private boolean userWeightLoaded = false;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth firebaseAuth;

    private DatabaseReference usersReference;


    // =========================================================
    // MEDIAPIPE
    // =========================================================

    private PoseLandmarkerHelper poseLandmarkerHelper;


    // =========================================================
    // CAMERA BACKGROUND THREAD
    // =========================================================

    private ExecutorService cameraExecutor;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        // =====================================================
        // FIREBASE
        // =====================================================

        firebaseAuth =
                FirebaseAuth.getInstance();


        usersReference =
                FirebaseDatabase
                        .getInstance(
                                "https://morpheusfitness-f1650-default-rtdb.asia-southeast1.firebasedatabase.app"
                        )
                        .getReference("users");


        // =====================================================
        // START WORKOUT TIMER
        // =====================================================

        workoutStartTime =
                System.currentTimeMillis();


        // =====================================================
        // GET SELECTED EXERCISE
        // =====================================================

        exerciseType =
                getIntent()
                        .getStringExtra(
                                "EXERCISE_TYPE"
                        );


        // Safety fallback
        if (exerciseType == null) {

            exerciseType = "SQUAT";
        }


        // =====================================================
        // CONNECT XML VIEWS
        // =====================================================

        previewView =
                findViewById(
                        R.id.previewView
                );


        overlayView =
                findViewById(
                        R.id.overlayView
                );


        tvExerciseName =
                findViewById(
                        R.id.tvExerciseName
                );


        tvKneeAngle =
                findViewById(
                        R.id.tvKneeAngle
                );


        tvHipAngle =
                findViewById(
                        R.id.tvHipAngle
                );


        tvTorsoAngle =
                findViewById(
                        R.id.tvTorsoAngle
                );


        tvPhase =
                findViewById(
                        R.id.tvPhase
                );


        tvReps =
                findViewById(
                        R.id.tvReps
                );


        tvFeedback =
                findViewById(
                        R.id.tvFeedback
                );


        // =====================================================
        // CALORIE COUNTER UI
        // =====================================================

        tvDuration =
                findViewById(
                        R.id.tvDuration
                );


        tvCalories =
                findViewById(
                        R.id.tvCalories
                );

        btnResetWorkout =
                findViewById(
                        R.id.btnResetWorkout
                );

        btnFinishWorkout =
                findViewById(
                        R.id.btnFinishWorkout
                );

        tvCalories.setText(
                "Calories: loading..."
        );


        // =====================================================
        // LOAD USER WEIGHT
        // =====================================================

        loadUserWeight();

        btnFinishWorkout.setOnClickListener(v -> finishWorkout());

        btnResetWorkout.setOnClickListener(v -> resetWorkout());

        // =====================================================
        // DISPLAY SELECTED EXERCISE
        // =====================================================

        switch (exerciseType) {

            case "PUSH_UP":

                tvExerciseName.setText(
                        "PUSH-UP"
                );

                break;


            case "PLANK":

                tvExerciseName.setText(
                        "PLANK"
                );

                break;


            case "SQUAT":

            default:

                tvExerciseName.setText(
                        "SQUAT"
                );

                break;
        }


        // =====================================================
        // INITIALIZE EXERCISE ANALYZERS
        // =====================================================

        squatAnalyzer =
                new SquatAnalyzer();


        pushUpAnalyzer =
                new PushUpAnalyzer();


        plankAnalyzer =
                new PlankAnalyzer();


        // =====================================================
        // CAMERA BACKGROUND THREAD
        // =====================================================

        cameraExecutor =
                Executors.newSingleThreadExecutor();


        // =====================================================
        // INITIALIZE MEDIAPIPE
        // =====================================================

        poseLandmarkerHelper =
                new PoseLandmarkerHelper(

                        this,

                        new PoseLandmarkerHelper.PoseResultListener() {

                            @Override
                            public void onResults(
                                    PoseLandmarkerResult result) {


                                runOnUiThread(() -> {


                                    // =========================================
                                    // NO PERSON DETECTED
                                    // =========================================

                                    if (result == null ||
                                            result.landmarks().isEmpty()) {


                                        overlayView.clear();


                                        tvKneeAngle.setText(
                                                "Angle: --°"
                                        );


                                        tvHipAngle.setVisibility(
                                                View.GONE
                                        );


                                        tvTorsoAngle.setVisibility(
                                                View.GONE
                                        );


                                        tvPhase.setText(
                                                "Phase: --"
                                        );


                                        tvFeedback.setText(
                                                "Feedback: BODY NOT DETECTED"
                                        );


                                        updateWorkoutStats();

                                        return;
                                    }


                                    // =========================================
                                    // DRAW MEDIAPIPE SKELETON
                                    // =========================================

                                    overlayView.setResults(
                                            result
                                    );


                                    // =========================================
                                    // GET LANDMARKS
                                    // =========================================

                                    List<NormalizedLandmark> landmarks =
                                            result
                                                    .landmarks()
                                                    .get(0);

                                    // =========================================
// CHECK BODY VISIBILITY
// =========================================

                                    if (!isBodyVisible(
                                            landmarks,
                                            exerciseType
                                    )) {

                                        tvKneeAngle.setText(
                                                "Angle: --°"
                                        );

                                        tvHipAngle.setVisibility(
                                                View.GONE
                                        );

                                        tvTorsoAngle.setVisibility(
                                                View.GONE
                                        );

                                        tvPhase.setText(
                                                "Phase: WAITING"
                                        );

                                        if ("PLANK".equals(exerciseType)) {

                                            // Pause plank timer when full body is not visible
                                            plankAnalyzer.pause();

                                            tvReps.setText(
                                                    "Hold: " + currentHoldSeconds + "s"
                                            );

                                        }

                                        else {

                                            tvReps.setText(
                                                    "Reps: " + currentReps
                                            );
                                        }

                                        tvFeedback.setText(
                                                "Feedback: MOVE BACK - FULL BODY NOT VISIBLE"
                                        );

                                        updateWorkoutStats();

                                        return;
                                    }


                                    // =========================================
                                    // SELECT EXERCISE
                                    // =========================================

                                    switch (exerciseType) {


                                        // =====================================
                                        // SQUAT
                                        // =====================================

                                        case "SQUAT": {


                                            SquatAnalyzer.SquatResult
                                                    squatResult =
                                                    squatAnalyzer
                                                            .analyze(
                                                                    landmarks
                                                            );

                                            currentReps = squatResult.getRepetitions();
                                            currentHoldSeconds = 0;


                                            String angleText =
                                                    String.format(
                                                            Locale.US,
                                                            "Knee Angle: %.1f°",
                                                            squatResult
                                                                    .getKneeAngle()
                                                    );


                                            String hipText =
                                                    String.format(
                                                            Locale.US,
                                                            "Hip Angle: %.1f°",
                                                            squatResult
                                                                    .getHipAngle()
                                                    );


                                            String torsoText =
                                                    String.format(
                                                            Locale.US,
                                                            "Torso Angle: %.1f°",
                                                            squatResult
                                                                    .getTorsoAngle()
                                                    );


                                            String phaseText =
                                                    "Phase: "
                                                            + squatResult
                                                            .getPhase()
                                                            .name();


                                            String repText =
                                                    "Reps: "
                                                            + squatResult
                                                            .getRepetitions();


                                            tvKneeAngle.setText(
                                                    angleText
                                            );


                                            tvHipAngle.setVisibility(
                                                    View.VISIBLE
                                            );


                                            tvHipAngle.setText(
                                                    hipText
                                            );


                                            tvTorsoAngle.setVisibility(
                                                    View.VISIBLE
                                            );


                                            tvTorsoAngle.setText(
                                                    torsoText
                                            );


                                            tvPhase.setText(
                                                    phaseText
                                            );


                                            tvReps.setText(
                                                    repText
                                            );


                                            tvFeedback.setText(
                                                    "Feedback: "
                                                            + squatResult
                                                            .getFeedback()
                                            );


                                            break;
                                        }


                                        // =====================================
                                        // PUSH-UP
                                        // =====================================

                                        case "PUSH_UP": {


                                            PushUpAnalyzer.PushUpResult
                                                    pushUpResult =
                                                    pushUpAnalyzer
                                                            .analyze(
                                                                    landmarks
                                                            );

                                            currentReps = pushUpResult.getRepetitions();
                                            currentHoldSeconds = 0;

                                            String angleText =
                                                    String.format(
                                                            Locale.US,
                                                            "Elbow Angle: %.1f°",
                                                            pushUpResult
                                                                    .getElbowAngle()
                                                    );


                                            String bodyText =
                                                    String.format(
                                                            Locale.US,
                                                            "Body Angle: %.1f°",
                                                            pushUpResult
                                                                    .getBodyAngle()
                                                    );


                                            String phaseText =
                                                    "Phase: "
                                                            + pushUpResult
                                                            .getPhase()
                                                            .name();


                                            String repText =
                                                    "Reps: "
                                                            + pushUpResult
                                                            .getRepetitions();


                                            tvKneeAngle.setText(
                                                    angleText
                                            );


                                            tvHipAngle.setVisibility(
                                                    View.VISIBLE
                                            );


                                            tvHipAngle.setText(
                                                    bodyText
                                            );


                                            tvTorsoAngle.setVisibility(
                                                    View.GONE
                                            );


                                            tvPhase.setText(
                                                    phaseText
                                            );


                                            tvReps.setText(
                                                    repText
                                            );


                                            tvFeedback.setText(
                                                    "Feedback: "
                                                            + pushUpResult
                                                            .getFeedback()
                                            );


                                            break;
                                        }


                                        // =====================================
                                        // PLANK
                                        // =====================================

                                        case "PLANK": {


                                            PlankAnalyzer.PlankResult
                                                    plankResult =
                                                    plankAnalyzer.analyze(
                                                            landmarks
                                                            );

                                            currentHoldSeconds = plankResult.getHoldSeconds();
                                            currentReps = 0;


                                            String angleText =
                                                    String.format(
                                                            Locale.US,
                                                            "Body Angle: %.1f°",
                                                            plankResult
                                                                    .getBodyAngle()
                                                    );


                                            String statusText =
                                                    "Status: "
                                                            + plankResult
                                                            .getStatus();


                                            String holdText =
                                                    "Hold: "
                                                            + plankResult
                                                            .getHoldSeconds()
                                                            + "s";


                                            tvKneeAngle.setText(
                                                    angleText
                                            );


                                            tvHipAngle.setVisibility(
                                                    View.GONE
                                            );


                                            tvTorsoAngle.setVisibility(
                                                    View.GONE
                                            );


                                            tvPhase.setText(
                                                    statusText
                                            );


                                            tvReps.setText(
                                                    holdText
                                            );


                                            tvFeedback.setText(
                                                    "Feedback: "
                                                            + plankResult
                                                            .getFeedback()
                                            );


                                            break;
                                        }


                                        // =====================================
                                        // DEFAULT
                                        // =====================================

                                        default: {


                                            tvKneeAngle.setText(
                                                    "Angle: --°"
                                            );


                                            tvPhase.setText(
                                                    "Phase: --"
                                            );


                                            tvReps.setText(
                                                    "Reps: 0"
                                            );


                                            tvFeedback.setText(
                                                    "Feedback: --"
                                            );


                                            break;
                                        }
                                    }


                                    // =========================================
                                    // UPDATE DURATION + CALORIES
                                    // =========================================

                                    updateWorkoutStats();

                                });
                            }


                            // =================================================
                            // MEDIAPIPE ERROR
                            // =================================================

                            @Override
                            public void onError(
                                    String error) {


                                Log.e(
                                        TAG,
                                        "MEDIAPIPE ERROR: "
                                                + error
                                );
                            }
                        }
                );


        // =====================================================
        // CAMERA PERMISSION
        // =====================================================

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {


            startCamera();


        } else {


            ActivityCompat.requestPermissions(
                    this,

                    new String[]{
                            Manifest.permission.CAMERA
                    },

                    CAMERA_PERMISSION_CODE
            );
        }
    }


    // =========================================================
    // LOAD CURRENT USER WEIGHT FROM FIREBASE
    // =========================================================

    private void loadUserWeight() {


        FirebaseUser currentUser =
                firebaseAuth
                        .getCurrentUser();


        // =====================================================
        // USER NOT LOGGED IN
        // =====================================================

        if (currentUser == null) {


            userWeightLoaded = false;


            Log.e(
                    TAG,
                    "No authenticated Firebase user"
            );


            tvCalories.setText(
                    "Calories: user not found"
            );


            return;
        }


        // =====================================================
        // GET CURRENT USER UID
        // =====================================================

        String uid =
                currentUser
                        .getUid();


        Log.d(
                TAG,
                "Loading weight for UID: "
                        + uid
        );


        // =====================================================
        // FIREBASE PATH:
        //
        // users / UID / weight
        // =====================================================

        usersReference
                .child(uid)
                .child("weight")
                .addListenerForSingleValueEvent(

                        new ValueEventListener() {


                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {


                                // =================================
                                // WEIGHT EXISTS
                                // =================================

                                if (snapshot.exists()) {


                                    try {


                                        /*
                                         * IMPORTANT FIX:
                                         *
                                         * Do NOT use:
                                         *
                                         * snapshot.getValue(Number.class)
                                         *
                                         * Firebase does not support
                                         * deserializing directly into
                                         * Number.class.
                                         */

                                        Object rawValue =
                                                snapshot.getValue();


                                        // =============================
                                        // NULL VALUE
                                        // =============================

                                        if (rawValue == null) {


                                            userWeightLoaded =
                                                    false;


                                            Log.e(
                                                    TAG,
                                                    "Weight value is null"
                                            );


                                            tvCalories.setText(
                                                    "Calories: weight unavailable"
                                            );


                                            return;
                                        }


                                        // =============================
                                        // CONVERT FIREBASE VALUE
                                        // =============================

                                        double weight;


                                        /*
                                         * Firebase normally returns:
                                         *
                                         * 55   → Long
                                         * 55.5 → Double
                                         *
                                         * String fallback is also
                                         * supported.
                                         */

                                        if (rawValue instanceof Long) {


                                            weight =
                                                    ((Long) rawValue)
                                                            .doubleValue();


                                        } else if (rawValue instanceof Double) {


                                            weight =
                                                    (Double) rawValue;


                                        } else {


                                            weight =
                                                    Double.parseDouble(
                                                            rawValue.toString()
                                                    );
                                        }


                                        // =============================
                                        // VALID WEIGHT
                                        // =============================

                                        if (weight > 0) {


                                            userWeightKg =
                                                    weight;


                                            userWeightLoaded =
                                                    true;


                                            Log.d(
                                                    TAG,
                                                    "User weight loaded: "
                                                            + userWeightKg
                                                            + " kg"
                                            );


                                            /*
                                             * Refresh calories
                                             * immediately.
                                             */

                                            updateWorkoutStats();


                                        } else {


                                            userWeightLoaded =
                                                    false;


                                            Log.e(
                                                    TAG,
                                                    "Invalid weight value: "
                                                            + weight
                                            );


                                            tvCalories.setText(
                                                    "Calories: invalid weight"
                                            );
                                        }


                                    } catch (Exception e) {


                                        // =============================
                                        // CONVERSION ERROR
                                        // =============================

                                        userWeightLoaded =
                                                false;


                                        Log.e(
                                                TAG,
                                                "Error reading user weight",
                                                e
                                        );


                                        tvCalories.setText(
                                                "Calories: weight error"
                                        );
                                    }


                                } else {


                                    // =================================
                                    // WEIGHT DOES NOT EXIST
                                    // =================================

                                    userWeightLoaded =
                                            false;


                                    Log.e(
                                            TAG,
                                            "Weight not found for UID: "
                                                    + uid
                                    );


                                    tvCalories.setText(
                                            "Calories: weight not found"
                                    );
                                }
                            }


                            // =========================================
                            // DATABASE ERROR
                            // =========================================

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {


                                userWeightLoaded =
                                        false;


                                Log.e(
                                        TAG,
                                        "Failed to load user weight: "
                                                + error.getMessage()
                                );


                                tvCalories.setText(
                                        "Calories: unable to load weight"
                                );
                            }
                        }
                );
    }

    // =========================================================
// CHECK REQUIRED BODY LANDMARKS
// =========================================================

    private boolean isBodyVisible(
            List<NormalizedLandmark> landmarks,
            String exerciseType
    ) {

        if (landmarks == null || landmarks.size() < 33) {
            return false;
        }

        // MediaPipe Pose landmark indexes
        final int LEFT_SHOULDER = 11;
        final int RIGHT_SHOULDER = 12;

        final int LEFT_ELBOW = 13;
        final int RIGHT_ELBOW = 14;

        final int LEFT_WRIST = 15;
        final int RIGHT_WRIST = 16;

        final int LEFT_HIP = 23;
        final int RIGHT_HIP = 24;

        final int LEFT_KNEE = 25;
        final int RIGHT_KNEE = 26;

        final int LEFT_ANKLE = 27;
        final int RIGHT_ANKLE = 28;


        switch (exerciseType) {

            // =================================================
            // SQUAT
            // Shoulder + Hip + Knee + Ankle
            // =================================================

            case "SQUAT":

                return isLandmarkVisible(landmarks, LEFT_SHOULDER)
                        && isLandmarkVisible(landmarks, RIGHT_SHOULDER)

                        && isLandmarkVisible(landmarks, LEFT_HIP)
                        && isLandmarkVisible(landmarks, RIGHT_HIP)

                        && isLandmarkVisible(landmarks, LEFT_KNEE)
                        && isLandmarkVisible(landmarks, RIGHT_KNEE)

                        && isLandmarkVisible(landmarks, LEFT_ANKLE)
                        && isLandmarkVisible(landmarks, RIGHT_ANKLE);


            // =================================================
            // PUSH-UP
            // Shoulder + Elbow + Wrist + Hip + Ankle
            // =================================================

            case "PUSH_UP":

                return isLandmarkVisible(landmarks, LEFT_SHOULDER)
                        && isLandmarkVisible(landmarks, RIGHT_SHOULDER)

                        && isLandmarkVisible(landmarks, LEFT_ELBOW)
                        && isLandmarkVisible(landmarks, RIGHT_ELBOW)

                        && isLandmarkVisible(landmarks, LEFT_WRIST)
                        && isLandmarkVisible(landmarks, RIGHT_WRIST)

                        && isLandmarkVisible(landmarks, LEFT_HIP)
                        && isLandmarkVisible(landmarks, RIGHT_HIP)

                        && isLandmarkVisible(landmarks, LEFT_ANKLE)
                        && isLandmarkVisible(landmarks, RIGHT_ANKLE);


            // =================================================
            // PLANK
            // Shoulder + Hip + Ankle
            // =================================================

            case "PLANK":

                return isLandmarkVisible(landmarks, LEFT_SHOULDER)
                        && isLandmarkVisible(landmarks, RIGHT_SHOULDER)

                        && isLandmarkVisible(landmarks, LEFT_HIP)
                        && isLandmarkVisible(landmarks, RIGHT_HIP)

                        && isLandmarkVisible(landmarks, LEFT_ANKLE)
                        && isLandmarkVisible(landmarks, RIGHT_ANKLE);


            default:

                return false;
        }
    }
    // =========================================================
// CHECK SINGLE LANDMARK
// =========================================================

    private boolean isLandmarkVisible(
            List<NormalizedLandmark> landmarks,
            int index
    ) {

        NormalizedLandmark landmark =
                landmarks.get(index);

        float x = landmark.x();
        float y = landmark.y();

        // Landmark must be inside camera frame
        boolean insideFrame =
                x >= 0.0f
                        && x <= 1.0f
                        && y >= 0.0f
                        && y <= 1.0f;


        // Check MediaPipe visibility if available
        float visibility =
                landmark.visibility().orElse(0.0f);

        boolean visibleEnough =
                visibility >= 0.5f;


        return insideFrame && visibleEnough;
    }

    // =========================================================
    // UPDATE WORKOUT DURATION + CALORIES
    // =========================================================

    private void updateWorkoutStats() {


        long currentTime =
                System.currentTimeMillis();


        long durationSeconds =
                (currentTime - workoutStartTime)
                        / 1000;

        currentDurationSeconds = durationSeconds;

        // =====================================================
        // FORMAT DURATION
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


        tvDuration.setText(
                durationText
        );


        // =====================================================
        // WAIT FOR FIREBASE WEIGHT
        // =====================================================

        if (!userWeightLoaded) {


            if (tvCalories
                    .getText()
                    .toString()
                    .startsWith("Calories: loading")) {


                tvCalories.setText(
                        "Calories: loading..."
                );
            }


            return;
        }


        // =====================================================
        // CALCULATE CALORIES
        // =====================================================

        double calories =
                CalorieCalculator
                        .calculateCalories(
                                exerciseType,
                                userWeightKg,
                                durationSeconds
                        );

        currentCalories = calories;

        // =====================================================
        // FORMAT CALORIES
        // =====================================================

        String calorieText =
                String.format(
                        Locale.US,
                        "Calories: %.2f kcal",
                        calories
                );


        tvCalories.setText(
                calorieText
        );
    }

    private void resetWorkout() {

        // Reset final workout values
        currentReps = 0;
        currentHoldSeconds = 0;
        currentDurationSeconds = 0;
        currentCalories = 0.0;

        // Restart workout timer
        workoutStartTime = System.currentTimeMillis();


        // Reset analyzer based on exercise
        switch (exerciseType) {

            case "SQUAT":

                squatAnalyzer = new SquatAnalyzer();

                tvReps.setText("Reps: 0");
                tvPhase.setText("Phase: --");
                tvFeedback.setText("Feedback: READY");

                break;


            case "PUSH_UP":

                pushUpAnalyzer = new PushUpAnalyzer();

                tvReps.setText("Reps: 0");
                tvPhase.setText("Phase: --");
                tvFeedback.setText("Feedback: READY");

                break;


            case "PLANK":

                plankAnalyzer.reset();

                tvReps.setText("Hold: 0s");
                tvPhase.setText("Status: --");
                tvFeedback.setText("Feedback: READY");

                break;
        }


        // Reset displayed timer
        tvDuration.setText("Duration: 00:00");

        // Reset displayed calories
        if (userWeightLoaded) {

            tvCalories.setText(
                    "Calories: 0.00 kcal"
            );

        } else {

            tvCalories.setText(
                    "Calories: loading..."
            );
        }
    }

    // =========================================================
// FINISH WORKOUT
// =========================================================

    private void finishWorkout() {

        // Get latest duration and calories
        updateWorkoutStats();

        Intent intent =
                new Intent(
                        MainActivity.this,
                        WorkoutSummaryActivity.class
                );

        // Exercise type
        intent.putExtra(
                "EXERCISE_TYPE",
                exerciseType
        );

        // Squat / Push-Up repetitions
        intent.putExtra(
                "REPS",
                currentReps
        );

        // Plank hold duration
        intent.putExtra(
                "HOLD_SECONDS",
                currentHoldSeconds
        );

        // Total workout duration
        intent.putExtra(
                "DURATION_SECONDS",
                currentDurationSeconds
        );

        // Total calories
        intent.putExtra(
                "CALORIES",
                currentCalories
        );

        // Open summary page
        startActivity(intent);

        // Close camera workout page
        finish();
    }


    // =========================================================
    // START CAMERA
    // =========================================================

    private void startCamera() {


        ListenableFuture<ProcessCameraProvider>
                cameraProviderFuture =

                ProcessCameraProvider
                        .getInstance(
                                this
                        );


        cameraProviderFuture.addListener(

                () -> {


                    try {


                        ProcessCameraProvider cameraProvider =
                                cameraProviderFuture
                                        .get();


                        // =====================================
                        // CAMERA PREVIEW
                        // =====================================

                        Preview preview =
                                new Preview.Builder()
                                        .build();


                        preview.setSurfaceProvider(
                                previewView
                                        .getSurfaceProvider()
                        );


                        // =====================================
                        // IMAGE ANALYSIS
                        // =====================================

                        ImageAnalysis imageAnalysis =
                                new ImageAnalysis.Builder()

                                        .setBackpressureStrategy(
                                                ImageAnalysis
                                                        .STRATEGY_KEEP_ONLY_LATEST
                                        )

                                        .build();


                        // =====================================
                        // CAMERA FRAME ANALYZER
                        // =====================================

                        imageAnalysis.setAnalyzer(

                                cameraExecutor,

                                this::processImageProxy
                        );


                        // =====================================
                        // FRONT CAMERA
                        // =====================================

                        CameraSelector cameraSelector =
                                CameraSelector
                                        .DEFAULT_FRONT_CAMERA;


                        // =====================================
                        // REMOVE PREVIOUS CAMERA USE CASES
                        // =====================================

                        cameraProvider
                                .unbindAll();


                        // =====================================
                        // BIND CAMERA
                        // =====================================

                        cameraProvider
                                .bindToLifecycle(
                                        this,
                                        cameraSelector,
                                        preview,
                                        imageAnalysis
                                );


                    } catch (
                            ExecutionException e) {


                        Log.e(
                                TAG,
                                "Unable to start camera",
                                e
                        );


                    } catch (
                            InterruptedException e) {


                        Thread
                                .currentThread()
                                .interrupt();


                        Log.e(
                                TAG,
                                "Camera initialization interrupted",
                                e
                        );
                    }

                },

                ContextCompat
                        .getMainExecutor(
                                this
                        )
        );
    }


    // =========================================================
    // PROCESS CAMERA FRAME
    // =========================================================

    private void processImageProxy(
            ImageProxy imageProxy) {


        try {


            // =================================================
            // IMAGEPROXY → BITMAP
            // =================================================

            Bitmap bitmap =
                    imageProxy
                            .toBitmap();


            // =================================================
            // GET CAMERA ROTATION
            // =================================================

            int rotationDegrees =
                    imageProxy
                            .getImageInfo()
                            .getRotationDegrees();


            Matrix matrix =
                    new Matrix();


            // =================================================
            // ROTATE BITMAP
            // =================================================

            matrix.postRotate(
                    rotationDegrees
            );


            // =================================================
            // MIRROR FRONT CAMERA
            // =================================================

            matrix.postScale(
                    -1f,
                    1f
            );


            // =================================================
            // CREATE CORRECTED BITMAP
            // =================================================

            Bitmap correctedBitmap =
                    Bitmap.createBitmap(

                            bitmap,

                            0,

                            0,

                            bitmap.getWidth(),

                            bitmap.getHeight(),

                            matrix,

                            true
                    );


            // =================================================
            // SEND FRAME TO MEDIAPIPE
            // =================================================

            if (poseLandmarkerHelper != null) {


                poseLandmarkerHelper
                        .detectLiveStream(
                                correctedBitmap
                        );
            }


        } catch (Exception e) {


            Log.e(
                    TAG,
                    "Error processing camera frame",
                    e
            );


        } finally {


            /*
             * Every ImageProxy must be closed.
             */

            imageProxy.close();
        }
    }


    // =========================================================
    // CAMERA PERMISSION RESULT
    // =========================================================

    @Override
    public void onRequestPermissionsResult(

            int requestCode,

            @NonNull String[] permissions,

            @NonNull int[] grantResults) {


        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );


        if (requestCode ==
                CAMERA_PERMISSION_CODE) {


            if (grantResults.length > 0 &&

                    grantResults[0] ==

                            PackageManager
                                    .PERMISSION_GRANTED) {


                startCamera();


            } else {


                Log.e(
                        TAG,
                        "CAMERA PERMISSION DENIED"
                );
            }
        }
    }


    // =========================================================
    // CLEAN UP
    // =========================================================

    @Override
    protected void onDestroy() {


        super.onDestroy();


        // =====================================================
        // CLOSE MEDIAPIPE
        // =====================================================

        if (poseLandmarkerHelper != null) {


            poseLandmarkerHelper
                    .close();
        }


        // =====================================================
        // STOP CAMERA BACKGROUND THREAD
        // =====================================================

        if (cameraExecutor != null) {


            cameraExecutor
                    .shutdown();
        }
    }
}