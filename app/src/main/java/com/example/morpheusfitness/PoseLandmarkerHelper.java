package com.example.morpheusfitness;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

public class PoseLandmarkerHelper {

    // =========================================================
    // CONSTANTS
    // =========================================================

    private static final String TAG = "PoseLandmarkerHelper";

    private static final String MODEL_NAME =
            "pose_landmarker_lite.task";


    // =========================================================
    // MEDIAPIPE
    // =========================================================

    private PoseLandmarker poseLandmarker;


    // =========================================================
    // RESULT LISTENER
    // =========================================================

    private PoseResultListener listener;


    /**
     * Interface used to send MediaPipe results
     * back to MainActivity.
     */
    public interface PoseResultListener {

        void onResults(PoseLandmarkerResult result);

        void onError(String error);
    }


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public PoseLandmarkerHelper(
            Context context,
            PoseResultListener listener) {

        this.listener = listener;

        setupPoseLandmarker(context);
    }


    // =========================================================
    // SETUP MEDIAPIPE POSE LANDMARKER
    // =========================================================

    private void setupPoseLandmarker(Context context) {

        try {

            // ---------------------------------------------
            // Load pose_landmarker_lite.task from assets
            // ---------------------------------------------

            BaseOptions baseOptions =
                    BaseOptions.builder()
                            .setModelAssetPath(MODEL_NAME)
                            .build();


            // ---------------------------------------------
            // Configure Pose Landmarker
            // ---------------------------------------------

            PoseLandmarker.PoseLandmarkerOptions options =
                    PoseLandmarker.PoseLandmarkerOptions
                            .builder()

                            .setBaseOptions(baseOptions)

                            // Real-time camera mode
                            .setRunningMode(
                                    RunningMode.LIVE_STREAM
                            )

                            // Detect one person
                            .setNumPoses(1)

                            // Detection confidence
                            .setMinPoseDetectionConfidence(
                                    0.5f
                            )

                            // Pose presence confidence
                            .setMinPosePresenceConfidence(
                                    0.5f
                            )

                            // Tracking confidence
                            .setMinTrackingConfidence(
                                    0.5f
                            )

                            // Result callback
                            .setResultListener(
                                    this::returnResult
                            )

                            // Error callback
                            .setErrorListener(
                                    this::returnError
                            )

                            .build();


            // ---------------------------------------------
            // Create Pose Landmarker
            // ---------------------------------------------

            poseLandmarker =
                    PoseLandmarker.createFromOptions(
                            context,
                            options
                    );


            Log.d(
                    TAG,
                    "Pose Landmarker successfully initialized."
            );


        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Failed to initialize Pose Landmarker.",
                    e
            );


            if (listener != null) {

                listener.onError(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Failed to initialize Pose Landmarker."
                );
            }
        }
    }


    // =========================================================
    // RECEIVE BITMAP FROM CAMERAX
    // =========================================================

    public void detectLiveStream(Bitmap bitmap) {

        if (poseLandmarker == null) {

            Log.e(
                    TAG,
                    "Pose Landmarker is not initialized."
            );

            return;
        }


        try {

            // ---------------------------------------------
            // Convert Bitmap → MPImage
            // ---------------------------------------------

            MPImage mpImage =
                    new BitmapImageBuilder(bitmap)
                            .build();


            // ---------------------------------------------
            // Timestamp required by LIVE_STREAM mode
            // ---------------------------------------------

            long timestamp =
                    System.currentTimeMillis();


            // ---------------------------------------------
            // Send image to MediaPipe
            // ---------------------------------------------

            poseLandmarker.detectAsync(
                    mpImage,
                    timestamp
            );


        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Error while processing camera frame.",
                    e
            );


            if (listener != null) {

                listener.onError(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Error processing camera frame."
                );
            }
        }
    }


    // =========================================================
    // MEDIAPIPE RESULT
    // =========================================================

    private void returnResult(
            PoseLandmarkerResult result,
            MPImage input) {


        // ---------------------------------------------
        // Check if MediaPipe detected a person
        // ---------------------------------------------

        if (result != null &&
                !result.landmarks().isEmpty()) {


            int numberOfLandmarks =
                    result
                            .landmarks()
                            .get(0)
                            .size();


            Log.d(
                    TAG,
                    "POSE DETECTED: "
                            + numberOfLandmarks
                            + " landmarks"
            );

        } else {

            Log.d(
                    TAG,
                    "No pose detected."
            );
        }


        // ---------------------------------------------
        // Send result back to MainActivity
        // ---------------------------------------------

        if (listener != null) {

            listener.onResults(result);
        }
    }


    // =========================================================
    // MEDIAPIPE ERROR
    // =========================================================

    private void returnError(
            RuntimeException error) {


        String errorMessage;


        if (error.getMessage() != null) {

            errorMessage =
                    error.getMessage();

        } else {

            errorMessage =
                    "Unknown MediaPipe error.";
        }


        Log.e(
                TAG,
                errorMessage,
                error
        );


        // Send error to MainActivity

        if (listener != null) {

            listener.onError(
                    errorMessage
            );
        }
    }


    // =========================================================
    // GET POSE LANDMARKER
    // =========================================================

    public PoseLandmarker getPoseLandmarker() {

        return poseLandmarker;
    }


    // =========================================================
    // CLOSE MEDIAPIPE
    // =========================================================

    public void close() {

        if (poseLandmarker != null) {

            poseLandmarker.close();

            poseLandmarker = null;


            Log.d(
                    TAG,
                    "Pose Landmarker closed."
            );
        }
    }
}