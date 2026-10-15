package com.example.morpheusfitness;

public class CalorieCalculator {

    // =========================================================
    // INITIAL PROTOTYPE MET VALUES
    // These values can be refined later during evaluation.
    // =========================================================

    private static final double MET_SQUAT = 5.0;
    private static final double MET_PUSH_UP = 3.8;
    private static final double MET_PLANK = 3.0;


    // =========================================================
    // GET MET BASED ON EXERCISE
    // =========================================================

    public static double getMET(String exerciseType) {

        if (exerciseType == null) {
            return 0.0;
        }

        switch (exerciseType) {

            case "SQUAT":
                return MET_SQUAT;

            case "PUSH_UP":
                return MET_PUSH_UP;

            case "PLANK":
                return MET_PLANK;

            default:
                return 0.0;
        }
    }


    // =========================================================
    // CALCULATE CALORIES
    //
    // Formula:
    // Calories = MET × Weight(kg) × Duration(hours)
    // =========================================================

    public static double calculateCalories(
            String exerciseType,
            double weightKg,
            long durationSeconds) {

        if (weightKg <= 0 || durationSeconds <= 0) {
            return 0.0;
        }

        double met =
                getMET(exerciseType);

        double durationHours =
                durationSeconds / 3600.0;

        return met
                * weightKg
                * durationHours;
    }
}