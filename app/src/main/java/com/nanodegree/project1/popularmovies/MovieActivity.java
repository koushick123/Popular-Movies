package com.nanodegree.project1.popularmovies;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MovieActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Movie>>{

    String MOVIE_DB_API_KEY = "9c8a44d30593675f02b346eee8f66839";
    String BASE_PICASSO_URL = "http://image.tmdb.org/t/p/";
    String SIZE = "w185";
    String MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3/movie/popular?api_key=";
    public static final String LOG_TAG = MovieActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLoaderManager().initLoader(1, null, getMovieObj()).forceLoad();
    }

    private MovieActivity getMovieObj()
    {
        return MovieActivity.this;
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG,"OnCreateLoader");
        return new MovieLoader(getApplicationContext(),MOVIE_DB_BASE_URL+MOVIE_DB_API_KEY);
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movieData) {
        if(movieData != null) {
            Iterator<Movie> iterator = movieData.iterator();
            while(iterator.hasNext())
            {
                Movie movie = iterator.next();
                Log.d(LOG_TAG,movie.getOriginalTitle());
                Log.d(LOG_TAG,movie.getPoster_path());
                Log.d(LOG_TAG,movie.getReleaseDate());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {

    }

    /*private void UpdateUI(ArrayList<Movie> earthQuakes)
    {
        earthquakeListView = (ListView)findViewById(R.id.list);
        Log.d("EarthQuakes == ",""+earthQuakes);
        if(earthQuakes != null && earthQuakes.size() > 0)
        {
            setEmptyListView(EMPTY_TEXT);
            final EarthQuakeAdapter adapter = new EarthQuakeAdapter(getBaseContext(), earthQuakes);
            earthquakeListView.setAdapter(adapter);

            earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
                {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(((EarthQuake) adapter.getItem(position)).getUrl()));
                    startActivity(browserIntent);
                }
            });
        }
        else if(earthQuakes == null || earthQuakes.size() == 0)
        {
            setEmptyListView(NO_DATA_FOUND);
        }
    }*/
}
