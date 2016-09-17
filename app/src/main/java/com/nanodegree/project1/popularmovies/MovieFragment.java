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
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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

import com.nanodegree.project1.popularmovies.data.MovieTableConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Koushick on 01-09-2016.
 */
public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Movie>>
{
    public static final String LOG_TAG = MovieFragment.class.getName();
    private GridView movieListView;
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    private ProgressBar spinner;
    ImageView placeHolderImage;
    private ArrayList<Movie> allMovies;
    SharedPreferences sharedPrefs;
    String oldSortOrder;

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
            if(!oldSortOrder.equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value)))
            {
                if(movies != null)
                {
                    updateMovies(movies);
                }
                else
                {
                    checkAndLoadMovies();
                }
            }
        }
        else
        {
            oldSortOrder = getPreferencesSetting();
            Log.d(LOG_TAG,"Sort order == "+oldSortOrder);
            if(!oldSortOrder.equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value)))
            {
                if(checkIfInternetIsAvailable())
                {
                    checkAndLoadMovies();
                }
                else
                {
                    setEmptyListView(MovieConstants.NO_INT_CONN);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG,"onSaveInstanceState");
        if(allMovies != null && allMovies.size() > 0)
        {
            Log.d(LOG_TAG, "Saving data before destruction..." + allMovies.size());
            outState.putParcelableArrayList("parcelMovies", allMovies);
        }
        if(oldSortOrder != null)
        {
            Log.d(LOG_TAG, "Saving SORT ORDER before destruction..." + oldSortOrder);
            outState.putString("oldSortOrder",oldSortOrder);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args)
    {
        Log.d(LOG_TAG,"OnCreateLoader");
        //SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = getPreferencesSetting();

        oldSortOrder = sortOrder;
        String modifiedUrl = MovieConstants.MOVIE_DB_BASE_URL+sortOrder+MovieConstants.MOVIE_DB_API_KEY;

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
        String sortOrder = getPreferencesSetting();
        Log.d(LOG_TAG,"Sort Order -> "+sortOrder);
        Log.d(LOG_TAG,"Old Sort Order -> "+oldSortOrder);
        if(allMovies == null || (!sortOrder.equalsIgnoreCase(oldSortOrder)))
        {
            if(sortOrder.equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value)))
            {
                loadFavoriteMovies();
            }
            else if(checkIfInternetIsAvailable())
            {
                if(allMovies == null) {
                    checkIfMoviesNeedToBeRefreshed();
                }
                else {
                    setEmptyListView(MovieConstants.EMPTY_TEXT);
                    reLoadMovies();
                }
            }
            else
            {
                setEmptyListView(MovieConstants.NO_INT_CONN);
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
                        String sortOrder = getPreferencesSetting();
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED && !sortOrder.equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value)))
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
            setEmptyListView(MovieConstants.EMPTY_TEXT);
            final MovieAdapter adapter = new MovieAdapter(getActivity().getApplicationContext(), allMovies);
            movieListView.setAdapter(adapter);

            movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
                {
                    Log.d(LOG_TAG,"========Item clicked......");
                    Intent movieDetailIntent = new Intent(getActivity().getApplicationContext(),MovieDetailActivity.class);
                    Log.d(LOG_TAG,"Movie id being passed === "+allMovies.get(position).getId());
                    movieDetailIntent.putExtra("movieDetail",allMovies.get(position));
                    Log.d(LOG_TAG,"Movie id being set === "+((Movie)movieDetailIntent.getParcelableExtra("movieDetail")).getId());
                    startActivity(movieDetailIntent);
                }
            });
        }
        else if(movies == null || movies.size() == 0)
        {
            setEmptyListView(MovieConstants.NO_DATA_FOUND);
        }
    }

    private void setEmptyListView(String msg)
    {
        MovieAdapter adapter = new MovieAdapter(getActivity().getApplicationContext(),new ArrayList<Movie>());
        if(msg.equalsIgnoreCase(MovieConstants.NO_DATA_FOUND))
        {
            placeHolderImage.setVisibility(View.VISIBLE);
            placeHolderImage.setImageResource(R.drawable.no_data);
            movieListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            movieListView.setEmptyView(placeHolderImage);
        }
        else if(msg.equalsIgnoreCase(MovieConstants.EMPTY_TEXT))
        {
            placeHolderImage.setVisibility(View.GONE);
            movieListView.setEmptyView(placeHolderImage);
        }
        else if(msg.equalsIgnoreCase(MovieConstants.NO_INT_CONN))
        {
            placeHolderImage.setVisibility(View.VISIBLE);
            placeHolderImage.setImageResource(R.drawable.no_internet_connection_message);
            movieListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            movieListView.setEmptyView(placeHolderImage);
        }
        else if(msg.equalsIgnoreCase(MovieConstants.NO_FAVORITES))
        {
            placeHolderImage.setVisibility(View.VISIBLE);
            placeHolderImage.setImageResource(R.drawable.no_favorites);
            movieListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
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
            setEmptyListView(MovieConstants.NO_INT_CONN);
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
            Log.d(LOG_TAG,"Empty view === "+movieListView);
            if (movieListView == null) {
                movieListView = (GridView) getActivity().findViewById(R.id.list);
            }
            setEmptyListView(MovieConstants.EMPTY_TEXT);
            loadMovies();
        }
    }

    private void loadFavoriteMovies()
    {
        Cursor favMovies = getActivity().getContentResolver().query(Uri.parse(MovieTableConstants.BASE_CONTENT_URI+"/allMovies"),null,null,null,null);
        Log.d(LOG_TAG,"FAv movie count === "+favMovies.getCount());
        if(favMovies.getCount() == 0){
            setEmptyListView(MovieConstants.NO_FAVORITES);
        }
        else{
            favMovies.moveToFirst();
            ArrayList<Movie> myFavMovies = new ArrayList<Movie>();
            do{
                String title = favMovies.getString(favMovies.getColumnIndex(MovieTableConstants.HEADING));
                String synopsis = favMovies.getString(favMovies.getColumnIndex(MovieTableConstants.SYNOPSIS));
                int userRating = favMovies.getInt(favMovies.getColumnIndex(MovieTableConstants.USER_RATING));
                String releaseDate = favMovies.getString(favMovies.getColumnIndex(MovieTableConstants.RELEASE_DATE));
                long Id = favMovies.getLong(favMovies.getColumnIndex(MovieTableConstants.MOVIE_ID));
                byte[] thumbnail = favMovies.getBlob(favMovies.getColumnIndex(MovieTableConstants.THUMBNAIL));
                Log.d(LOG_TAG,thumbnail.toString());
                Movie dbMovies = new Movie(title,null,synopsis,userRating,releaseDate,Id,thumbnail);
                myFavMovies.add(dbMovies);
            }while(favMovies.moveToNext());
            updateMovies(myFavMovies);
        }
    }

    private String getPreferencesSetting()
    {
        return sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
    }
}
