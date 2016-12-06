package com.nanodegree.project1.popularmovies;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.nanodegree.project1.popularmovies.data.MovieTableConstants;

/**
 * Created by koushick on 21-Nov-16.
 */
public class MovieCursorAdapter extends CursorAdapter {

    GridView movieGrid;
    ImageView movieThumbnail;
    public static final String LOG_TAG = MovieCursorAdapter.class.getName();

    public MovieCursorAdapter(Context context,Cursor cursor,int flags){
        super(context,cursor,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View listItem = LayoutInflater.from(context).inflate(R.layout.movie_list_item,viewGroup,false);
        movieGrid = (GridView)viewGroup.findViewById(R.id.favList);
        movieThumbnail = (ImageView)listItem.findViewById(R.id.moviePoster);
        int orientation = context.getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            movieGrid.setNumColumns(3);
        }
        else if (orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            movieGrid.setNumColumns(2);
        }
        return listItem;
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        byte[] thumbnail = cursor.getBlob(cursor.getColumnIndex(MovieTableConstants.THUMBNAIL));
        Bitmap moviePoster = BitmapFactory.decodeByteArray(thumbnail,0,thumbnail.length);
        movieThumbnail = (ImageView)view.findViewById(R.id.moviePoster);
        movieThumbnail.setImageBitmap(moviePoster);
    }
}
