package com.example.android.quakereport;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.loader.content.AsyncTaskLoader;

import java.util.List;

public class EarthquakeLoader extends AsyncTaskLoader<List<Earthquake>> {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = EarthquakeLoader.class.getName();

    /**
     * Query URL
     */
    private String mUrl;

    /**
     * Constructs a new {@link EarthquakeLoader}.
     *
     * @param context of the activity
     * @param url     to load data from
     */
    public EarthquakeLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        Log.i(LOG_TAG, "TEST: onStartLoading() called...");
        //No forceLoad the Loader, since we are doing it directly from the startLoader() method
        //forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<Earthquake> loadInBackground() {

        if (mUrl == null || !isNetworkActive()) {
            Log.i(LOG_TAG,"TEST: LoadInBackground() called, No Internet connection");
            return null;
        }

        Log.i(LOG_TAG, "TEST: LoadInBackground() called, There is Internet connection");
        // Perform the network request, parse the response, and extract a list of earthquakes.
        List<Earthquake> earthquakes = QueryUtils.fetchEarthquakeData(mUrl);
        return earthquakes;
    }

    public boolean isNetworkActive() {
        // Check for connectivity status
        ConnectivityManager cm = (ConnectivityManager) getContext().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}