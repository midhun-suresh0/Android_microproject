package com.example.dailyhabittracker;

import android.app.Activity;
import android.os.Bundle;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText etHabit;
    private Button btnAdd;
    private TextView tvProgress;
    private ProgressBar progressBar;
    private LinearLayout habitContainer;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        etHabit = findViewById(R.id.etHabit);
        btnAdd = findViewById(R.id.btnAdd);
        tvProgress = findViewById(R.id.tvProgress);
        progressBar = findViewById(R.id.progressBar);
        habitContainer = findViewById(R.id.habitContainer);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addHabit();
            }
        });

        loadHabits();
    }

    private void addHabit() {
        String habitName = etHabit.getText().toString().trim();

        if (habitName.isEmpty()) {
            etHabit.setError("Please enter a habit");
            etHabit.requestFocus();
            return;
        }

        boolean inserted = databaseHelper.addHabit(habitName);

        if (inserted) {
            etHabit.setText("");
            Toast.makeText(
                    this,
                    "Habit added successfully!",
                    Toast.LENGTH_SHORT
            ).show();

            loadHabits();
        } else {
            Toast.makeText(
                    this,
                    "Unable to add habit",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void loadHabits() {
        habitContainer.removeAllViews();

        Cursor cursor = databaseHelper.getAllHabits();

        try {
            if (cursor.getCount() == 0) {
                TextView emptyMessage = new TextView(this);

                emptyMessage.setText(
                        "No habits added yet.\nAdd your first habit above!"
                );

                emptyMessage.setTextSize(16);
                emptyMessage.setTextColor(Color.GRAY);
                emptyMessage.setGravity(Gravity.CENTER);
                emptyMessage.setPadding(12, 30, 12, 30);

                habitContainer.addView(emptyMessage);
            }

            while (cursor.moveToNext()) {
                final int habitId = cursor.getInt(0);
                String habitName = cursor.getString(1);
                String completedDate = cursor.getString(2);

                final boolean isCompleted =
                        databaseHelper.isCompletedToday(completedDate);

                addHabitRow(habitId, habitName, isCompleted);
            }
        } finally {
            cursor.close();
        }

        updateProgress();
    }

    private void addHabitRow(
            final int habitId,
            String habitName,
            boolean isCompleted) {

        LinearLayout row = new LinearLayout(this);

        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(12, 8, 8, 8);
        row.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        rowParams.setMargins(0, 0, 0, 10);
        row.setLayoutParams(rowParams);

        CheckBox checkBox = new CheckBox(this);

        checkBox.setText(habitName);
        checkBox.setTextSize(16);
        checkBox.setTextColor(Color.rgb(30, 41, 59));
        checkBox.setButtonTintList(
                android.content.res.ColorStateList.valueOf(
                        Color.rgb(37, 99, 235)
                )
        );

        checkBox.setChecked(isCompleted);

        LinearLayout.LayoutParams checkParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        row.addView(checkBox, checkParams);

        Button deleteButton = new Button(this);

        deleteButton.setText("Delete");
        deleteButton.setTextSize(12);
        deleteButton.setAllCaps(false);

        row.addView(
                deleteButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        checkBox.setOnCheckedChangeListener(
                (buttonView, checked) -> {

                    boolean updated =
                            databaseHelper.updateHabitStatus(
                                    habitId,
                                    checked
                            );

                    if (updated) {
                        updateProgress();

                        Toast.makeText(
                                MainActivity.this,
                                checked
                                        ? "Habit completed!"
                                        : "Habit marked incomplete",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                                MainActivity.this,
                                "Unable to update habit",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadHabits();
                    }
                }
        );

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean deleted =
                        databaseHelper.deleteHabit(habitId);

                if (deleted) {
                    Toast.makeText(
                            MainActivity.this,
                            "Habit deleted",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadHabits();
                } else {
                    Toast.makeText(
                            MainActivity.this,
                            "Unable to delete habit",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });

        habitContainer.addView(row);
    }

    private void updateProgress() {

        int total = databaseHelper.getTotalHabits();
        int completed = databaseHelper.getCompletedHabits();

        int percentage = 0;

        if (total > 0) {
            percentage = (completed * 100) / total;
        }

        tvProgress.setText(
                completed + " of " + total +
                        " habits completed (" + percentage + "%)"
        );

        progressBar.setProgress(percentage);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && habitContainer != null) {
            loadHabits();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}