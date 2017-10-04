package com.github.harrynp.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.harrynp.popularmovies.adapters.MovieGridAdapter;
import com.github.harrynp.popularmovies.data.Movie;
import com.github.harrynp.popularmovies.utils.MovieDBJsonUtils;
import com.github.harrynp.popularmovies.utils.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private MovieGridAdapter moviesAdapter;
    private TextView mErrorMessageTextView;
    private ProgressBar mLoadingIndicator;
    private GridView mGridViewMovies;
    private SwipeRefreshLayout mSwipeContainer;


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
        moviesAdapter = new MovieGridAdapter(getActivity(), new ArrayList<Movie>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mErrorMessageTextView = (TextView) rootView.findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = (ProgressBar) rootView.findViewById(R.id.pb_loading_indicator);
        //Implemented swipe to refresh
        mSwipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                updateMovies();
            }
        });
        mGridViewMovies = (GridView) rootView.findViewById(R.id.gridview_movies);
        mGridViewMovies.setAdapter(moviesAdapter);
        mGridViewMovies.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = moviesAdapter.getItem(position);
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                detailIntent.putExtra(DetailActivityFragment.MOVIE_DETAILS, movie);
                startActivity(detailIntent);
            }
        });
        return rootView;
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
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
        mGridViewMovies.setVisibility(View.VISIBLE);
    }
    private void showErrorMessage(){
        mGridViewMovies.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.VISIBLE);
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
            FetchMoviesTask moviesTask = new FetchMoviesTask();
            moviesTask.execute(sortOrder);
        } else {
            mSwipeContainer.setRefreshing(false);
            showErrorMessage();
        }
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie>>{
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showGridView();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... strings) {
            String moviesJsonString;

            // Verify there's a sort order.
            if (strings.length == 0){
                return null;
            }
            URL url = NetworkUtils.buildUrl(strings[0]);
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

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            //If there is new data from the JSON object
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            mSwipeContainer.setRefreshing(false);
            if (movies != null){
                showGridView();
                moviesAdapter.clear();
                for (Movie movie : movies){
                    moviesAdapter.add(movie);
                }
            } else {
                showErrorMessage();
            }
        }
    }
}
