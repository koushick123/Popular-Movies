package com.nanodegree.project1.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Koushick on 05-09-2016.
 */
public class MovieTrailerAdapter extends ArrayAdapter
{
    public MovieTrailerAdapter(Context context, ArrayList<String> trailers) {
        super(context, 0, trailers);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItem = convertView;

        if(listItem == null)
        {
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.movie_trailer_list_item,parent,false);
        }
        String trailerName = (String)getItem(position);
        TextView trailer = (TextView)listItem.findViewById(R.id.textTrailer);
        trailer.setText(trailerName);
        return listItem;
    }
}
