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
    String oldSortOrder;
    Boolean tabletMode;
    Boolean isSelected;

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
        Log.d(LOG_TAG,"onCreateView for Movie Detail");

        if(savedInstanceState == null)
        {
            movieBundle = getArguments();
            if(movieBundle != null) {
                movieDisplay = (Movie) movieBundle.getParcelable("movieDetail");
                oldSortOrder = getPreferencesSetting();
                if(((Boolean)movieBundle.getBoolean("isTwoPane")) != null) {
                    tabletMode = (Boolean) movieBundle.getBoolean("isTwoPane");
                    Log.d(LOG_TAG,"Tab mode == "+tabletMode.booleanValue());
                }
                else{
                    tabletMode = false;
                    Log.d(LOG_TAG,"NOT Tab mode == "+tabletMode.booleanValue());
                }

                if(((Boolean)movieBundle.getBoolean("isSelected")) != null) {
                    isSelected = (Boolean) movieBundle.getBoolean("isSelected");
                    Log.d(LOG_TAG,"Is selected == "+isSelected.booleanValue());
                }
                else{
                    isSelected = false;
                    Log.d(LOG_TAG,"NOT Is selected == "+isSelected.booleanValue());
                }
            }
            else{
                //If movieBundle is null and we get a request upto here, then the device is a tablet.
                tabletMode = true;
                Movie movieSelected = ((MovieSelect)getActivity().getApplication()).getMovieInfo();
                movieBundle = ((MovieSelect)getActivity().getApplication()).getMovieBund();
                movieDisplay = movieSelected;
                Log.d(LOG_TAG,"Movie selected === "+movieSelected);
                Log.d(LOG_TAG,"Movie bundle selected === "+movieBundle);
                String sortOrder = getPreferencesSetting();
                Log.d(LOG_TAG,"Sort order === "+sortOrder);
                oldSortOrder = ((MovieSelect)getActivity().getApplication()).getMovieSetting();
                Log.d(LOG_TAG,"Old Sort order === "+oldSortOrder);
                if(movieSelected == null || movieBundle == null){
                    isSelected = false;
                    if(oldSortOrder == null) {
                        oldSortOrder = sortOrder;
                    }
                }
                else if(oldSortOrder.equalsIgnoreCase(sortOrder)){
                    isSelected = true;
                    trailerAndReviewInfoMovie = new Movie(movieSelected.getOriginalTitle(),movieSelected.getPoster_path(),movieSelected.getSynopsis(),movieSelected.getUserRating(),
                            movieSelected.getReleaseDate(),movieSelected.getId(),movieSelected.getMovieThumbnail(),movieSelected.getDbMovieId());
                    if(movieSelected.getAuthors() != null && movieSelected.getContents() != null && movieSelected.getKey() != null && movieSelected.getTrailerName() != null) {
                        trailerAndReviewInfoMovie.setAuthors(movieSelected.getAuthors());
                        trailerAndReviewInfoMovie.setContents(movieSelected.getContents());
                        trailerAndReviewInfoMovie.setKey(movieSelected.getKey());
                        trailerAndReviewInfoMovie.setTrailerName(movieSelected.getTrailerName());
                    }
                    else{
                        trailerAndReviewInfoMovie = null;
                    }
                }
                else{
                    isSelected = false;
                }

                if(!isSelected){
                    Log.d(LOG_TAG,"setting movie select to NULL");
                    ((MovieSelect) getActivity().getApplication()).setMovieInfo(null);
                    ((MovieSelect) getActivity().getApplication()).setMovieBund(null);
                }
                Log.d(LOG_TAG,"Is selected 22 == "+isSelected.booleanValue());
                Log.d(LOG_TAG,"Tab mode 22== "+tabletMode.booleanValue());
            }
            if(getPreferencesSetting().equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value))) {
                addedToFav = true;
            }
        }
        else
        {
            addedToFav = savedInstanceState.getBoolean("addedToFav");
            dbMovieIdInsertDelete = savedInstanceState.getInt("selectedDbMovieId");
            movieBundle = savedInstanceState.getParcelable("movieInfo");
            tabletMode = savedInstanceState.getBoolean("tabletMode");
            isSelected = savedInstanceState.getBoolean("isSelected");
            if(movieBundle != null) {
                movieDisplay = ((Movie) movieBundle.getParcelable("movieDetail"));
            }
            moviePoster = savedInstanceState.getByteArray("movieThumbnailImage");
            trailerAndReviewInfoMovie = savedInstanceState.getParcelable("movieTrailer");
        }
        return rootView;
    }

    public interface DetailCallback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemRemove();
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
        String sortOrder = getPreferencesSetting();
        Log.d(LOG_TAG,"Sort Order -> "+sortOrder);
        Log.d(LOG_TAG,"Old Sort Order -> "+oldSortOrder);
        Log.d(LOG_TAG,"is movie selected -> "+isSelected);
        Log.d(LOG_TAG,"Tablet mode -> "+tabletMode);
        //If the sort settings change, don't show any movie. Set the selection to false.
        if(!oldSortOrder.equalsIgnoreCase(sortOrder) || isSelected == null){
            isSelected=false;
        }
        Log.d(LOG_TAG,"is movie selected after -> "+isSelected);
        if((tabletMode != null && tabletMode.booleanValue()) && (isSelected != null && !isSelected.booleanValue())){// && !oldSortOrder.equalsIgnoreCase(sortOrder)){
            setEmptyListView(MovieConstants.NO_MOVIE);
            //oldSortOrder = sortOrder;
        }
        else {
            if (trailerAndReviewInfoMovie == null && !sortOrder.equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value))) {
                if (checkIfInternetIsAvailable()) {
                    checkIfMoviesNeedToBeRefreshed();
                }
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

                Log.d(LOG_TAG,"MOvie length == "+byteArrayOutputStream.size());
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
                    String sortOrder = getPreferencesSetting();
                    if(tabletMode && sortOrder.equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value))){
                        ((MovieSelect)getActivity().getApplication()).setMovieInfo(null);
                        ((MovieSelect)getActivity().getApplication()).setMovieBund(null);
                        ((MovieSelect)getActivity().getApplication()).setMoviePosition(0);
                        ((DetailCallback)getActivity()).onItemRemove();
                    }
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
                        Log.d(LOG_TAG,"Is selected receiver == "+isSelected.booleanValue());
                        Log.d(LOG_TAG,"Tab mode receiver == "+tabletMode.booleanValue());
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED && !getPreferencesSetting().equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value))
                                && ((tabletMode.booleanValue() && isSelected.booleanValue()) || !tabletMode.booleanValue()))
                        {
                            Log.d(LOG_TAG,"Refreshing movies....");
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
        Log.d(LOG_TAG,tabletMode+"");
        Movie temp = null;
        if(movieBundle != null)
        {
            outState.putParcelable("movieInfo",movieBundle);
            temp = (Movie) movieBundle.getParcelable("movieDetail");
        }

        if(trailerAndReviewInfoMovie != null)
        {
            outState.putParcelable("movieTrailer",trailerAndReviewInfoMovie);
            if(temp != null){
                temp.setTrailerName(trailerAndReviewInfoMovie.getTrailerName());
                temp.setKey(trailerAndReviewInfoMovie.getKey());
                temp.setContents(trailerAndReviewInfoMovie.getContents());
                temp.setAuthors(trailerAndReviewInfoMovie.getAuthors());
            }
        }

        if(tabletMode != null && tabletMode.booleanValue()) {
            Log.d(LOG_TAG,"Saving tab mode == "+tabletMode.booleanValue()+"");
            ((MovieSelect) getActivity().getApplication()).setTabletMode(tabletMode.booleanValue());
            if(isSelected.booleanValue()) {
                Log.d(LOG_TAG,temp+"");
                Log.d(LOG_TAG,movieBundle+"");
                ((MovieSelect) getActivity().getApplication()).setMovieInfo(temp);
                ((MovieSelect) getActivity().getApplication()).setMovieBund(movieBundle);
            }
        }

        if(addedToFav != null) {
            outState.putBoolean("addedToFav", addedToFav.booleanValue());
        }

        //Save the old sort order , to get it back when fragment is resumed.
        oldSortOrder = getPreferencesSetting();
        if(oldSortOrder != null)
        {
            Log.d(LOG_TAG, "Saving SORT ORDER before destruction..." + oldSortOrder);
            outState.putString("oldSortOrder",oldSortOrder);
            ((MovieSelect) getActivity().getApplication()).setMovieSetting(oldSortOrder);
        }

        Log.d(LOG_TAG,"Movie selection saved == "+isSelected);
        if(isSelected != null){
            outState.putBoolean("isSelected",isSelected);
        }

        if(tabletMode != null){
            outState.putBoolean("tabletMode",tabletMode);
        }

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
        Log.d(LOG_TAG,"==== onActivityCreated ==== trailerAndReviewInfoMovie === "+trailerAndReviewInfoMovie);
        boolean alreadyDisplayed = false;
        if(savedInstanceState != null){
            oldSortOrder = savedInstanceState.getString("oldSortOrder");
        }
        if(tabletMode.booleanValue())
        {
            if(isSelected && trailerAndReviewInfoMovie != null && movieBundle != null) {
                Log.d(LOG_TAG,"display selected movie");
                displayMovieDetails(movieBundle);
                displayMovieTrailerAndReviewDetails(trailerAndReviewInfoMovie);
                alreadyDisplayed = true;
            }
            else if(isSelected && trailerAndReviewInfoMovie == null && movieBundle != null)
            {
                hideTopMovieInfo();
                Log.d(LOG_TAG,"load selected movie");
                checkAndLoadMovies();
            }
            else if(movieBundle == null)
            {
                setEmptyListView(MovieConstants.NO_DATA_FOUND);
                return;
            }
            else if(!isSelected)
            {
                Log.d(LOG_TAG,"Setting as empty 1111");
                setEmptyListView(MovieConstants.NO_MOVIE);
                return;
            }
        }
        else
        {
            hideTopMovieInfo();
            if(!getPreferencesSetting().equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value))) {
                checkAndLoadMovies();
            }
        }

        Log.d(LOG_TAG,"addedToFav is == "+addedToFav);
        Log.d(LOG_TAG,"pref. setting == "+getPreferencesSetting());

        if(!getPreferencesSetting().equalsIgnoreCase(getResources().getString(R.string.settings_order_by_favorites_value))) {
            //Check if movie already added to DB, to set the favorite button accordingly
            Cursor allMovies = getActivity().getContentResolver().query(Uri.parse(MovieTableConstants.BASE_CONTENT_URI + "/allMovies"), null, null, null, null);

            if (addedToFav == null && movieBundle != null) {

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
            } else if (addedToFav != null && addedToFav.booleanValue() == true) {

                Log.d(LOG_TAG, "movie exists " + dbMovieIdInsertDelete);
                favStar.setImageResource(R.drawable.ic_grade_black_24dp);
            } else {

                Log.d(LOG_TAG, "movie NOT exists in favorite ");
                favStar.setImageResource(R.drawable.ic_star_border_black_24dp);
            }
        }
        else{
            if(addedToFav) {
                favStar.setImageResource(R.drawable.ic_grade_black_24dp);
            }
            else{
                favStar.setImageResource(R.drawable.ic_star_border_black_24dp);
            }

            if(!alreadyDisplayed) {
                if (trailerAndReviewInfoMovie == null && movieBundle != null) {
                    Cursor favMovieTrailers = getActivity().getContentResolver().query(Uri.parse(MovieTableConstants.BASE_CONTENT_URI + "/movie_trailer_id/" + movieDisplay.getDbMovieId()),
                            null, null, null, null);
                    Cursor favMovieReviews = getActivity().getContentResolver().query(Uri.parse(MovieTableConstants.BASE_CONTENT_URI + "/movie_review_id/" + movieDisplay.getDbMovieId()),
                            null, null, null, null);
                    Log.d(LOG_TAG, "Movie DB ID == " + movieDisplay.getDbMovieId());
                    favMovieTrailers.moveToFirst();
                    favMovieReviews.moveToFirst();
                    dbMovieIdInsertDelete = (int) movieDisplay.getDbMovieId();
                    ArrayList<String> keys = new ArrayList<String>();
                    ArrayList<String> names = new ArrayList<String>();
                    ArrayList<String> authors = new ArrayList<String>();
                    ArrayList<String> contents = new ArrayList<String>();
                    try {
                        if (favMovieTrailers.getCount() > 0) {
                            do {
                                String temp = favMovieTrailers.getString(favMovieTrailers.getColumnIndex(MovieTableConstants.KEY));
                                Log.d(LOG_TAG, "Key = " + temp);
                                keys.add(temp);

                                temp = favMovieTrailers.getString(favMovieTrailers.getColumnIndex(MovieTableConstants.NAME));
                                Log.d(LOG_TAG, "Name = " + temp);
                                names.add(temp);
                            } while (favMovieTrailers.moveToNext());
                        }

                        if (favMovieReviews.getCount() > 0) {
                            do {
                                String temp = favMovieReviews.getString(favMovieReviews.getColumnIndex(MovieTableConstants.AUTHOR));
                                Log.d(LOG_TAG, "Author = " + temp);
                                authors.add(temp);

                                temp = favMovieReviews.getString(favMovieReviews.getColumnIndex(MovieTableConstants.CONTENT));
                                Log.d(LOG_TAG, "Content = " + temp);
                                contents.add(temp);
                            } while (favMovieReviews.moveToNext());
                        }
                    } finally {
                        favMovieReviews.close();
                        favMovieTrailers.close();
                    }

                    trailerAndReviewInfoMovie = new Movie((movieDisplay.getOriginalTitle()), null, (movieDisplay.getSynopsis()),
                            (movieDisplay.getUserRating()), (movieDisplay.getReleaseDate()),
                            (movieDisplay.getId()), (movieDisplay.getMovieThumbnail()),
                            movieDisplay.getDbMovieId());
                    trailerAndReviewInfoMovie.setContents(contents.toArray(new String[contents.size()]));
                    trailerAndReviewInfoMovie.setKey(keys.toArray(new String[keys.size()]));
                    trailerAndReviewInfoMovie.setTrailerName(names.toArray(new String[names.size()]));
                    trailerAndReviewInfoMovie.setAuthors(authors.toArray(new String[authors.size()]));
                    displayMovieDetails(movieBundle);
                    displayMovieTrailerAndReviewDetails(trailerAndReviewInfoMovie);
                } else {
                    Log.d(LOG_TAG, "Setting as empty");
                    setEmptyListView(MovieConstants.NO_MOVIE);
                }
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
        else if(msg.equalsIgnoreCase(MovieConstants.NO_MOVIE))
        {
            placeHolderImage.setVisibility(View.INVISIBLE);
            movieDetails.setVisibility(View.INVISIBLE);
            Log.d(LOG_TAG,"Visibility ==> "+movieDetails.getVisibility());
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
            Bitmap movieBm = null;
            byte[] movieImage = null;
            if(moviePoster == null) {
                Cursor movieThumbnailCursor = getActivity().getContentResolver().query(Uri.parse(MovieTableConstants.BASE_CONTENT_URI + "/getMovieThumbnail/" + movieDisplay.getDbMovieId()),
                        null, null, null, null);
                movieThumbnailCursor.moveToFirst();
                movieImage = movieThumbnailCursor.getBlob(movieThumbnailCursor.getColumnIndex(MovieTableConstants.THUMBNAIL));
                moviePoster = movieImage;
                movieThumbnailCursor.close();
            }
            else{
                movieImage= moviePoster;
            }
            movieBm = BitmapFactory.decodeByteArray(movieImage, 0, movieImage.length);
            movieThumbnail.setImageBitmap(movieBm);
            movieThumbnail.setScaleY(0.9F);
            movieThumbnail.setScaleX(0.9F);

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

    private void hideTopMovieInfo()
    {
        favStar.setVisibility(View.GONE);
        movieHeading.setVisibility(View.GONE);
        line.setVisibility(View.GONE);
        trailerHeading.setVisibility(View.GONE);
    }
}
