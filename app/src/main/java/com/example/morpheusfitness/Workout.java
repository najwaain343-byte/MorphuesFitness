package com.example.morpheusfitness;

public class Workout {

    private String workoutId;
    private String exerciseType;

    private int reps;
    private long holdSeconds;
    private long durationSeconds;

    private double calories;

    private long timestamp;


    // =========================================================
    // REQUIRED EMPTY CONSTRUCTOR FOR FIREBASE
    // =========================================================

    public Workout() {
    }


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Workout(
            String exerciseType,
            int reps,
            long holdSeconds,
            long durationSeconds,
            double calories,
            long timestamp
    ) {

        this.exerciseType = exerciseType;
        this.reps = reps;
        this.holdSeconds = holdSeconds;
        this.durationSeconds = durationSeconds;
        this.calories = calories;
        this.timestamp = timestamp;
    }


    // =========================================================
    // WORKOUT ID
    // =========================================================

    public String getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(String workoutId) {
        this.workoutId = workoutId;
    }


    // =========================================================
    // EXERCISE TYPE
    // =========================================================

    public String getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }


    // =========================================================
    // REPS
    // =========================================================

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }


    // =========================================================
    // HOLD SECONDS
    // =========================================================

    public long getHoldSeconds() {
        return holdSeconds;
    }

    public void setHoldSeconds(long holdSeconds) {
        this.holdSeconds = holdSeconds;
    }


    // =========================================================
    // DURATION
    // =========================================================

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }


    // =========================================================
    // CALORIES
    // =========================================================

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }


    // =========================================================
    // TIMESTAMP
    // =========================================================

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}