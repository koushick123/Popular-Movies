package com.nanodegree.project1.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

public class MovieActivity extends AppCompatActivity implements MovieFragment.Callback, MovieDetailFragment.DetailCallback{

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    public static final String LOG_TAG = MovieActivity.class.getName();
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        if(findViewById(R.id.fragmentDetail) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragmentDetail, new MovieDetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        }
        else{
            mTwoPane = false;
        }
        ((MovieSelect)getApplication()).setTabletMode(mTwoPane);
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
            getFragmentManager().beginTransaction()
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
            GridView gridView = (GridView)findViewById(R.id.list);
            MovieAdapter movieAdapter = (MovieAdapter)gridView.getAdapter();
            movieAdapter.notifyDataSetChanged();
            gridView.refreshDrawableState();
            gridView.setItemChecked(0,false);

            MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragmentDetail, movieDetailFragment, null)
                    .commit();
        }
    }
}
