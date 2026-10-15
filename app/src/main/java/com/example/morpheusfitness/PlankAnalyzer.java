package com.example.morpheusfitness;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.util.List;

public class PlankAnalyzer {

    // =========================================================
    // MEDIAPIPE LANDMARK INDEXES
    // =========================================================

    private static final int LEFT_SHOULDER = 11;
    private static final int RIGHT_SHOULDER = 12;

    private static final int LEFT_HIP = 23;
    private static final int RIGHT_HIP = 24;

    private static final int LEFT_ANKLE = 27;
    private static final int RIGHT_ANKLE = 28;


    // =========================================================
    // INITIAL PROTOTYPE THRESHOLDS
    // Tune later using actual testing data
    // =========================================================

    /*
     * Shoulder → Hip → Ankle should approach 180 degrees
     * when the body is straight.
     */
    private static final double GOOD_FORM_MIN = 160.0;

    /*
     * Hysteresis threshold.
     *
     * Once a valid plank starts, posture must fall below
     * this value before the timer is paused.
     *
     * This helps prevent the timer from rapidly switching
     * ON/OFF when the detected angle fluctuates near 160°.
     */
    private static final double BAD_FORM_THRESHOLD = 157.0;


    // =========================================================
    // TIMER VARIABLES
    // =========================================================

    /*
     * Time when the current valid plank interval started.
     */
    private long holdStartTime = 0;


    /*
     * Total valid plank time accumulated from previous
     * intervals.
     */
    private long totalHoldTime = 0;


    /*
     * True only while the user is currently holding
     * a valid plank.
     */
    private boolean holding = false;


    // =========================================================
    // ANALYZE PLANK
    // =========================================================

    public PlankResult analyze(
            List<NormalizedLandmark> landmarks) {

        // =====================================================
        // NO BODY DETECTED
        // =====================================================

        if (landmarks == null ||
                landmarks.size() < 33) {

            pauseTimer();

            return new PlankResult(
                    0.0,
                    "NO POSE",
                    getCurrentHoldSeconds(),
                    "BODY NOT DETECTED"
            );
        }


        // =====================================================
        // CALCULATE BODY ALIGNMENT
        // =====================================================

        double bodyAngle =
                calculateAverageBodyAngle(
                        landmarks
                );


        String status;
        String feedback;


        // =====================================================
        // GOOD PLANK POSTURE
        // =====================================================

        if (!holding &&
                bodyAngle >= GOOD_FORM_MIN) {

            /*
             * User has entered a valid plank position.
             */

            startTimer();

            status =
                    "GOOD FORM";

            feedback =
                    "MAINTAIN POSITION";
        }


        // =====================================================
        // CURRENTLY HOLDING A VALID PLANK
        // =====================================================

        else if (holding) {

            /*
             * We use a slightly lower threshold here.
             *
             * Example:
             *
             * Start plank:
             * bodyAngle >= 160°
             *
             * Pause plank:
             * bodyAngle < 157°
             *
             * This prevents tiny MediaPipe fluctuations such as
             * 159.8°, 160.2°, 159.9° from repeatedly pausing and
             * restarting the timer.
             */

            if (bodyAngle < BAD_FORM_THRESHOLD) {

                pauseTimer();

                status =
                        "INCORRECT";

                feedback =
                        "KEEP YOUR BODY STRAIGHT";
            }

            else {

                status =
                        "GOOD FORM";

                feedback =
                        "MAINTAIN POSITION";
            }
        }


        // =====================================================
        // INCORRECT PLANK POSTURE
        // =====================================================

        else {

            pauseTimer();

            status =
                    "INCORRECT";

            feedback =
                    "KEEP YOUR BODY STRAIGHT";
        }


        // =====================================================
        // RETURN RESULT
        // =====================================================

        return new PlankResult(
                bodyAngle,
                status,
                getCurrentHoldSeconds(),
                feedback
        );
    }


    // =========================================================
    // START HOLD TIMER
    // =========================================================

    private void startTimer() {

        if (!holding) {

            holding = true;

            holdStartTime =
                    System.currentTimeMillis();
        }
    }


    // =========================================================
    // PAUSE HOLD TIMER
    // =========================================================

    private void pauseTimer() {

        if (holding) {

            totalHoldTime +=
                    System.currentTimeMillis()
                            - holdStartTime;

            holding = false;

            holdStartTime = 0;
        }
    }


    // =========================================================
    // GET CURRENT HOLD TIME
    // =========================================================

    private long getCurrentHoldSeconds() {

        long currentTime =
                totalHoldTime;


        /*
         * If the user is currently holding a valid plank,
         * include the active interval.
         */

        if (holding) {

            currentTime +=
                    System.currentTimeMillis()
                            - holdStartTime;
        }


        return currentTime / 1000;
    }


    // =========================================================
    // PAUSE FROM MAIN ACTIVITY
    // =========================================================

    /*
     * MainActivity calls this when the required body
     * landmarks are not sufficiently visible.
     *
     * The accumulated hold time is preserved.
     */

    public void pause() {

        pauseTimer();
    }


    // =========================================================
    // RESET PLANK
    // =========================================================

    /*
     * Used by RESET WORKOUT.
     *
     * Unlike pause(), reset() removes all accumulated
     * plank time.
     */

    public void reset() {

        holdStartTime = 0;

        totalHoldTime = 0;

        holding = false;
    }


    // =========================================================
    // BODY ALIGNMENT
    //
    // Shoulder → Hip → Ankle
    //
    // A straight body should produce an angle
    // approaching 180 degrees.
    // =========================================================

    public static double calculateAverageBodyAngle(
            List<NormalizedLandmark> landmarks) {

        if (landmarks == null ||
                landmarks.size() < 33) {

            return 0.0;
        }


        // -----------------------------------------------------
        // LEFT SIDE
        // -----------------------------------------------------

        double leftAngle =
                calculateAngle(
                        landmarks.get(LEFT_SHOULDER),
                        landmarks.get(LEFT_HIP),
                        landmarks.get(LEFT_ANKLE)
                );


        // -----------------------------------------------------
        // RIGHT SIDE
        // -----------------------------------------------------

        double rightAngle =
                calculateAngle(
                        landmarks.get(RIGHT_SHOULDER),
                        landmarks.get(RIGHT_HIP),
                        landmarks.get(RIGHT_ANKLE)
                );


        // -----------------------------------------------------
        // SAFETY FALLBACK
        // -----------------------------------------------------

        if (leftAngle == 0.0) {

            return rightAngle;
        }


        if (rightAngle == 0.0) {

            return leftAngle;
        }


        // -----------------------------------------------------
        // AVERAGE BOTH SIDES
        // -----------------------------------------------------

        return (
                leftAngle +
                        rightAngle
        ) / 2.0;
    }


    // =========================================================
    // GENERAL 3-POINT ANGLE CALCULATION
    // =========================================================

    private static double calculateAngle(
            NormalizedLandmark pointA,
            NormalizedLandmark pointB,
            NormalizedLandmark pointC) {

        double angle =
                Math.toDegrees(

                        Math.atan2(
                                pointC.y() - pointB.y(),
                                pointC.x() - pointB.x()
                        )

                                -

                                Math.atan2(
                                        pointA.y() - pointB.y(),
                                        pointA.x() - pointB.x()
                                )
                );


        angle =
                Math.abs(angle);


        if (angle > 180.0) {

            angle =
                    360.0 - angle;
        }


        return angle;
    }


    // =========================================================
    // RESULT CLASS
    // =========================================================

    public static class PlankResult {

        private final double bodyAngle;

        private final String status;

        private final long holdSeconds;

        private final String feedback;


        public PlankResult(
                double bodyAngle,
                String status,
                long holdSeconds,
                String feedback) {

            this.bodyAngle =
                    bodyAngle;

            this.status =
                    status;

            this.holdSeconds =
                    holdSeconds;

            this.feedback =
                    feedback;
        }


        public double getBodyAngle() {

            return bodyAngle;
        }


        public String getStatus() {

            return status;
        }


        public long getHoldSeconds() {

            return holdSeconds;
        }


        public String getFeedback() {

            return feedback;
        }
    }
}