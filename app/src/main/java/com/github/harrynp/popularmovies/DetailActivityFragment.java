package com.github.harrynp.popularmovies;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.github.harrynp.popularmovies.adapters.ReviewAdapter;
import com.github.harrynp.popularmovies.adapters.TrailerAdapter;
import com.github.harrynp.popularmovies.data.Movie;
import com.github.harrynp.popularmovies.data.Review;
import com.github.harrynp.popularmovies.data.Trailer;
import com.github.harrynp.popularmovies.databinding.FragmentDetailBinding;
import com.github.harrynp.popularmovies.utils.MovieDBJsonUtils;
import com.github.harrynp.popularmovies.utils.NetworkUtils;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements TrailerAdapter.TrailerAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Bundle>{
    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final int FETCH_EXTRA_DATA_LOADER = 24;
    private static final String MOVIE_ID_EXTRA = "movie_id";
    private static final String MOVIE_RUNTIME_EXTRA = "runtime";
    private static final String MOVIE_TRAILERS_EXTRA = "trailers";
    private static final String MOVIE_REVIEWS_EXTRA = "reviews";
    public static final String MOVIE_DETAILS = "MOVIE_DETAILS";
    private TrailerAdapter trailerAdapter;
    private ReviewAdapter reviewAdapter;
    private Movie movie;
    FragmentDetailBinding mBinding;

    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false);
        Intent detailIntent = getActivity().getIntent();
        if (detailIntent != null && detailIntent.hasExtra(MOVIE_DETAILS)) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean hq = sharedPref.getBoolean(getString(R.string.pref_hq_key), false);
            movie = detailIntent.getParcelableExtra(MOVIE_DETAILS);
            int movieId = movie.getMovieId();
            String backdropUrl = hq ? "http://image.tmdb.org/t/p/w1280/" + movie.getBackdropPath() : "http://image.tmdb.org/t/p/w300/" + movie.getBackdropPath();
            String posterUrl = hq ? "http://image.tmdb.org/t/p/w780/" + movie.getPosterPath() : "http://image.tmdb.org/t/p/w185/" + movie.getPosterPath();

            Glide.with(getContext())
                    .load(backdropUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(mBinding.backdropImageview);
            mBinding.detailTitle.setText(movie.getMovieName());
            Glide.with(getContext())
                    .load(posterUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.mipmap.movie_placeholder))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(mBinding.posterImageview);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                String releaseDate = DateUtils.formatDateTime(getContext(), simpleDateFormat.parse(movie.getReleaseDate()).getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);
                mBinding.detailReleaseDate.append(" " + releaseDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mBinding.detailRating.append(" " + Double.toString(movie.getRating()) + "/10");
            mBinding.detailVoteCount.setText(Integer.toString(movie.getVoteCount()) + " ratings");
            mBinding.detailOverview.setText(movie.getOverview());
            Bundle movieIdBundle = new Bundle();
            movieIdBundle.putInt(MOVIE_ID_EXTRA, movieId);
            getActivity().getSupportLoaderManager().restartLoader(FETCH_EXTRA_DATA_LOADER, movieIdBundle, this);
            trailerAdapter = new TrailerAdapter(getContext(), this);
            LinearLayoutManager trailersLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            mBinding.recyclerviewTrailers.setLayoutManager(trailersLayoutManager);
            mBinding.recyclerviewTrailers.setAdapter(trailerAdapter);
            reviewAdapter = new ReviewAdapter(getContext());
            LinearLayoutManager reviewsLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            reviewsLayoutManager.setAutoMeasureEnabled(true);
            mBinding.recyclerviewReviews.setLayoutManager(reviewsLayoutManager);
            mBinding.recyclerviewReviews.setAdapter(reviewAdapter);
            mBinding.recyclerviewReviews.setNestedScrollingEnabled(false);
        }
        return mBinding.getRoot();
    }

    private void showTrailers(){
        mBinding.tvNoTrailersMessage.setVisibility(View.INVISIBLE);
        mBinding.recyclerviewTrailers.setVisibility(View.VISIBLE);
    }
    private void showNoTrailers(){
        mBinding.recyclerviewTrailers.setVisibility(View.INVISIBLE);
        mBinding.tvNoTrailersMessage.setVisibility(View.VISIBLE);
    }

    private void showReviews(){
        mBinding.tvNoReviewsMessage.setVisibility(View.INVISIBLE);
        mBinding.recyclerviewReviews.setVisibility(View.VISIBLE);
    }
    private void showNoReviews(){
        mBinding.recyclerviewReviews.setVisibility(View.INVISIBLE);
        mBinding.tvNoReviewsMessage.setVisibility(View.VISIBLE);
    }

    public static void watchYoutubeVideo(Context context, Activity activity, String id){
        Intent standalonePlayerIntent = YouTubeStandalonePlayer.createVideoIntent(activity, BuildConfig.YOUTUBE_DEVELOPER_API_KEY, id, 0, true, true);
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            if (BuildConfig.YOUTUBE_DEVELOPER_API_KEY != "{YOUTUBE_API_KEY}"){
                context.startActivity(standalonePlayerIntent);
            } else {
                context.startActivity(appIntent);
            }
        } catch (ActivityNotFoundException ex) {
            context.startActivity(webIntent);
        }
    }

    @Override
    public void onClick(Trailer trailer) {
        watchYoutubeVideo(getContext(),getActivity(), trailer.getKey());
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<Bundle> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<Bundle>(getContext()) {

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                //No Movie ID
                if (args == null){
                    return;
                }
                forceLoad();
            }

            @Override
            public Bundle loadInBackground() {
                String movieJsonString;
                String trailersJsonString;
                String reviewsJsonString;
                Bundle movieExtras = new Bundle();
                int movieId = args.getInt(MOVIE_ID_EXTRA);
                URL url = NetworkUtils.buildURL(movieId, null);
                URL videosUrl = NetworkUtils.buildURL(movieId, "videos");
                URL reviewsUrl = NetworkUtils.buildURL(movieId, "reviews");
                if (url != null) {
                    try {
                        movieJsonString = NetworkUtils.getResponseFromHttpUrl(url);
                        trailersJsonString = NetworkUtils.getResponseFromHttpUrl(videosUrl);
                        reviewsJsonString = NetworkUtils.getResponseFromHttpUrl(reviewsUrl);
                    } catch (IOException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                        return null;
                    }
                    try {
                        movieExtras.putInt(MOVIE_RUNTIME_EXTRA, MovieDBJsonUtils.getMovieRuntimeFromJson(movieJsonString));
                        movieExtras.putParcelableArray(MOVIE_TRAILERS_EXTRA, MovieDBJsonUtils.getTrailerDataFromJson(trailersJsonString));
                        movieExtras.putParcelableArray(MOVIE_REVIEWS_EXTRA, MovieDBJsonUtils.getReviewDataFromJson(reviewsJsonString));
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    }
                    return movieExtras;
                }

                // This will only happen if there was an error getting or parsing the movies.
                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Bundle> loader, Bundle data) {
        Integer runtime = data.getInt(MOVIE_RUNTIME_EXTRA);
        Parcelable[] trailers = data.getParcelableArray(MOVIE_TRAILERS_EXTRA);
        Parcelable[] reviews = data.getParcelableArray(MOVIE_REVIEWS_EXTRA);
        if (mBinding.detailRuntime.getText().toString().equals(getString(R.string.runtime))){
            if (runtime != null){
                mBinding.detailRuntime.append(" " + runtime + " minutes");
            } else {
                mBinding.detailRuntime.append(" N/A");
            }
        }
        if (trailers != null && trailers.length != 0){
            showTrailers();
            trailerAdapter.clear();
            for (Parcelable trailer: trailers){
                trailerAdapter.addTrailer((Trailer) trailer);
            }
        } else {
            showNoTrailers();
        }
        if (reviews != null && reviews.length != 0){
            showReviews();
            reviewAdapter.clear();
            for (Parcelable review: reviews){
                reviewAdapter.addReview((Review) review);
            }
        } else {
            showNoReviews();
        }
    }

    @Override
    public void onLoaderReset(Loader<Bundle> loader) {

    }
}
