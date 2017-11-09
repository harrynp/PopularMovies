package com.github.harrynp.popularmovies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import com.github.harrynp.popularmovies.data.FavoritesContract;
import com.github.harrynp.popularmovies.data.Movie;
import com.github.harrynp.popularmovies.databinding.FragmentMainBinding;
import com.github.harrynp.popularmovies.utils.GridAutofitLayoutManager;
import com.github.harrynp.popularmovies.utils.MovieDBJsonUtils;
import com.github.harrynp.popularmovies.utils.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements MovieAdapter.MovieAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Movie[]>,
        SharedPreferences.OnSharedPreferenceChangeListener{
    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private MovieAdapter movieAdapter;
    private FragmentMainBinding mBinding;
    private static final String SORT_ORDER_EXTRA = "sort";
    private static final int FETCH_MOVIE_LOADER = 23;
    private static MenuItem actionSortPopular;
    private static MenuItem actionSortTopRated;
    private static MenuItem actionSortFavorites;

    public MainActivityFragment() {
        setHasOptionsMenu(true);
    }

    private void setSortCheckedSummaryPopular(){
//        actionSortTopRated.setChecked(false);
//        actionSortFavorites.setChecked(false);
        actionSortPopular.setChecked(true);
    }

    private void setSortCheckedSummaryTopRated(){
//        actionSortPopular.setChecked(false);
//        actionSortFavorites.setChecked(false);
        actionSortTopRated.setChecked(true);
    }

    private void setSortCheckedSummaryFavorites(){
//        actionSortPopular.setChecked(false);
//        actionSortTopRated.setChecked(false);
        actionSortFavorites.setChecked(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main_fragment, menu);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = sharedPref.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popular));
        actionSortPopular = menu.findItem(R.id.action_sort_popular);
        actionSortTopRated = menu.findItem(R.id.action_sort_top_rated);
        actionSortFavorites = menu.findItem(R.id.action_sort_favorites);
        if (sortOrder.equals(getString(R.string.pref_sort_popular))) {
            setSortCheckedSummaryPopular();
        } else if (sortOrder.equals(getString(R.string.pref_sort_top_rated))){
            setSortCheckedSummaryTopRated();
        } else {
            setSortCheckedSummaryFavorites();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        switch (id){
            case R.id.action_sort_popular:
                setSortCheckedSummaryPopular();
                sharedPref.edit().putString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popular)).apply();
                return true;
            case R.id.action_sort_top_rated:
                setSortCheckedSummaryTopRated();
                sharedPref.edit().putString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_top_rated)).apply();
                return true;
            case R.id.action_sort_favorites:
                setSortCheckedSummaryFavorites();
                sharedPref.edit().putString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_favorites)).apply();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_sort_key))) {
            updateMovies();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
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
    public void onStart() {
        updateMovies();
        super.onStart();
    }

    private void showGridView(){
        mBinding.tvErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mBinding.noFavoritesMessage.setVisibility(View.INVISIBLE);
        mBinding.recyclerviewMovies.setVisibility(View.VISIBLE);
    }

    private void showNoFavoritesMessage(){
        mBinding.recyclerviewMovies.setVisibility(View.INVISIBLE);
        mBinding.tvErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mBinding.noFavoritesMessage.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(){
        mBinding.recyclerviewMovies.setVisibility(View.INVISIBLE);
        mBinding.noFavoritesMessage.setVisibility(View.INVISIBLE);
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
        switch (id) {
            case FETCH_MOVIE_LOADER:
                return new AsyncTaskLoader<Movie[]>(getContext()) {

                    @Override
                    protected void onStartLoading() {
                        super.onStartLoading();
                        //No sort order
                        if (args == null) {
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

                        if (sortOrder.equals(getString(R.string.pref_sort_favorites))) {
                            Cursor cursor = getActivity().getContentResolver().query(
                                    FavoritesContract.FavoritesEntry.CONTENT_URI,
                                    null,
                                    null,
                                    null,
                                    FavoritesContract.FavoritesEntry.COLUMN_TITLE);
                            Movie[] movies = new Movie[cursor.getCount()];
                            int position = 0;
                            while(cursor.moveToNext()){
                                movies[position] = new Movie(
                                        cursor.getString(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_TITLE)),
                                        cursor.getInt(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID)),
                                        cursor.getString(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH)),
                                        cursor.getString(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_BACKDROP_PATH)),
                                        cursor.getString(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW)),
                                        cursor.getDouble(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_VOTE_AVERAGE)),
                                        cursor.getString(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE)),
                                        cursor.getInt(cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_VOTE_COUNT))
                                );
                            }
                            return movies;
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
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Movie[]> loader, Movie[] movies) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = sharedPref.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popular));
        //If there is new data from the JSON object or favorites
        mBinding.pbLoadingIndicator.setVisibility(View.INVISIBLE);
        mBinding.swiperefresh.setRefreshing(false);

        if (sortOrder.equals(getString(R.string.pref_sort_favorites)) && movies.length == 0) {
            showNoFavoritesMessage();
        } else if (movies != null){
            mBinding.recyclerviewMovies.smoothScrollToPosition(0);
            movieAdapter.clear();
            for (Movie movie : movies){
                movieAdapter.addMovie(movie);
            }
            showGridView();
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
