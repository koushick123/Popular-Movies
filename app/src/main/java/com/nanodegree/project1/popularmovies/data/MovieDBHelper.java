package com.nanodegree.project1.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Koushick on 09-09-2016.
 */
public class MovieDBHelper extends SQLiteOpenHelper
{
    public static String LOG_TAG = MovieDBHelper.class.getName();
    public static String DATABASE_NAME = "favorites.db";
    private static int DATABASE_VERSION = 1;
    public static String DB_PATH = "/data/data/com/nanodegree/project1/popularmovies/databases/";
    private final Context myContext;

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
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if(dbExist){
            this.getWritableDatabase();
            /*try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }*/
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DATABASE_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        }catch(SQLiteException e){
            e.printStackTrace();
        }

        if(checkDB != null){
            checkDB.close();
        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DATABASE_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DATABASE_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }
        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        String CREATE_TABLE = "CREATE TABLE "+MovieTableConstants.MOVIE_TABLE+" ( "+
                MovieTableConstants.ID + " INTEGER NOT NULL PRIMARY KEY , "+
                MovieTableConstants.MOVIE_ID + " INTEGER NOT NULL , " +
                MovieTableConstants.HEADING + " TEXT, " +
                MovieTableConstants.RELEASE_DATE + " TEXT, " +
                MovieTableConstants.USER_RATING + " INTEGER, " +
                MovieTableConstants.SYNOPSIS + " TEXT, "+
                MovieTableConstants.THUMBNAIL + " BLOB "+
                " ); ";
        Log.d(LOG_TAG,CREATE_TABLE);
        sqLiteDatabase.execSQL(CREATE_TABLE);

        String CREATE_TABLE_TRAILERS = "CREATE TABLE " + MovieTableConstants.MOVIE_TRAILERS_TABLE + " ( " +
                MovieTableConstants.MOVIE_TRAILER_ID + " INTEGER NOT NULL PRIMARY KEY , " +
                MovieTableConstants.ID + " INTEGER NOT NULL, "+
                MovieTableConstants.NAME + " TEXT, " +
                MovieTableConstants.KEY + " TEXT, "+
                "FOREIGN KEY ( "+MovieTableConstants.ID +" ) "+" REFERENCES "+MovieTableConstants.MOVIE_TABLE+" ( "+MovieTableConstants.ID +" ) "+
                " ); ";
        Log.d(LOG_TAG,CREATE_TABLE_TRAILERS);
        sqLiteDatabase.execSQL(CREATE_TABLE_TRAILERS);

        String CREATE_TABLE_REVIEWS = "CREATE TABLE " + MovieTableConstants.MOVIE_REVIEWS_TABLE + " ( " +
                MovieTableConstants.MOVIE_REVIEW_ID + " INTEGER NOT NULL PRIMARY KEY , " +
                MovieTableConstants.ID + " INTEGER NOT NULL, "+
                MovieTableConstants.AUTHOR + " TEXT, "+
                MovieTableConstants.CONTENT + " TEXT, "+
                "FOREIGN KEY ( "+MovieTableConstants.ID +" ) "+" REFERENCES "+MovieTableConstants.MOVIE_TABLE+" ( "+MovieTableConstants.ID +" ) "+
                " ); ";
        Log.d(LOG_TAG,CREATE_TABLE_REVIEWS);
        sqLiteDatabase.execSQL(CREATE_TABLE_REVIEWS);
    }
}
