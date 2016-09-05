package com.nanodegree.project1.popularmovies;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Koushick on 31-08-2016.
 */
public class MovieAdapter extends ArrayAdapter
{
    String BASE_PICASSO_URL = "http://image.tmdb.org/t/p/";
    String IMAGE_SIZE = "w185/";
    private static final String LOG_TAG = MovieAdapter.class.getName();

    public MovieAdapter(Context context, ArrayList<Movie> movies) {
        super(context, 0, movies);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;

        if(listItem == null)
        {
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.movie_list_item,parent,false);
        }

        Movie movie = (Movie)getItem(position);
        ImageView moviePoster = (ImageView)listItem.findViewById(R.id.moviePoster);
        GridView movieGrid = (GridView)parent.findViewById(R.id.list);
        Log.d(LOG_TAG,"Orientation == "+getContext().getResources().getConfiguration().orientation);
        int orientation = getContext().getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            movieGrid.setNumColumns(3);
        }
        else if (orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            movieGrid.setNumColumns(2);
        }
        Picasso.with(getContext()).load(BASE_PICASSO_URL+IMAGE_SIZE+movie.getPoster_path()).into(moviePoster);

        return listItem;
    }
}
