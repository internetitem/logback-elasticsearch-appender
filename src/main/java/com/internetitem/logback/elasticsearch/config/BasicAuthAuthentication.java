package com.internetitem.logback.elasticsearch.config;

import java.net.HttpURLConnection;
import java.util.Base64;

public class BasicAuthAuthentication implements Authentication {
    public void addAuth(HttpURLConnection urlConnection, String body) {
        String userInfo = urlConnection.getURL().getUserInfo();
        if (userInfo != null) {
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userInfo.getBytes()));
            urlConnection.setRequestProperty("Authorization", basicAuth);
        }
    }
}
