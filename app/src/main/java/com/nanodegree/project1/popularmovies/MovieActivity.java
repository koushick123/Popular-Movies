package com.nanodegree.project1.popularmovies;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MovieActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment);
        if(fragment == null)
        {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment, new MovieFragment())
                    .commit();
        }
    }
}
