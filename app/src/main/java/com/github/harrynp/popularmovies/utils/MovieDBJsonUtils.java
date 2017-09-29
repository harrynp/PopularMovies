package com.github.harrynp.popularmovies.utils;

import com.github.harrynp.popularmovies.data.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by harry on 9/29/2017.
 */

public final class MovieDBJsonUtils {

    public static ArrayList<Movie> getMovieDataFromJson(String moviesJsonString) throws JSONException {
        ArrayList<Movie> movies = new ArrayList<Movie>();
        final String TMDB_RESULTS = "results";
        JSONObject moviesJson = new JSONObject(moviesJsonString);
        JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

        for (int i = 0; i < moviesArray.length(); ++i){
            movies.add(new Movie(moviesArray.getJSONObject(i)));
        }
        return movies;
    }
}
