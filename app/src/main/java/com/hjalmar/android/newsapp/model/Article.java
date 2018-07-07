package com.hjalmar.android.newsapp.model;

import java.util.Collections;
import java.util.List;

/**
 * Created by hjalmar
 * On 24/06/2018.
 */
public class Article {

    private final String mArticleTitle;
    private final List<String> mAuthors;
    private final String mSectionName;
    private final String mPublicationDate;
    private final String mUrl;

    public Article(String articleTitle, List<String> authors, String sectionName, String publicationDate, String url) {
        this.mArticleTitle = articleTitle;
        this.mAuthors = authors != null ? authors : Collections.<String>emptyList();
        this.mSectionName = sectionName;
        this.mPublicationDate = publicationDate;
        this.mUrl = url;
    }

    public String getArticleTitle() {
        return mArticleTitle;
    }

    public List<String> getAuthors() {
        return mAuthors;
    }

    public String getSectionName() {
        return mSectionName;
    }

    public String getPublicationDate() {
        return mPublicationDate;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public String toString() {
        return "Article{" +
                "articleTitle='" + mArticleTitle + '\'' +
                ", authors=" + mAuthors.toString() +
                ", sectionName='" + mSectionName + '\'' +
                ", publicationDate='" + mPublicationDate + '\'' +
                ", url='" + mUrl + '\'' +
                '}';
    }

}
