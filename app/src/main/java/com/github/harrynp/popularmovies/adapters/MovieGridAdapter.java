package com.github.harrynp.popularmovies.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.harrynp.popularmovies.R;
import com.github.harrynp.popularmovies.data.Movie;

import java.util.List;

/**
 * Created by harry on 9/26/2017.
 */

public class MovieGridAdapter extends ArrayAdapter<Movie>{
    private static final String LOG_TAG = MovieGridAdapter.class.getSimpleName();

    public MovieGridAdapter(Context context, List<Movie> movies) {
        super(context, 0, movies);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        // Gets the Movie object from the ArrayAdapter at the appropriate position
        Movie movie = getItem(position);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean hq = sharedPref.getBoolean(getContext().getString(R.string.pref_hq_key), false);

        String posterUrl = hq ? "http://image.tmdb.org/t/p/w780/" + movie.getPosterPath() : "http://image.tmdb.org/t/p/w185/" + movie.getPosterPath();

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_movie, parent, false);
        }

        ImageView posterView = convertView.findViewById(R.id.grid_item_imageview);
        Glide.with(getContext())
                .load(posterUrl)
                .apply(new RequestOptions()
                .placeholder(R.mipmap.movie_placeholder))
                .into(posterView);
        return convertView;

    }
}
