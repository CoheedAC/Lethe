package com.cs48.lethe.utils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * A http client class with static accessors to make it
 * easy to communicate with the Heroku server’s API
 */
public class HerokuRestClient {

    // URL of the Heroku server
    private static final String BASE_URL = "https://frozen-sea-8879.herokuapp.com/";

    // Static class variable of the AsyncHttpClient
    private static AsyncHttpClient client = new AsyncHttpClient();

    /**
     * Performs a HTTP GET request with parameters.
     *
     * @param url             the URL to send the request to.
     * @param params          additional GET parameters to send with the request.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    /**
     * Perform a HTTP POST request with parameters.
     *
     * @param url             the URL to send the request to.
     * @param params          additional POST parameters or files to send with the request.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    /**
     * Gets the absolute URL given a relative URL
     *
     * @param relativeUrl The URL that goes after the server URL
     * @return The combined URL
     */
    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
