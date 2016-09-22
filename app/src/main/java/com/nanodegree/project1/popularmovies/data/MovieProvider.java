package com.nanodegree.project1.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Koushick on 09-09-2016.
 */
public class MovieProvider extends ContentProvider
{
    private static MovieDBHelper movieDBHelper;
    private static SQLiteDatabase writeMovieDatabase;
    private static String LOG_TAG = MovieProvider.class.getName();
    private static final int GET_ALL_MOVIE = 100;
    private static final int GET_MOVIE_TRAILER_WITH_ID = 200;
    private static final int GET_MOVIE_REVIEW_WITH_ID = 1000;
    private static final int INSERT_MOVIE = 300;
    private static final int INSERT_MOVIE_TRAILER = 500;
    private static final int INSERT_MOVIE_REVIEW = 600;
    private static final int DELETE_MOVIE = 400;
    private static final int GET_MAX_MOVIE_ID = 900;
    private int MOVIE_ID = 1000;
    private int MOVIE_REVIEW_ID = 2000;
    private int MOVIE_TRAILER_ID = 3000;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static
    {
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/allMovies",GET_ALL_MOVIE);
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/movie_trailer_id/#",GET_MOVIE_TRAILER_WITH_ID);
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/movie_review_id/#",GET_MOVIE_REVIEW_WITH_ID);
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/addMovie",INSERT_MOVIE);
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/addMovie/trailer",INSERT_MOVIE_TRAILER);
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/addMovie/review",INSERT_MOVIE_REVIEW);
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/deleteMovie/#",DELETE_MOVIE);
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/getMaxMovieId/",GET_MAX_MOVIE_ID);
    }

    @Override
    public boolean onCreate() {
        getMovieDBHelper();
        return true;
    }

    public void getMovieDBHelper(){
        if(movieDBHelper == null) {
            movieDBHelper = new MovieDBHelper(getContext());
        }
    }

    public static SQLiteDatabase getWriteMovieDatabase(){
        if(writeMovieDatabase == null){
            String myPath = MovieDBHelper.DB_PATH + MovieDBHelper.DATABASE_NAME;
            writeMovieDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
            if(writeMovieDatabase != null){
                return writeMovieDatabase;
            }
            Log.d(LOG_TAG,"DB Path == "+writeMovieDatabase.getPath());
        }
        return writeMovieDatabase;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor movie;
        String getMovie;
        int match = sUriMatcher.match(uri);
        switch (match)
        {
            case GET_MOVIE_TRAILER_WITH_ID:
                Log.d(LOG_TAG,String.valueOf(ContentUris.parseId(uri)));

                getMovie = "SELECT key, name " +
                        "FROM movie_trailers " +
                        "WHERE movie_trailers._ID = "+"'"+ContentUris.parseId(uri)+"'";
                movie = getWriteMovieDatabase().rawQuery(getMovie, null);
            break;

            case GET_MOVIE_REVIEW_WITH_ID:
                Log.d(LOG_TAG,String.valueOf(ContentUris.parseId(uri)));

                getMovie = "SELECT author, content " +
                        "FROM movie_reviews " +
                        "WHERE movie_reviews._ID = "+"'"+ContentUris.parseId(uri)+"'";
                movie = getWriteMovieDatabase().rawQuery(getMovie, null);
            break;

            case GET_ALL_MOVIE:

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_TABLE + " movie1 ",null,null,null,null,MovieTableConstants.ID,null );
                movie = getWriteMovieDatabase().rawQuery(getMovie, null);
            break;

            case GET_MAX_MOVIE_ID:

                getMovie = "SELECT MAX(_ID) AS MOVIE_ID,HEADING, SYNOPSIS, USER_RATING FROM MOVIE";
                    movie = getWriteMovieDatabase().rawQuery(getMovie, null);
            break;

            default:
                return null;
        }
        return movie;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        String insertMovie, getMovie;

        int match = sUriMatcher.match(uri);

        switch(match)
        {
            case INSERT_MOVIE_REVIEW:

                Cursor movie_rev_id;
                int movie_review_id_insert = -1;

                //Insert Review info

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_REVIEWS_TABLE,null,null,null,null,MovieTableConstants.MOVIE_REVIEW_ID,null);
                Cursor movie_review_exist = getWriteMovieDatabase().rawQuery(getMovie, null);

                if (movie_review_exist.getCount() > 0){
                    for(int i=0;i<movie_review_exist.getColumnNames().length;i++){
                        Log.d(LOG_TAG," Column name === > "+movie_review_exist.getColumnNames()[i]);
                    }
                }
                if (movie_review_exist.getCount() > 0)
                {
                    movie_rev_id = getWriteMovieDatabase().rawQuery("SELECT MAX(_REVIEW_ID) AS MOVIE_REV_ID FROM MOVIE_REVIEWS;", null);
                    movie_rev_id.moveToFirst();
                    movie_review_id_insert = movie_rev_id.getInt(movie_rev_id.getColumnIndex("MOVIE_REV_ID")) + 1;
                }
                else
                {
                    movie_review_id_insert = MOVIE_REVIEW_ID;
                }

                try {
                    getWriteMovieDatabase().beginTransaction();
                    boolean error = false;
                    String insertMov = "INSERT INTO " + MovieTableConstants.MOVIE_REVIEWS_TABLE + " ( " + MovieTableConstants.MOVIE_REVIEW_ID + " , " + MovieTableConstants.ID + " , " +
                            MovieTableConstants.AUTHOR + " , " + MovieTableConstants.CONTENT + " ) VALUES (?,?,?,?)";
                    SQLiteStatement sqLiteStatement = getWriteMovieDatabase().compileStatement(insertMov);
                    for (int i = 0; i < values.length; i++) {
                        sqLiteStatement.bindLong(1, movie_review_id_insert);
                        sqLiteStatement.bindLong(2, values[i].getAsInteger("movieID"));
                        sqLiteStatement.bindString(3, values[i].getAsString("author"));
                        sqLiteStatement.bindString(4, values[i].getAsString("content"));
                        if (sqLiteStatement.executeInsert() != -1) {
                            movie_review_id_insert++;
                        }
                        else{
                            error = true;
                            break;
                        }
                    }
                    sqLiteStatement.close();
                    if(!error){
                        getWriteMovieDatabase().setTransactionSuccessful();
                        getContext().getContentResolver().notifyChange(uri, null);
                    }
                }
                finally {
                    getWriteMovieDatabase().endTransaction();
                }

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_REVIEWS_TABLE,new String[]{MovieTableConstants.MOVIE_REVIEW_ID, MovieTableConstants.AUTHOR, MovieTableConstants.ID},
                        null,null,null,MovieTableConstants.MOVIE_REVIEW_ID,null);
                Cursor movie_exist = getWriteMovieDatabase().rawQuery(getMovie, null);
                movie_exist.moveToFirst();
                if(movie_exist.getCount() > 0) {
                    do {
                        Log.d(LOG_TAG, "Review ID === " + movie_exist.getInt(movie_exist.getColumnIndex(MovieTableConstants.MOVIE_REVIEW_ID)));
                        Log.d(LOG_TAG, "Review Author === " + movie_exist.getString(movie_exist.getColumnIndex(MovieTableConstants.AUTHOR)));
                        Log.d(LOG_TAG, "Review Movie ID === " + movie_exist.getInt(movie_exist.getColumnIndex(MovieTableConstants.ID)));
                    }while (movie_exist.moveToNext());
                }
                movie_exist.close();

                break;

            case INSERT_MOVIE_TRAILER:

                //Insert Trailer info

                Cursor movie_trail_id;

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_TRAILERS_TABLE,new String[]{MovieTableConstants.KEY,MovieTableConstants.NAME},
                        null,null,null,MovieTableConstants.MOVIE_TRAILER_ID,null);
                Cursor movie_trailer_exist = getWriteMovieDatabase().rawQuery(getMovie, null);
                movie_trailer_exist.moveToFirst();
                int movie_trailer_id_insert = -1;

                if (movie_trailer_exist.getCount() > 0)
                {
                    movie_trail_id = getWriteMovieDatabase().rawQuery("SELECT MAX(_TRAILER_ID) AS MOVIE_TRAIL_ID FROM MOVIE_TRAILERS;", null);
                    movie_trail_id.moveToFirst();
                    movie_trailer_id_insert = movie_trail_id.getInt(movie_trail_id.getColumnIndex("MOVIE_TRAIL_ID")) + 1;
                }
                else
                {
                    movie_trailer_id_insert = MOVIE_TRAILER_ID;
                }
                String insertMov = "INSERT INTO " + MovieTableConstants.MOVIE_TRAILERS_TABLE + " ( " + MovieTableConstants.MOVIE_TRAILER_ID + " , " + MovieTableConstants.ID + " , " + MovieTableConstants.KEY + " , " +
                        MovieTableConstants.NAME + " ) VALUES (?, ?, ?, ?)";
                boolean error = false;
                try {
                    getWriteMovieDatabase().beginTransaction();
                    SQLiteStatement sqLiteStatement = getWriteMovieDatabase().compileStatement(insertMov);
                    for (int i = 0; i < values.length; i++) {
                        sqLiteStatement.bindLong(1, movie_trailer_id_insert);
                        sqLiteStatement.bindLong(2, values[i].getAsInteger("movieID"));
                        sqLiteStatement.bindString(3, values[i].getAsString("key"));
                        sqLiteStatement.bindString(4, values[i].getAsString("name"));
                        if (sqLiteStatement.executeInsert() != -1) {
                            movie_trailer_id_insert++;
                        }
                        else{
                            error = true;
                            break;
                        }
                    }
                    sqLiteStatement.close();
                    if(!error){
                        getWriteMovieDatabase().setTransactionSuccessful();
                        getContext().getContentResolver().notifyChange(uri, null);
                    }
                }
                finally {
                    getWriteMovieDatabase().endTransaction();
                }

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_TRAILERS_TABLE,new String[]{MovieTableConstants.MOVIE_TRAILER_ID,MovieTableConstants.NAME,MovieTableConstants.ID},
                        null,null,null,MovieTableConstants.MOVIE_TRAILER_ID,null);
                movie_exist = getWriteMovieDatabase().rawQuery(getMovie, null);
                movie_exist.moveToFirst();
                if(movie_exist.getCount() > 0) {
                    do {
                        Log.d(LOG_TAG, "Trailer ID === " + movie_exist.getInt(movie_exist.getColumnIndex(MovieTableConstants.MOVIE_TRAILER_ID)));
                        Log.d(LOG_TAG, "Trailer Name === " + movie_exist.getString(movie_exist.getColumnIndex(MovieTableConstants.NAME)));
                        Log.d(LOG_TAG, "Trailer Movie ID === " + movie_exist.getInt(movie_exist.getColumnIndex(MovieTableConstants.ID)));
                    }while (movie_exist.moveToNext());
                }
                movie_exist.close();

                break;
        }
        return 0;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues)
    {
        String insertMovie, getMovie;

        int match = sUriMatcher.match(uri);
        switch (match)
        {
            case INSERT_MOVIE:

                Log.d(LOG_TAG,"inserting Movie");
                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_TABLE + " movie1 ",null,null,null,null,MovieTableConstants.ID,null );
                Cursor movie_exist = getWriteMovieDatabase().rawQuery(getMovie, null);
                boolean present = false;
                if(movie_exist.getCount() > 0)
                {
                    present = true;
                }

                Cursor movie_id = null;
                int movie_id_inserted = -1;

                if(present)
                {
                    movie_id = getWriteMovieDatabase().rawQuery("SELECT MAX(_ID) AS MOVIE_ID FROM MOVIE;",null);
                    movie_id.moveToFirst();
                    movie_id_inserted = movie_id.getInt(movie_id.getColumnIndex("MOVIE_ID"))+1;
                }
                else
                {
                    movie_id_inserted = MOVIE_ID;
                }
                try {
                    boolean error = false;
                    getWriteMovieDatabase().beginTransaction();

                    String insertMov = "INSERT INTO " + MovieTableConstants.MOVIE_TABLE + " ( " + MovieTableConstants.ID + " , " + MovieTableConstants.MOVIE_ID + " , " + MovieTableConstants.HEADING + " , " +
                            MovieTableConstants.THUMBNAIL + " , " + MovieTableConstants.RELEASE_DATE + " , " + MovieTableConstants.USER_RATING + " , " + MovieTableConstants.SYNOPSIS +
                            " ) VALUES (?,?,?,?,?,?,? )";
                    SQLiteStatement sqLiteStatement = getWriteMovieDatabase().compileStatement(insertMov);

                    sqLiteStatement.bindLong(1, movie_id_inserted);
                    sqLiteStatement.bindLong(2, contentValues.getAsLong("movieID"));
                    sqLiteStatement.bindString(3, contentValues.getAsString("heading"));
                    sqLiteStatement.bindBlob(4, contentValues.getAsByteArray("thumbnail"));
                    sqLiteStatement.bindString(5, contentValues.getAsString("releaseDate"));
                    sqLiteStatement.bindDouble(6, contentValues.getAsDouble("userRating"));
                    sqLiteStatement.bindString(7, contentValues.getAsString("synopsis"));
                    if (sqLiteStatement.executeInsert() == -1) {
                        error = true;
                        Log.d(LOG_TAG, "not inserted...some error");
                    } else {
                        Log.d(LOG_TAG, "inserted!!== > " + movie_id_inserted);
                    }
                    sqLiteStatement.close();
                    if(!error){
                        getWriteMovieDatabase().setTransactionSuccessful();
                        getContext().getContentResolver().notifyChange(uri, null);
                    }
                }
                finally {
                    getWriteMovieDatabase().endTransaction();
                }

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_TABLE,new String[]{MovieTableConstants.ID, MovieTableConstants.HEADING,MovieTableConstants.MOVIE_ID},
                        null,null,null,MovieTableConstants.ID,null );
                movie_exist = getWriteMovieDatabase().rawQuery(getMovie, null);
                movie_exist.moveToFirst();
                Log.d(LOG_TAG,"Count == "+movie_exist.getCount());
                if(movie_exist.getCount() > 0) {
                    do {
                        Log.d(LOG_TAG, "ID === " + movie_exist.getInt(movie_exist.getColumnIndex(MovieTableConstants.ID)));
                        Log.d(LOG_TAG, "Heading === " + movie_exist.getString(movie_exist.getColumnIndex(MovieTableConstants.HEADING)));
                        Log.d(LOG_TAG, "Movie ID === " + movie_exist.getLong(movie_exist.getColumnIndex(MovieTableConstants.MOVIE_ID)));
                    } while (movie_exist.moveToNext());
                }
                movie_exist.close();

                break;
        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings)
    {
        int match = sUriMatcher.match(uri);
        switch(match)
        {
            case DELETE_MOVIE:

                long movieID = ContentUris.parseId(uri);
                Log.d(LOG_TAG,"Deleting movie..."+movieID);

                //Delete review
                String delete_movie = "DELETE FROM "+MovieTableConstants.MOVIE_REVIEWS_TABLE+" WHERE "+MovieTableConstants.ID+" = ?";
                SQLiteStatement sqLiteStatement = getWriteMovieDatabase().compileStatement(delete_movie);
                sqLiteStatement.bindLong(1, movieID);
                if(sqLiteStatement.executeUpdateDelete() == 0){
                    Log.d(LOG_TAG, "No rows deleted.....");
                }
                else{
                    Log.d(LOG_TAG,"Rows were deleted");
                }

                //Delete trailer
                delete_movie = "DELETE FROM "+MovieTableConstants.MOVIE_TRAILERS_TABLE+" WHERE "+MovieTableConstants.ID+" = ?";
                sqLiteStatement = getWriteMovieDatabase().compileStatement(delete_movie);
                sqLiteStatement.bindLong(1, movieID);
                if(sqLiteStatement.executeUpdateDelete() == 0){
                    Log.d(LOG_TAG, "No Trailers deleted.....");
                }
                else{
                    Log.d(LOG_TAG,"Trailers were deleted");
                }

                //Delete movie
                delete_movie = "DELETE FROM "+MovieTableConstants.MOVIE_TABLE+" WHERE "+MovieTableConstants.ID+" = ?";
                sqLiteStatement = getWriteMovieDatabase().compileStatement(delete_movie);
                sqLiteStatement.bindLong(1, movieID);
                if(sqLiteStatement.executeUpdateDelete() == 0){
                    Log.d(LOG_TAG, "No Reviews deleted.....");
                }
                else{
                    Log.d(LOG_TAG,"Reviews were deleted");
                }

                break;
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
