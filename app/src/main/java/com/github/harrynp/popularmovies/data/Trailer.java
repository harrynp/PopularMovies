package com.github.harrynp.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by harry on 11/7/2017.
 */

public class Trailer implements Parcelable {
    private final String LOG_TAG = Trailer.class.getSimpleName();
    private String id;
    private String key;
    private String name;
    private String site;
    private String type;

    public Trailer(JSONObject trailerInfo) {
        try{
            id = trailerInfo.getString("id");
            key = trailerInfo.getString("key");
            name = trailerInfo.getString("name");
            site = trailerInfo.getString("site");
            type = trailerInfo.getString("type");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    protected Trailer(Parcel in) {
        id = in.readString();
        key = in.readString();
        name = in.readString();
        site = in.readString();
        type = in.readString();
    }

    public static final Creator<Trailer> CREATOR = new Creator<Trailer>() {
        @Override
        public Trailer createFromParcel(Parcel in) {
            return new Trailer(in);
        }

        @Override
        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(id);
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(site);
        dest.writeString(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId(){
        return id;
    }

    public String getKey(){
        return key;
    }

    public String getName(){
        return name;
    }

    public String getSite(){
        return site;
    }

    public String getType(){
        return type;
    }
}
