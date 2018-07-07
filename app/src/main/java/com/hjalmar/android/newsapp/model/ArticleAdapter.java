package com.hjalmar.android.newsapp.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hjalmar.android.newsapp.R;

import java.util.List;

/**
 * Created by hjalmar
 * On 24/06/2018.
 */
public class ArticleAdapter extends ArrayAdapter<Article> {

    private static final int DATE_STRING_LENGTH = 10;

    private final LayoutInflater mInflater = LayoutInflater.from(getContext());

    private static class ViewHolder {
        TextView articleTitleView;
        TextView articleAuthorsView;
        TextView articleSectionView;
        TextView articleDateView;
    }

    public ArticleAdapter(@NonNull Context context, int resource, @NonNull List<Article> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.article_item, parent, false);

            holder = new ViewHolder();
            holder.articleTitleView = convertView.findViewById(R.id.article_title);
            holder.articleAuthorsView = convertView.findViewById(R.id.article_authors);
            holder.articleSectionView = convertView.findViewById(R.id.article_section);
            holder.articleDateView = convertView.findViewById(R.id.article_date);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Article article = getItem(position);
        if (article != null) {
            holder.articleTitleView.setText(article.getArticleTitle());
            holder.articleAuthorsView.setText(buildAuthorsString(article.getAuthors()));
            holder.articleSectionView.setText(article.getSectionName());
            holder.articleDateView.setText(article.getPublicationDate().substring(0, DATE_STRING_LENGTH));
        }

        return convertView;
    }

    private String buildAuthorsString(List<String> authors) {
        if (authors == null || authors.size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int lastIndex = authors.size() - 1;
        for (int i = 0; i < lastIndex; i++) {
            sb.append(authors.get(i));
            sb.append(", ");
        }
        sb.append(authors.get(lastIndex));

        return sb.toString();
    }

}
