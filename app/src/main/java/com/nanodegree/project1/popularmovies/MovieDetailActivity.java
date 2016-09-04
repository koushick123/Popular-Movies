package com.nanodegree.project1.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Log.d(MovieDetailActivity.class.getName(),"SAVED INSTANCE STATE == "+((Movie)getIntent().getParcelableExtra("movieDetail")).getOriginalTitle());
        MovieDetailFragment movieDetailFragment = (MovieDetailFragment)getFragmentManager().findFragmentById(R.id.fragmentDetail);
        if(movieDetailFragment == null)
        {
            movieDetailFragment = new MovieDetailFragment();
            Bundle movie = new Bundle();
            movie.putParcelable("movieDetail",getIntent().getParcelableExtra("movieDetail"));
            movieDetailFragment.setArguments(movie);
            getFragmentManager().beginTransaction()
                    .add(R.id.fragmentDetail, movieDetailFragment, null)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Toast.makeText(this,"Clicking back == "+id,Toast.LENGTH_LONG).show();
        return super.onOptionsItemSelected(item);
    }
}
