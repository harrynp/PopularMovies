package com.github.harrynp.popularmovies.utils;

/**
 * Created by harry on 9/29/2017.
 */

import android.net.Uri;

import com.github.harrynp.popularmovies.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * These utilities will be used to communicate with the network.
 */
public final class NetworkUtils {

    //    final String MOVIE_POPULAR_BASE_URL = "https://api.themoviedb.org/3/movie/popular?";
//    final String MOVIE_TOP_RATED_BASE_URL = "https://api.themoviedb.org/3/movie/top_rated?";
    final static String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie/";

    final static String PARAM_SORT_POPULAR = "popular";
    final static String PARAM_SORT_TOP_RATED = "top_rated";
    final static String PARAM_API_KEY = "api_key";


    public static URL buildUrl(String sortBy) {
        Uri builtUri;
        if (sortBy.equals(PARAM_SORT_POPULAR)) {
            builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendPath(PARAM_SORT_POPULAR)
                    .appendQueryParameter(PARAM_API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();
        } else if (sortBy.equals(PARAM_SORT_TOP_RATED)) {
            builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendPath(PARAM_SORT_TOP_RATED)
                    .appendQueryParameter(PARAM_API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();
        } else {
            return null;
        }
        URL url = null;
//        Log.v("NetworkUtils", builtUri.toString());
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static URL buildURL(Integer movieID, String path){
        Uri builtUri;
        if (movieID != null && path == null) {
            builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendPath(movieID.toString())
                    .appendQueryParameter(PARAM_API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();
        } else if (movieID != null && path != null){
            builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendPath(movieID.toString())
                    .appendPath(path)
                    .appendQueryParameter(PARAM_API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();
        } else {
            return null;
        }
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        // These two need to be declared outside the try/catch so that they can be closed in the finally block.
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonString = null;

        urlConnection.setRequestMethod("GET");
        urlConnection.connect();

        // Read the input stream into a String
        InputStream inputStream = urlConnection.getInputStream();
        StringBuffer buffer = new StringBuffer();

        if (inputStream == null) {
            // Nothing to do since nothing is there
            return null;
        }

        reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = reader.readLine()) != null) {
            // Add new line for easier readability while debugging
            buffer.append(line + "\n");
        }

        if (buffer.length() == 0) {
            // Stream was empty.  No point in parsing.
            return null;
        }

        moviesJsonString = buffer.toString();

        if (urlConnection != null) {
            urlConnection.disconnect();
        }
        if (reader != null) {
            reader.close();
        }
        return moviesJsonString;
    }
}