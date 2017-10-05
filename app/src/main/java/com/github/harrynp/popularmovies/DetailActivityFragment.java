package com.github.harrynp.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.github.harrynp.popularmovies.data.Movie;
import com.github.harrynp.popularmovies.utils.MovieDBJsonUtils;
import com.github.harrynp.popularmovies.utils.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    public static final String MOVIE_DETAILS = "MOVIE_DETAILS";
    private Movie movie;

    ImageView backdropImage;
    TextView detailTitle;
    ImageView posterImage;
    TextView detailReleaseDate;
    TextView detailRating;
    TextView detailVoteCount;
    TextView detailRuntime;
    TextView detailOverview;

    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
//        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent detailIntent = getActivity().getIntent();
        if (detailIntent != null && detailIntent.hasExtra(MOVIE_DETAILS)) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean hq = sharedPref.getBoolean(getString(R.string.pref_hq_key), false);
            movie = detailIntent.getParcelableExtra(MOVIE_DETAILS);
            int movieId = movie.getMovieId();
            backdropImage = (ImageView) rootView.findViewById(R.id.backdrop_imageview);
            detailTitle = (TextView) rootView.findViewById(R.id.detail_title);
            posterImage = (ImageView) rootView.findViewById(R.id.poster_imageview);
            detailReleaseDate = (TextView) rootView.findViewById(R.id.detail_release_date);
            detailRating = (TextView) rootView.findViewById(R.id.detail_rating);
            detailVoteCount = (TextView) rootView.findViewById(R.id.detail_vote_count);
            detailRuntime = (TextView) rootView.findViewById(R.id.detail_runtime);
            detailOverview = (TextView) rootView.findViewById(R.id.detail_overview);
            String backdropUrl = hq ? "http://image.tmdb.org/t/p/w1280/" + movie.getBackdropPath() : "http://image.tmdb.org/t/p/w300/" + movie.getBackdropPath();
            String posterUrl = hq ? "http://image.tmdb.org/t/p/w780/" + movie.getPosterPath() : "http://image.tmdb.org/t/p/w185/" + movie.getPosterPath();

            Glide.with(getContext())
                    .load(backdropUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(backdropImage);
            detailTitle.setText(movie.getMovieName());
            Glide.with(getContext())
                    .load(posterUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.mipmap.movie_placeholder))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(posterImage);
            detailReleaseDate.append(" " + movie.getReleaseDate());
            detailRating.append(" " + Double.toString(movie.getRating()) + "/10");
            detailVoteCount.setText(Integer.toString(movie.getVoteCount()) + " ratings");
            detailOverview.setText(movie.getOverview());
            FetchRuntimeTask runtimeTask = new FetchRuntimeTask();
            runtimeTask.execute(movieId);

        }

        return rootView;
    }

    public class FetchRuntimeTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(Integer... integers) {
            String movieJsonString;

            if(integers.length == 0){
                return null;
            }
            URL url = NetworkUtils.buildURL(integers[0]);
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

        @Override
        protected void onPostExecute(Integer integer) {
            if (integer != null){
                detailRuntime.append(" " + integer + " minutes");
            } else {
                detailRuntime.append(" N/A");
            }
        }
    }
}
