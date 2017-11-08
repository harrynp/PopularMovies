package com.github.harrynp.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by harry on 11/7/2017.
 */

public class Review implements Parcelable {
    private final String LOG_TAG = Review.class.getSimpleName();
    private String id;
    private String author;
    private String content;
    private String url;

    public Review(JSONObject reviewInfo) {
        try{
            id = reviewInfo.getString("id");
            author = reviewInfo.getString("author");
            content = reviewInfo.getString("content");
            url = reviewInfo.getString("url");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    protected Review(Parcel in) {
        id = in.readString();
        author = in.readString();
        content = in.readString();
        url = in.readString();
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(id);
        dest.writeString(author);
        dest.writeString(content);
        dest.writeString(url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId(){
        return id;
    }

    public String getAuthor(){
        return author;
    }

    public String getContent(){
        return content;
    }

    public String getUrl(){
        return url;
    }
}
