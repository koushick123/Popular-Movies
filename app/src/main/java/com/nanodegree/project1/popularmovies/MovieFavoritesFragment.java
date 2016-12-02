package com.nanodegree.project1.popularmovies;

import android.content.SharedPreferences;
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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nanodegree.project1.popularmovies.data.MovieTableConstants;

/**
 * Created by koushick on 21-Nov-16.
 */
public class MovieFavoritesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private GridView movieListView;
    private ImageView placeHolderImage;
    private View listView;
    private MovieCursorAdapter cursorAdapter;
    Boolean deleteMovie;
    String oldSortOrder;
    SharedPreferences sharedPrefs;
    boolean tabletMode;
    int selectionPosition = -1;
    Bundle savedState;
    public static final String LOG_TAG = MovieFavoritesFragment.class.getName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(LOG_TAG,"onCreateView");
        if(getArguments() != null){
            deleteMovie = getArguments().getBoolean("deleteMovie");
        }
        View rootView = inflater.inflate(R.layout.fragment_favorite_movie,container,false);
        listView = inflater.inflate(R.layout.movie_list_item,container,false);
        placeHolderImage = (ImageView)rootView.findViewById(R.id.placeHolderFavImage);
        movieListView = (GridView)rootView.findViewById(R.id.favList);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        cursorAdapter = new MovieCursorAdapter(getContext(),null,0);
        movieListView.setAdapter(cursorAdapter);
        selectionPosition = ((MovieSelect)getActivity().getApplicationContext()).getMoviePosition();
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
        savedState = savedInstanceState;
        getLoaderManager().initLoader(1,null,this);
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
        cursorAdapter.swapCursor(data);

        String sortOrder = getPreferencesSetting();
        Log.d(LOG_TAG,"sort order === "+sortOrder+", tabletMode == "+tabletMode);
        Log.d(LOG_TAG,"old sort order === "+oldSortOrder);
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
                Log.d(LOG_TAG,"selected position == "+selectionPosition);
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
                Log.d(LOG_TAG,"========Fav Item clicked......");
                String title = data.getString(data.getColumnIndex(MovieTableConstants.HEADING));
                Log.d(LOG_TAG,"Title == "+title);
                String synopsis = data.getString(data.getColumnIndex(MovieTableConstants.SYNOPSIS));
                int userRating = data.getInt(data.getColumnIndex(MovieTableConstants.USER_RATING));
                String releaseDate = data.getString(data.getColumnIndex(MovieTableConstants.RELEASE_DATE));
                long Id = data.getLong(data.getColumnIndex(MovieTableConstants.MOVIE_ID));
                long dbId = data.getLong(data.getColumnIndex(MovieTableConstants.ID));
                byte[] thumbnail = data.getBlob(data.getColumnIndex(MovieTableConstants.THUMBNAIL));
                Movie dbMovies = new Movie(title, null, synopsis, userRating, releaseDate, Id, thumbnail, dbId);
                deleteMovie = new Boolean(false);
                selectionPosition = movieListView.getCheckedItemPosition();
                Log.d(LOG_TAG,"selection position SET == "+selectionPosition);
                ((MovieSelect)getActivity().getApplicationContext()).setMoviePosition(selectionPosition);
                Log.d(LOG_TAG,"Selected item == "+movieListView.getSelectedItemId());
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
