package com.nanodegree.project1.popularmovies;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Koushick on 30-08-2016.
 */
public class JSONUtils {

    private static String LOG_TAG = JSONUtils.class.getName();
    public static List<Movie> extractMovieDetails(String movie_response)
    {
        List<Movie> movies = new ArrayList<Movie>();

        try {

            JSONObject rootObj = new JSONObject(movie_response);
            JSONArray results = rootObj.optJSONArray("results");
            Log.d(LOG_TAG,"Results == "+results.length());
            for(int i=0;i<results.length();i++)
            {
                JSONObject resultObj = results.getJSONObject(i);
                movies.add(new Movie(resultObj.getString("original_title"),resultObj.getString("poster_path"),resultObj.getString("overview"),resultObj.getDouble("vote_average"),
                        resultObj.getString("release_date"),resultObj.getLong("id"),null));
            }

        } catch (JSONException e) {
            Log.e("JSONUtils", "Problem parsing the movie JSON results", e);
        }
        return  movies;
    }

    public static Movie extractMovieTrailerDetails(String movie_response, Movie movie)
    {
        ArrayList<String> keys = new ArrayList<String>();
        ArrayList<String> trailers = new ArrayList<String>();
        try {
            JSONObject rootObj = new JSONObject(movie_response);
            JSONArray results = rootObj.optJSONArray("results");
            Log.d(LOG_TAG,"Results for trailer and video == "+results.length());
            for(int i=0;i<results.length();i++)
            {
                JSONObject resultObj = results.getJSONObject(i);
                keys.add(resultObj.getString("key"));
                trailers.add(resultObj.getString("name"));
            }
        } catch (JSONException e) {
            Log.e("JSONUtils", "Problem parsing the movie JSON results", e);
        }
        movie.setKey(keys.toArray(new String[keys.size()]));
        movie.setTrailerName(trailers.toArray(new String[trailers.size()]));
        return  movie;
    }

    public static Movie extractMovieReviewDetails(String movie_response, Movie movie)
    {
        ArrayList<String> authors = new ArrayList<String>();
        ArrayList<String> contents = new ArrayList<String>();
        try {
            JSONObject rootObj = new JSONObject(movie_response);
            JSONArray results = rootObj.optJSONArray("results");
            Log.d(LOG_TAG,"Results for review == "+results.length());
            for(int i=0;i<results.length();i++)
            {
                JSONObject resultObj = results.getJSONObject(i);
                authors.add(resultObj.getString("author"));
                contents.add(resultObj.getString("content"));
            }
        } catch (JSONException e) {
            Log.e("JSONUtils", "Problem parsing the movie JSON results", e);
        }
        movie.setAuthors(authors.toArray(new String[authors.size()]));
        movie.setContents(contents.toArray(new String[contents.size()]));
        return  movie;
    }
}
