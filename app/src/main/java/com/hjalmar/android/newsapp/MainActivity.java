package com.hjalmar.android.newsapp;

import android.app.LoaderManager;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hjalmar.android.newsapp.loader.ArticleLoader;
import com.hjalmar.android.newsapp.model.Article;
import com.hjalmar.android.newsapp.model.ArticleAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Article>> {

    private static final int ARTICLE_LOADER_ID = 0;

    private static final String GUARDIAN_REQUEST_URL = "https://content.guardianapis.com/search";

    /**
     * Keys used for the query parameters to build the http request
     */
    private enum RequestParameterKey {
        API_KEY("api-key"), FORMAT("format"), TAG("tag"), SHOW_TAGS("show-tags"), Q("q"), ORDER_BY("order-by");

        private final String tag;

        RequestParameterKey(String tag) {
            this.tag = tag;
        }

        String tag() {
            return tag;
        }
    }

    /**
     * Adapter for the list of articles
     */
    private ArrayAdapter<Article> mAdapter;

    /**
     * ProgressBar that is displayed while waiting for the data from the server
     */
    private ProgressBar mProgressBar;

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find a reference to the {@link ListView} in the layout
        ListView articleListView = findViewById(R.id.list);

        mProgressBar = findViewById(R.id.loading_spinner);

        // Set a default empty view used when the adapter is not filled
        mEmptyStateTextView = findViewById(R.id.empty_view);
        articleListView.setEmptyView(mEmptyStateTextView);

        // Create a new {@link ArrayAdapter} of article
        mAdapter = new ArticleAdapter(this, 0, new ArrayList<Article>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        articleListView.setAdapter(mAdapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected article.
        articleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Article article = mAdapter.getItem(position);
                if (article != null) {
                    openWebPage(article.getUrl());
                }
            }
        });

        if (isDeviceConnected()) {
            // Start the AsyncTaskLoader to fetch the article data
            getLoaderManager().initLoader(ARTICLE_LOADER_ID, null, this);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public Loader<List<Article>> onCreateLoader(int id, Bundle args) {
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Retrieve preferences settings
        final String gameTopicSetting = defaultSharedPreferences.getString(getString(R.string.settings_game_topic_key), getString(R.string.settings_game_topic_default));
        final String orderBySetting = defaultSharedPreferences.getString(getString(R.string.settings_order_by_key), getString(R.string.settings_order_by_default));

        Uri baseUri = Uri.parse(GUARDIAN_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Append query parameters and their values
        uriBuilder.appendQueryParameter(RequestParameterKey.API_KEY.tag(), getString(R.string.request_parameter_value_api_key));
        uriBuilder.appendQueryParameter(RequestParameterKey.FORMAT.tag(), getString(R.string.request_parameter_value_format));
        uriBuilder.appendQueryParameter(RequestParameterKey.TAG.tag(), getString(R.string.request_parameter_value_tag));
        uriBuilder.appendQueryParameter(RequestParameterKey.SHOW_TAGS.tag(), getString(R.string.request_parameter_value_show_tags));
        uriBuilder.appendQueryParameter(RequestParameterKey.Q.tag(), gameTopicSetting);
        uriBuilder.appendQueryParameter(RequestParameterKey.ORDER_BY.tag(), orderBySetting);

        // Create a new loader for the given URL
        return new ArticleLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articles) {
        // We hide the progress bar
        mProgressBar.setVisibility(View.GONE);

        // Clear the adapter of previous data
        mAdapter.clear();

        // If there is a valid list of {@link Article}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (articles != null && !articles.isEmpty()) {
            mAdapter.addAll(articles);
        }

        // We add the text to the empty View only after,
        // so it will get displayed only in case of failure
        mEmptyStateTextView.setText(R.string.news_not_found);
    }

    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    private void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Check if the device is connected to internet
     */
    private boolean isDeviceConnected() {
        boolean isConnected = false;

        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        }

        return isConnected;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
