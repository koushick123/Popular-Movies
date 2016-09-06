package com.nanodegree.project1.popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Movie>
{
    public static final String LOG_TAG = MovieDetailFragment.class.getName();
    private TextView movieHeading;
    private ImageView movieThumbnail;
    private TextView movieReleaseDate;
    private TextView movieUserRating;
    private TextView movieSynopsis;
    private Bundle movieBundle;
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    private ProgressBar spinner;
    LinearLayout trailerList;
    ImageView placeHolderImage;
    Movie savedMovie;

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
        spinner = (ProgressBar)rootView.findViewById(R.id.trailerSpinner);
        trailerList = (LinearLayout)rootView.findViewById(R.id.trailerList);
        placeHolderImage = (ImageView)rootView.findViewById(R.id.trailerPlaceHolderImage);
        if(savedInstanceState == null)
        {
            movieBundle = getArguments();
            checkAndLoadMovies();
            Log.d(LOG_TAG,movieBundle+"  oncreateview");
        }
        else
        {
            movieBundle = savedInstanceState.getParcelable("movieInfo");
            savedMovie = savedInstanceState.getParcelable("movieTrailer");
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
        if(savedMovie != null)
        {
            outState.putParcelable("movieTrailer",savedMovie);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Movie> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG,"onCreateLoader");
        Movie movie = (Movie)movieBundle.getParcelable("movieDetail");
        return new MovieTrailerAndReviewLoader(getActivity().getApplicationContext(),movie);
    }

    @Override
    public void onLoadFinished(Loader<Movie> loader, Movie movies) {
        Log.d(LOG_TAG,"onLoadFinished");
        Log.d(LOG_TAG,movies.getOriginalTitle()+"");
        Log.d(LOG_TAG,movies.getId()+"");
        Log.d(LOG_TAG,movies.getKey()+"");
        Log.d(LOG_TAG,movies.getTrailerName()+"");
        Log.d(LOG_TAG,movies.getContents()+"");
        Log.d(LOG_TAG,movies.getAuthors()+"");
        displayMovieTrailerAndReviewDetails(movies);
    }

    @Override
    public void onLoaderReset(Loader<Movie> loader) {
        Log.d(LOG_TAG,"onLoaderReset");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private MovieDetailFragment getMovieObj()
    {
        return MovieDetailFragment.this;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG,"==== onActivityCreated ===="+savedInstanceState);
        Log.d(LOG_TAG,"==== onActivityCreated ==== movieBundle === "+movieBundle);
        displayMovieDetails(movieBundle);
        if(savedMovie != null)
        {
            displayMovieTrailerAndReviewDetails(savedMovie);
        }
    }

    private void setEmptyListView(String msg)
    {
        if(msg.equalsIgnoreCase(MovieConstants.NO_DATA_FOUND))
        {
            placeHolderImage.setVisibility(View.VISIBLE);
            placeHolderImage.setImageResource(R.drawable.no_data);
            //trailerList.setEmptyView(placeHolderImage);
            trailerList.setVisibility(View.INVISIBLE);
        }
        else if(msg.equalsIgnoreCase(MovieConstants.EMPTY_TEXT))
        {
            placeHolderImage.setVisibility(View.INVISIBLE);
            //trailerList.setEmptyView(placeHolderImage);
            trailerList.setVisibility(View.VISIBLE);
        }
        else if(msg.equalsIgnoreCase(MovieConstants.NO_INT_CONN))
        {
            placeHolderImage.setVisibility(View.VISIBLE);
            placeHolderImage.setImageResource(R.drawable.no_internet_connection_message);
            //trailerList.setEmptyView(placeHolderImage);
            trailerList.setVisibility(View.INVISIBLE);
        }
        spinner.setVisibility(View.INVISIBLE);
    }

    private void displayMovieDetails(Bundle movie)
    {
        movieHeading.setText(((Movie)movie.getParcelable("movieDetail")).getOriginalTitle());
        Picasso.with(getActivity().getApplicationContext()).load(MovieConstants.BASE_PICASSO_URL+MovieConstants.IMAGE_SIZE+((Movie)movie.getParcelable("movieDetail")).getPoster_path()).into(movieThumbnail);
        movieReleaseDate.setText(((Movie)movie.getParcelable("movieDetail")).getReleaseDate());
        movieUserRating.setText(((Movie)movie.getParcelable("movieDetail")).getUserRating()+"/10");
        movieSynopsis.setText(((Movie)movie.getParcelable("movieDetail")).getSynopsis());
    }

    private void displayMovieTrailerAndReviewDetails(final Movie movieTrailerAndReview)
    {
        if(movieTrailerAndReview != null)
        {
            for(int i=0;i<movieTrailerAndReview.getTrailerName().length;i++)
            {
                //Create Linearlayout
                LinearLayout linearLayout = new LinearLayout(getActivity());
                linearLayout.setId(i + 1);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                linearLayout.setLayoutParams(params);

                final String key = movieTrailerAndReview.getKey()[i];
                //Add media player image view
                ImageView trailerPlayer = new ImageView(getActivity());
                //params.setMargins(16,10,0,20);
                trailerPlayer.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                linearLayout.addView(trailerPlayer);
                trailerPlayer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getActivity(),key,Toast.LENGTH_LONG).show();
                    }
                });
                //trailerPlayer.setLayoutParams(params);
                //Create a textview
                TextView textView = new TextView(getActivity());
                textView.setTextSize(16F);
                textView.setText(movieTrailerAndReview.getTrailerName()[i]);
                //params.setMargins(16,6,0,10);
                //textView.setLayoutParams(params);
                textView.setGravity(Gravity.CENTER_VERTICAL);
                linearLayout.addView(textView);
                trailerList.addView(linearLayout);
            }
            savedMovie = movieTrailerAndReview;
            //MovieTrailerAdapter movieTrailerAdapter = new MovieTrailerAdapter(getActivity().getApplicationContext(),trailerNames);
            //trailerList.setAdapter(movieTrailerAdapter);
            setEmptyListView(MovieConstants.EMPTY_TEXT);
        }
        else
        {
            setEmptyListView(MovieConstants.NO_DATA_FOUND);
        }
    }

    private boolean checkIfInternetIsAvailable()
    {
        connectivityManager = (ConnectivityManager)getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        return ((networkInfo != null && networkInfo.isConnectedOrConnecting()) ? true : false);
    }

    private void checkAndLoadMovies()
    {
        if(checkIfInternetIsAvailable())
        {
            loadMovies();
        }
        else
        {
            setEmptyListView(MovieConstants.NO_INT_CONN);
        }
    }

    private void loadMovies()
    {
        getLoaderManager().initLoader(1, null, getMovieObj()).forceLoad();
        spinner.setVisibility(View.VISIBLE);
    }
}
