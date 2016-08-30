package com.nanodegree.project1.popularmovies;

import android.widget.ImageView;

/**
 * Created by Koushick on 30-08-2016.
 */
public class Movie {

    String originalTitle;
    String poster_path;
    String synopsis;
    double userRating;
    String releaseDate;
    long id;

    public Movie(String originalTitle, String poster_path, String synopsis, double userRating, String releaseDate, long id) {
        this.originalTitle = originalTitle;
        this.poster_path = poster_path;
        this.synopsis = synopsis;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public double getUserRating() {
        return userRating;
    }

    public void setUserRating(double userRating) {
        this.userRating = userRating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
}
