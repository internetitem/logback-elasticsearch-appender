package com.internetitem.logback.elasticsearch.config;

import java.net.HttpURLConnection;

public interface Authentication {
    /**
     * Modify the given urlConnection for whatever authentication scheme is used.
     *
     * @param urlConnection the connection to the server
     * @param body the message being sent
     */
    void addAuth(HttpURLConnection urlConnection, String body);
}
