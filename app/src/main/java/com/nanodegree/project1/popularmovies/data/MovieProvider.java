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
    private static final int ALL_MOVIE = 100;
    private static final int MOVIE_WITH_ID = 200;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static
    {
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/allMovies",ALL_MOVIE);
        sUriMatcher.addURI(MovieTableConstants.CONTENT_AUTHORITY,"/movie_id/#",MOVIE_WITH_ID);
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
        int match = sUriMatcher.match(uri);
        switch (match)
        {
            case MOVIE_WITH_ID:

            String getMovie = SQLiteQueryBuilder.buildQueryString(false,MovieTableConstants.MOVIE_TABLE + " movie1, " +
                    MovieTableConstants.MOVIE_TRAILERS_TABLE + " movie_trail, " +
                    MovieTableConstants.MOVIE_REVIEWS_TABLE + " movie_rev ",new String[] {"movie1.thumbnail, movie1.heading, movie1.synopsis, movie1.release_date, movie1.user_rating, movie_trail.name, movie_trail.key, movie_rev.author, movie_rev.content"},
                    "movie1._ID = movie_trail._ID AND movie1._ID = movie_rev._ID AND movie1._ID = "+String.valueOf(ContentUris.parseId(uri)),null,null,MovieTableConstants.ID,null);

            movie = movieDBHelper.getReadableDatabase().rawQuery(getMovie, null);
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
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {

        return 0;
    }
}
