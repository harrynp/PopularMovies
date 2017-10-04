package com.github.harrynp.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by harry on 9/26/2017.
 */

public class Movie implements Parcelable {
    private final String LOG_TAG = Movie.class.getSimpleName();
    private String movieName;
    private int movieId;
    private String posterPath;
    private String backdropPath;
    private String overview;
    private double rating;
    private String releaseDate;
    private int voteCount;

    public Movie(JSONObject movieInfo){
        try{
            movieName = movieInfo.getString("title");
            movieId = movieInfo.getInt("id");
            posterPath = movieInfo.getString("poster_path");
            backdropPath = movieInfo.getString("backdrop_path");
            overview = movieInfo.getString("overview");
            rating = movieInfo.getDouble("vote_average");
            releaseDate = movieInfo.getString("release_date");
            voteCount = movieInfo.getInt("vote_count");

        } catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private Movie(Parcel in){
        movieName = in.readString();
        movieId = in.readInt();
        posterPath = in.readString();
        backdropPath = in.readString();
        overview = in.readString();
        rating = in.readDouble();
        releaseDate = in.readString();
        voteCount = in.readInt();
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>(){
        public Movie createFromParcel(Parcel in){
            return new Movie(in);
        }
        public Movie[] newArray(int size){
            return new Movie[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(movieName);
        dest.writeInt(movieId);
        dest.writeString(posterPath);
        dest.writeString(backdropPath);
        dest.writeString(overview);
        dest.writeDouble(rating);
        dest.writeString(releaseDate);
        dest.writeInt(voteCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getMovieName(){
        return movieName;
    }

    public int getMovieId(){
        return movieId;
    }

    public String getPosterPath(){
        return posterPath;
    }

    public String getBackdropPath(){
        return backdropPath;
    }

    public String getOverview(){
        return overview;
    }

    public double getRating(){
        return rating;
    }

    public String getReleaseDate(){
        return releaseDate;
    }

    public int getVoteCount(){
        return voteCount;
    }

}
