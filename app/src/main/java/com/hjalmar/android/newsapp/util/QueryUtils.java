package com.hjalmar.android.newsapp.util;

import android.text.TextUtils;
import android.util.Log;

import com.hjalmar.android.newsapp.model.Article;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hjalmar
 * On 24/06/2018.
 */
public class QueryUtils {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * The timeout in milliseconds to estabilish a connection
     */
    private static final int CONNECT_TIMEOUT = 15000;

    /**
     * The timeout in milliseconds when reading from the input stream when a connection is estabilished to a resource
     */
    private static final int READ_TIMEOUT = 10000;

    private enum GuardianApiJsonTags {
        RESPONSE("response"), RESULTS("results"), TITLE("webTitle"),
        SECTION("sectionName"), DATE("webPublicationDate"), URL("webUrl"), TAGS("tags");

        private final String tag;

        GuardianApiJsonTags(String tag) {
            this.tag = tag;
        }

        public String tag() {
            return tag;
        }
    }

    private enum HttpRequestMethod {
        GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE
    }

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the Guardian REST API and return a list of {@link Article} objects.
     */
    public static List<Article> fetchArticleData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = "";
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and return a list of {@link Article}s
        return extractFeatureFromJson(jsonResponse);
    }

    /**
     * Return new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error while instantiating the URL", exception);
            return null;
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        if (url == null) {
            return "";
        }

        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(HttpRequestMethod.GET.toString());
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.setReadTimeout(READ_TIMEOUT);
            urlConnection.connect();

            final int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Http response code not successful: " + responseCode + " (" + urlConnection.getResponseMessage() + ")");
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException caught while requesting data from server", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link Article} objects that has been built up from
     * parsing a JSON response.
     */
    private static List<Article> extractFeatureFromJson(String jsonResponse) {
        // If the JSON string is empty or null, then don't go forward
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        List<Article> articles = new ArrayList<>();

        try {
            // Parse the response given by the JSON response string and
            // build up a list of Article objects with the corresponding data.
            JSONObject root = new JSONObject(jsonResponse);
            JSONObject response = root.getJSONObject(GuardianApiJsonTags.RESPONSE.tag());
            JSONArray jsonResultsArray = response.getJSONArray(GuardianApiJsonTags.RESULTS.tag());

            // For each result in the jsonResultsArray, create an {@link Article} object
            for (int i = 0; i < jsonResultsArray.length(); i++) {
                JSONObject jsonResultObject = jsonResultsArray.getJSONObject(i);

                String articleTitle = jsonResultObject.getString(GuardianApiJsonTags.TITLE.tag());
                String sectionName = jsonResultObject.getString(GuardianApiJsonTags.SECTION.tag());
                String publicationDate = jsonResultObject.getString(GuardianApiJsonTags.DATE.tag());
                String url = jsonResultObject.getString(GuardianApiJsonTags.URL.tag());

                // Fill authors list if found in tags array
                List<String> authors = new ArrayList<>();
                JSONArray jsonTagsArray = jsonResultObject.getJSONArray(GuardianApiJsonTags.TAGS.tag());
                if (jsonTagsArray != null && jsonTagsArray.length() != 0) {
                    for (int j = 0; j < jsonTagsArray.length(); j++) {
                        JSONObject jsonTagObject = jsonTagsArray.getJSONObject(j);
                        authors.add(jsonTagObject.getString(GuardianApiJsonTags.TITLE.tag()));
                    }
                }

                Article article = new Article(articleTitle, authors, sectionName, publicationDate, url);
                articles.add(article);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the JSON Response", e);
        }

        return articles;
    }

}
