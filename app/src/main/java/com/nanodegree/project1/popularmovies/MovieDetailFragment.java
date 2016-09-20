package com.nanodegree.project1.popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import com.nanodegree.project1.popularmovies.data.MovieTableConstants;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;

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
    ImageView favStar;
    LinearLayout movieDetails;
    Movie trailerAndReviewInfoMovie;
    SharedPreferences sharedPrefs;
    Boolean addedToFav;
    int dbMovieIdInsertDelete = -1;
    int movieId;
    Movie movieDisplay;
    TableRow line;
    byte[] moviePoster;

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
        movieDetails = (LinearLayout)rootView.findViewById(R.id.movieDetails);
        favStar = (ImageView)rootView.findViewById(R.id.favoriteStar);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        line = (TableRow)rootView.findViewById(R.id.hr);
        if(savedInstanceState == null)
        {
            movieBundle = getArguments();
            movieDisplay = (Movie)movieBundle.getParcelable("movieDetail");
            moviePoster = movieBundle.getByteArray("movieThumbnail");
            Log.d(LOG_TAG,movieBundle+"  oncreateview "+moviePoster);
        }
        else
        {
            addedToFav = savedInstanceState.getBoolean("addedToFav");
            dbMovieIdInsertDelete = savedInstanceState.getInt("selectedDbMovieId");
            movieBundle = savedInstanceState.getParcelable("movieInfo");
            movieDisplay = ((Movie)movieBundle.getParcelable("movieDetail"));
            moviePoster = savedInstanceState.getByteArray("movieThumbnailImage");
            trailerAndReviewInfoMovie = savedInstanceState.getParcelable("movieTrailer");
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
        Log.d(LOG_TAG,"onResume ==>"+trailerAndReviewInfoMovie);
        Log.d(LOG_TAG,"resume preference setting == "+getPreferencesSetting());
        if(trailerAndReviewInfoMovie == null && !getPreferencesSetting().equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value)))
        {
            if(checkIfInternetIsAvailable())
            {
                checkIfMoviesNeedToBeRefreshed();
            }
        }
    }

    ImageView.OnClickListener favStarListener = new ImageView.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            if(favStar.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_star_border_black_24dp).getConstantState())
            {
                //Get Movie details
                ContentValues movie = new ContentValues();
                movie.put("heading",movieDisplay.getOriginalTitle());

                //Extract Bitmap from thumbnail and convert to byte array
                movieThumbnail.buildDrawingCache();
                Bitmap bitmap = movieThumbnail.getDrawingCache();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);

                movie.put("thumbnail",byteArrayOutputStream.toByteArray());
                movie.put("releaseDate",movieDisplay.getReleaseDate());
                movie.put("userRating",movieDisplay.getUserRating());
                movie.put("synopsis",movieDisplay.getSynopsis());
                movie.put("movieID",movieDisplay.getId());
                getActivity().getContentResolver().insert(Uri.parse(MovieTableConstants.BASE_CONTENT_URI+"/addMovie"),movie);

                //Get Movie trailer
                Cursor max_movie_id = getActivity().getContentResolver().query(Uri.parse(MovieTableConstants.BASE_CONTENT_URI+"/getMaxMovieId/"), null, null, null, null);
                if(trailerAndReviewInfoMovie.getTrailerName().length > 0)
                {
                    ContentValues[] trailers = new ContentValues[trailerAndReviewInfoMovie.getTrailerName().length];
                    if(max_movie_id == null)
                    {
                        max_movie_id = getActivity().getContentResolver().query(Uri.parse(MovieTableConstants.BASE_CONTENT_URI+"/getMaxMovieId/"), null, null, null, null);
                    }
                    max_movie_id.moveToFirst();

                    for (int i = 0; i < trailerAndReviewInfoMovie.getTrailerName().length; i++)
                    {
                        trailers[i] = new ContentValues();
                        trailers[i].put("name", trailerAndReviewInfoMovie.getTrailerName()[i]);
                        trailers[i].put("key", trailerAndReviewInfoMovie.getKey()[i]);
                        trailers[i].put("movieID", max_movie_id.getInt(max_movie_id.getColumnIndex("MOVIE_ID")));
                    }
                    getActivity().getContentResolver().bulkInsert(Uri.parse(MovieTableConstants.BASE_CONTENT_URI+"/addMovie/trailer"), trailers);
                }

                //Get Movie review
                if(trailerAndReviewInfoMovie.getAuthors().length > 0)
                {
                    ContentValues[] reviews = new ContentValues[trailerAndReviewInfoMovie.getAuthors().length];
                    if(max_movie_id == null)
                    {
                        max_movie_id = getActivity().getContentResolver().query(Uri.parse(MovieTableConstants.BASE_CONTENT_URI+"/getMaxMovieId/"), null, null, null, null);
                    }
                    max_movie_id.moveToFirst();

                    for (int i = 0; i < trailerAndReviewInfoMovie.getAuthors().length; i++)
                    {
                        reviews[i] = new ContentValues();
                        reviews[i].put("author", trailerAndReviewInfoMovie.getAuthors()[i]);
                        reviews[i].put("content", trailerAndReviewInfoMovie.getContents()[i]);
                        reviews[i].put("movieID", max_movie_id.getInt(max_movie_id.getColumnIndex("MOVIE_ID")));
                    }
                    getActivity().getContentResolver().bulkInsert(Uri.parse(MovieTableConstants.BASE_CONTENT_URI+"/addMovie/review"), reviews);
                }
                dbMovieIdInsertDelete = max_movie_id.getInt(max_movie_id.getColumnIndex("MOVIE_ID"));
                Log.d(LOG_TAG,"Added movie ID "+dbMovieIdInsertDelete+" to fav");
                max_movie_id.close();

                Cursor allMovies = getActivity().getContentResolver().query(Uri.parse(MovieTableConstants.BASE_CONTENT_URI+"/allMovies"), null, null, null, null);
                allMovies.moveToFirst();
                if(allMovies.getCount() > 0) {
                    do
                    {
                        Log.d(LOG_TAG, "MOVIES ==== " + allMovies.getString(allMovies.getColumnIndex("heading")));
                        Log.d(LOG_TAG, "MOVIES ID ==== " + allMovies.getInt(allMovies.getColumnIndex("_ID")));
                    }while(allMovies.moveToNext());
                }

                Toast.makeText(getActivity().getApplicationContext(),"Added "+movieDisplay.getOriginalTitle().toUpperCase()+" to favorites",Toast.LENGTH_SHORT).show();
                favStar.setImageResource(R.drawable.ic_grade_black_24dp);
                addedToFav = true;
            }
            else
            {
                if(dbMovieIdInsertDelete != -1) {
                    Log.d(LOG_TAG,"Deleting movie id "+dbMovieIdInsertDelete);
                    int no_of_records = getActivity().getContentResolver().delete(Uri.parse(MovieTableConstants.BASE_CONTENT_URI + "/deleteMovie/" + dbMovieIdInsertDelete), null, null);
                    Toast.makeText(getActivity().getApplicationContext(), "Deleted " + ((Movie) movieBundle.getParcelable("movieDetail")).getOriginalTitle().toUpperCase() + " from favorites", Toast.LENGTH_SHORT).show();
                    favStar.setImageResource(R.drawable.ic_star_border_black_24dp);
                    dbMovieIdInsertDelete = -1;
                    addedToFav = false;
                }
            }
        }
    };

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
                        Log.d(LOG_TAG,"Preference setting == "+getPreferencesSetting());
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED && !getPreferencesSetting().equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value)))
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
        if(trailerAndReviewInfoMovie != null)
        {
            outState.putParcelable("movieTrailer",trailerAndReviewInfoMovie);
        }
        outState.putBoolean("addedToFav",addedToFav.booleanValue());
        outState.putInt("selectedDbMovieId",dbMovieIdInsertDelete);
        outState.putByteArray("movieThumbnailImage",moviePoster);
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
        displayMovieDetails(movieBundle);
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
        if(trailerAndReviewInfoMovie != null)
        {
            displayMovieDetails(movieBundle);
            displayMovieTrailerAndReviewDetails(trailerAndReviewInfoMovie);
        }
        else
        {
            favStar.setVisibility(View.GONE);
            movieHeading.setVisibility(View.GONE);
            line.setVisibility(View.GONE);
            trailerHeading.setVisibility(View.GONE);
            if(!getPreferencesSetting().equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value))) {
                checkAndLoadMovies();
            }
        }

        Log.d(LOG_TAG,"addedToFav is == "+addedToFav);
        Log.d(LOG_TAG,"pref. setting == "+getPreferencesSetting());

        if(!getPreferencesSetting().equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value))) {
            //Check if movie already added to DB, to set the favorite button accordingly
            Cursor allMovies = getActivity().getContentResolver().query(Uri.parse(MovieTableConstants.BASE_CONTENT_URI + "/allMovies"), null, null, null, null);

            if (addedToFav == null) {

                Log.d(LOG_TAG, allMovies.getCount() + "");
                if (allMovies.getCount() == 0) {
                    addedToFav = false;
                    favStar.setImageResource(R.drawable.ic_star_border_black_24dp);
                } else {
                    long selectedMovieId = ((Movie) movieBundle.getParcelable("movieDetail")).getId();
                    Log.d(LOG_TAG, "selected movie " + selectedMovieId);
                    allMovies.moveToFirst();
                    do {
                        movieId = allMovies.getInt(allMovies.getColumnIndex(MovieTableConstants.MOVIE_ID));
                        if (selectedMovieId == movieId) {
                            dbMovieIdInsertDelete = allMovies.getInt(allMovies.getColumnIndex(MovieTableConstants.ID));
                            addedToFav = true;
                            Log.d(LOG_TAG, "movie exists in favorite " + dbMovieIdInsertDelete);
                            favStar.setImageResource(R.drawable.ic_grade_black_24dp);
                            break;
                        }
                        addedToFav = false;
                        dbMovieIdInsertDelete = -1;
                        Log.d(LOG_TAG, "movie does not exist in favorite ");
                        favStar.setImageResource(R.drawable.ic_star_border_black_24dp);
                    } while (allMovies.moveToNext());
                    Log.d(LOG_TAG, "inside if");
                }
            } else if (addedToFav.booleanValue() == true) {

                Log.d(LOG_TAG, "movie exists " + dbMovieIdInsertDelete);
                favStar.setImageResource(R.drawable.ic_grade_black_24dp);
            } else {

                Log.d(LOG_TAG, "movie NOT exists in favorite ");
                favStar.setImageResource(R.drawable.ic_star_border_black_24dp);
            }
        }
        else{
            addedToFav = true;
            favStar.setImageResource(R.drawable.ic_grade_black_24dp);
            Cursor allMovies = getActivity().getContentResolver().query(Uri.parse(MovieTableConstants.BASE_CONTENT_URI + "/movie_id/"+movieDisplay.getDbMovieId()),
                    null, null, null, null);
            Log.d(LOG_TAG,"Movie DB ID == "+movieDisplay.getDbMovieId());

            if(trailerAndReviewInfoMovie == null) {
                allMovies.moveToFirst();
                dbMovieIdInsertDelete = (int)movieDisplay.getDbMovieId();
                ArrayList<String> keys = new ArrayList<String>();
                ArrayList<String> names = new ArrayList<String>();
                ArrayList<String> authors = new ArrayList<String>();
                ArrayList<String> contents = new ArrayList<String>();
                do {
                    String temp = allMovies.getString(allMovies.getColumnIndex(MovieTableConstants.KEY));
                    Log.d(LOG_TAG,"Key = "+temp);
                    if (keys.size() > 0) {
                        if (!keys.contains(temp)) {
                            keys.add(temp);
                        }
                    }
                    else if(keys.size() == 0){
                        keys.add(temp);
                    }

                    temp = allMovies.getString(allMovies.getColumnIndex(MovieTableConstants.NAME));
                    Log.d(LOG_TAG,"Name = "+temp);
                    if (names.size() > 0) {
                        if (!names.contains(temp)) {
                            names.add(temp);
                        }
                    }
                    else if(names.size() == 0){
                        names.add(temp);
                    }

                    temp = allMovies.getString(allMovies.getColumnIndex(MovieTableConstants.AUTHOR));
                    Log.d(LOG_TAG,"Author = "+temp);
                    if (authors.size() > 0) {
                        if (!authors.contains(temp)) {
                            authors.add(temp);
                        }
                    }
                    else if(authors.size() == 0){
                        authors.add(temp);
                    }

                    temp = allMovies.getString(allMovies.getColumnIndex(MovieTableConstants.CONTENT));
                    Log.d(LOG_TAG,"Content = "+temp);
                    if (contents.size() > 0) {
                        if (!contents.contains(temp)) {
                            contents.add(temp);
                        }
                    }
                    else if(contents.size() == 0){
                        contents.add(temp);
                    }
                } while (allMovies.moveToNext());

                trailerAndReviewInfoMovie = new Movie((movieDisplay.getOriginalTitle()),null,(movieDisplay.getSynopsis()),
                        (movieDisplay.getUserRating()),(movieDisplay.getReleaseDate()),
                        (movieDisplay.getId()),(movieDisplay.getMovieThumbnail()),
                        movieDisplay.getDbMovieId());
                Collections.reverse(contents);
                trailerAndReviewInfoMovie.setContents(contents.toArray(new String[contents.size()]));
                trailerAndReviewInfoMovie.setKey(keys.toArray(new String[keys.size()]));
                trailerAndReviewInfoMovie.setTrailerName(names.toArray(new String[names.size()]));
                Collections.reverse(authors);
                trailerAndReviewInfoMovie.setAuthors(authors.toArray(new String[authors.size()]));
                displayMovieDetails(movieBundle);
                displayMovieTrailerAndReviewDetails(trailerAndReviewInfoMovie);
            }
        }
        favStar.setOnClickListener(favStarListener);
    }

    private void setEmptyListView(String msg)
    {
        if(msg.equalsIgnoreCase(MovieConstants.NO_DATA_FOUND))
        {
            placeHolderImage.setVisibility(View.VISIBLE);
            placeHolderImage.setImageResource(R.drawable.no_data);
            movieDetails.setVisibility(View.INVISIBLE);
        }
        else if(msg.equalsIgnoreCase(MovieConstants.EMPTY_TEXT))
        {
            placeHolderImage.setVisibility(View.INVISIBLE);
            movieDetails.setVisibility(View.VISIBLE);
        }
        else if(msg.equalsIgnoreCase(MovieConstants.NO_INT_CONN))
        {
            placeHolderImage.setVisibility(View.VISIBLE);
            placeHolderImage.setImageResource(R.drawable.no_internet_connection_message);
            movieDetails.setVisibility(View.INVISIBLE);
        }
        spinner.setVisibility(View.INVISIBLE);
    }

    private void displayMovieDetails(Bundle movie)
    {
        if(favStar.getVisibility() == View.GONE && movieHeading.getVisibility() == View.GONE && line.getVisibility() == View.GONE && trailerHeading.getVisibility() == View.GONE) {
            favStar.setVisibility(View.VISIBLE);
            movieHeading.setVisibility(View.VISIBLE);
            line.setVisibility(View.VISIBLE);
            trailerHeading.setVisibility(View.VISIBLE);
        }

        movieHeading.setText(((Movie)movie.getParcelable("movieDetail")).getOriginalTitle());
        if(((Movie)movie.getParcelable("movieDetail")).getPoster_path() != null) {
            Picasso.with(getActivity().getApplicationContext()).load(MovieConstants.BASE_PICASSO_URL + MovieConstants.IMAGE_SIZE + ((Movie) movie.getParcelable("movieDetail")).getPoster_path()).into(movieThumbnail);
        }
        else{
            Bitmap movieBm = BitmapFactory.decodeByteArray(moviePoster,0,moviePoster.length);
            movieThumbnail.setScaleY(0.9F);
            movieThumbnail.setScaleX(0.9F);
            movieThumbnail.setImageBitmap(movieBm);
        }
        movieReleaseDate.setText(((Movie)movie.getParcelable("movieDetail")).getReleaseDate());
        movieUserRating.setText(((Movie)movie.getParcelable("movieDetail")).getUserRating()+"/10");
        movieSynopsis.setText(((Movie)movie.getParcelable("movieDetail")).getSynopsis());
    }

    private void displayMovieTrailerAndReviewDetails(final Movie movieTrailerAndReview)
    {
        if(movieTrailerAndReview != null)
        {
            if(movieTrailerAndReview.getTrailerName().length > 0)
            {
                for (int i = 0; i < movieTrailerAndReview.getTrailerName().length; i++) {
                    //Create Linearlayout
                    LinearLayout linearLayout = new LinearLayout(getActivity());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    linearLayout.setLayoutParams(params);

                    final String key = movieTrailerAndReview.getKey()[i];

                    //Add media player image view
                    final ImageView trailerPlayer = new ImageView(getActivity());
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins((int) getResources().getDimension(R.dimen.mediaPlayerMarginLeft), (int) getResources().getDimension(R.dimen.mediaPlayerMarginTop),
                            (int) getResources().getDimension(R.dimen.mediaPlayerMarginRight), (int) getResources().getDimension(R.dimen.mediaPlayerMarginBottom));
                    params.gravity = Gravity.CENTER;
                    trailerPlayer.setImageResource(R.drawable.color_change);
                    trailerPlayer.setLayoutParams(params);
                    linearLayout.addView(trailerPlayer);
                    trailerPlayer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MovieConstants.YOUTUBE_LINK + key));
                            startActivity(youtubeIntent);
                        }
                    });

                    //Create a Trailer Info
                    TextView textView = new TextView(getActivity());
                    textView.setTextSize(getResources().getDimension(R.dimen.trailerAndReviewTextSize));
                    textView.setText(movieTrailerAndReview.getTrailerName()[i]);
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.CENTER;
                    params.setMargins((int) getResources().getDimension(R.dimen.trailerTextMarginLeft), (int) getResources().getDimension(R.dimen.trailerTextMarginTop),
                            (int) getResources().getDimension(R.dimen.trailerTextMarginRight), (int) getResources().getDimension(R.dimen.trailerTextMarginBottom));
                    textView.setLayoutParams(params);
                    textView.setGravity(Gravity.CENTER_VERTICAL);
                    linearLayout.addView(textView);

                    trailerAndReviewList.addView(linearLayout);

                    //Add line separator
                    TableRow separator = new TableRow(getActivity());
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
                    params.setMargins((int) getResources().getDimension(R.dimen.lineSeparatorMarginLeft), (int) getResources().getDimension(R.dimen.lineSeparatorMarginTop),
                            (int) getResources().getDimension(R.dimen.lineSeparatorMarginRight), (int) getResources().getDimension(R.dimen.lineSeparatorMarginBottom));
                    separator.setBackgroundColor(getResources().getColor(R.color.listDividerColor));
                    separator.setLayoutParams(params);

                    LinearLayout separatorLinearLayout = new LinearLayout(getActivity());
                    separatorLinearLayout.setId(new Integer(100));
                    separatorLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    separatorLinearLayout.setLayoutParams(params);
                    separatorLinearLayout.addView(separator);

                    if (i < movieTrailerAndReview.getTrailerName().length - 1) {
                        trailerAndReviewList.addView(separatorLinearLayout);
                    }
                }
            }
            else
            {
                TextView textView = new TextView(getActivity());
                textView.setTextSize(getResources().getDimension(R.dimen.trailerAndReviewTextSize));
                textView.setText(R.string.noTrailers);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                params.setMargins((int) getResources().getDimension(R.dimen.trailerTextMarginLeft), (int) getResources().getDimension(R.dimen.trailerTextMarginTop),
                        (int) getResources().getDimension(R.dimen.trailerTextMarginRight), (int) getResources().getDimension(R.dimen.trailerTextMarginBottom));
                textView.setLayoutParams(params);
                textView.setGravity(Gravity.CENTER);
                trailerAndReviewList.addView(textView);
            }

            //Add line separator
            TableRow separator = new TableRow(getActivity());
            LinearLayout.LayoutParams sepparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
            sepparams.setMargins((int) getResources().getDimension(R.dimen.trailerLastLineSeparatorMarginLeft), (int) getResources().getDimension(R.dimen.trailerLastLineSeparatorMarginTop),
                    (int) getResources().getDimension(R.dimen.trailerLastLineSeparatorMarginRight), (int) getResources().getDimension(R.dimen.trailerLastLineSeparatorMarginBottom));
            separator.setBackgroundColor(getResources().getColor(android.R.color.black));
            separator.setLayoutParams(sepparams);
            trailerAndReviewList.addView(separator);

            //Create Review heading
            TextView reviewText = new TextView(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins((int) getResources().getDimension(R.dimen.reviewHeadingMarginLeft), (int) getResources().getDimension(R.dimen.reviewHeadingMarginTop),
                    (int) getResources().getDimension(R.dimen.reviewHeadingMarginRight), (int) getResources().getDimension(R.dimen.reviewHeadingMarginBottom));
            reviewText.setLayoutParams(params);
            reviewText.setText(getResources().getString(R.string.reviewHeading));
            reviewText.setTextSize(getResources().getDimension(R.dimen.trailerAndReviewHeadingTextSize));
            //reviewLinearLayout.addView(reviewText);

            if(movieTrailerAndReview.getAuthors().length > 0)
            {
                for (int i = 0; i < movieTrailerAndReview.getAuthors().length; i++)
                {
                    if(i == 0)
                    {
                        trailerAndReviewList.addView(reviewText);
                    }

                    //Create Linearlayout
                    LinearLayout reviewLinearLayout = new LinearLayout(getActivity());
                    reviewLinearLayout.setOrientation(LinearLayout.VERTICAL);
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    reviewLinearLayout.setLayoutParams(params);

                    //Create Review author
                    TextView author = new TextView(getActivity());
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins((int) getResources().getDimension(R.dimen.reviewAuthorMarginLeft), (int) getResources().getDimension(R.dimen.reviewAuthorMarginTop),
                            (int) getResources().getDimension(R.dimen.reviewAuthorMarginRight), (int) getResources().getDimension(R.dimen.reviewAuthorMarginBottom));
                    author.setLayoutParams(params);
                    author.setText(movieTrailerAndReview.getAuthors()[i]);
                    author.setTypeface(null, Typeface.BOLD);
                    reviewLinearLayout.addView(author);

                    //Create Review content
                    TextView content = new TextView(getActivity());
                    LinearLayout.LayoutParams conparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    conparams.setMargins((int) getResources().getDimension(R.dimen.reviewContentMarginLeft), (int) getResources().getDimension(R.dimen.reviewContentMarginTop),
                            (int) getResources().getDimension(R.dimen.reviewContentMarginRight), (int) getResources().getDimension(R.dimen.reviewContentMarginBottom));
                    content.setLayoutParams(conparams);
                    content.setText(movieTrailerAndReview.getContents()[i]);
                    reviewLinearLayout.addView(content);

                    trailerAndReviewList.addView(reviewLinearLayout);

                    //Add line separator
                    TableRow reviewSeparator = new TableRow(getActivity());
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
                    params.setMargins((int) getResources().getDimension(R.dimen.lineSeparatorMarginLeft), (int) getResources().getDimension(R.dimen.lineSeparatorMarginTop),
                            (int) getResources().getDimension(R.dimen.lineSeparatorMarginRight), (int) getResources().getDimension(R.dimen.lineSeparatorMarginBottom));
                    reviewSeparator.setBackgroundColor(getResources().getColor(R.color.listDividerColor));
                    reviewSeparator.setLayoutParams(params);

                    LinearLayout separatorLinearLayout = new LinearLayout(getActivity());
                    separatorLinearLayout.setId(new Integer(100));
                    separatorLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    separatorLinearLayout.setLayoutParams(params);
                    separatorLinearLayout.addView(reviewSeparator);

                    trailerAndReviewList.addView(separatorLinearLayout);
                }
            }
            else
            {
                trailerAndReviewList.addView(reviewText);
                TextView textView = new TextView(getActivity());
                textView.setTextSize(getResources().getDimension(R.dimen.trailerAndReviewTextSize));
                textView.setText(R.string.noReviews);
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                params.setMargins((int) getResources().getDimension(R.dimen.trailerTextMarginLeft), (int) getResources().getDimension(R.dimen.trailerTextMarginTop),
                        (int) getResources().getDimension(R.dimen.trailerTextMarginRight), (int) getResources().getDimension(R.dimen.trailerTextMarginBottom));
                textView.setLayoutParams(params);
                textView.setGravity(Gravity.CENTER_VERTICAL);
                trailerAndReviewList.addView(textView);
            }
            trailerAndReviewInfoMovie = movieTrailerAndReview;
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
        if(movieDetails == null || movieDetails.getVisibility() == View.INVISIBLE)
        {
            Log.d(LOG_TAG,"Empty view === "+movieDetails);
            if (movieDetails == null) {
                movieDetails = (LinearLayout) getActivity().findViewById(R.id.movieDetails);
            }
            setEmptyListView(MovieConstants.EMPTY_TEXT);
            loadMovies();
        }
    }

    private String getPreferencesSetting()
    {
        return sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
    }
}
