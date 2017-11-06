package com.github.harrynp.popularmovies;

import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.github.harrynp.popularmovies.databinding.ActivityDetailBinding;

public class DetailActivity extends AppCompatActivity {

    ActivityDetailBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sharedPref.getString(getString(R.string.pref_theme_key), getString(R.string.pref_theme_light));
        if (theme.equals(getString(R.string.pref_theme_dark))) {
            setTheme(R.style.ActivityTheme_Primary_Base_Dark);
        }
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);
        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
