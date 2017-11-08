package com.github.harrynp.popularmovies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.harrynp.popularmovies.adapters.MovieAdapter;
import com.github.harrynp.popularmovies.data.Movie;
import com.github.harrynp.popularmovies.databinding.FragmentMainBinding;
import com.github.harrynp.popularmovies.utils.GridAutofitLayoutManager;
import com.github.harrynp.popularmovies.utils.MovieDBJsonUtils;
import com.github.harrynp.popularmovies.utils.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements MovieAdapter.MovieAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Movie[]>{
    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private MovieAdapter movieAdapter;
    private FragmentMainBinding mBinding;
    private static final String SORT_ORDER_EXTRA = "sort";
    private static final int FETCH_MOVIE_LOADER = 23;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        movieAdapter = new MovieAdapter(getContext(), this);

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        //Implemented swipe to refresh
        mBinding.swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                updateMovies();
            }
        });
        GridAutofitLayoutManager layoutManager = new GridAutofitLayoutManager(getContext(), 700);
        mBinding.recyclerviewMovies.setLayoutManager(layoutManager);
        mBinding.recyclerviewMovies.setHasFixedSize(true);
        mBinding.recyclerviewMovies.setAdapter(movieAdapter);
        return mBinding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh){
            updateMovies();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    private void showGridView(){
        mBinding.tvErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mBinding.recyclerviewMovies.setVisibility(View.VISIBLE);
    }
    private void showErrorMessage(){
        mBinding.recyclerviewMovies.setVisibility(View.INVISIBLE);
        mBinding.tvErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void updateMovies(){
        if(isOnline()) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortOrder = sharedPref.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popular));
            Bundle sortBundle = new Bundle();
            sortBundle.putString(SORT_ORDER_EXTRA, sortOrder);
            getActivity().getSupportLoaderManager().restartLoader(FETCH_MOVIE_LOADER, sortBundle, this);
        } else {
            mBinding.swiperefresh.setRefreshing(false);
            showErrorMessage();
        }
    }


    @Override
    public void onClick(Movie movie) {
        Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
        detailIntent.putExtra(DetailActivityFragment.MOVIE_DETAILS, movie);
        startActivity(detailIntent);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<Movie[]> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<Movie[]>(getContext()) {

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                //No sort order
                if (args == null){
                    return;
                }
                showGridView();
                mBinding.pbLoadingIndicator.setVisibility(View.VISIBLE);
                forceLoad();
            }

            @Override
            public Movie[] loadInBackground() {
                String sortOrder = args.getString(SORT_ORDER_EXTRA);
                // Verify there's a sort order.
                if (sortOrder == null || TextUtils.isEmpty(sortOrder)) {
                    return null;
                }
                String moviesJsonString;

                URL url = NetworkUtils.buildUrl(sortOrder);
                if (url != null) {
                    try {
                        moviesJsonString = NetworkUtils.getResponseFromHttpUrl(url);
                    } catch (IOException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                        return null;
                    }
                    try {
                        return MovieDBJsonUtils.getMovieDataFromJson(moviesJsonString);
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
    public void onLoadFinished(Loader<Movie[]> loader, Movie[] movies) {
        //If there is new data from the JSON object
        mBinding.pbLoadingIndicator.setVisibility(View.INVISIBLE);
        mBinding.swiperefresh.setRefreshing(false);
        if (movies != null){
            mBinding.recyclerviewMovies.smoothScrollToPosition(0);
            showGridView();
            movieAdapter.clear();
            for (Movie movie : movies){
                movieAdapter.addMovie(movie);
            }
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<Movie[]> loader) {
        /*
         * Since this Loader's data is now invalid, we need to clear the Adapter that is
         * displaying the data.
         */
        movieAdapter.clear();
    }
}
