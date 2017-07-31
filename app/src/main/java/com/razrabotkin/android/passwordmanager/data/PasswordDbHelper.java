package com.razrabotkin.android.passwordmanager.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.razrabotkin.android.passwordmanager.data.PasswordContract.PasswordEntry;

/**
 * Created by Володя on 25.06.2017.
 */

public class PasswordDbHelper extends SQLiteOpenHelper {

    /** Имя файла базы данных */
    private static final String DATABASE_NAME = "password_manager.db";

    /** Версия базы данных. При изменении схемы базы данных версию нужно увеличивать на 1. */
    private static final int DATABASE_VERSION = 1;

    public PasswordDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создаём строку, которая содержит SQL-выражение для создания таблицы
        // CREATE TABLE passwords (id INTEGER PRIMARY KEY AUTOINCREMENT, login TEXT NOT NULL, password TEXT NOT NULL);
        String SQL_CREATE_PASSWORDS_TABLE = "CREATE TABLE " + PasswordEntry.TABLE_NAME + "("
                + PasswordEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PasswordEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + PasswordEntry.COLUMN_LOGIN + " TEXT NOT NULL, "
                + PasswordEntry.COLUMN_PASSWORD + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_PASSWORDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
