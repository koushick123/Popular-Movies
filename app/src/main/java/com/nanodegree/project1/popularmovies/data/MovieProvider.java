package com.nanodegree.project1.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

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

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_TABLE + " movie1, ",new String[]{"movie1.thumbnail, movie1._ID"},null,null,null,MovieTableConstants.ID,null );
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

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues)
    {
        String insertMovie, getMovie;

        getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_TABLE + " movie1, ",new String[]{"movie1._ID"},null,null,null,MovieTableConstants.ID,null );
        Cursor movie_exist = movieDBHelper.getReadableDatabase().rawQuery(getMovie, null);
        boolean present = false;
        if(movie_exist.getCount() > 0)
        {
            present = true;
        }
        movieDBHelper.close();

        int match = sUriMatcher.match(uri);
        switch (match)
        {
            case INSERT_MOVIE:

                Cursor movie_id = null;
                int movie_id_inserted = -1;

                if(present)
                {
                    movie_id = movieDBHelper.getWritableDatabase().rawQuery("SELECT MAX(_ID) AS MOVIE_ID FROM MOVIE;",null);
                    movie_id_inserted = movie_id.getInt(movie_id.getColumnIndex("MOVIE_ID"))+1;
                }
                else
                {
                    movie_id_inserted = MOVIE_ID;
                }

                insertMovie = "INSERT INTO "+MovieTableConstants.MOVIE_TABLE+" ( "+MovieTableConstants.ID+" , "+MovieTableConstants.HEADING+" , "+MovieTableConstants.THUMBNAIL+" , "+
                        MovieTableConstants.RELEASE_DATE+" , "+MovieTableConstants.USER_RATING+" , "+MovieTableConstants.SYNOPSIS+" ) VALUES ( "+movie_id_inserted+", "+contentValues.getAsString("heading")+" , "+
                        contentValues.getAsByteArray("thumbnail")+" , "+contentValues.getAsString("releaseDate")+" , "+contentValues.getAsInteger("üserRating")+contentValues.getAsString("synopsis");
                movieDBHelper.getWritableDatabase().rawQuery(insertMovie,null);

                movieDBHelper.close();

                break;

            case INSERT_MOVIE_REVIEW:

                //Insert Review info

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_REVIEWS_TABLE + " movie1, ",new String[]{"movie1._REVIEW_ID"},null,null,null,MovieTableConstants.MOVIE_REVIEW_ID,null );
                Cursor movie_review_exist = movieDBHelper.getReadableDatabase().rawQuery(getMovie, null);
                int movie_review_id_insert = -1;

                if(movie_review_exist.getCount() > 0)
                {
                    movie_id = movieDBHelper.getWritableDatabase().rawQuery("SELECT MAX(_REVIEW_ID) AS MOVIE_REV_ID FROM MOVIE_REVIEWS;",null);
                    movie_review_id_insert = movie_id.getInt(movie_id.getColumnIndex("MOVIE_REV_ID"))+1;
                }
                else
                {
                    movie_review_id_insert = MOVIE_REVIEW_ID;
                }

                insertMovie = "INSERT INTO "+MovieTableConstants.MOVIE_REVIEWS_TABLE+" ( "+MovieTableConstants.MOVIE_REVIEW_ID+" , "+MovieTableConstants.ID+" , "+MovieTableConstants.AUTHOR+" , "+
                        MovieTableConstants.CONTENT+" ) VALUES ( "+movie_review_id_insert+" , "+contentValues.getAsInteger("movieID")+" , "+contentValues.getAsString("author")+contentValues.getAsString("content");

                movieDBHelper.getWritableDatabase().rawQuery(insertMovie,null);

                movieDBHelper.close();

                break;

            case INSERT_MOVIE_TRAILER:

                //Insert Trailer info

                getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_REVIEWS_TABLE + " movie1, ",new String[]{"movie1._REVIEW_ID"},null,null,null,MovieTableConstants.MOVIE_REVIEW_ID,null );
                Cursor movie_trailer_exist = movieDBHelper.getReadableDatabase().rawQuery(getMovie, null);
                int movie_trailer_id_insert = -1;

                if(movie_trailer_exist.getCount() > 0)
                {
                    movie_id = movieDBHelper.getWritableDatabase().rawQuery("SELECT MAX(_REVIEW_ID) AS MOVIE_REV_ID FROM MOVIE_REVIEWS;",null);
                    movie_trailer_id_insert = movie_id.getInt(movie_id.getColumnIndex("MOVIE_REV_ID"))+1;
                }
                else
                {
                    movie_trailer_id_insert = MOVIE_TRAILER_ID;
                }

                insertMovie = "INSERT INTO "+MovieTableConstants.MOVIE_TRAILERS_TABLE+" ( "+MovieTableConstants.MOVIE_TRAILER_ID+" , "+MovieTableConstants.ID+" , "+MovieTableConstants.KEY+" , "+
                        MovieTableConstants.NAME+" ) VALUES ( "+movie_trailer_id_insert+" , "+contentValues.getAsInteger("movieID")+" , "+contentValues.getAsString("key")+contentValues.getAsString("name");

                movieDBHelper.getWritableDatabase().rawQuery(insertMovie,null);

                movieDBHelper.close();
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

                //Delete review
                String delete_movie = "DELETE FROM "+MovieTableConstants.MOVIE_REVIEWS_TABLE+" WHERE "+MovieTableConstants.ID+" = "+ContentUris.parseId(uri);
                movieDBHelper.getWritableDatabase().rawQuery(delete_movie,null);

                //Delete trailer
                delete_movie = "DELETE FROM "+MovieTableConstants.MOVIE_TRAILERS_TABLE+" WHERE "+MovieTableConstants.ID+" = "+ContentUris.parseId(uri);
                movieDBHelper.getWritableDatabase().rawQuery(delete_movie,null);

                //Delete movie
                delete_movie = "DELETE FROM "+MovieTableConstants.MOVIE_TABLE+" WHERE "+MovieTableConstants.ID+" = "+ContentUris.parseId(uri);
                movieDBHelper.getWritableDatabase().rawQuery(delete_movie,null);

                movieDBHelper.close();
                break;
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {

        return 0;
    }
}
