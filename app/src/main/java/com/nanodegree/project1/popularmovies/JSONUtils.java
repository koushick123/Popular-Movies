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
                        resultObj.getString("release_date"),resultObj.getLong("id")));
            }

        } catch (JSONException e) {
            Log.e("JSONUtils", "Problem parsing the movie JSON results", e);
        }
        return  movies;
    }
}
