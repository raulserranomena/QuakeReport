/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.Loader;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

public class EarthquakeActivity extends AppCompatActivity implements androidx.loader.app.LoaderManager.LoaderCallbacks<List<Earthquake>> {

    public static final String LOG_TAG = EarthquakeActivity.class.getName();

    /**
     * Constant value for the earthquake loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int EARTHQUAKE_LOADER_ID = 1;

    /**
     * URL for earthquake data from the USGS dataset
     */
    private static final String USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&orderby=time&minmag=6&limit=10";

    /**
     * Adapter for the list of earthquakes
     */
    private EarthquakeAdapter mAdapter;

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;

    /**
     * ProgressBar to indicate loading state
     */
    private View loadingIndicator;

    /**
     * Retry button to try loading again
     */
    private Button retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(LOG_TAG, "TEST: Earthquake Activity onCreate() called ");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        //Look up the Retry button
        retryButton = findViewById(R.id.retry_button);

        //Look up the Progress Bar
        loadingIndicator = findViewById(R.id.loading_indicator);

        // Find a reference to the {@link ListView} in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

        //hook up the TextView as the empty view of the ListView
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        earthquakeListView.setEmptyView(mEmptyStateTextView);

        // Create a new adapter that takes an empty list of earthquakes as input
        mAdapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        earthquakeListView.setAdapter(mAdapter);

//        // Get a reference to the ConnectivityManager to check state of network connectivity
//        ConnectivityManager connMgr = (ConnectivityManager)
//                getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        // Get details on the currently active default data network
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//
//        // If there is a network connection, fetch data
//        if (networkInfo != null && networkInfo.isConnected()) {
//            // Get a reference to the LoaderManager, in order to interact with loaders.
//            LoaderManager loaderManager = getLoaderManager();
//
//            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
//            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
//            // because this activity implements the LoaderCallbacks interface).
//            //loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
//            Log.i(LOG_TAG, "TEST: calling initloader()...");
//            androidx.loader.app.LoaderManager.getInstance(this).initLoader(EARTHQUAKE_LOADER_ID, null, this);
//
//        } else {
//            // Otherwise, display error
//            // First, hide loading indicator so error message will be visible
//            View loadingIndicator = findViewById(R.id.loading_indicator);
//            loadingIndicator.setVisibility(View.GONE);
//
//            // Update empty state with no connection error message
//            mEmptyStateTextView.setText(R.string.no_internet_connection);
//        }

        //Checks Internet Connection and Starts the Loader
        startLoader();

        // Set an item click listener on the Retry button if there is no Internet Connection
        // to try to Start the Loader again. Has a delay of 1 second to show the Progress Bar
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEmptyStateTextView.setText("");
                retryButton.setVisibility(View.GONE);
                loadingIndicator.setVisibility(View.VISIBLE);
                retryButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startLoader();
                    }
                }, 1000);
            }
        });


        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected earthquake.
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current earthquake that was clicked on
                Earthquake currentEarthquake = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri earthquakeUri = Uri.parse(currentEarthquake.getUrl());

                // Create a new intent to view the earthquake URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });


    }

    @Override
    public Loader<List<Earthquake>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
        Log.i(LOG_TAG, "TEST: onCreateLoader() called...");
        return new EarthquakeLoader(this, USGS_REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> earthquakes) {

        Log.i(LOG_TAG, "TEST: onLoadFinished() called...");
        // Hide loading indicator because the data has been loaded
        loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No earthquakes found."
        mEmptyStateTextView.setText(R.string.no_earthquakes);

        // Clear the adapter of previous earthquake data
        mAdapter.clear();

        //If the list of earthquakes is empty and there is no Internet Connection, update and
        //show it in the emptyTextView
        if (earthquakes == null && !isNetworkActive()){
            mEmptyStateTextView.setText("No Internet connection");
        }

        // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (earthquakes != null && !earthquakes.isEmpty()) {
            mAdapter.addAll(earthquakes);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Earthquake>> loader) {
        Log.i(LOG_TAG, "TEST: onLoaderReset() called...");
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    private void startLoader() {
        Log.i(LOG_TAG, "TEST:  startLoader() called...");

        // If network active start fetching data
        if (isNetworkActive()) {
            Log.i(LOG_TAG, "TEST: There is Internet connection, calling initloader()...");
            loadingIndicator.setVisibility(View.VISIBLE);
            androidx.loader.app.LoaderManager.getInstance(this).initLoader(EARTHQUAKE_LOADER_ID, null, this).forceLoad();
        } else {
            Log.i(LOG_TAG, "TEST: No Internet connection...");
            mEmptyStateTextView.setText("No internet connection");
            retryButton.setVisibility(View.VISIBLE);
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    public boolean isNetworkActive() {
        // Check for connectivity status
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

}


