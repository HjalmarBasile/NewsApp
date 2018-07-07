package com.hjalmar.android.newsapp.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.hjalmar.android.newsapp.model.Article;
import com.hjalmar.android.newsapp.util.QueryUtils;

import java.util.List;

/**
 * Loads a list of articles by using an AsyncTask to perform the
 * network request to the given URL.
 */
public class ArticleLoader extends AsyncTaskLoader<List<Article>> {

    private String mUrl;

    public ArticleLoader(Context context, String url) {
        super(context);
        this.mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        // Required to trigger loadInBackground()
        forceLoad();
    }

    @Override
    public List<Article> loadInBackground() {
        // Don't perform the request if the url is null.
        if (mUrl == null) {
            return null;
        }

        return QueryUtils.fetchArticleData(mUrl);
    }

}
