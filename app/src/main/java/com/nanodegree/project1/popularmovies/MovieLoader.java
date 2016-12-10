package com.nanodegree.project1.popularmovies;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by Koushick on 30-08-2016.
 */
public class MovieLoader extends AsyncTaskLoader<List<Movie>> {

    String movie_db_url;

    public MovieLoader(Context context) {
        super(context);
    }

    public MovieLoader(Context context, String url) {
        super(context);
        this.movie_db_url=url;
    }

    @Override
    public List<Movie> loadInBackground() {
        Log.d(""+this.getClass(),"loadInBackground");
        if (this.movie_db_url != null) {
            try {
                URL movie_url = new URL(this.movie_db_url);
                HttpURLConnection httpURLConnection;
                InputStream inputStream;
                try {
                    Log.d("MovieLoader ==> ",this.movie_db_url);
                    httpURLConnection = (HttpURLConnection) movie_url.openConnection();
                    httpURLConnection.setReadTimeout(10000);
                    httpURLConnection.setConnectTimeout(15000);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.connect();
                    if (httpURLConnection.getResponseCode() == 200) {
                        inputStream = httpURLConnection.getInputStream();
                        String jsonResp = readFromStream(inputStream);
                        return JSONUtils.extractMovieDetails(jsonResp);
                    }
                }

                catch(UnknownHostException unknown){
                    unknown.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
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

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
    }
}
