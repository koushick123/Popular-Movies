package com.nanodegree.project1.popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Koushick on 01-09-2016.
 */
public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Movie>>
{
    private String MOVIE_DB_API_KEY = "?api_key=9c8a44d30593675f02b346eee8f66839";
    private String MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3/movie/";
    public static final String LOG_TAG = MovieFragment.class.getName();
    private GridView movieListView;
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    private ProgressBar spinner;
    TextView placeHolderText;
    private ArrayList<Movie> allMovies;
    private String EMPTY_TEXT = "", NO_INT_CONN = "No Internet Connection", NO_DATA_FOUND = "No Data Found";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(LOG_TAG,"onCreateView == "+savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_movie,container,false);
        spinner = (ProgressBar)rootView.findViewById(R.id.spinner);
        placeHolderText = (TextView)rootView.findViewById(R.id.placeholderText);
        movieListView = (GridView)rootView.findViewById(R.id.list);
        if(savedInstanceState == null)
        {
            checkAndLoadMovies();
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG,"onActivityCreated");
        if(savedInstanceState != null)
        {
            ArrayList<Movie> movies = savedInstanceState.getParcelableArrayList("parcelMovies");
            Log.d(LOG_TAG,"load movies");
            if(movies != null)
            {
                updateMovies(movies);
            }
            else if(checkIfInternetIsAvailable())
            {
                checkAndLoadMovies();
            }
            else
            {
                setEmptyListView(NO_INT_CONN);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG,"onSaveInstanceState");
        if(allMovies != null)
        {
            Log.d(LOG_TAG, "Saving data before destruction..." + allMovies.size());
            outState.putParcelableArrayList("parcelMovies", allMovies);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args)
    {
        Log.d(LOG_TAG,"OnCreateLoader");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        String modifiedUrl = MOVIE_DB_BASE_URL+sortOrder+MOVIE_DB_API_KEY;

        return new MovieLoader(getActivity().getApplicationContext(),modifiedUrl);
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movieData)
    {
        Log.d(LOG_TAG,"onLoadFinished");
        if(movieData != null)
        {
            updateMovies((ArrayList<Movie>) movieData);
        }
        else
        {
            updateMovies(new ArrayList<Movie>());
        }
    }

    private MovieFragment getMovieObj()
    {
        return MovieFragment.this;
    }

    @Override
    public void onStop()
    {
        Log.d(LOG_TAG,"OnStop");
        super.onStop();
        deRegisterConnectionReceiver();
    }

    @Override
    public void onStart()
    {
        Log.d(LOG_TAG,"OnStart");
        super.onStart();
        getActivity().registerReceiver(getBroadcastReceiver(),new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(LOG_TAG,"onResume -> "+allMovies);
        if(allMovies == null)
        {
            if(checkIfInternetIsAvailable())
            {
                checkIfMoviesNeedToBeRefreshed();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader)
    {
        Log.d(LOG_TAG,"onLoaderReset");
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    if (key.equalsIgnoreCase(getResources().getString(R.string.networkInfo)))
                    {
                        NetworkInfo networkInfo = (NetworkInfo) extras.get(key);
                        Log.d(LOG_TAG, "" + networkInfo.getState());
                        Log.d(LOG_TAG,"listView -> "+movieListView);
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED)
                        {
                            checkIfMoviesNeedToBeRefreshed();
                        }
                    }
                }
            }
        }
    };

    private void updateMovies(ArrayList<Movie> movies)
    {
        movieListView = (GridView)getActivity().findViewById(R.id.list);
        Log.d("Movies == ",""+movies);
        allMovies = movies;
        if(movies != null && movies.size() > 0)
        {
            setEmptyListView(EMPTY_TEXT);
            final MovieAdapter adapter = new MovieAdapter(getActivity().getApplicationContext(), allMovies);
            movieListView.setAdapter(adapter);

            movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
                {
                    /*Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(((Movie) adapter.getItem(position)).getUrl()));
                    startActivity(browserIntent);*/
                }
            });
        }
        else if(movies == null || movies.size() == 0)
        {
            setEmptyListView(NO_DATA_FOUND);
        }
    }

    private void setEmptyListView(String msg)
    {
        Log.d(LOG_TAG,placeHolderText+"");
        if(msg.equalsIgnoreCase(NO_DATA_FOUND))
        {
            placeHolderText.setText(getResources().getString(R.string.noDataFound));
            movieListView.setEmptyView(placeHolderText);
        }
        else if(msg.equalsIgnoreCase(EMPTY_TEXT))
        {
            placeHolderText.setText(getResources().getString(R.string.emptyData));
            movieListView.setEmptyView(placeHolderText);
        }
        else if(msg.equalsIgnoreCase(NO_INT_CONN))
        {
            placeHolderText.setText(getResources().getString(R.string.noIntConn));
            movieListView.setEmptyView(placeHolderText);
        }
        spinner.setVisibility(View.INVISIBLE);
    }

    private boolean checkIfInternetIsAvailable()
    {
        connectivityManager = (ConnectivityManager)getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        return ((networkInfo != null && networkInfo.isConnectedOrConnecting()) ? true : false);
    }

    private BroadcastReceiver getBroadcastReceiver()
    {
        return getMovieObj().broadcastReceiver;
    }

    private void deRegisterConnectionReceiver()
    {
        if(getBroadcastReceiver() != null)
        {
            try {
                getActivity().unregisterReceiver(getBroadcastReceiver());
                getMovieObj().broadcastReceiver = null;
            }
            catch (IllegalArgumentException illegal)
            {
                Log.e(LOG_TAG,illegal.getMessage());
            }
        }
    }

    private void checkAndLoadMovies()
    {
        if(checkIfInternetIsAvailable())
        {
            loadMovies();
        }
        else
        {
            setEmptyListView(NO_INT_CONN);
        }
    }

    private void loadMovies()
    {
        getLoaderManager().initLoader(1, null, getMovieObj()).forceLoad();
        spinner.setVisibility(View.VISIBLE);
    }

    private void checkIfMoviesNeedToBeRefreshed()
    {
        if(movieListView == null || (movieListView.getEmptyView() != null && !((TextView) movieListView.getEmptyView()).getText().toString().equalsIgnoreCase(EMPTY_TEXT)))
        {
            if (movieListView == null) {
                movieListView = (GridView) getActivity().findViewById(R.id.list);
            }
            setEmptyListView(EMPTY_TEXT);
            loadMovies();
        }
    }
}
