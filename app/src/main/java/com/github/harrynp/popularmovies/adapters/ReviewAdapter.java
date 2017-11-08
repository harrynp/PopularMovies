package com.github.harrynp.popularmovies.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.harrynp.popularmovies.R;
import com.github.harrynp.popularmovies.data.Review;
import com.github.harrynp.popularmovies.databinding.ItemMovieReviewBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by harry on 11/7/2017.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    private final Context mContext;
    private ItemMovieReviewBinding mBinding;
    private List<Review> mReviewList;

    public void clear(){
        int size = mReviewList.size();
        mReviewList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addReview(Review review){
        mReviewList.add(review);
        notifyDataSetChanged();
    }


    public ReviewAdapter(@NonNull Context context) {
        mContext = context;
        mReviewList = new ArrayList<>();
    }

    @Override
    public ReviewAdapter.ReviewAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_movie_review, parent, false);
        return new ReviewAdapterViewHolder(mBinding.getRoot());
    }

    @Override
    public void onBindViewHolder(ReviewAdapter.ReviewAdapterViewHolder holder, int position) {
        Review review = mReviewList.get(position);
        holder.author.setText(review.getAuthor());
        holder.content.setText(review.getContent());
    }

    @Override
    public int getItemCount() {
        return mReviewList.size();
    }

    class ReviewAdapterViewHolder extends RecyclerView.ViewHolder {
        final TextView author;
        final TextView content;

        ReviewAdapterViewHolder(View itemView) {
            super(itemView);
            author = mBinding.textviewReviewAuthor;
            content = mBinding.textviewReviewContent;
        }
    }
}
