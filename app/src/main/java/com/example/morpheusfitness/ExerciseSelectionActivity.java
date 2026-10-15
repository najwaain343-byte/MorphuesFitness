
package com.example.morpheusfitness;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ExerciseSelectionActivity extends AppCompatActivity {

    private EditText etSearchExercise;
    private TextView btnClearSearch;

    private View cardSquat;
    private View cardPushUp;
    private View cardPlank;
    private View layoutNoResults;

    private Button btnSquat;
    private Button btnPushUp;
    private Button btnPlank;
    private Button btnWorkoutHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_exercise_selection);

        // CONNECT XML
        etSearchExercise = findViewById(R.id.etSearchExercise);
        btnClearSearch = findViewById(R.id.btnClearSearch);

        cardSquat = findViewById(R.id.cardSquat);
        cardPushUp = findViewById(R.id.cardPushUp);
        cardPlank = findViewById(R.id.cardPlank);

        layoutNoResults = findViewById(R.id.layoutNoResults);

        btnSquat = findViewById(R.id.btnSquat);
        btnPushUp = findViewById(R.id.btnPushUp);
        btnPlank = findViewById(R.id.btnPlank);
        btnWorkoutHistory = findViewById(R.id.btnWorkoutHistory);
        Button btnBackHome = findViewById(R.id.btnBackHome);

        // EXERCISE NAVIGATION
        btnSquat.setOnClickListener(v -> openExercise("SQUAT"));

        btnPushUp.setOnClickListener(v -> openExercise("PUSH_UP"));

        btnPlank.setOnClickListener(v -> openExercise("PLANK"));

        // WORKOUT HISTORY
        btnWorkoutHistory.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ExerciseSelectionActivity.this,
                    WorkoutHistoryActivity.class
            );
            startActivity(intent);
        });

        btnBackHome.setOnClickListener(v -> {
            finish();
        });

        // SEARCH FUNCTION
        etSearchExercise.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {
                filterExercises(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // CLEAR SEARCH
        btnClearSearch.setOnClickListener(v -> {
            etSearchExercise.setText("");
        });

        // SHOW ALL ON FIRST LOAD
        filterExercises("");
    }

    private void openExercise(String exerciseType) {

        Intent intent = new Intent(
                ExerciseSelectionActivity.this,
                MainActivity.class
        );

        intent.putExtra("EXERCISE_TYPE", exerciseType);

        startActivity(intent);
    }

    private void filterExercises(String searchText) {

        String query = searchText
                .trim()
                .toLowerCase(Locale.ROOT);

        // SQUAT: name or category
        boolean showSquat =
                "squat".contains(query)
                        || "lower body".contains(query)
                        || "legs".contains(query);

        // PUSH-UP: name or category
        boolean showPushUp =
                "push-up".contains(query)
                        || "push up".contains(query)
                        || "pushup".contains(query)
                        || "upper body".contains(query)
                        || "arms".contains(query);

        // PLANK: name or category
        boolean showPlank =
                "plank".contains(query)
                        || "core stability".contains(query)
                        || "core".contains(query)
                        || "abs".contains(query);

        cardSquat.setVisibility(
                showSquat ? View.VISIBLE : View.GONE
        );

        cardPushUp.setVisibility(
                showPushUp ? View.VISIBLE : View.GONE
        );

        cardPlank.setVisibility(
                showPlank ? View.VISIBLE : View.GONE
        );

        // NO RESULTS
        boolean noResults =
                !showSquat && !showPushUp && !showPlank;

        layoutNoResults.setVisibility(
                noResults ? View.VISIBLE : View.GONE
        );

        // CLEAR ICON
        btnClearSearch.setVisibility(
                query.isEmpty() ? View.GONE : View.VISIBLE
        );
    }
}
