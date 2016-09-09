package com.nanodegree.project1.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Koushick on 09-09-2016.
 */
public class MovieDBHelper extends SQLiteOpenHelper
{
    public static final String LOG_TAG = MovieDBHelper.class.getName();
    private static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 1;

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
        Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " +
                newVersion + ". OLD DATA WILL BE DESTROYED");

        // Drop the table
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieTableConstants.MOVIE_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieTableConstants.MOVIE_REVIEWS_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieTableConstants.MOVIE_TRAILERS_TABLE);

        // re-create database
        onCreate(sqLiteDatabase);
    }

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        String CREATE_TABLE = "CREATE "+MovieTableConstants.MOVIE_TABLE+" ( "+
                MovieTableConstants.ID + " INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, "+
                MovieTableConstants.HEADING + " TEXT, " +
                MovieTableConstants.RELEASE_DATE + " TEXT, " +
                MovieTableConstants.USER_RATING + " INT, " +
                MovieTableConstants.SYNOPSIS + " TEXT, "+
                MovieTableConstants.THUMBNAIL + " BLOB "+
                " ); ";
        Log.d(LOG_TAG,CREATE_TABLE);
        sqLiteDatabase.execSQL(CREATE_TABLE);

        String CREATE_TABLE_TRAILERS = "CREATE " + MovieTableConstants.MOVIE_TRAILERS_TABLE + " ( " +
                MovieTableConstants.MOVIE_TRAILER_ID + " INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                MovieTableConstants.ID + " INTEGER NOT NULL, "+
                MovieTableConstants.NAME + " TEXT, " +
                MovieTableConstants.KEY + " TEXT, "+
                "FOREIGN KEY ( "+MovieTableConstants.ID +" ) "+" REFERENCES "+MovieTableConstants.MOVIE_TABLE+" ( "+MovieTableConstants.ID +" ) "+
                " ); ";
        Log.d(LOG_TAG,CREATE_TABLE_TRAILERS);
        sqLiteDatabase.execSQL(CREATE_TABLE_TRAILERS);

        String CREATE_TABLE_REVIEWS = "CREATE " + MovieTableConstants.MOVIE_REVIEWS_TABLE + " ( " +
                MovieTableConstants.MOVIE_REVIEWS_TABLE + " INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                MovieTableConstants.ID + " INTEGER NOT NULL, "+
                MovieTableConstants.AUTHOR + " TEXT, "+
                MovieTableConstants.CONTENT + " TEXT, "+
                "FOREIGN KEY ( "+MovieTableConstants.ID +" ) "+" REFERENCES "+MovieTableConstants.MOVIE_TABLE+" ( "+MovieTableConstants.ID +" ) "+
                " ); ";
        Log.d(LOG_TAG,CREATE_TABLE_REVIEWS);
        sqLiteDatabase.execSQL(CREATE_TABLE_REVIEWS);
    }
}
