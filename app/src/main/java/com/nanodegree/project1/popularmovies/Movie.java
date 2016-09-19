package com.nanodegree.project1.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Koushick on 30-08-2016.
 */
public class Movie implements Parcelable{

    private String originalTitle;
    private String poster_path;
    private byte[] movieThumbnail;
    private String synopsis;
    private double userRating;
    private String releaseDate;
    private long id;
    private long dbMovieId;
    private String[] key;
    private String[] trailerName;
    private String[] authors;
    private String[] contents;

    public Movie(String originalTitle, String poster_path, String synopsis, double userRating, String releaseDate, long id, byte[] movieThumbnail, long dbMovieId) {
        this.originalTitle = originalTitle;
        this.poster_path = poster_path;
        this.synopsis = synopsis;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
        this.id = id;
        this.movieThumbnail = movieThumbnail;
        this.dbMovieId = dbMovieId;
    }

    private Movie(Parcel in)
    {
        originalTitle = in.readString();
        poster_path = in.readString();
        synopsis = in.readString();
        userRating = in.readDouble();
        releaseDate = in.readString();
        id = in.readLong();
        if(this.movieThumbnail != null) {
            in.readByteArray(this.movieThumbnail);
        }
        dbMovieId = in.readLong();
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

    public String[] getKey() {
        return key;
    }

    public void setKey(String[] key) {
        this.key = key;
    }

    public String[] getTrailerName() {
        return trailerName;
    }

    public void setTrailerName(String[] trailerName) {
        this.trailerName = trailerName;
    }

    public String[] getAuthors() {
        return authors;
    }

    public void setAuthors(String[] authors) {
        this.authors = authors;
    }

    public String[] getContents() {
        return contents;
    }

    public void setContents(String[] contents) {
        this.contents = contents;
    }

    public byte[] getMovieThumbnail() {
        return movieThumbnail;
    }

    public void setMovieThumbnail(byte[] movieThumbnail) {
        this.movieThumbnail = movieThumbnail;
    }

    public long getDbMovieId() {
        return dbMovieId;
    }

    public void setDbMovieId(long dbMovieId) {
        this.dbMovieId = dbMovieId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(originalTitle);
        parcel.writeString(poster_path);
        parcel.writeString(synopsis);
        parcel.writeDouble(userRating);
        parcel.writeString(releaseDate);
        parcel.writeLong(id);
        if(movieThumbnail != null) {
            parcel.writeByteArray(movieThumbnail);
        }
        parcel.writeLong(dbMovieId);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }

    };
}
