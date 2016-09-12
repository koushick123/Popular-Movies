package com.nanodegree.project1.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Koushick on 09-09-2016.
 */
public class MovieProvider extends ContentProvider
{
    private MovieDBHelper movieDBHelper;
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
        //sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/deleteMovie/review",DELETE_MOVIE_REVIEW);
        //sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/addMovie/trailer",DELETE_MOVIE_TRAILER);
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/getMaxMovieId/",GET_MAX_MOVIE_ID);
    }

    @Override
    public boolean onCreate() {
        movieDBHelper = new MovieDBHelper(getContext());
        return true;
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

                movie = movieDBHelper.getReadableDatabase().rawQuery(getMovie, null);
                movieDBHelper.close();
            break;

            case GET_ALL_MOVIE:

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_TABLE + " movie1, ",new String[]{"movie1.thumbnail, movie1._ID, movie1.heading"},null,null,null,MovieTableConstants.ID,null );
                movie = movieDBHelper.getReadableDatabase().rawQuery(getMovie, null);
                movieDBHelper.close();
            break;

            case GET_MAX_MOVIE_ID:

                getMovie = "SELECT MAX(_ID) AS MOVIE_ID FROM MOVIE";
                movie = movieDBHelper.getReadableDatabase().rawQuery(getMovie, null);
                movieDBHelper.close();
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

                SQLiteDatabase sqLiteDatabase = movieDBHelper.getWritableDatabase();
                SQLiteDatabase sqLiteDatabaseRead = movieDBHelper.getReadableDatabase();

                for(int i=0;i < values.length;i++)
                {
                    getMovie = SQLiteQueryBuilder.buildQueryString(false, MovieTableConstants.MOVIE_REVIEWS_TABLE + " movie1 ", new String[]{"movie1._REVIEW_ID"}, null, null, null, MovieTableConstants.MOVIE_REVIEW_ID, null);
                    Cursor movie_review_exist = sqLiteDatabaseRead.rawQuery(getMovie, null);

                    if (movie_review_exist.getCount() > 0)
                    {
                        movie_rev_id = sqLiteDatabase.rawQuery("SELECT MAX(_REVIEW_ID) AS MOVIE_REV_ID FROM MOVIE_REVIEWS;", null);
                        movie_review_id_insert = movie_rev_id.getInt(movie_rev_id.getColumnIndex("MOVIE_REV_ID")) + 1;
                    }
                    else
                    {
                        movie_review_id_insert = MOVIE_REVIEW_ID;
                    }

                    insertMovie = "INSERT INTO " + MovieTableConstants.MOVIE_REVIEWS_TABLE + " ( " + MovieTableConstants.MOVIE_REVIEW_ID + " , " + MovieTableConstants.ID + " , " + MovieTableConstants.AUTHOR + " , " +
                            MovieTableConstants.CONTENT + " ) VALUES ( " + movie_review_id_insert + " , " + values[i].getAsInteger("movieID") + " , " + values[i].getAsString("author") + values[i].getAsString("content");

                    sqLiteDatabase.rawQuery(insertMovie, null);
                    sqLiteDatabase.execSQL("COMMIT;");
                }
                sqLiteDatabase.close();

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_REVIEWS_TABLE + " movie1 ",null,null,null,null,MovieTableConstants.MOVIE_REVIEW_ID,null);
                Cursor movie_exist = sqLiteDatabaseRead.rawQuery(getMovie, null);
                movie_exist.moveToFirst();
                while(movie_exist.moveToNext())
                {
                    Log.d(LOG_TAG,"Review ID === "+movie_exist.getInt(movie_exist.getColumnIndex(MovieTableConstants.MOVIE_REVIEW_ID)));
                    Log.d(LOG_TAG,"Review Author === "+movie_exist.getString(movie_exist.getColumnIndex(MovieTableConstants.AUTHOR)));
                }
                movie_exist.close();

                sqLiteDatabaseRead.close();

                break;

            case INSERT_MOVIE_TRAILER:

                //Insert Trailer info

                sqLiteDatabase = movieDBHelper.getWritableDatabase();
                sqLiteDatabaseRead = movieDBHelper.getReadableDatabase();
                Cursor movie_trail_id;

                for(int i=0;i<values.length;i++)
                {
                    getMovie = SQLiteQueryBuilder.buildQueryString(false, MovieTableConstants.MOVIE_TRAILERS_TABLE + " movie1, ", new String[]{"movie1._TRAILER_ID"}, null, null, null, MovieTableConstants.MOVIE_TRAILER_ID, null);
                    Cursor movie_trailer_exist = sqLiteDatabaseRead.rawQuery(getMovie, null);
                    int movie_trailer_id_insert = -1;

                    if (movie_trailer_exist.getCount() > 0)
                    {
                        movie_trail_id = sqLiteDatabase.rawQuery("SELECT MAX(_REVIEW_ID) AS MOVIE_REV_ID FROM MOVIE_REVIEWS;", null);
                        movie_trailer_id_insert = movie_trail_id.getInt(movie_trail_id.getColumnIndex("MOVIE_REV_ID")) + 1;
                    }
                    else
                    {
                        movie_trailer_id_insert = MOVIE_TRAILER_ID;
                    }

                    insertMovie = "INSERT INTO " + MovieTableConstants.MOVIE_TRAILERS_TABLE + " ( " + MovieTableConstants.MOVIE_TRAILER_ID + " , " + MovieTableConstants.ID + " , " + MovieTableConstants.KEY + " , " +
                            MovieTableConstants.NAME + " ) VALUES ( " + movie_trailer_id_insert + " , " + values[i].getAsInteger("movieID") + " , " + values[i].getAsString("key") + values[i].getAsString("name");
                    sqLiteDatabase.rawQuery(insertMovie,null);
                    sqLiteDatabase.execSQL("COMMIT;");
                }

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_TRAILERS_TABLE + " movie1 ",null,null,null,null,MovieTableConstants.MOVIE_TRAILER_ID,null);
                movie_exist = sqLiteDatabaseRead.rawQuery(getMovie, null);
                movie_exist.moveToFirst();
                while(movie_exist.moveToNext())
                {
                    Log.d(LOG_TAG,"Trailer ID === "+movie_exist.getInt(movie_exist.getColumnIndex(MovieTableConstants.MOVIE_TRAILER_ID)));
                    Log.d(LOG_TAG,"Trailer Name === "+movie_exist.getString(movie_exist.getColumnIndex(MovieTableConstants.NAME)));
                }
                movie_exist.close();
                sqLiteDatabase.close();

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

                SQLiteDatabase sqLiteDatabase = movieDBHelper.getWritableDatabase();
                SQLiteDatabase sqLiteDatabaseRead = movieDBHelper.getReadableDatabase();
                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_TABLE + " movie1 ",null,null,null,null,MovieTableConstants.ID,null );
                Cursor movie_exist = sqLiteDatabaseRead.rawQuery(getMovie, null);
                boolean present = false;
                if(movie_exist.getCount() > 0)
                {
                    present = true;
                }

                Cursor movie_id = null;
                int movie_id_inserted = -1;

                if(present)
                {
                    movie_id = sqLiteDatabaseRead.rawQuery("SELECT MAX(_ID) AS MOVIE_ID FROM MOVIE;",null);
                    movie_id_inserted = movie_id.getInt(movie_id.getColumnIndex("MOVIE_ID"))+1;
                }
                else
                {
                    movie_id_inserted = MOVIE_ID;
                }

                insertMovie = "INSERT INTO "+MovieTableConstants.MOVIE_TABLE+" ( "+MovieTableConstants.ID+" , "+MovieTableConstants.HEADING+" , "+MovieTableConstants.THUMBNAIL+" , "+
                        MovieTableConstants.RELEASE_DATE+" , "+MovieTableConstants.USER_RATING+" , "+MovieTableConstants.SYNOPSIS+" ) VALUES ( "+movie_id_inserted+", "+"'"+contentValues.getAsString("heading")+"'"+" , "+
                        "'"+contentValues.getAsByteArray("thumbnail")+"'"+" , "+contentValues.getAsString("releaseDate")+" , "+contentValues.getAsDouble("userRating")+" , "+contentValues.getAsString("synopsis");
                sqLiteDatabase.rawQuery(insertMovie,null);
                sqLiteDatabase.execSQL("COMMIT;");

                sqLiteDatabase.close();

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_TABLE + " movie1, ",null,null,null,null,MovieTableConstants.ID,null );
                movie_exist = sqLiteDatabaseRead.rawQuery(getMovie, null);
                movie_exist.moveToFirst();
                while(movie_exist.moveToNext())
                {
                    Log.d(LOG_TAG,"ID === "+movie_exist.getInt(movie_exist.getColumnIndex(MovieTableConstants.ID)));
                    Log.d(LOG_TAG,"Synopsis === "+movie_exist.getString(movie_exist.getColumnIndex(MovieTableConstants.SYNOPSIS)));
                }
                movie_exist.close();
                sqLiteDatabaseRead.close();

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
                sqLiteDatabase.rawQuery(delete_movie,null);

                //Delete trailer
                delete_movie = "DELETE FROM "+MovieTableConstants.MOVIE_TRAILERS_TABLE+" WHERE "+MovieTableConstants.ID+" = "+ContentUris.parseId(uri);
                sqLiteDatabase.rawQuery(delete_movie,null);

                //Delete movie
                delete_movie = "DELETE FROM "+MovieTableConstants.MOVIE_TABLE+" WHERE "+MovieTableConstants.ID+" = "+ContentUris.parseId(uri);
                sqLiteDatabase.rawQuery(delete_movie,null);

                sqLiteDatabase.close();
                break;
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {

        return 0;
    }
}
