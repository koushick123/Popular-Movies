package com.nanodegree.project1.popularmovies;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by Koushick on 05-09-2016.
 */
public class MovieTrailerAndReviewLoader extends AsyncTaskLoader<Movie> {

    private static final String LOG_TAG = MovieTrailerAndReviewLoader.class.getName();
    private Movie movieDetail;

    public MovieTrailerAndReviewLoader(Context context) {
        super(context);
    }

    public MovieTrailerAndReviewLoader(Context context, Movie movie) {
        super(context);
        movieDetail = movie;
    }

    @Override
    public Movie loadInBackground()
    {
        Movie movie = retrieveMovieTrailerDetails(movieDetail,0);
        return retrieveMovieTrailerDetails(movie,1);
    }

    private Movie retrieveMovieTrailerDetails(Movie movie, int reviewOrVideoFlag)
    {
        HttpURLConnection httpURLConnection=null;
        InputStream inputStream=null;
        try
        {
            URL movie_url =  null;
            if(reviewOrVideoFlag == 0)
            {
                movie_url = new URL(MovieConstants.MOVIE_DB_BASE_URL + movie.getId() + MovieConstants.MOVIE_VIDEOS_ENDPOINT + MovieConstants.MOVIE_DB_API_KEY);
            }
            else
            {
                movie_url = new URL(MovieConstants.MOVIE_DB_BASE_URL + movie.getId() + MovieConstants.MOVIE_REVIEWS_ENDPOINT + MovieConstants.MOVIE_DB_API_KEY);
            }
                Log.d(LOG_TAG,"MovieLoader for Reviews and Trailers ==> "+movie_url.getPath());
                httpURLConnection = (HttpURLConnection) movie_url.openConnection();
                httpURLConnection.setReadTimeout(10000);
                httpURLConnection.setConnectTimeout(15000);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();
                if (httpURLConnection.getResponseCode() == 200)
                {
                    inputStream = httpURLConnection.getInputStream();
                    String jsonResp = readFromStream(inputStream);
                    if(reviewOrVideoFlag == 0)
                    {
                        return JSONUtils.extractMovieTrailerDetails(jsonResp, movie);
                    }
                    else
                    {
                        return JSONUtils.extractMovieReviewDetails(jsonResp, movie);
                    }
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(httpURLConnection != null)
            {
                httpURLConnection.disconnect();
                try {
                    if(inputStream != null)
                    {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
