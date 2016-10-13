package com.nanodegree.project1.popularmovies;

import android.app.Application;
import android.os.Bundle;

/**
 * Created by Koushick on 10-10-2016.
 */
public class MovieSelect extends Application {

    private Movie movieInfo;
    private Bundle movieBund;
    private String movieSetting;
    private int moviePosition;

    public int getMoviePosition() {
        return moviePosition;
    }

    public void setMoviePosition(int moviePosition) {
        this.moviePosition = moviePosition;
    }

    public String getMovieSetting() {
        return movieSetting;
    }

    public void setMovieSetting(String movieSetting) {
        this.movieSetting = movieSetting;
    }

    public Bundle getMovieBund() {
        return movieBund;
    }

    public void setMovieBund(Bundle movieBund) {
        this.movieBund = movieBund;
    }

    public Movie getMovieInfo() {
        return movieInfo;
    }

    public void setMovieInfo(Movie movieInfo) {
        this.movieInfo = movieInfo;
    }
}
