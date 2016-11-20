package com.nanodegree.project1.popularmovies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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
    Boolean refresh;
    String oldSortOrder;
    int selectionPosition = -1;
    View listView;
    Bundle savedState;
    Boolean deleteMovie;
    boolean tabletMode;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(LOG_TAG,"onCreateView == "+savedInstanceState);
        Log.d(LOG_TAG,getArguments()+"");
        if(getArguments() != null){
            deleteMovie = getArguments().getBoolean("deleteMovie");
        }
        View rootView = inflater.inflate(R.layout.fragment_movie,container,false);
        listView = inflater.inflate(R.layout.movie_list_item,container,false);
        spinner = (ProgressBar)rootView.findViewById(R.id.spinner);
        placeHolderImage = (ImageView)rootView.findViewById(R.id.placeHolderImage);
        movieListView = (GridView)rootView.findViewById(R.id.list);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        selectionPosition = ((MovieSelect)getActivity().getApplicationContext()).getMoviePosition();
        Log.d(LOG_TAG,"selection position GET == "+selectionPosition);
        return rootView;
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Movie movieBundle);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG,"onActivityCreated");
        tabletMode = ((MovieSelect) getActivity().getApplication()).isTabletMode();
        savedState = savedInstanceState;
        if(savedInstanceState != null)
        {
            ArrayList<Movie> movies = savedInstanceState.getParcelableArrayList("parcelMovies");
            Log.d(LOG_TAG,"load movies");
            oldSortOrder = savedInstanceState.getString("oldSortOrder");
            refresh = savedInstanceState.getBoolean("refresh");
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
            else
            {
                if(movies != null) {
                    updateMovies(movies);
                }
                else{
                    allMovies = null;
                    setEmptyListView(MovieConstants.NO_FAVORITES);
                }
            }
        }
        else
        {
            if(tabletMode){
                oldSortOrder = ((MovieSelect) getActivity().getApplication()).getMovieSetting();
                Log.d(LOG_TAG,"old Sort order === "+oldSortOrder);
                if(oldSortOrder == null){
                    oldSortOrder = getPreferencesSetting();
                }
            }
            else {
                oldSortOrder = getPreferencesSetting();
            }
            Log.d(LOG_TAG,"Old Sort order === "+oldSortOrder);
            String sortOrder = getPreferencesSetting();
            if(!sortOrder.equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value)))
            {
                if(checkIfInternetIsAvailable())
                {
                    checkAndLoadMovies();
                }
                else
                {
                    allMovies = null;
                    setEmptyListView(MovieConstants.NO_INT_CONN);
                }
            }
            else
            {
                loadFavoriteMovies();
                refresh = false;
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
        //Save the old sort order , to get it back when fragment is resumed.
        oldSortOrder = getPreferencesSetting();
        if(oldSortOrder != null)
        {
            Log.d(LOG_TAG, "Saving SORT ORDER before destruction..." + oldSortOrder);
            outState.putString("oldSortOrder",oldSortOrder);
            ((MovieSelect) getActivity().getApplication()).setMovieSetting(oldSortOrder);
        }
        if(refresh != null)
        {
            Log.d(LOG_TAG,"Refresh == "+refresh.booleanValue());
            outState.putBoolean("refresh",refresh);
        }
        Log.d(LOG_TAG,"Position == "+selectionPosition);
        ((MovieSelect)getActivity().getApplicationContext()).setMoviePosition(selectionPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args)
    {
        Log.d(LOG_TAG,"OnCreateLoader");
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
        Log.d(LOG_TAG,"OnStart -> "+savedState);
        Log.d(LOG_TAG,"Old Sort Order -> "+oldSortOrder);
        super.onStart();
        getActivity().registerReceiver(getBroadcastReceiver(),new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(LOG_TAG,"onResume -> "+allMovies);
        String sortOrder = getPreferencesSetting();
        Log.d(LOG_TAG,"saved state == "+savedState);
        if(savedState == null){
            oldSortOrder = ((MovieSelect)getActivity().getApplication()).getMovieSetting();
        }
        Log.d(LOG_TAG,"Sort Order -> "+sortOrder);
        Log.d(LOG_TAG,"Old Sort Order -> "+oldSortOrder);
        Log.d(LOG_TAG,"Resources == "+getResources());
        Log.d(LOG_TAG,"setting == "+getResources().getString(R.string.settings_order_by_favorites_value));
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
                allMovies = null;
                setEmptyListView(MovieConstants.NO_INT_CONN);
            }
        }
        else if(sortOrder.equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value)) && refresh)
        {
            loadFavoriteMovies();
            refresh = false;
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
            String sortOrder = getPreferencesSetting();
            if(oldSortOrder!=null) {
                Log.d(LOG_TAG, "oldSortOrder == " + oldSortOrder);
            }
            Log.d(LOG_TAG,sortOrder);
            final MovieAdapter adapter = new MovieAdapter(getActivity().getApplicationContext(), allMovies);
            movieListView.setAdapter(adapter);
            Log.d(LOG_TAG,"selection position == "+selectionPosition+", delete movie == "+deleteMovie);
            if(tabletMode) {
                if(deleteMovie != null && deleteMovie.booleanValue()){
                    if(selectionPosition == 0){
                        selectionPosition++;
                    }
                    else if (selectionPosition > 0){
                        selectionPosition--;
                    }
                    Log.d(LOG_TAG,"scroll to "+selectionPosition);
                    movieListView.setSelection(selectionPosition);
                }
                else {
                    if (!sortOrder.equalsIgnoreCase(oldSortOrder)) {
                        selectionPosition = 0;
                        ((MovieSelect) getActivity().getApplicationContext()).setMoviePosition(selectionPosition);
                    }

                    if (((MovieSelect) getActivity().getApplication()).getMoviePosition() != -1) {
                        movieListView.setSelection(selectionPosition);
                        movieListView.setItemChecked(selectionPosition, true);
                    } else {
                        movieListView.setItemChecked(0, false);
                    }
                }
            }
            movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
                {
                    Log.d(LOG_TAG,"========Item clicked......");
                    Movie temp = allMovies.get(position);
                    deleteMovie = new Boolean(false);
                    temp.setMovieThumbnail(null);
                    selectionPosition = movieListView.getCheckedItemPosition();
                    Log.d(LOG_TAG,"selection position SET == "+selectionPosition);
                    ((MovieSelect)getActivity().getApplicationContext()).setMoviePosition(selectionPosition);
                    Log.d(LOG_TAG,"Selected item == "+movieListView.getSelectedItemId());
                    ((Callback)getActivity()).onItemSelected(temp);
                    refresh = true;
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
            allMovies = null;
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
        Cursor favMovies = null;
        try {
            favMovies = getActivity().getContentResolver().query(Uri.parse(MovieTableConstants.BASE_CONTENT_URI+"/allMovies"),null,null,null,null);
            Log.d(LOG_TAG, "FAv movie count === " + favMovies.getCount());
            if (favMovies.getCount() == 0) {
                allMovies = null;
                setEmptyListView(MovieConstants.NO_FAVORITES);
            } else {
                favMovies.moveToFirst();
                ArrayList<Movie> myFavMovies = new ArrayList<Movie>();
                do {
                    String title = favMovies.getString(favMovies.getColumnIndex(MovieTableConstants.HEADING));
                    String synopsis = favMovies.getString(favMovies.getColumnIndex(MovieTableConstants.SYNOPSIS));
                    int userRating = favMovies.getInt(favMovies.getColumnIndex(MovieTableConstants.USER_RATING));
                    String releaseDate = favMovies.getString(favMovies.getColumnIndex(MovieTableConstants.RELEASE_DATE));
                    long Id = favMovies.getLong(favMovies.getColumnIndex(MovieTableConstants.MOVIE_ID));
                    long dbId = favMovies.getLong(favMovies.getColumnIndex(MovieTableConstants.ID));
                    byte[] thumbnail = favMovies.getBlob(favMovies.getColumnIndex(MovieTableConstants.THUMBNAIL));
                    Movie dbMovies = new Movie(title, null, synopsis, userRating, releaseDate, Id, thumbnail, dbId);
                    myFavMovies.add(dbMovies);
                } while (favMovies.moveToNext());
                updateMovies(myFavMovies);
            }
        }
        finally {
            if(favMovies != null){
                favMovies.close();
            }
        }
    }

    private String getPreferencesSetting()
    {
        return sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
    }
}
