package com.nanodegree.project1.popularmovies;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.nanodegree.project1.popularmovies.data.MovieTableConstants;

/**
 * Created by koushick on 21-Nov-16.
 */
public class MovieFavoritesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private GridView movieListView;
    private ImageView placeHolderImage;
    private MovieCursorAdapter cursorAdapter;
    Boolean deleteMovie;
    String oldSortOrder;
    SharedPreferences sharedPrefs;
    boolean tabletMode;
    int selectionPosition = -1;
    Bundle savedState;
    int scrollPosition = -1;
    private Boolean isSelected = null;
    public static final String LOG_TAG = MovieFavoritesFragment.class.getName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(LOG_TAG,"onCreateView");
        if(getArguments() != null){
            deleteMovie = getArguments().getBoolean("deleteMovie");
        }
        View rootView = inflater.inflate(R.layout.fragment_favorite_movie,container,false);
        placeHolderImage = (ImageView)rootView.findViewById(R.id.placeHolderFavImage);
        movieListView = (GridView)rootView.findViewById(R.id.favList);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        cursorAdapter = new MovieCursorAdapter(getContext(),null,0);
        movieListView.setAdapter(cursorAdapter);
        selectionPosition = ((MovieSelect)getActivity().getApplicationContext()).getMoviePosition();
        scrollPosition = ((MovieSelect)getActivity().getApplicationContext()).getScrollPosition();
        if(savedInstanceState != null){
            isSelected = savedInstanceState.getBoolean("isSelected");
        }
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
        public void onFavItemSelected(Movie movieBundle);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(LOG_TAG,"onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null){
            oldSortOrder = savedInstanceState.getString("oldSortOrder");
        }
        tabletMode = ((MovieSelect) getActivity().getApplication()).isTabletMode();
        getLoaderManager().initLoader(1,null,this);
        savedState = savedInstanceState;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Save the old sort order , to get it back when fragment is resumed.
        oldSortOrder = getPreferencesSetting();
        if(oldSortOrder != null)
        {
            Log.d(LOG_TAG, "Saving SORT ORDER before destruction..." + oldSortOrder);
            outState.putString("oldSortOrder",oldSortOrder);
            ((MovieSelect) getActivity().getApplication()).setMovieSetting(oldSortOrder);
        }
        Log.d(LOG_TAG,"Position == "+selectionPosition);
        ((MovieSelect)getActivity().getApplicationContext()).setMoviePosition(selectionPosition);
        if(isSelected != null){
            outState.putBoolean("isSelected",isSelected);
        }
        ((MovieSelect)getActivity().getApplicationContext()).setScrollPosition(scrollPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG,"onResume");
        Log.d(LOG_TAG,"saved state == "+savedState);
        if(savedState == null){
            oldSortOrder = ((MovieSelect)getActivity().getApplication()).getMovieSetting();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG_TAG,"onStop");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = getPreferencesSetting();

        oldSortOrder = sortOrder;
        return new CursorLoader(getContext(), Uri.parse(MovieTableConstants.BASE_CONTENT_URI+"/allMovies"),null,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        Log.d(LOG_TAG,"onLoadFinished == "+(data!=null?data.getCount():"No data"));
        if(data.getCount() == 0){
            placeHolderImage.setVisibility(View.VISIBLE);
            placeHolderImage.setImageResource(R.drawable.no_favorites);
            movieListView.setAdapter(cursorAdapter);
            cursorAdapter.notifyDataSetChanged();
            movieListView.setEmptyView(placeHolderImage);
        }
        else {
            cursorAdapter.swapCursor(data);
        }

        int orientation = getActivity().getApplicationContext().getResources().getConfiguration().orientation;

        if(orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            movieListView.setNumColumns(3);
        }
        else if (orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            movieListView.setNumColumns(2);
        }

        String sortOrder = getPreferencesSetting();
        Log.d(LOG_TAG,"sort order === "+sortOrder+", tabletMode == "+tabletMode);
        Log.d(LOG_TAG,"old sort order === "+oldSortOrder);
        if(deleteMovie != null && deleteMovie.booleanValue()){
            if(selectionPosition == 0){
                selectionPosition++;
            }
            else if (selectionPosition > 0){
                selectionPosition--;
            }
            Log.d(LOG_TAG,"scroll to "+selectionPosition);
            movieListView.setSelection(selectionPosition);
            //Set scroll position to be same as selection, in case of delete
            scrollPosition = selectionPosition;
            //Remove selection in case of delete
            isSelected = null;
            //selectionPosition = -1;
        }
        else {
            Log.d(LOG_TAG,"selected position == "+selectionPosition);
            if (!sortOrder.equalsIgnoreCase(oldSortOrder)) {
                selectionPosition = -1;
                scrollPosition = -1;
                ((MovieSelect) getActivity().getApplicationContext()).setMoviePosition(selectionPosition);
                ((MovieSelect) getActivity().getApplicationContext()).setScrollPosition(scrollPosition);
            }

            Log.d(LOG_TAG,"scrollPosition == "+scrollPosition);
            if (((MovieSelect) getActivity().getApplication()).getScrollPosition() != -1) {
                movieListView.setSelection(scrollPosition);
                if (((MovieSelect) getActivity().getApplication()).getMoviePosition() != -1) {
                    if(isSelected != null && isSelected.booleanValue()) {
                        movieListView.setItemChecked(selectionPosition, true);
                    }
                }
            }
            /*if (((MovieSelect) getActivity().getApplication()).getMoviePosition() != -1) {
                movieListView.setSelection(selectionPosition);
                movieListView.setItemChecked(selectionPosition, true);
            }*/
        }

        movieListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                scrollPosition = firstVisibleItem;
            }
        });

        movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                Log.d(LOG_TAG,"========Fav Item clicked......");
                String title = data.getString(data.getColumnIndex(MovieTableConstants.HEADING));
                Log.d(LOG_TAG,"Title == "+title);
                String synopsis = data.getString(data.getColumnIndex(MovieTableConstants.SYNOPSIS));
                int userRating = data.getInt(data.getColumnIndex(MovieTableConstants.USER_RATING));
                String releaseDate = data.getString(data.getColumnIndex(MovieTableConstants.RELEASE_DATE));
                long Id = data.getLong(data.getColumnIndex(MovieTableConstants.MOVIE_ID));
                long dbId = data.getLong(data.getColumnIndex(MovieTableConstants.ID));
                Movie dbMovies = new Movie(title, null, synopsis, userRating, releaseDate, Id, null, dbId);
                deleteMovie = new Boolean(false);
                selectionPosition = position;
                scrollPosition = position;
                if(tabletMode) {
                    isSelected = Boolean.TRUE;
                }
                Log.d(LOG_TAG,"selection position SET == "+selectionPosition);
                ((MovieSelect)getActivity().getApplicationContext()).setMoviePosition(selectionPosition);
                ((Callback)getActivity()).onFavItemSelected(dbMovies);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG,"onLoaderReset");
        cursorAdapter.swapCursor(null);
    }

    private String getPreferencesSetting()
    {
        return sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
    }
}
