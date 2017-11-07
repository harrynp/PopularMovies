package com.github.harrynp.popularmovies;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.github.harrynp.popularmovies.data.Movie;
import com.github.harrynp.popularmovies.databinding.FragmentDetailBinding;
import com.github.harrynp.popularmovies.utils.MovieDBJsonUtils;
import com.github.harrynp.popularmovies.utils.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Integer>{
    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final int FETCH_EXTRA_DATA_LOADER = 24;
    private static final String MOVIE_ID_EXTRA = "movie_id";
    public static final String MOVIE_DETAILS = "MOVIE_DETAILS";
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
            mBinding.detailReleaseDate.append(" " + movie.getReleaseDate());
            mBinding.detailRating.append(" " + Double.toString(movie.getRating()) + "/10");
            mBinding.detailVoteCount.setText(Integer.toString(movie.getVoteCount()) + " ratings");
            mBinding.detailOverview.setText(movie.getOverview());
            Bundle movieIdBundle = new Bundle();
            movieIdBundle.putInt(MOVIE_ID_EXTRA, movieId);
            getActivity().getSupportLoaderManager().restartLoader(FETCH_EXTRA_DATA_LOADER, movieIdBundle, this);
        }
        return mBinding.getRoot();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<Integer> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<Integer>(getContext()) {

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
            public Integer loadInBackground() {
                String movieJsonString;
                int movieId = args.getInt(MOVIE_ID_EXTRA);
                URL url = NetworkUtils.buildURL(movieId);
                if (url != null) {
                    try {
                        movieJsonString = NetworkUtils.getResponseFromHttpUrl(url);
                    } catch (IOException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                        return null;
                    }
                    try {
                        return MovieDBJsonUtils.getMovieRuntimeFromJson(movieJsonString);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    }
                }

                // This will only happen if there was an error getting or parsing the movies.
                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Integer> loader, Integer data) {
        if (data != null){
            mBinding.detailRuntime.append(" " + data + " minutes");
        } else {
            mBinding.detailRuntime.append(" N/A");
        }
    }

    @Override
    public void onLoaderReset(Loader<Integer> loader) {

    }
}
