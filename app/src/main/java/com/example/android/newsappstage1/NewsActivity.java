package com.example.android.newsappstage1;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity {

    /**
     * URL for news data from the dataset
     */
    private static final String Api_key=BuildConfig.API_KEY;
    private static final String URL_API_KEY = "&api-key=" + Api_key;
    private static final String REQUEST_URL_String ="https://content.guardianapis.com/search?";
    private static final String URL_EXTRAS="&show-tags=contributor";
    private static final String LOG_TAG = "NEWSACTIVITY_URL";
    /**
     * Constant value for the news loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int NEWS_LOADER_ID = 1;

    //Context
    private Context currentContext;

    /**
     * Adapter for the list of news
     */
    private NewsAdapter newsAdapter;

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView emptyStateTextView;

    //News loader
    private final LoaderCallbacks<List<News>> newsLoader
            = new LoaderCallbacks<List<News>>() {
        @Override
        public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {
            // Create a new loader for the given URL
            String SECTION_CHOICE = getPreferenceStringValue(R.string.pref_topic_key, R.string.by_default_tags);
            String ORDER_BY = getPreferenceStringValue(R.string.pref_order_by_key, R.string.pref_order_label_1_value);
            StringBuilder stringBuilder=new StringBuilder();
            stringBuilder.append(REQUEST_URL_String);
            if(SECTION_CHOICE!=null)
            {
                stringBuilder.append(SECTION_CHOICE);

            }
            else{
                stringBuilder.append(getString(R.string.by_default_tags));
            }
            if(ORDER_BY!=null)
            {
                stringBuilder.append("&order-by="+ORDER_BY);

            }
            else{
                stringBuilder.append("&order-by="+getString(R.string.pref_order_by_default));
            }
            stringBuilder.append(URL_EXTRAS);
            stringBuilder.append(URL_API_KEY);
            Log.i(LOG_TAG, "API GUARDIAN_REQUEST_URL: " + stringBuilder.toString());
            return new NewsLoader(currentContext, stringBuilder.toString());
        }

        @Override
        public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Set empty state text to display "No news found."
            emptyStateTextView.setText(R.string.no_news);

            // If there is a valid list of {@link News}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (news != null && !news.isEmpty()) {
                newsAdapter.addAll(news);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<News>> loader) {
            // Loader reset, so we can clear out our existing data.
            newsAdapter.clear();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_activity);

        //Set context
        currentContext = this;

        // Find a reference to the {@link ListView} in the layout
        ListView newsListView = findViewById(R.id.list);

        emptyStateTextView = findViewById(R.id.empty_view);
        newsListView.setEmptyView(emptyStateTextView);

        // Create a new adapter that takes an empty list of news as input
        newsAdapter = new NewsAdapter(this, new ArrayList<News>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        newsListView.setAdapter(newsAdapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected news.
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current news that was clicked on
                News currentNews = newsAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri newsUri = Uri.parse(currentNews.getUrl());

                // Create a new intent to view the news URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loaders. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(NEWS_LOADER_ID, null, newsLoader);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            emptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
        case R.id.setting:
            Intent i=new Intent(NewsActivity.this,SettingActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);

    }
    public String getPreferenceStringValue(int key, int defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getString(
                getString(key),
                getString(defaultValue)
        );
    }

}