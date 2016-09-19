package com.nanodegree.project1.popularmovies;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
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

    static class ViewHolderImage{
        GridView movieGrid;
        ImageView movieThumbnail;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        ViewHolderImage viewHolderImage;

        if(listItem == null)
        {
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.movie_list_item,parent,false);
            viewHolderImage = new ViewHolderImage();
            viewHolderImage.movieGrid = (GridView)parent.findViewById(R.id.list);
            viewHolderImage.movieThumbnail = (ImageView)listItem.findViewById(R.id.moviePoster);

            listItem.setTag(viewHolderImage);
        }
        else
        {
            viewHolderImage = (ViewHolderImage)listItem.getTag();
        }

        Movie movie = (Movie)getItem(position);
        int orientation = getContext().getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            viewHolderImage.movieGrid.setNumColumns(3);
        }
        else if (orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            viewHolderImage.movieGrid.setNumColumns(2);
        }
        if(movie.getMovieThumbnail() == null) {
            Picasso.with(getContext()).load(BASE_PICASSO_URL + IMAGE_SIZE + movie.getPoster_path()).into(viewHolderImage.movieThumbnail);
        }
        else{
            Bitmap moviePoster = BitmapFactory.decodeByteArray(movie.getMovieThumbnail(),0,movie.getMovieThumbnail().length);
            viewHolderImage.movieThumbnail.setScaleY(1.1F);
            viewHolderImage.movieThumbnail.setImageBitmap(moviePoster);
        }

        return listItem;
    }
}
