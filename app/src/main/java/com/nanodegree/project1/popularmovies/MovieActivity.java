package com.nanodegree.project1.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MovieActivity extends AppCompatActivity implements MovieFragment.Callback, MovieDetailFragment.DetailCallback, MovieFavoritesFragment.Callback{

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    public static final String LOG_TAG = MovieActivity.class.getName();
    private boolean mTwoPane;
    SharedPreferences sharedPrefs;
    String sortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_movie);
        Log.d(LOG_TAG,"önCreate");
        if(findViewById(R.id.fragmentDetail) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                sortOrder = getPreferencesSetting();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentDetail, new MovieDetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
                if(sortOrder.equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value))){
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment, new MovieFavoritesFragment(), null)
                            .commit();
                }
                else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment, new MovieFragment(), null)
                            .commit();
                }
            }
        }
        else{
            mTwoPane = false;
        }
        ((MovieSelect)getApplication()).setTabletMode(mTwoPane);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG,"onResume");
        sortOrder = getPreferencesSetting();
        changeActionBar(sortOrder);
        if(sortOrder.equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value))){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, new MovieFavoritesFragment(), null)
                    .commit();
        }
        else{
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, new MovieFragment(), null)
                    .commit();
        }
    }

    private String getPreferencesSetting()
    {
        return sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sortby_settings)
        {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Movie movieDetail) {

        Log.d(LOG_TAG,"Tablet === >"+mTwoPane);
        if(mTwoPane){
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
            Bundle movie = new Bundle();
            movie.putParcelable("movieDetail",movieDetail);
            movie.putBoolean("isTwoPane",new Boolean(mTwoPane));
            movie.putBoolean("isSelected",new Boolean(true));
            movieDetailFragment.setArguments(movie);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentDetail, movieDetailFragment, null)
                    .commit();
        }
        else{
            Intent movieDetailActivity = new Intent(this, MovieDetailActivity.class);
            movieDetailActivity.putExtra("movieDetail",movieDetail);
            movieDetailActivity.putExtra("isTwoPane",mTwoPane);
            startActivity(movieDetailActivity);
        }
    }

    @Override
    public void onItemRemove(){

        Log.d(LOG_TAG,"onItemRemove");
        if(mTwoPane){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentDetail, new MovieDetailFragment(), null)
                    .commit();

            if(getPreferencesSetting().equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value))){
                MovieFavoritesFragment movieFavoritesFragment = new MovieFavoritesFragment();
                Bundle delete = new Bundle();
                delete.putBoolean("deleteMovie", new Boolean(true));
                movieFavoritesFragment.setArguments(delete);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, movieFavoritesFragment, null)
                        .commit();
            }
            else {
                MovieFragment movieFragment = new MovieFragment();
                Bundle delete = new Bundle();
                delete.putBoolean("deleteMovie", new Boolean(true));
                movieFragment.setArguments(delete);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, movieFragment, null)
                        .commit();
            }
        }
    }

    private void changeActionBar(String sortOrder)
    {
        if(sortOrder.equalsIgnoreCase("top_rated")){
            this.setTitle("Top Rated Movies");
        }
        else if(sortOrder.equalsIgnoreCase("popular")){
            this.setTitle("Popular Movies");
        }
        else{
            this.setTitle("My Favorites");
        }
    }

    @Override
    public void onFavItemSelected(Movie movieBundle) {

        Log.d(LOG_TAG,"Fav item selected mTwopane == "+mTwoPane);
        if(mTwoPane) {
            MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
            Bundle movie = new Bundle();
            movie.putParcelable("movieDetail", movieBundle);
            movie.putBoolean("isSelected", new Boolean(true));
            movie.putBoolean("isTwoPane", new Boolean(mTwoPane));
            movieDetailFragment.setArguments(movie);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentDetail, movieDetailFragment, null)
                    .commit();
        }
        else{
            Intent movieDetailActivity = new Intent(this, MovieDetailActivity.class);
            movieDetailActivity.putExtra("movieDetail",movieBundle);
            movieDetailActivity.putExtra("isTwoPane",mTwoPane);
            startActivity(movieDetailActivity);
        }
    }
}
