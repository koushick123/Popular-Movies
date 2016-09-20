package com.nanodegree.project1.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Log.d(MovieDetailActivity.class.getName(),"SAVED INSTANCE STATE == "+getIntent().getByteArrayExtra("movieThumbnail"));
        MovieDetailFragment movieDetailFragment = (MovieDetailFragment)getFragmentManager().findFragmentById(R.id.fragmentDetail);
        if(movieDetailFragment == null)
        {
            movieDetailFragment = new MovieDetailFragment();
            Bundle movie = new Bundle();
            movie.putParcelable("movieDetail",getIntent().getParcelableExtra("movieDetail"));
            movie.putByteArray("movieThumbnail",getIntent().getByteArrayExtra("movieThumbnail"));
            movieDetailFragment.setArguments(movie);
            getFragmentManager().beginTransaction()
                    .add(R.id.fragmentDetail, movieDetailFragment, null)
                    .commit();
        }
    }
}
