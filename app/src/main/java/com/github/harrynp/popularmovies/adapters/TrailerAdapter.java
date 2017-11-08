package com.github.harrynp.popularmovies.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.github.harrynp.popularmovies.R;
import com.github.harrynp.popularmovies.data.Trailer;
import com.github.harrynp.popularmovies.databinding.ItemMovieTrailerBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by harry on 11/7/2017.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerAdapterViewHolder> {

    private final Context mContext;
    final private TrailerAdapter.TrailerAdapterOnClickHandler mClickHandler;
    private ItemMovieTrailerBinding mBinding;
    private List<Trailer> mTrailerList;

    public interface TrailerAdapterOnClickHandler {
        void onClick(Trailer trailer);
    }

    public void clear(){
        int size = mTrailerList.size();
        mTrailerList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addTrailer(Trailer trailer){
        mTrailerList.add(trailer);
        notifyDataSetChanged();
    }

    public TrailerAdapter(@NonNull Context context, TrailerAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
        mTrailerList = new ArrayList<>();
    }

    @Override
    public TrailerAdapter.TrailerAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_movie_trailer, parent, false);
        return new TrailerAdapterViewHolder(mBinding.getRoot());
    }

    @Override
    public void onBindViewHolder(TrailerAdapter.TrailerAdapterViewHolder holder, int position) {
        Trailer trailer = mTrailerList.get(position);
        if (trailer.getSite().toLowerCase().equals("youtube")){
            String thumbnailUrl = "http://img.youtube.com/vi/" + mTrailerList.get(position).getKey() + "/0.jpg";
            Glide.with(mContext)
                .load(thumbnailUrl)
                .apply(new RequestOptions())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.thumbnail);
        }
        holder.name.setText(trailer.getName());
    }

    @Override
    public int getItemCount() {
        return mTrailerList.size();
    }

    class TrailerAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView thumbnail;
        final TextView name;

        TrailerAdapterViewHolder(View itemView) {
            super(itemView);
            thumbnail = mBinding.trailerImageview;
            name = mBinding.textviewTrailerName;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mClickHandler.onClick(mTrailerList.get(adapterPosition));
        }
    }
}
