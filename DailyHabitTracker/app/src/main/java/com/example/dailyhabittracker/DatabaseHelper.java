package com.example.dailyhabittracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HabitTracker.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_HABITS = "habits";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_COMPLETED_DATE = "completed_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_HABITS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_COMPLETED_DATE + " TEXT)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABITS);
        onCreate(db);
    }

    private String getToday() {
        return new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault()
        ).format(new Date());
    }

    public boolean addHabit(String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name.trim());
        values.putNull(COLUMN_COMPLETED_DATE);

        long result = db.insert(TABLE_HABITS, null, values);

        return result != -1;
    }

    public Cursor getAllHabits() {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_HABITS,
                new String[]{
                        COLUMN_ID,
                        COLUMN_NAME,
                        COLUMN_COMPLETED_DATE
                },
                null,
                null,
                null,
                null,
                COLUMN_ID + " DESC"
        );
    }

    public boolean updateHabitStatus(int id, boolean completed) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        if (completed) {
            values.put(COLUMN_COMPLETED_DATE, getToday());
        } else {
            values.putNull(COLUMN_COMPLETED_DATE);
        }

        int result = db.update(
                TABLE_HABITS,
                values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        return result > 0;
    }

    public boolean deleteHabit(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                TABLE_HABITS,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        return result > 0;
    }

    public boolean isCompletedToday(String completedDate) {
        return completedDate != null &&
                completedDate.equals(getToday());
    }

    public int getTotalHabits() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_HABITS,
                null
        );

        int total = 0;

        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }

        cursor.close();

        return total;
    }

    public int getCompletedHabits() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_HABITS +
                        " WHERE " + COLUMN_COMPLETED_DATE + " = ?",
                new String[]{getToday()}
        );

        int completed = 0;

        if (cursor.moveToFirst()) {
            completed = cursor.getInt(0);
        }

        cursor.close();

        return completed;
    }
}