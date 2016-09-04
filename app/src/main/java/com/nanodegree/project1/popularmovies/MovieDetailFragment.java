package com.nanodegree.project1.popularmovies;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetailFragment extends Fragment
{
    String BASE_PICASSO_URL = "http://image.tmdb.org/t/p/";
    String IMAGE_SIZE = "w185/";
    public static final String LOG_TAG = MovieDetailFragment.class.getName();
    private TextView movieHeading;
    private ImageView movieThumbnail;
    private TextView movieReleaseDate;
    private TextView movieUserRating;
    private TextView movieSynopsis;
    private Bundle movieBundle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail,container,false);

        //Recreate View elements if orientation changed
        movieHeading = (TextView)rootView.findViewById(R.id.movieHeading);
        movieThumbnail = (ImageView)rootView.findViewById(R.id.movieThumbnail);
        movieReleaseDate = (TextView)rootView.findViewById(R.id.movieReleaseDate);
        movieUserRating = (TextView)rootView.findViewById(R.id.movieUserRating);
        movieSynopsis = (TextView)rootView.findViewById(R.id.movieSynopsis);
        if(savedInstanceState == null)
        {
            movieBundle = getArguments();
            Log.d(LOG_TAG,movieBundle+"  oncreateview");
        }
        else
        {
            movieBundle = savedInstanceState.getParcelable("movieInfo");
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        Log.d(LOG_TAG,"OnSaveInstanceState == "+movieBundle+"");
        if(movieBundle != null)
        {
            outState.putParcelable("movieInfo",movieBundle);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG,"==== onActivityCreated ===="+savedInstanceState);
        Log.d(LOG_TAG,"==== onActivityCreated ==== movieBundle === "+movieBundle);
        if(savedInstanceState != null)
        {
            Bundle movie = savedInstanceState.getParcelable("movieInfo");
            displayMovieDetails(movie);
        }
        else
        {
            displayMovieDetails(movieBundle);
        }
    }

    private void displayMovieDetails(Bundle movie)
    {
        movieHeading.setText(((Movie)movie.getParcelable("movieDetail")).getOriginalTitle());
        Picasso.with(getActivity().getApplicationContext()).load(BASE_PICASSO_URL+IMAGE_SIZE+((Movie)movie.getParcelable("movieDetail")).getPoster_path()).into(movieThumbnail);
        movieReleaseDate.setText(((Movie)movie.getParcelable("movieDetail")).getReleaseDate());
        movieUserRating.setText(((Movie)movie.getParcelable("movieDetail")).getUserRating()+"/10");
        movieSynopsis.setText(((Movie)movie.getParcelable("movieDetail")).getSynopsis());
    }
}
