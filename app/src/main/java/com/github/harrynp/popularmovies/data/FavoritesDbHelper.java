package com.github.harrynp.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by harry on 11/5/2017.
 */

public class FavoritesDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 1;

    public FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_FAVORITES_TABLE =
                "CREATE TABLE " +
                        FavoritesContract.FavoritesEntry.TABLE_NAME             + " (" +
                        FavoritesContract.FavoritesEntry._ID                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        FavoritesContract.FavoritesEntry.COLUMN_TITLE           + " TEXT NOT NULL, "                 +
                        FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID        + " INTEGER NOT NULL,"                  +
                        FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH     + " TEXT NOT NULL, "                    +
                        FavoritesContract.FavoritesEntry.COLUMN_BACKDROP_PATH   + " TEXT NOT NULL, "                    +
                        FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW        + " TEXT NOT NULL, "                    +
                        FavoritesContract.FavoritesEntry.COLUMN_VOTE_AVERAGE    + " REAL NOT NULL, "                    +
                        FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE    + " TEXT NOT NULL, "                    +
                        FavoritesContract.FavoritesEntry.COLUMN_VOTE_COUNT      + " INTEGER NOT NULL, "                    +
                        " UNIQUE (" + FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoritesContract.FavoritesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
