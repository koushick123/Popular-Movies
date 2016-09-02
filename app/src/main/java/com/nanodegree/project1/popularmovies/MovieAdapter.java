package com.nanodegree.project1.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
        Picasso.with(getContext()).load(BASE_PICASSO_URL+IMAGE_SIZE+movie.getPoster_path()).into(moviePoster);

        return listItem;
    }
}
