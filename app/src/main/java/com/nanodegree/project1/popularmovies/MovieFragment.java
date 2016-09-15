package com.nanodegree.project1.popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Koushick on 01-09-2016.
 */
public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Movie>>
{
    private String MOVIE_DB_API_KEY = "?api_key=";
    private String MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3/movie/";
    public static final String LOG_TAG = MovieFragment.class.getName();
    private GridView movieListView;
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    private ProgressBar spinner;
    ImageView placeHolderImage;
    private ArrayList<Movie> allMovies;
    SharedPreferences sharedPrefs;
    String oldSortOrder;
    private String EMPTY_TEXT = "", NO_INT_CONN = "No Internet Connection", NO_DATA_FOUND = "No Data Found";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(LOG_TAG,"onCreateView == "+savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_movie,container,false);
        spinner = (ProgressBar)rootView.findViewById(R.id.spinner);
        placeHolderImage = (ImageView)rootView.findViewById(R.id.placeHolderImage);
        movieListView = (GridView)rootView.findViewById(R.id.list);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

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
            oldSortOrder = savedInstanceState.getString("oldSortOrder");
            if(movies != null)
            {
                updateMovies(movies);
            }
            else
            {
                checkAndLoadMovies();
            }
        }
        else
        {
            if(checkIfInternetIsAvailable())
            {
                checkAndLoadMovies();
            }
            else
            {
                setEmptyListView(NO_INT_CONN);
            }
            oldSortOrder = sharedPrefs.getString(
                    getString(R.string.settings_order_by_key),
                    getString(R.string.settings_order_by_default));
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
        if(oldSortOrder != null)
        {
            Log.d(LOG_TAG, "Saving SORT ORDER before destruction..." + oldSortOrder);
            outState.putString("oldSortOrder",oldSortOrder);
        }
        outState.putInt("orientation",getResources().getConfiguration().orientation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args)
    {
        Log.d(LOG_TAG,"OnCreateLoader");
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        oldSortOrder = sortOrder;
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
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
        Log.d(LOG_TAG,"Sort Order -> "+sortOrder);
        Log.d(LOG_TAG,"Old Sort Order -> "+oldSortOrder);
        if(allMovies == null || (!sortOrder.equalsIgnoreCase(oldSortOrder)))
        {
            if(checkIfInternetIsAvailable())
            {
                if(allMovies == null) {
                    checkIfMoviesNeedToBeRefreshed();
                }
                else {
                    setEmptyListView(EMPTY_TEXT);
                    reLoadMovies();
                }
            }
            else
            {
                setEmptyListView(NO_INT_CONN);
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
            int orientation = getActivity().getApplicationContext().getResources().getConfiguration().orientation;
            if(orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                movieListView.setNumColumns(3);
            }
            else if (orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                movieListView.setNumColumns(2);
            }
            setEmptyListView(EMPTY_TEXT);
            final MovieAdapter adapter = new MovieAdapter(getActivity().getApplicationContext(), allMovies);
            movieListView.setAdapter(adapter);

            movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
                {
                    Log.d(LOG_TAG,"========Item clicked......");
                    Intent movieDetailIntent = new Intent(getActivity().getApplicationContext(),MovieDetailActivity.class);
                    movieDetailIntent.putExtra("movieDetail",allMovies.get(position));
                    startActivity(movieDetailIntent);
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
        Log.d(LOG_TAG,placeHolderImage+"");
        if(msg.equalsIgnoreCase(NO_DATA_FOUND))
        {
            placeHolderImage.setVisibility(View.VISIBLE);
            placeHolderImage.setImageResource(R.drawable.no_data);
            movieListView.setEmptyView(placeHolderImage);
        }
        else if(msg.equalsIgnoreCase(EMPTY_TEXT))
        {
            placeHolderImage.setVisibility(View.INVISIBLE);
            movieListView.setEmptyView(placeHolderImage);
        }
        else if(msg.equalsIgnoreCase(NO_INT_CONN))
        {
            placeHolderImage.setVisibility(View.VISIBLE);
            placeHolderImage.setImageResource(R.drawable.no_internet_connection_message);
            movieListView.setEmptyView(placeHolderImage);
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

    private void reLoadMovies()
    {
        Log.d(LOG_TAG, "reLoading Movies");
        getLoaderManager().restartLoader(1,null,getMovieObj()).forceLoad();
        spinner.setVisibility(View.VISIBLE);
    }

    private void loadMovies()
    {
        getLoaderManager().initLoader(1, null, getMovieObj()).forceLoad();
        spinner.setVisibility(View.VISIBLE);
    }

    private void checkIfMoviesNeedToBeRefreshed()
    {
        if(movieListView == null || (movieListView.getEmptyView() != null && (((ImageView) movieListView.getEmptyView()).getVisibility() == View.VISIBLE)))
        {
            if (movieListView == null) {
                movieListView = (GridView) getActivity().findViewById(R.id.list);
            }
            setEmptyListView(EMPTY_TEXT);
            loadMovies();
        }
    }
}
