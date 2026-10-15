package com.example.morpheusfitness;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class WorkoutHistoryActivity
        extends AppCompatActivity {


    // =========================================================
    // FIREBASE URL
    // =========================================================

    private static final String DATABASE_URL =
            "https://morpheusfitness-f1650-default-rtdb.asia-southeast1.firebasedatabase.app";


    // =========================================================
    // XML
    // =========================================================

    private RecyclerView recyclerWorkoutHistory;

    private TextView tvEmptyHistory;
    private TextView tvEmptySelectedDay;

    private TextView tvHistoryMonth;

    private TextView tvWeeklyWorkouts;
    private TextView tvWeeklyDuration;
    private TextView tvWeeklyCalories;

    private TextView tvSelectedDayTitle;
    private TextView tvSelectedDate;

    private LinearLayout layoutWeekSelector;

    private Button btnHistoryBack;


    // =========================================================
    // WORKOUT DATA
    // =========================================================

    private final List<Workout> allWorkouts =
            new ArrayList<>();

    private final List<Workout> selectedDayWorkouts =
            new ArrayList<>();

    private WorkoutAdapter workoutAdapter;


    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseAuth firebaseAuth;

    private DatabaseReference workoutsReference;

    private String currentUid;


    // =========================================================
    // DATE
    // =========================================================

    private Calendar selectedDate =
            Calendar.getInstance();

    private Calendar startOfCurrentWeek;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.activity_workout_history
        );


        // =====================================================
        // CONNECT XML
        // =====================================================

        recyclerWorkoutHistory =
                findViewById(
                        R.id.recyclerWorkoutHistory
                );

        tvEmptyHistory =
                findViewById(
                        R.id.tvEmptyHistory
                );

        tvEmptySelectedDay =
                findViewById(
                        R.id.tvEmptySelectedDay
                );

        tvHistoryMonth =
                findViewById(
                        R.id.tvHistoryMonth
                );

        tvWeeklyWorkouts =
                findViewById(
                        R.id.tvWeeklyWorkouts
                );

        tvWeeklyDuration =
                findViewById(
                        R.id.tvWeeklyDuration
                );

        tvWeeklyCalories =
                findViewById(
                        R.id.tvWeeklyCalories
                );

        tvSelectedDayTitle =
                findViewById(
                        R.id.tvSelectedDayTitle
                );

        tvSelectedDate =
                findViewById(
                        R.id.tvSelectedDate
                );

        layoutWeekSelector =
                findViewById(
                        R.id.layoutWeekSelector
                );

        btnHistoryBack =
                findViewById(
                        R.id.btnHistoryBack
                );


        // =====================================================
        // RECYCLER VIEW
        // =====================================================

        recyclerWorkoutHistory.setLayoutManager(
                new LinearLayoutManager(this)
        );


        workoutAdapter =
                new WorkoutAdapter(
                        selectedDayWorkouts,

                        workout ->
                                showDeleteConfirmation(
                                        workout
                                )
                );


        recyclerWorkoutHistory.setAdapter(
                workoutAdapter
        );


        // =====================================================
        // CURRENT WEEK
        // =====================================================

        startOfCurrentWeek =
                getStartOfWeek(
                        Calendar.getInstance()
                );


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


        currentUid =
                currentUser.getUid();


        workoutsReference =
                FirebaseDatabase
                        .getInstance(
                                DATABASE_URL
                        )
                        .getReference(
                                "workouts"
                        );


        // =====================================================
        // INITIAL UI
        // =====================================================

        updateMonthTitle();

        createWeekSelector();

        updateSelectedDateHeader();


        // =====================================================
        // LOAD WORKOUTS
        // =====================================================

        loadWorkoutHistory();


        // =====================================================
        // BACK
        // =====================================================

        btnHistoryBack
                .setOnClickListener(
                        v -> finish()
                );
    }


    // =========================================================
    // LOAD WORKOUT HISTORY
    // =========================================================

    private void loadWorkoutHistory() {

        workoutsReference
                .child(currentUid)
                .addListenerForSingleValueEvent(

                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot
                            ) {

                                allWorkouts.clear();


                                for (
                                        DataSnapshot workoutSnapshot
                                        : snapshot.getChildren()
                                ) {

                                    Workout workout =
                                            workoutSnapshot
                                                    .getValue(
                                                            Workout.class
                                                    );


                                    if (workout != null) {

                                        // IMPORTANT:
                                        // Firebase push key becomes
                                        // the workout ID used for delete.

                                        workout.setWorkoutId(
                                                workoutSnapshot.getKey()
                                        );


                                        allWorkouts.add(
                                                workout
                                        );
                                    }
                                }


                                // =================================
                                // NEWEST FIRST
                                // =================================

                                Collections.sort(
                                        allWorkouts,

                                        (workout1, workout2) ->

                                                Long.compare(
                                                        workout2.getTimestamp(),
                                                        workout1.getTimestamp()
                                                )
                                );


                                // =================================
                                // EMPTY HISTORY
                                // =================================

                                if (allWorkouts.isEmpty()) {

                                    tvEmptyHistory.setVisibility(
                                            View.VISIBLE
                                    );

                                    recyclerWorkoutHistory.setVisibility(
                                            View.GONE
                                    );

                                    tvEmptySelectedDay.setVisibility(
                                            View.GONE
                                    );


                                    tvWeeklyWorkouts.setText(
                                            "0"
                                    );

                                    tvWeeklyDuration.setText(
                                            "00:00"
                                    );

                                    tvWeeklyCalories.setText(
                                            "0.00"
                                    );


                                    createWeekSelector();

                                } else {

                                    tvEmptyHistory.setVisibility(
                                            View.GONE
                                    );


                                    updateWeeklySummary();

                                    filterWorkoutsForSelectedDate();

                                    createWeekSelector();
                                }
                            }


                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error
                            ) {

                                Toast.makeText(
                                        WorkoutHistoryActivity.this,
                                        "Unable to load workout history.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }


    // =========================================================
    // DELETE CONFIRMATION
    // =========================================================

    private void showDeleteConfirmation(
            Workout workout
    ) {

        String exerciseName =
                workout.getExerciseType();


        if (exerciseName == null) {

            exerciseName =
                    "this workout";

        } else {

            exerciseName =
                    exerciseName
                            .replace(
                                    "_",
                                    "-"
                            );
        }


        new AlertDialog.Builder(this)

                .setTitle(
                        "Delete Workout?"
                )

                .setMessage(
                        "Are you sure you want to delete "
                                + exerciseName
                                + " from your workout history?"
                )

                .setNegativeButton(
                        "CANCEL",
                        (dialog, which) ->
                                dialog.dismiss()
                )

                .setPositiveButton(
                        "DELETE",
                        (dialog, which) ->
                                deleteWorkout(
                                        workout
                                )
                )

                .show();
    }


    // =========================================================
    // DELETE WORKOUT FROM FIREBASE
    // =========================================================

    private void deleteWorkout(
            Workout workout
    ) {

        String workoutId =
                workout.getWorkoutId();


        if (
                workoutId == null
                        ||
                        workoutId.trim().isEmpty()
        ) {

            Toast.makeText(
                    this,
                    "Unable to identify workout.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        workoutsReference
                .child(currentUid)
                .child(workoutId)
                .removeValue()

                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                WorkoutHistoryActivity.this,
                                "Workout deleted successfully.",
                                Toast.LENGTH_SHORT
                        ).show();


                        // Reload Firebase so:
                        // - card disappears
                        // - weekly total changes
                        // - calories update
                        // - yellow date dot updates

                        loadWorkoutHistory();

                    } else {

                        String message =
                                "Unable to delete workout.";


                        if (
                                task.getException() != null
                                        &&
                                        task.getException()
                                                .getMessage() != null
                        ) {

                            message =
                                    task
                                            .getException()
                                            .getMessage();
                        }


                        Toast.makeText(
                                WorkoutHistoryActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }


    // =========================================================
    // CREATE WEEK SELECTOR
    // =========================================================

    private void createWeekSelector() {

        layoutWeekSelector.removeAllViews();


        Calendar day =
                (Calendar)
                        startOfCurrentWeek.clone();


        for (int i = 0; i < 7; i++) {

            final Calendar date =
                    (Calendar)
                            day.clone();


            LinearLayout dayContainer =
                    new LinearLayout(this);


            dayContainer.setOrientation(
                    LinearLayout.VERTICAL
            );

            dayContainer.setGravity(
                    Gravity.CENTER
            );


            LinearLayout.LayoutParams containerParams =
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                    );


            dayContainer.setLayoutParams(
                    containerParams
            );


            // =================================================
            // DAY NAME
            // =================================================

            TextView tvDayName =
                    new TextView(this);


            SimpleDateFormat dayFormat =
                    new SimpleDateFormat(
                            "EEE",
                            Locale.getDefault()
                    );


            tvDayName.setText(
                    dayFormat
                            .format(
                                    date.getTime()
                            )
                            .toUpperCase(
                                    Locale.getDefault()
                            )
            );


            tvDayName.setTextSize(
                    8
            );

            tvDayName.setTypeface(
                    null,
                    Typeface.BOLD
            );

            tvDayName.setGravity(
                    Gravity.CENTER
            );


            // =================================================
            // DAY NUMBER
            // =================================================

            TextView tvDayNumber =
                    new TextView(this);


            tvDayNumber.setText(
                    String.valueOf(
                            date.get(
                                    Calendar.DAY_OF_MONTH
                            )
                    )
            );


            tvDayNumber.setTextSize(
                    16
            );

            tvDayNumber.setTypeface(
                    null,
                    Typeface.BOLD
            );

            tvDayNumber.setGravity(
                    Gravity.CENTER
            );


            LinearLayout.LayoutParams numberParams =
                    new LinearLayout.LayoutParams(
                            dpToPx(38),
                            dpToPx(38)
                    );


            numberParams.topMargin =
                    dpToPx(4);


            tvDayNumber.setLayoutParams(
                    numberParams
            );


            // =================================================
            // WORKOUT DOT
            // =================================================

            TextView workoutDot =
                    new TextView(this);


            workoutDot.setText(
                    "•"
            );

            workoutDot.setTextSize(
                    16
            );

            workoutDot.setGravity(
                    Gravity.CENTER
            );


            // =================================================
            // SELECTED DATE
            // =================================================

            if (
                    isSameDay(
                            date,
                            selectedDate
                    )
            ) {

                tvDayName.setTextColor(
                        Color.parseColor(
                                "#FFC400"
                        )
                );


                tvDayNumber.setTextColor(
                        Color.parseColor(
                                "#111111"
                        )
                );


                tvDayNumber.setBackgroundColor(
                        Color.parseColor(
                                "#FFC400"
                        )
                );

            } else {

                tvDayName.setTextColor(
                        Color.parseColor(
                                "#777C7C"
                        )
                );


                tvDayNumber.setTextColor(
                        Color.WHITE
                );


                tvDayNumber.setBackgroundColor(
                        Color.TRANSPARENT
                );
            }


            // =================================================
            // WORKOUT INDICATOR
            // =================================================

            if (
                    hasWorkoutOnDate(
                            date
                    )
            ) {

                workoutDot.setTextColor(
                        Color.parseColor(
                                "#FFC400"
                        )
                );

            } else {

                workoutDot.setTextColor(
                        Color.TRANSPARENT
                );
            }


            // =================================================
            // CLICK DATE
            // =================================================

            dayContainer.setOnClickListener(v -> {

                selectedDate =
                        (Calendar)
                                date.clone();


                createWeekSelector();

                updateSelectedDateHeader();

                filterWorkoutsForSelectedDate();
            });


            dayContainer.addView(
                    tvDayName
            );

            dayContainer.addView(
                    tvDayNumber
            );

            dayContainer.addView(
                    workoutDot
            );


            layoutWeekSelector.addView(
                    dayContainer
            );


            day.add(
                    Calendar.DAY_OF_MONTH,
                    1
            );
        }
    }


    // =========================================================
    // FILTER WORKOUTS BY SELECTED DATE
    // =========================================================

    private void filterWorkoutsForSelectedDate() {

        selectedDayWorkouts.clear();


        for (
                Workout workout
                : allWorkouts
        ) {

            Calendar workoutDate =
                    Calendar.getInstance();


            workoutDate.setTimeInMillis(
                    workout.getTimestamp()
            );


            if (
                    isSameDay(
                            workoutDate,
                            selectedDate
                    )
            ) {

                selectedDayWorkouts.add(
                        workout
                );
            }
        }


        Collections.sort(
                selectedDayWorkouts,

                (workout1, workout2) ->

                        Long.compare(
                                workout2.getTimestamp(),
                                workout1.getTimestamp()
                        )
        );


        workoutAdapter
                .notifyDataSetChanged();


        if (
                selectedDayWorkouts.isEmpty()
        ) {

            recyclerWorkoutHistory.setVisibility(
                    View.GONE
            );

            tvEmptySelectedDay.setVisibility(
                    View.VISIBLE
            );

        } else {

            recyclerWorkoutHistory.setVisibility(
                    View.VISIBLE
            );

            tvEmptySelectedDay.setVisibility(
                    View.GONE
            );
        }
    }


    // =========================================================
    // WEEKLY SUMMARY
    // =========================================================

    private void updateWeeklySummary() {

        int workoutCount =
                0;

        long totalDuration =
                0;

        double totalCalories =
                0.0;


        Calendar weekEnd =
                (Calendar)
                        startOfCurrentWeek.clone();


        weekEnd.add(
                Calendar.DAY_OF_MONTH,
                7
        );


        for (
                Workout workout
                : allWorkouts
        ) {

            long timestamp =
                    workout.getTimestamp();


            if (
                    timestamp >=
                            startOfCurrentWeek
                                    .getTimeInMillis()

                            &&

                            timestamp <
                                    weekEnd
                                            .getTimeInMillis()
            ) {

                workoutCount++;


                totalDuration +=
                        workout
                                .getDurationSeconds();


                totalCalories +=
                        workout
                                .getCalories();
            }
        }


        tvWeeklyWorkouts.setText(
                String.valueOf(
                        workoutCount
                )
        );


        long minutes =
                totalDuration / 60;

        long seconds =
                totalDuration % 60;


        tvWeeklyDuration.setText(
                String.format(
                        Locale.US,
                        "%02d:%02d",
                        minutes,
                        seconds
                )
        );


        tvWeeklyCalories.setText(
                String.format(
                        Locale.US,
                        "%.2f",
                        totalCalories
                )
        );
    }


    // =========================================================
    // SELECTED DATE HEADER
    // =========================================================

    private void updateSelectedDateHeader() {

        Calendar today =
                Calendar.getInstance();


        if (
                isSameDay(
                        selectedDate,
                        today
                )
        ) {

            tvSelectedDayTitle.setText(
                    "TODAY"
            );

        } else {

            SimpleDateFormat titleFormat =
                    new SimpleDateFormat(
                            "EEEE",
                            Locale.getDefault()
                    );


            tvSelectedDayTitle.setText(
                    titleFormat
                            .format(
                                    selectedDate
                                            .getTime()
                            )
                            .toUpperCase(
                                    Locale.getDefault()
                            )
            );
        }


        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "EEEE, d MMMM",
                        Locale.getDefault()
                );


        tvSelectedDate.setText(
                dateFormat.format(
                        selectedDate.getTime()
                )
        );


        updateMonthTitle();
    }


    // =========================================================
    // MONTH TITLE
    // =========================================================

    private void updateMonthTitle() {

        SimpleDateFormat monthFormat =
                new SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.getDefault()
                );


        tvHistoryMonth.setText(
                monthFormat
                        .format(
                                selectedDate.getTime()
                        )
                        .toUpperCase(
                                Locale.getDefault()
                        )
        );
    }


    // =========================================================
    // CHECK IF DATE HAS WORKOUT
    // =========================================================

    private boolean hasWorkoutOnDate(
            Calendar date
    ) {

        for (
                Workout workout
                : allWorkouts
        ) {

            Calendar workoutDate =
                    Calendar.getInstance();


            workoutDate.setTimeInMillis(
                    workout.getTimestamp()
            );


            if (
                    isSameDay(
                            workoutDate,
                            date
                    )
            ) {

                return true;
            }
        }


        return false;
    }


    // =========================================================
    // SAME DAY
    // =========================================================

    private boolean isSameDay(
            Calendar date1,
            Calendar date2
    ) {

        return

                date1.get(
                        Calendar.YEAR
                )

                        ==

                        date2.get(
                                Calendar.YEAR
                        )

                        &&

                        date1.get(
                                Calendar.DAY_OF_YEAR
                        )

                                ==

                                date2.get(
                                        Calendar.DAY_OF_YEAR
                                );
    }


    // =========================================================
    // GET MONDAY OF CURRENT WEEK
    // =========================================================

    private Calendar getStartOfWeek(
            Calendar calendar
    ) {

        Calendar start =
                (Calendar)
                        calendar.clone();


        start.setFirstDayOfWeek(
                Calendar.MONDAY
        );


        int currentDay =
                start.get(
                        Calendar.DAY_OF_WEEK
                );


        int difference =
                currentDay
                        - Calendar.MONDAY;


        if (
                difference < 0
        ) {

            difference +=
                    7;
        }


        start.add(
                Calendar.DAY_OF_MONTH,
                -difference
        );


        start.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        start.set(
                Calendar.MINUTE,
                0
        );

        start.set(
                Calendar.SECOND,
                0
        );

        start.set(
                Calendar.MILLISECOND,
                0
        );


        return start;
    }


    // =========================================================
    // DP TO PX
    // =========================================================

    private int dpToPx(
            int dp
    ) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;


        return Math.round(
                dp * density
        );
    }
}