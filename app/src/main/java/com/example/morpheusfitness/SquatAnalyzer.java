package com.example.morpheusfitness;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.util.List;

public class SquatAnalyzer {

    // =========================================================
    // MEDIAPIPE LANDMARK INDEXES
    // =========================================================

    private static final int LEFT_SHOULDER = 11;
    private static final int RIGHT_SHOULDER = 12;

    private static final int LEFT_HIP = 23;
    private static final int RIGHT_HIP = 24;

    private static final int LEFT_KNEE = 25;
    private static final int RIGHT_KNEE = 26;

    private static final int LEFT_ANKLE = 27;
    private static final int RIGHT_ANKLE = 28;


    // =========================================================
    // INITIAL PROTOTYPE THRESHOLDS
    // Tune these later during testing
    // =========================================================

    private static final double STANDING_ANGLE = 160.0;
    private static final double BOTTOM_ANGLE = 100.0;

    /*
     * Torso inclination threshold.
     *
     * 90 degrees = approximately upright.
     * Smaller values = more forward lean.
     *
     * This is an INITIAL prototype threshold only.
     */
    private static final double MIN_TORSO_ANGLE = 45.0;

    // Ignore very small angle changes caused by landmark noise
    private static final double MOVEMENT_TOLERANCE = 2.0;


    // =========================================================
    // SQUAT PHASE
    // =========================================================

    public enum SquatPhase {
        STANDING,
        ECCENTRIC,
        BOTTOM,
        CONCENTRIC
    }


    // =========================================================
    // STATE VARIABLES
    // =========================================================

    private SquatPhase currentPhase =
            SquatPhase.STANDING;

    private double previousAngle = 180.0;

    private int repetitionCount = 0;

    private boolean reachedBottom = false;


    // =========================================================
    // ANALYZE SQUAT
    // =========================================================

    public SquatResult analyze(
            List<NormalizedLandmark> landmarks) {

        if (landmarks == null ||
                landmarks.size() < 33) {

            return new SquatResult(
                    0.0,
                    0.0,
                    0.0,
                    currentPhase,
                    repetitionCount,
                    "BODY NOT DETECTED"
            );
        }


        // =====================================================
        // CALCULATE ANGLES
        // =====================================================

        double kneeAngle =
                calculateAverageKneeAngle(landmarks);

        double hipAngle =
                calculateAverageHipAngle(landmarks);

        double torsoAngle =
                calculateTorsoAngle(landmarks);


        // =====================================================
        // DETERMINE SQUAT PHASE
        // =====================================================

        if (kneeAngle >= STANDING_ANGLE) {

            // Count rep only after user reached bottom
            // and returned to standing.

            if (reachedBottom) {

                repetitionCount++;

                reachedBottom = false;
            }

            currentPhase =
                    SquatPhase.STANDING;
        }

        else if (kneeAngle <= BOTTOM_ANGLE) {

            currentPhase =
                    SquatPhase.BOTTOM;

            reachedBottom = true;
        }

        else {

            double angleDifference =
                    kneeAngle - previousAngle;

            // Moving downward
            if (angleDifference < -MOVEMENT_TOLERANCE) {

                currentPhase =
                        SquatPhase.ECCENTRIC;
            }

            // Moving upward
            else if (angleDifference > MOVEMENT_TOLERANCE) {

                currentPhase =
                        SquatPhase.CONCENTRIC;
            }

            // Small changes are treated as landmark noise.
            // Keep the current phase.
        }


        // =====================================================
        // REAL-TIME FEEDBACK
        // =====================================================

        String feedback;


        /*
         * Priority 1:
         * Check torso posture while performing the squat.
         *
         * We don't apply this while standing because some
         * natural movement before starting could otherwise
         * trigger unnecessary warnings.
         */

        if (currentPhase != SquatPhase.STANDING
                && torsoAngle < MIN_TORSO_ANGLE) {

            feedback =
                    "KEEP YOUR BACK STRAIGHT";
        }


        // -----------------------------------------------------
        // STANDING
        // -----------------------------------------------------

        else if (currentPhase == SquatPhase.STANDING) {

            feedback =
                    "READY";
        }


        // -----------------------------------------------------
        // MOVING DOWN
        // -----------------------------------------------------

        else if (currentPhase == SquatPhase.ECCENTRIC) {

            feedback =
                    "KEEP GOING DOWN";
        }


        // -----------------------------------------------------
        // BOTTOM
        // -----------------------------------------------------

        else if (currentPhase == SquatPhase.BOTTOM) {

            /*
             * Reaching the knee-angle threshold means
             * sufficient depth for this prototype.
             *
             * Torso posture has already been checked above.
             */

            feedback =
                    "GOOD FORM";
        }


        // -----------------------------------------------------
        // MOVING UP
        // -----------------------------------------------------

        else if (currentPhase == SquatPhase.CONCENTRIC) {

            feedback =
                    "PUSH UP";
        }


        else {

            feedback =
                    "--";
        }


        // =====================================================
        // SAVE CURRENT ANGLE
        // =====================================================

        previousAngle =
                kneeAngle;


        // =====================================================
        // RETURN RESULT
        // =====================================================

        return new SquatResult(
                kneeAngle,
                hipAngle,
                torsoAngle,
                currentPhase,
                repetitionCount,
                feedback
        );
    }


    // =========================================================
    // LEFT KNEE ANGLE
    // Hip → Knee → Ankle
    // =========================================================

    public static double calculateLeftKneeAngle(
            List<NormalizedLandmark> landmarks) {

        if (landmarks == null ||
                landmarks.size() < 33) {

            return 0.0;
        }

        return calculateAngle(
                landmarks.get(LEFT_HIP),
                landmarks.get(LEFT_KNEE),
                landmarks.get(LEFT_ANKLE)
        );
    }


    // =========================================================
    // RIGHT KNEE ANGLE
    // Hip → Knee → Ankle
    // =========================================================

    public static double calculateRightKneeAngle(
            List<NormalizedLandmark> landmarks) {

        if (landmarks == null ||
                landmarks.size() < 33) {

            return 0.0;
        }

        return calculateAngle(
                landmarks.get(RIGHT_HIP),
                landmarks.get(RIGHT_KNEE),
                landmarks.get(RIGHT_ANKLE)
        );
    }


    // =========================================================
    // AVERAGE KNEE ANGLE
    // =========================================================

    public static double calculateAverageKneeAngle(
            List<NormalizedLandmark> landmarks) {

        double leftAngle =
                calculateLeftKneeAngle(landmarks);

        double rightAngle =
                calculateRightKneeAngle(landmarks);

        if (leftAngle == 0.0) {
            return rightAngle;
        }

        if (rightAngle == 0.0) {
            return leftAngle;
        }

        return (leftAngle + rightAngle) / 2.0;
    }


    // =========================================================
    // AVERAGE HIP ANGLE
    // Shoulder → Hip → Knee
    // =========================================================

    public static double calculateAverageHipAngle(
            List<NormalizedLandmark> landmarks) {

        if (landmarks == null ||
                landmarks.size() < 33) {

            return 0.0;
        }


        double leftHipAngle =
                calculateAngle(
                        landmarks.get(LEFT_SHOULDER),
                        landmarks.get(LEFT_HIP),
                        landmarks.get(LEFT_KNEE)
                );


        double rightHipAngle =
                calculateAngle(
                        landmarks.get(RIGHT_SHOULDER),
                        landmarks.get(RIGHT_HIP),
                        landmarks.get(RIGHT_KNEE)
                );


        if (leftHipAngle == 0.0) {
            return rightHipAngle;
        }

        if (rightHipAngle == 0.0) {
            return leftHipAngle;
        }


        return (
                leftHipAngle +
                        rightHipAngle
        ) / 2.0;
    }


    // =========================================================
    // TORSO ANGLE
    //
    // Measures torso inclination relative to horizontal.
    //
    // Mid shoulder
    //      ●
    //      |
    //      |
    //      ● Mid hip
    //
    // Around 90° = upright torso.
    // Smaller angle = more forward lean.
    // =========================================================

    public static double calculateTorsoAngle(
            List<NormalizedLandmark> landmarks) {

        if (landmarks == null ||
                landmarks.size() < 33) {

            return 0.0;
        }


        NormalizedLandmark leftShoulder =
                landmarks.get(LEFT_SHOULDER);

        NormalizedLandmark rightShoulder =
                landmarks.get(RIGHT_SHOULDER);

        NormalizedLandmark leftHip =
                landmarks.get(LEFT_HIP);

        NormalizedLandmark rightHip =
                landmarks.get(RIGHT_HIP);


        // Midpoint between left and right shoulders

        double shoulderX =
                (leftShoulder.x()
                        + rightShoulder.x()) / 2.0;

        double shoulderY =
                (leftShoulder.y()
                        + rightShoulder.y()) / 2.0;


        // Midpoint between left and right hips

        double hipX =
                (leftHip.x()
                        + rightHip.x()) / 2.0;

        double hipY =
                (leftHip.y()
                        + rightHip.y()) / 2.0;


        double deltaX =
                shoulderX - hipX;

        double deltaY =
                shoulderY - hipY;


        /*
         * atan2 gives torso orientation.
         *
         * abs() makes left/right camera orientation
         * easier to handle.
         */

        double torsoAngle =
                Math.toDegrees(
                        Math.atan2(
                                Math.abs(deltaY),
                                Math.abs(deltaX)
                        )
                );


        return torsoAngle;
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

    public static class SquatResult {

        private final double kneeAngle;

        private final double hipAngle;

        private final double torsoAngle;

        private final SquatPhase phase;

        private final int repetitions;

        private final String feedback;


        public SquatResult(
                double kneeAngle,
                double hipAngle,
                double torsoAngle,
                SquatPhase phase,
                int repetitions,
                String feedback) {

            this.kneeAngle =
                    kneeAngle;

            this.hipAngle =
                    hipAngle;

            this.torsoAngle =
                    torsoAngle;

            this.phase =
                    phase;

            this.repetitions =
                    repetitions;

            this.feedback =
                    feedback;
        }


        public double getKneeAngle() {

            return kneeAngle;
        }


        public double getHipAngle() {

            return hipAngle;
        }


        public double getTorsoAngle() {

            return torsoAngle;
        }


        public SquatPhase getPhase() {

            return phase;
        }


        public int getRepetitions() {

            return repetitions;
        }


        public String getFeedback() {

            return feedback;
        }
    }
}