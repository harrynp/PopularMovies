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
import java.util.List;

/**
 * Created by harry on 9/26/2017.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder>{


    private final Context mContext;
    final private MovieAdapterOnClickHandler mClickHandler;
    private GridItemMovieBinding mBinding;
    private List<Movie> mMovieList;

    public interface MovieAdapterOnClickHandler {
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

    public MovieAdapter(@NonNull Context context, MovieAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
        mMovieList = new ArrayList<>();
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.grid_item_movie, parent, false);
        return new MovieAdapterViewHolder(mBinding.getRoot());
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        Movie movie = mMovieList.get(position);
        if(movie != null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
            boolean hq = sharedPref.getBoolean(mContext.getString(R.string.pref_hq_key), true);
            String posterUrl = hq ? "http://image.tmdb.org/t/p/w780/" + movie.getPosterPath() : "http://image.tmdb.org/t/p/w185/" + movie.getPosterPath();
            Glide.with(mContext)
                    .load(posterUrl)
                    .apply(new RequestOptions())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.posterView);
        }
    }

    @Override
    public int getItemCount() {
        if (mMovieList == null) return 0;
        return mMovieList.size();
    }

    class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        final ImageView posterView;

        MovieAdapterViewHolder(View itemView) {
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
