package com.internetitem.logback.elasticsearch.config;

import javax.xml.bind.DatatypeConverter;
import java.net.HttpURLConnection;

public class BasicAuthentication implements Authentication {
    public void addAuth(HttpURLConnection urlConnection, String body) {
        String userInfo = urlConnection.getURL().getUserInfo();
        if (userInfo != null) {
            String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userInfo.getBytes());
            urlConnection.setRequestProperty("Authorization", basicAuth);
        }
    }
}
