package com.github.harrynp.popularmovies.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.github.harrynp.popularmovies.R;
import com.github.harrynp.popularmovies.data.Movie;
import com.github.harrynp.popularmovies.databinding.GridItemMovieBinding;

import java.util.ArrayList;

/**
 * Created by harry on 9/26/2017.
 */

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder>{


    private final Context mContext;
    final private MoviesAdapterOnClickHandler mClickHandler;
    private GridItemMovieBinding mBinding;
    private ArrayList<Movie> mMovieList;

    public interface MoviesAdapterOnClickHandler {
        void onClick(Movie movie);
    }

    public void clear(){
        int size = mMovieList.size();
        mMovieList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addMovie(Movie movie){
        mMovieList.add(movie);
        notifyDataSetChanged();
    }

    public MoviesAdapter(@NonNull Context context, MoviesAdapterOnClickHandler clickHandler, ArrayList<Movie> movies) {
        mContext = context;
        mClickHandler = clickHandler;
        mMovieList = movies;
    }

    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.grid_item_movie, parent, false);
        return new MoviesAdapterViewHolder(mBinding.getRoot());
    }

    @Override
    public void onBindViewHolder(MoviesAdapterViewHolder holder, int position) {
        Movie movie = mMovieList.get(position);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean hq = sharedPref.getBoolean(mContext.getString(R.string.pref_hq_key), false);
        String posterUrl = hq ? "http://image.tmdb.org/t/p/w780/" + movie.getPosterPath() : "http://image.tmdb.org/t/p/w185/" + movie.getPosterPath();
        Glide.with(mContext)
                .load(posterUrl)
                .apply(new RequestOptions())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.posterView);
    }

    @Override
    public int getItemCount() {
        if (mMovieList == null) return 0;
        return mMovieList.size();
    }

    class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        final ImageView posterView;

        MoviesAdapterViewHolder(View itemView) {
            super(itemView);
            posterView = mBinding.gridItemImageview;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mClickHandler.onClick(mMovieList.get(adapterPosition));
        }
    }
}
