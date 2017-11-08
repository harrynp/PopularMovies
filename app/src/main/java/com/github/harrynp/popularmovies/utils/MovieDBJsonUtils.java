package com.github.harrynp.popularmovies.utils;

import com.github.harrynp.popularmovies.data.Movie;
import com.github.harrynp.popularmovies.data.Review;
import com.github.harrynp.popularmovies.data.Trailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by harry on 9/29/2017.
 */

public final class MovieDBJsonUtils {
    private static final String TMDB_RESULTS = "results";

    private static JSONArray getResultsFromJsonString(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);
        JSONArray results = json.getJSONArray(TMDB_RESULTS);
        return results;
    }

    public static Movie[] getMovieDataFromJson(String moviesJsonString) throws JSONException {
        JSONArray moviesArray = getResultsFromJsonString(moviesJsonString);
        Movie[] movies = new Movie[moviesArray.length()];
        for (int i = 0; i < moviesArray.length(); ++i){
            movies[i] = new Movie(moviesArray.getJSONObject(i));
        }
        return movies;
    }

    public static Trailer[] getTrailerDataFromJson(String trailerJsonString) throws JSONException {
        JSONArray trailersArray = getResultsFromJsonString(trailerJsonString);
        Trailer[] trailers = new Trailer[trailersArray.length()];
        for (int i = 0; i < trailersArray.length(); ++i){
            trailers[i] = new Trailer(trailersArray.getJSONObject(i));
        }
        return trailers;
    }

    public static Review[] getReviewDataFromJson(String reviewJsonString) throws JSONException {
        JSONArray reviewsArray = getResultsFromJsonString(reviewJsonString);
        Review[] reviews = new Review[reviewsArray.length()];
        for (int i = 0; i < reviewsArray.length(); ++i){
            reviews[i] = new Review(reviewsArray.getJSONObject(i));;
        }
        return reviews;
    }

    public static Integer getMovieRuntimeFromJson(String movieJsonString) throws JSONException {
        final String MOVIE_RUNTIME = "runtime";
        JSONObject movieJson = new JSONObject(movieJsonString);
        return movieJson.getInt(MOVIE_RUNTIME);
    }
}
