package com.example.morpheusfitness;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.util.List;

public class PushUpAnalyzer {

    // =========================================================
    // MEDIAPIPE LANDMARK INDEXES
    // =========================================================

    private static final int LEFT_SHOULDER = 11;
    private static final int RIGHT_SHOULDER = 12;

    private static final int LEFT_ELBOW = 13;
    private static final int RIGHT_ELBOW = 14;

    private static final int LEFT_WRIST = 15;
    private static final int RIGHT_WRIST = 16;

    private static final int LEFT_HIP = 23;
    private static final int RIGHT_HIP = 24;

    private static final int LEFT_ANKLE = 27;
    private static final int RIGHT_ANKLE = 28;


    // =========================================================
    // INITIAL PROTOTYPE THRESHOLDS
    // Tune later during actual testing
    // =========================================================

    // Arm is considered extended
    private static final double UP_ANGLE = 160.0;

    // User must reach this depth before a rep can count
    private static final double DOWN_ANGLE = 90.0;

    // Shoulder → Hip → Ankle should be close to straight
    private static final double GOOD_BODY_ALIGNMENT = 160.0;

    // Ignore small MediaPipe landmark fluctuations
    private static final double MOVEMENT_TOLERANCE = 2.0;


    // =========================================================
    // PUSH-UP PHASE
    // =========================================================

    public enum PushUpPhase {

        UP,
        DESCENDING,
        DOWN,
        ASCENDING
    }


    // =========================================================
    // STATE VARIABLES
    // =========================================================

    private PushUpPhase currentPhase =
            PushUpPhase.UP;

    private double previousAngle =
            180.0;

    private int repetitionCount =
            0;

    private boolean reachedDown =
            false;


    // =========================================================
    // ANALYZE PUSH-UP
    // =========================================================

    public PushUpResult analyze(
            List<NormalizedLandmark> landmarks) {

        // =====================================================
        // INVALID / MISSING POSE
        // =====================================================

        if (landmarks == null ||
                landmarks.size() < 33) {

            return new PushUpResult(
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

        double elbowAngle =
                calculateAverageElbowAngle(
                        landmarks
                );

        double bodyAngle =
                calculateAverageBodyAngle(
                        landmarks
                );


        // =====================================================
        // DETERMINE PUSH-UP PHASE
        // =====================================================

        // -----------------------------------------------------
        // UP POSITION
        // -----------------------------------------------------

        if (elbowAngle >= UP_ANGLE) {

            /*
             * A repetition is counted only if:
             *
             * 1. User previously reached DOWN position.
             * 2. User returns to UP position.
             */

            if (reachedDown) {

                repetitionCount++;

                reachedDown =
                        false;
            }

            currentPhase =
                    PushUpPhase.UP;
        }


        // -----------------------------------------------------
        // DOWN POSITION
        // -----------------------------------------------------

        else if (elbowAngle <= DOWN_ANGLE) {

            currentPhase =
                    PushUpPhase.DOWN;

            reachedDown =
                    true;
        }


        // -----------------------------------------------------
        // BETWEEN UP AND DOWN
        // -----------------------------------------------------

        else {

            double angleDifference =
                    elbowAngle - previousAngle;


            // Moving downward:
            // elbow angle is decreasing.

            if (angleDifference <
                    -MOVEMENT_TOLERANCE) {

                currentPhase =
                        PushUpPhase.DESCENDING;
            }


            // Moving upward:
            // elbow angle is increasing.

            else if (angleDifference >
                    MOVEMENT_TOLERANCE) {

                currentPhase =
                        PushUpPhase.ASCENDING;
            }


            /*
             * If the change is within ±2 degrees,
             * treat it as MediaPipe landmark noise
             * and keep the previous phase.
             */
        }


        // =====================================================
        // POSTURE FEEDBACK
        // =====================================================

        String feedback;


        // -----------------------------------------------------
        // BODY NOT STRAIGHT
        // -----------------------------------------------------

        if (bodyAngle <
                GOOD_BODY_ALIGNMENT) {

            feedback =
                    "KEEP YOUR BODY STRAIGHT";
        }


        // -----------------------------------------------------
        // BODY ALIGNMENT ACCEPTABLE
        // -----------------------------------------------------

        else {

            switch (currentPhase) {

                case UP:

                    feedback =
                            "READY";

                    break;


                case DESCENDING:

                    feedback =
                            "LOWER YOUR BODY";

                    break;


                case DOWN:

                    feedback =
                            "GOOD DEPTH";

                    break;


                case ASCENDING:

                    feedback =
                            "PUSH UP";

                    break;


                default:

                    feedback =
                            "--";

                    break;
            }
        }


        // =====================================================
        // SAVE CURRENT ANGLE
        // =====================================================

        previousAngle =
                elbowAngle;


        // =====================================================
        // RETURN RESULT
        // =====================================================

        return new PushUpResult(
                elbowAngle,
                bodyAngle,
                currentPhase,
                repetitionCount,
                feedback
        );
    }


    // =========================================================
    // LEFT ELBOW ANGLE
    // Shoulder → Elbow → Wrist
    // =========================================================

    public static double calculateLeftElbowAngle(
            List<NormalizedLandmark> landmarks) {

        if (landmarks == null ||
                landmarks.size() < 33) {

            return 0.0;
        }


        return calculateAngle(
                landmarks.get(LEFT_SHOULDER),
                landmarks.get(LEFT_ELBOW),
                landmarks.get(LEFT_WRIST)
        );
    }


    // =========================================================
    // RIGHT ELBOW ANGLE
    // Shoulder → Elbow → Wrist
    // =========================================================

    public static double calculateRightElbowAngle(
            List<NormalizedLandmark> landmarks) {

        if (landmarks == null ||
                landmarks.size() < 33) {

            return 0.0;
        }


        return calculateAngle(
                landmarks.get(RIGHT_SHOULDER),
                landmarks.get(RIGHT_ELBOW),
                landmarks.get(RIGHT_WRIST)
        );
    }


    // =========================================================
    // AVERAGE ELBOW ANGLE
    // =========================================================

    public static double calculateAverageElbowAngle(
            List<NormalizedLandmark> landmarks) {

        double leftAngle =
                calculateLeftElbowAngle(
                        landmarks
                );

        double rightAngle =
                calculateRightElbowAngle(
                        landmarks
                );


        if (leftAngle == 0.0) {

            return rightAngle;
        }


        if (rightAngle == 0.0) {

            return leftAngle;
        }


        return (
                leftAngle +
                        rightAngle
        ) / 2.0;
    }


    // =========================================================
    // BODY ALIGNMENT
    // Shoulder → Hip → Ankle
    // =========================================================

    public static double calculateAverageBodyAngle(
            List<NormalizedLandmark> landmarks) {

        if (landmarks == null ||
                landmarks.size() < 33) {

            return 0.0;
        }


        double leftBodyAngle =
                calculateAngle(
                        landmarks.get(LEFT_SHOULDER),
                        landmarks.get(LEFT_HIP),
                        landmarks.get(LEFT_ANKLE)
                );


        double rightBodyAngle =
                calculateAngle(
                        landmarks.get(RIGHT_SHOULDER),
                        landmarks.get(RIGHT_HIP),
                        landmarks.get(RIGHT_ANKLE)
                );


        if (leftBodyAngle == 0.0) {

            return rightBodyAngle;
        }


        if (rightBodyAngle == 0.0) {

            return leftBodyAngle;
        }


        return (
                leftBodyAngle +
                        rightBodyAngle
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

    public static class PushUpResult {

        private final double elbowAngle;

        private final double bodyAngle;

        private final PushUpPhase phase;

        private final int repetitions;

        private final String feedback;


        public PushUpResult(
                double elbowAngle,
                double bodyAngle,
                PushUpPhase phase,
                int repetitions,
                String feedback) {

            this.elbowAngle =
                    elbowAngle;

            this.bodyAngle =
                    bodyAngle;

            this.phase =
                    phase;

            this.repetitions =
                    repetitions;

            this.feedback =
                    feedback;
        }


        public double getElbowAngle() {

            return elbowAngle;
        }


        public double getBodyAngle() {

            return bodyAngle;
        }


        public PushUpPhase getPhase() {

            return currentPhaseValue();
        }


        private PushUpPhase currentPhaseValue() {

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