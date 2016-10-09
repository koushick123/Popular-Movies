package com.nanodegree.project1.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MovieActivity extends AppCompatActivity implements MovieFragment.Callback{

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
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
}
