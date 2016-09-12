package com.nanodegree.project1.popularmovies.data;

/**
 * Created by Koushick on 09-09-2016.
 */
public class MovieTableConstants
{
    public static final String CONTENT_AUTHORITY = "com.nanodegree.project1.popularmovies.data";
    public static final String BASE_CONTENT_URI = "content://" + CONTENT_AUTHORITY;

    //Tables Names
    public static final String MOVIE_TABLE = "movie";
    public static final String MOVIE_REVIEWS_TABLE = "movie_reviews";
    public static final String MOVIE_TRAILERS_TABLE = "movie_trailers";

    //Movie Table Columns
    public static final String ID = "_ID";
    public static final String THUMBNAIL = "thumbnail";
    public static final String HEADING = "heading";
    public static final String SYNOPSIS = "synopsis";
    public static final String RELEASE_DATE = "release_date";
    public static final String USER_RATING = "user_rating";

    //Movie_Review Table Columns
    public static final String MOVIE_REVIEW_ID = "_REVIEW_ID";
    public static final String AUTHOR = "author";
    public static final String CONTENT = "content";

    //Movie_Trailer Table Columns
    public static final String MOVIE_TRAILER_ID = "_TRAILER_ID";
    public static final String KEY = "key";
    public static final String NAME = "name";
}
