package com.nanodegree.project1.popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import android.widget.TableRow;
import android.widget.TextView;

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
    LinearLayout trailerAndReviewList;
    ImageView placeHolderImage;
    TextView trailerHeading;
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
        trailerAndReviewList = (LinearLayout)rootView.findViewById(R.id.trailerAndReviewList);
        placeHolderImage = (ImageView)rootView.findViewById(R.id.trailerPlaceHolderImage);
        trailerHeading = (TextView)rootView.findViewById(R.id.trailerHeading);
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
    public void onStop()
    {
        Log.d(LOG_TAG,"OnStop");
        super.onStop();
        deRegisterConnectionReceiver();
    }

    @Override
    public void onStart()
    {
        Log.d(LOG_TAG,"OnStart");
        super.onStart();
        getActivity().registerReceiver(getBroadcastReceiver(),new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG,"onResume ==>"+savedMovie);
        if(savedMovie == null)
        {
            if(checkIfInternetIsAvailable())
            {
                checkIfMoviesNeedToBeRefreshed();
            }
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    if (key.equalsIgnoreCase(getResources().getString(R.string.networkInfo)))
                    {
                        NetworkInfo networkInfo = (NetworkInfo) extras.get(key);
                        Log.d(LOG_TAG, "" + networkInfo.getState());
                        Log.d(LOG_TAG,"listView -> "+trailerAndReviewList);
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED)
                        {
                            checkIfMoviesNeedToBeRefreshed();
                        }
                    }
                }
            }
        }
    };

    private BroadcastReceiver getBroadcastReceiver()
    {
        return getMovieObj().broadcastReceiver;
    }

    private void deRegisterConnectionReceiver()
    {
        if(getBroadcastReceiver() != null)
        {
            try {
                getActivity().unregisterReceiver(getBroadcastReceiver());
                getMovieObj().broadcastReceiver = null;
            }
            catch (IllegalArgumentException illegal)
            {
                Log.e(LOG_TAG,illegal.getMessage());
            }
        }
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
            if(trailerHeading.getVisibility() == View.INVISIBLE)
            {
                trailerHeading.setVisibility(View.VISIBLE);
            }
            displayMovieTrailerAndReviewDetails(savedMovie);
        }
        else
        {
            if(checkIfInternetIsAvailable())
            {
                loadMovies();
            }
            else
            {
                trailerHeading.setVisibility(View.INVISIBLE);
                setEmptyListView(MovieConstants.NO_INT_CONN);
            }
        }
    }

    private void setEmptyListView(String msg)
    {
        if(msg.equalsIgnoreCase(MovieConstants.NO_DATA_FOUND))
        {
            placeHolderImage.setVisibility(View.VISIBLE);
            placeHolderImage.setImageResource(R.drawable.no_data);
            trailerAndReviewList.setVisibility(View.INVISIBLE);
        }
        else if(msg.equalsIgnoreCase(MovieConstants.EMPTY_TEXT))
        {
            placeHolderImage.setVisibility(View.INVISIBLE);
            trailerAndReviewList.setVisibility(View.VISIBLE);
        }
        else if(msg.equalsIgnoreCase(MovieConstants.NO_INT_CONN))
        {
            placeHolderImage.setVisibility(View.VISIBLE);
            placeHolderImage.setImageResource(R.drawable.no_internet_connection_message);
            trailerAndReviewList.setVisibility(View.INVISIBLE);
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
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setLayoutParams(params);

                final String key = movieTrailerAndReview.getKey()[i];

                //Add media player image view
                final ImageView trailerPlayer = new ImageView(getActivity());
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins((int)getResources().getDimension(R.dimen.mediaPlayerMarginLeft),(int)getResources().getDimension(R.dimen.mediaPlayerMarginTop),
                        (int)getResources().getDimension(R.dimen.mediaPlayerMarginRight),(int)getResources().getDimension(R.dimen.mediaPlayerMarginBottom));
                params.gravity = Gravity.CENTER;
                trailerPlayer.setImageResource(R.drawable.color_change);
                trailerPlayer.setLayoutParams(params);
                linearLayout.addView(trailerPlayer);
                trailerPlayer.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MovieConstants.YOUTUBE_LINK+key));
                        startActivity(youtubeIntent);
                    }
                });

                //Create a Trailer Info
                TextView textView = new TextView(getActivity());
                textView.setTextSize(getResources().getDimension(R.dimen.trailerAndReviewTextSize));
                textView.setText(movieTrailerAndReview.getTrailerName()[i]);
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                params.setMargins((int)getResources().getDimension(R.dimen.trailerTextMarginLeft),(int)getResources().getDimension(R.dimen.trailerTextMarginTop),
                        (int)getResources().getDimension(R.dimen.trailerTextMarginRight),(int)getResources().getDimension(R.dimen.trailerTextMarginBottom));
                textView.setLayoutParams(params);
                textView.setGravity(Gravity.CENTER_VERTICAL);
                linearLayout.addView(textView);

                trailerAndReviewList.addView(linearLayout);

                //Add line separator
                TableRow separator = new TableRow(getActivity());
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,1);
                params.setMargins((int)getResources().getDimension(R.dimen.lineSeparatorMarginLeft),(int)getResources().getDimension(R.dimen.lineSeparatorMarginTop),
                        (int)getResources().getDimension(R.dimen.lineSeparatorMarginRight),(int)getResources().getDimension(R.dimen.lineSeparatorMarginBottom));
                separator.setBackgroundColor(getResources().getColor(R.color.listDividerColor));
                separator.setLayoutParams(params);

                LinearLayout separatorLinearLayout = new LinearLayout(getActivity());
                separatorLinearLayout.setId(new Integer(100));
                separatorLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                separatorLinearLayout.setLayoutParams(params);
                separatorLinearLayout.addView(separator);

                if(i < movieTrailerAndReview.getTrailerName().length-1)
                {
                    trailerAndReviewList.addView(separatorLinearLayout);
                }
            }

            if(movieTrailerAndReview.getAuthors().length > 0)
            {
                //Add line separator
                TableRow separator = new TableRow(getActivity());
                LinearLayout.LayoutParams sepparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
                sepparams.setMargins((int)getResources().getDimension(R.dimen.trailerLastLineSeparatorMarginLeft),(int)getResources().getDimension(R.dimen.trailerLastLineSeparatorMarginTop),
                        (int)getResources().getDimension(R.dimen.trailerLastLineSeparatorMarginRight),(int)getResources().getDimension(R.dimen.trailerLastLineSeparatorMarginBottom));
                separator.setBackgroundColor(getResources().getColor(android.R.color.black));
                separator.setLayoutParams(sepparams);
                trailerAndReviewList.addView(separator);
            }

            for(int i=0;i<movieTrailerAndReview.getAuthors().length;i++)
            {
                //Create Linearlayout
                LinearLayout reviewLinearLayout = new LinearLayout(getActivity());
                reviewLinearLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                reviewLinearLayout.setLayoutParams(params);

                //Create Review heading
                TextView reviewText = new TextView(getActivity());
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins((int)getResources().getDimension(R.dimen.reviewHeadingMarginLeft),(int)getResources().getDimension(R.dimen.reviewHeadingMarginTop),
                        (int)getResources().getDimension(R.dimen.reviewHeadingMarginRight),(int)getResources().getDimension(R.dimen.reviewHeadingMarginBottom));
                reviewText.setLayoutParams(params);
                reviewText.setText(getResources().getString(R.string.reviewHeading));
                reviewText.setTextSize(getResources().getDimension(R.dimen.trailerAndReviewHeadingTextSize));
                if(i == 0)
                {
                    reviewLinearLayout.addView(reviewText);
                }

                //Create Review author
                TextView author = new TextView(getActivity());
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins((int)getResources().getDimension(R.dimen.reviewAuthorMarginLeft),(int)getResources().getDimension(R.dimen.reviewAuthorMarginTop),
                        (int)getResources().getDimension(R.dimen.reviewAuthorMarginRight),(int)getResources().getDimension(R.dimen.reviewAuthorMarginBottom));
                author.setLayoutParams(params);
                author.setText(movieTrailerAndReview.getAuthors()[i]);
                author.setTypeface(null,Typeface.BOLD);
                reviewLinearLayout.addView(author);

                //Create Review content
                TextView content = new TextView(getActivity());
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins((int)getResources().getDimension(R.dimen.reviewContentMarginLeft),(int)getResources().getDimension(R.dimen.reviewContentMarginTop),
                        (int)getResources().getDimension(R.dimen.reviewContentMarginRight),(int)getResources().getDimension(R.dimen.reviewContentMarginBottom));
                content.setLayoutParams(params);
                content.setText(movieTrailerAndReview.getContents()[i]);
                reviewLinearLayout.addView(content);

                trailerAndReviewList.addView(reviewLinearLayout);

                //Add line separator
                TableRow reviewSeparator = new TableRow(getActivity());
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,1);
                params.setMargins((int)getResources().getDimension(R.dimen.lineSeparatorMarginLeft),(int)getResources().getDimension(R.dimen.lineSeparatorMarginTop),
                        (int)getResources().getDimension(R.dimen.lineSeparatorMarginRight),(int)getResources().getDimension(R.dimen.lineSeparatorMarginBottom));
                reviewSeparator.setBackgroundColor(getResources().getColor(R.color.listDividerColor));
                reviewSeparator.setLayoutParams(params);

                LinearLayout separatorLinearLayout = new LinearLayout(getActivity());
                separatorLinearLayout.setId(new Integer(100));
                separatorLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                separatorLinearLayout.setLayoutParams(params);
                separatorLinearLayout.addView(reviewSeparator);

                trailerAndReviewList.addView(separatorLinearLayout);
            }
            savedMovie = movieTrailerAndReview;
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

    private void checkIfMoviesNeedToBeRefreshed()
    {
        if(trailerAndReviewList == null || trailerAndReviewList.getVisibility() == View.INVISIBLE)
        {
            Log.d(LOG_TAG,"Empty view === "+trailerAndReviewList);
            if (trailerAndReviewList == null) {
                trailerAndReviewList = (LinearLayout) getActivity().findViewById(R.id.trailerAndReviewList);
            }
            setEmptyListView(MovieConstants.EMPTY_TEXT);
            loadMovies();
        }
    }
}
