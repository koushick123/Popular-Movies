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

    static class ViewHolderImage {
        ImageView movieThumbnail;
        GridView gridView;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        ViewHolderImage viewHolderImage;

        if(listItem == null)
        {
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.movie_list_item,parent,false);

            //Setup viewholder
            viewHolderImage = new ViewHolderImage();
            viewHolderImage.movieThumbnail = (ImageView)listItem.findViewById(R.id.moviePoster);
            viewHolderImage.gridView = (GridView)parent.findViewById(R.id.list);

            listItem.setTag(viewHolderImage);
        }
        else
        {
            viewHolderImage = (ViewHolderImage)listItem.getTag();
        }

        Movie movie = (Movie)getItem(position);
        int orientation = getContext().getResources().getConfiguration().orientation;
        Log.d(LOG_TAG,"Orientation == "+orientation);
        if(orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            viewHolderImage.gridView.setNumColumns(3);
        }
        else if (orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            viewHolderImage.gridView.setNumColumns(2);
        }
        Picasso.with(getContext()).load(BASE_PICASSO_URL+IMAGE_SIZE+movie.getPoster_path()).into(viewHolderImage.movieThumbnail);

        return listItem;
    }
}
