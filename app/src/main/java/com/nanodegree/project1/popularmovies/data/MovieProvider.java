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
    private String LOG_TAG = MovieProvider.class.getName();
    private static final int GET_ALL_MOVIE = 100;
    private static final int GET_MOVIE_WITH_ID = 200;
    private static final int INSERT_MOVIE = 300;
    private static final int INSERT_MOVIE_TRAILER = 500;
    private static final int INSERT_MOVIE_REVIEW = 600;
    private static final int DELETE_MOVIE = 400;
    //private static final int DELETE_MOVIE_REVIEW = 700;
    //private static final int DELETE_MOVIE_TRAILER = 800;
    private static final int GET_MAX_MOVIE_ID = 900;
    private static final int DROP_MOVIE_TABLE = 1000;
    private int MOVIE_ID = 1000;
    private int MOVIE_REVIEW_ID = 2000;
    private int MOVIE_TRAILER_ID = 3000;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static
    {
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/allMovies",GET_ALL_MOVIE);
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/movie_id/#",GET_MOVIE_WITH_ID);
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/addMovie",INSERT_MOVIE);
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/addMovie/trailer",INSERT_MOVIE_TRAILER);
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/addMovie/review",INSERT_MOVIE_REVIEW);
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/deleteMovie/#",DELETE_MOVIE);
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/dropMovie/",DROP_MOVIE_TABLE);
        //sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/deleteMovie/review",DELETE_MOVIE_REVIEW);
        //sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/addMovie/trailer",DELETE_MOVIE_TRAILER);
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
            writeMovieDatabase = movieDBHelper.getWritableDatabase();
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
            case GET_MOVIE_WITH_ID:

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_TABLE + " movie1, " +
                    MovieTableConstants.MOVIE_TRAILERS_TABLE + " movie_trail, " +
                    MovieTableConstants.MOVIE_REVIEWS_TABLE + " movie_rev ",new String[] {"movie1.thumbnail, movie1.heading, movie1.synopsis, movie1.release_date, movie1.user_rating, movie_trail.name, movie_trail.key, movie_rev.author, movie_rev.content"},
                    "movie1._ID = movie_trail._ID AND movie1._ID = movie_rev._ID AND movie1._ID = "+String.valueOf(ContentUris.parseId(uri)),null,null,MovieTableConstants.ID,null);

                movie = getWriteMovieDatabase().rawQuery(getMovie, null);
                //movieDBHelper.close();
            break;

            case GET_ALL_MOVIE:

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_TABLE + " movie1, ",new String[]{"movie1.thumbnail, movie1._ID, movie1.heading"},null,null,null,MovieTableConstants.ID,null );
                movie = getWriteMovieDatabase().rawQuery(getMovie, null);
                //movieDBHelper.close();
            break;

            case GET_MAX_MOVIE_ID:

                getMovie = "SELECT MAX(_ID) AS MOVIE_ID,HEADING, SYNOPSIS, USER_RATING FROM MOVIE";
                    movie = getWriteMovieDatabase().rawQuery(getMovie, null);
                //movieDBHelper.close();
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

                if (movie_review_exist.getCount() > 0)
                {
                    movie_rev_id = getWriteMovieDatabase().rawQuery("SELECT MAX(_REVIEW_ID) AS MOVIE_REV_ID FROM MOVIE_REVIEWS;", null);
                    movie_review_id_insert = movie_rev_id.getInt(movie_rev_id.getColumnIndex("MOVIE_REV_ID")) + 1;
                }
                else
                {
                    movie_review_id_insert = MOVIE_REVIEW_ID;
                }

                for(int i=0;i < values.length;i++)
                {
                    getWriteMovieDatabase().beginTransaction();
                    insertMovie = "INSERT INTO " + MovieTableConstants.MOVIE_REVIEWS_TABLE + " ( " + MovieTableConstants.MOVIE_REVIEW_ID + " , " + MovieTableConstants.ID + " , " + MovieTableConstants.AUTHOR + " , " +
                            MovieTableConstants.CONTENT + " ) VALUES ( " +movie_review_id_insert+ " , " +values[i].getAsInteger("movieID") + " , " + "'"+values[i].getAsString("author") +"'"+
                            "'"+values[i].getAsString("content")+"')";
                    String insertMov = "INSERT INTO " + MovieTableConstants.MOVIE_REVIEWS_TABLE + " ( " + MovieTableConstants.MOVIE_REVIEW_ID + " , " + MovieTableConstants.ID + " , " +
                            MovieTableConstants.AUTHOR + " , " +MovieTableConstants.CONTENT + " ) VALUES (?,?,?,?)";
                    SQLiteStatement sqLiteStatement = getWriteMovieDatabase().compileStatement(insertMov);
                    sqLiteStatement.bindLong(1,movie_review_id_insert);
                    sqLiteStatement.bindLong(2,values[i].getAsInteger("movieID"));
                    sqLiteStatement.bindString(3,values[i].getAsString("author"));
                    sqLiteStatement.bindString(4,values[i].getAsString("content"));
                    if(sqLiteStatement.executeInsert() == -1)
                    {
                        getWriteMovieDatabase().endTransaction();
                    }
                    else
                    {
                        getWriteMovieDatabase().setTransactionSuccessful();
                        getContext().getContentResolver().notifyChange(uri,null);
                        movie_review_id_insert++;
                    }
                    sqLiteStatement.close();
                    //getWriteMovieDatabase().rawQuery(insertMovie, null);
                }

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_REVIEWS_TABLE,null,null,null,null,MovieTableConstants.MOVIE_REVIEW_ID,null);
                Cursor movie_exist = getWriteMovieDatabase().rawQuery(getMovie, null);
                movie_exist.moveToFirst();
                while(movie_exist.moveToNext())
                {
                    Log.d(LOG_TAG,"Review ID === "+movie_exist.getInt(movie_exist.getColumnIndex(MovieTableConstants.MOVIE_REVIEW_ID)));
                    Log.d(LOG_TAG,"Review Author === "+movie_exist.getString(movie_exist.getColumnIndex(MovieTableConstants.AUTHOR)));
                }
                movie_exist.close();
                //getWriteMovieDatabase().close();

                break;

            case INSERT_MOVIE_TRAILER:

                //Insert Trailer info

                Cursor movie_trail_id;

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_TRAILERS_TABLE,null,null,null,null,MovieTableConstants.MOVIE_TRAILER_ID,null);
                Cursor movie_trailer_exist = getWriteMovieDatabase().rawQuery(getMovie, null);
                movie_trailer_exist.moveToFirst();
                int movie_trailer_id_insert = -1;

                if (movie_trailer_exist.getCount() > 0)
                {
                    movie_trail_id = getWriteMovieDatabase().rawQuery("SELECT MAX(_TRAILER_ID) AS MOVIE_TRAIL_ID FROM MOVIE_TRAILERS;", null);
                    movie_trailer_id_insert = movie_trail_id.getInt(movie_trail_id.getColumnIndex("MOVIE_TRAIL_ID")) + 1;
                }
                else
                {
                    movie_trailer_id_insert = MOVIE_TRAILER_ID;
                }

                for(int i=0;i<values.length;i++)
                {
                    getWriteMovieDatabase().beginTransaction();
                    insertMovie = "INSERT INTO " + MovieTableConstants.MOVIE_TRAILERS_TABLE + " ( " + MovieTableConstants.MOVIE_TRAILER_ID + " , " + MovieTableConstants.ID + " , " + MovieTableConstants.KEY + " , " +
                            MovieTableConstants.NAME + " ) VALUES ( " +movie_trailer_id_insert + " , " + values[i].getAsInteger("movieID") + " , " + "'"+ values[i].getAsString("key")+"'"
                            +"'"+ values[i].getAsString("name")+"')";
                    String insertMov = "INSERT INTO " + MovieTableConstants.MOVIE_TRAILERS_TABLE + " ( " + MovieTableConstants.MOVIE_TRAILER_ID + " , " + MovieTableConstants.ID + " , " + MovieTableConstants.KEY + " , " +
                            MovieTableConstants.NAME + " ) VALUES (?, ?, ?, ?)";
                    SQLiteStatement sqLiteStatement = getWriteMovieDatabase().compileStatement(insertMov);
                    sqLiteStatement.bindLong(1, movie_trailer_id_insert);
                    sqLiteStatement.bindLong(2, values[i].getAsInteger("movieID"));
                    sqLiteStatement.bindString(3, values[i].getAsString("key"));
                    sqLiteStatement.bindString(4, values[i].getAsString("name"));
                    if(sqLiteStatement.executeInsert() == -1)
                    {
                        getWriteMovieDatabase().endTransaction();
                    }
                    else
                    {
                        getWriteMovieDatabase().setTransactionSuccessful();
                        getContext().getContentResolver().notifyChange(uri,null);
                        movie_trailer_id_insert++;
                    }
                    sqLiteStatement.close();
                }

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_TRAILERS_TABLE,null,null,null,null,MovieTableConstants.MOVIE_TRAILER_ID,null);
                movie_exist = getWriteMovieDatabase().rawQuery(getMovie, null);
                movie_exist.moveToFirst();
                while(movie_exist.moveToNext())
                {
                    Log.d(LOG_TAG,"Trailer ID === "+movie_exist.getInt(movie_exist.getColumnIndex(MovieTableConstants.MOVIE_TRAILER_ID)));
                    Log.d(LOG_TAG,"Trailer Name === "+movie_exist.getString(movie_exist.getColumnIndex(MovieTableConstants.NAME)));
                }
                movie_exist.close();
                //getWriteMovieDatabase().close();

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
                    movie_id_inserted = movie_id.getInt(movie_id.getColumnIndex("MOVIE_ID"))+1;
                }
                else
                {
                    movie_id_inserted = MOVIE_ID;
                }

                getWriteMovieDatabase().beginTransaction();
                insertMovie = "INSERT INTO "+MovieTableConstants.MOVIE_TABLE+" ( "+MovieTableConstants.ID+" , "+MovieTableConstants.HEADING+" , "+MovieTableConstants.THUMBNAIL+" , "+
                        MovieTableConstants.RELEASE_DATE+" , "+MovieTableConstants.USER_RATING+" , "+MovieTableConstants.SYNOPSIS+" ) VALUES ( "+movie_id_inserted+", "+"'"+contentValues.getAsString("heading")
                        +"'"+" , "+"'"+contentValues.getAsByteArray("thumbnail")+"'"+" , "+"'"+contentValues.getAsString("releaseDate")+"'"+" , "+contentValues.getAsDouble("userRating")+" , "
                        +"'"+contentValues.getAsString("synopsis")+"')";

                String insertMov = "INSERT INTO "+MovieTableConstants.MOVIE_TABLE+" ( "+MovieTableConstants.ID+" , "+MovieTableConstants.MOVIE_ID+" , "+MovieTableConstants.HEADING+" , "+
                        MovieTableConstants.THUMBNAIL+" , "+MovieTableConstants.RELEASE_DATE+" , "+MovieTableConstants.USER_RATING+" , "+MovieTableConstants.SYNOPSIS+
                        " ) VALUES (?,?,?,?,?,?,? )";
                SQLiteStatement sqLiteStatement = getWriteMovieDatabase().compileStatement(insertMov);

                sqLiteStatement.bindLong(1, movie_id_inserted);
                sqLiteStatement.bindLong(2, contentValues.getAsLong("movieID"));
                sqLiteStatement.bindString(3, contentValues.getAsString("heading"));
                sqLiteStatement.bindBlob(4, contentValues.getAsByteArray("thumbnail"));
                sqLiteStatement.bindString(5, contentValues.getAsString("releaseDate"));
                sqLiteStatement.bindDouble(6, contentValues.getAsDouble("userRating"));
                sqLiteStatement.bindString(7, contentValues.getAsString("synopsis"));
                if(sqLiteStatement.executeInsert() == -1)
                {
                    Log.d(LOG_TAG,"not inserted...some error");
                    getWriteMovieDatabase().endTransaction();
                }
                else
                {
                    Log.d(LOG_TAG,"inserted!!== > "+movie_id_inserted);
                    getWriteMovieDatabase().setTransactionSuccessful();
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                sqLiteStatement.close();

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_TABLE,null,null,null,null,MovieTableConstants.ID,null );
                movie_exist = getWriteMovieDatabase().rawQuery(getMovie, null);
                movie_exist.moveToFirst();
                while(movie_exist.moveToNext())
                {
                    Log.d(LOG_TAG,"ID === "+movie_exist.getInt(movie_exist.getColumnIndex(MovieTableConstants.ID)));
                    Log.d(LOG_TAG,"Heading === "+movie_exist.getString(movie_exist.getColumnIndex(MovieTableConstants.HEADING)));
                    Log.d(LOG_TAG,"Movie ID === "+movie_exist.getLong(movie_exist.getColumnIndex(MovieTableConstants.MOVIE_ID)));
                }
                movie_exist.close();
                //getWriteMovieDatabase().close();

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

                SQLiteDatabase sqLiteDatabase = movieDBHelper.getWritableDatabase();
                //Delete review
                String delete_movie = "DELETE FROM "+MovieTableConstants.MOVIE_REVIEWS_TABLE+" WHERE "+MovieTableConstants.ID+" = "+ContentUris.parseId(uri);
                getWriteMovieDatabase().rawQuery(delete_movie,null);

                //Delete trailer
                delete_movie = "DELETE FROM "+MovieTableConstants.MOVIE_TRAILERS_TABLE+" WHERE "+MovieTableConstants.ID+" = "+ContentUris.parseId(uri);
                getWriteMovieDatabase().rawQuery(delete_movie,null);

                //Delete movie
                delete_movie = "DELETE FROM "+MovieTableConstants.MOVIE_TABLE+" WHERE "+MovieTableConstants.ID+" = "+ContentUris.parseId(uri);
                getWriteMovieDatabase().rawQuery(delete_movie,null);

                //getWriteMovieDatabase().close();
                break;

            case DROP_MOVIE_TABLE:

                sqLiteDatabase = movieDBHelper.getWritableDatabase();
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
                getWriteMovieDatabase().execSQL(CREATE_TABLE);
                Log.d(LOG_TAG, "Created table Movie..");
                break;
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {

        return 0;
    }
}
