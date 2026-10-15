package com.example.morpheusfitness;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutAdapter
        extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {


    // =========================================================
    // DELETE LISTENER
    // =========================================================

    public interface OnWorkoutDeleteListener {

        void onDeleteWorkout(
                Workout workout
        );
    }


    private final List<Workout> workoutList;

    private final OnWorkoutDeleteListener deleteListener;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public WorkoutAdapter(
            List<Workout> workoutList,
            OnWorkoutDeleteListener deleteListener
    ) {

        this.workoutList =
                workoutList;

        this.deleteListener =
                deleteListener;
    }


    // =========================================================
    // CREATE VIEW
    // =========================================================

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_workout,
                                parent,
                                false
                        );

        return new WorkoutViewHolder(
                view
        );
    }


    // =========================================================
    // BIND
    // =========================================================

    @Override
    public void onBindViewHolder(
            @NonNull WorkoutViewHolder holder,
            int position
    ) {

        Workout workout =
                workoutList.get(position);


        // =====================================================
        // EXERCISE NAME
        // =====================================================

        String exerciseName =
                workout.getExerciseType();


        if (exerciseName == null) {

            exerciseName =
                    "WORKOUT";

        } else if (
                exerciseName.equalsIgnoreCase(
                        "PUSH_UP"
                )
        ) {

            exerciseName =
                    "PUSH-UP";

        } else {

            exerciseName =
                    exerciseName.replace(
                            "_",
                            " "
                    );
        }


        holder.tvItemExercise.setText(
                exerciseName.toUpperCase(
                        Locale.getDefault()
                )
        );


        // =====================================================
        // DATE + TIME
        // =====================================================

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "dd MMM\nhh:mm a",
                        Locale.getDefault()
                );


        String dateText =
                dateFormat.format(
                        new Date(
                                workout.getTimestamp()
                        )
                );


        holder.tvItemDate.setText(
                dateText.toUpperCase(
                        Locale.getDefault()
                )
        );


        // =====================================================
        // PERFORMANCE
        // =====================================================

        if (
                workout.getExerciseType() != null
                        &&
                        workout.getExerciseType()
                                .equalsIgnoreCase(
                                        "PLANK"
                                )
        ) {

            holder.tvItemPerformance.setText(
                    String.format(
                            Locale.US,
                            "%d SEC",
                            workout.getHoldSeconds()
                    )
            );

        } else {

            holder.tvItemPerformance.setText(
                    String.format(
                            Locale.US,
                            "%d REPS",
                            workout.getReps()
                    )
            );
        }


        // =====================================================
        // DURATION
        // =====================================================

        long durationSeconds =
                workout.getDurationSeconds();

        long minutes =
                durationSeconds / 60;

        long seconds =
                durationSeconds % 60;


        holder.tvItemDuration.setText(
                String.format(
                        Locale.US,
                        "%02d:%02d",
                        minutes,
                        seconds
                )
        );


        // =====================================================
        // CALORIES
        // =====================================================

        holder.tvItemCalories.setText(
                String.format(
                        Locale.US,
                        "%.2f KCAL",
                        workout.getCalories()
                )
        );


        // =====================================================
        // DELETE
        // =====================================================

        holder.btnDeleteWorkout
                .setOnClickListener(v -> {

                    if (deleteListener != null) {

                        deleteListener
                                .onDeleteWorkout(
                                        workout
                                );
                    }
                });
    }


    // =========================================================
    // ITEM COUNT
    // =========================================================

    @Override
    public int getItemCount() {

        return workoutList.size();
    }


    // =========================================================
    // VIEW HOLDER
    // =========================================================

    static class WorkoutViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvItemExercise;
        TextView tvItemDate;
        TextView tvItemPerformance;
        TextView tvItemDuration;
        TextView tvItemCalories;

        TextView btnDeleteWorkout;


        public WorkoutViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);


            tvItemExercise =
                    itemView.findViewById(
                            R.id.tvItemExercise
                    );

            tvItemDate =
                    itemView.findViewById(
                            R.id.tvItemDate
                    );

            tvItemPerformance =
                    itemView.findViewById(
                            R.id.tvItemPerformance
                    );

            tvItemDuration =
                    itemView.findViewById(
                            R.id.tvItemDuration
                    );

            tvItemCalories =
                    itemView.findViewById(
                            R.id.tvItemCalories
                    );

            btnDeleteWorkout =
                    itemView.findViewById(
                            R.id.btnDeleteWorkout
                    );
        }
    }
}