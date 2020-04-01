package com.internetitem.logback.elasticsearch.config;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.ReadLimitInfo;
import com.amazonaws.SignableRequest;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.regions.Regions;
import com.amazonaws.util.StringInputStream;

/**
 * This class implements Amazon AWS v4 Signature signing for ElasticSearch.
 *
 * @author blagerweij
 */
public class AWSAuthentication implements Authentication {

    private final AWS4Signer signer;
    private final AWSCredentials credentials;

    public AWSAuthentication() {
        signer = new AWS4Signer(false);
        signer.setServiceName("es");
        signer.setRegionName(getCurrentRegion());
        AWSCredentialsProvider credsProvider = new DefaultAWSCredentialsProviderChain();
        credentials = credsProvider.getCredentials();
    }

    @Override
    public void addAuth(HttpURLConnection urlConnection, String body) {

        signer.sign(new URLConnectionSignableRequest(urlConnection, body), credentials);
    }
    
    private String getCurrentRegion() {
		if(Regions.getCurrentRegion() != null) {
			return Regions.getCurrentRegion().getName();
		}
		return null;
	}

    /**
     * Wrapper for signing a HttpURLConnection
     */
    private static class URLConnectionSignableRequest implements SignableRequest<HttpURLConnection> {

        private final HttpURLConnection urlConnection;
        private final String body;
        private final Map<String,String> headers = new HashMap<>();

        public URLConnectionSignableRequest(HttpURLConnection urlConnection, String body) {
            this.urlConnection = urlConnection;
            this.body = body;
            addHeader("User-Agent","ElasticSearchWriter/1.0");
            addHeader("Accept","*/*");
            addHeader("Content-Type","application/json");
            addHeader("Content-Length",String.valueOf(body.length()));
        }

        @Override
        public void addHeader(String name, String value) {
            this.urlConnection.addRequestProperty(name, value);
            headers.put(name,value);
        }

        @Override
        public Map<String, String> getHeaders() {
            return headers;
        }

        @Override
        public String getResourcePath() {
            return urlConnection.getURL().getPath();
        }

        @Override
        public void addParameter(String name, String value) {

        }

        @Override
        public Map<String, List<String>> getParameters() {
            return Collections.emptyMap();
        }

        @Override
        public URI getEndpoint() {
            try {
                URL u = urlConnection.getURL();
                return new URI(u.getProtocol(),null, u.getHost(), u.getPort(), null, null, null);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public HttpMethodName getHttpMethod() {
            return HttpMethodName.fromValue(urlConnection.getRequestMethod());
        }

        @Override
        public int getTimeOffset() {
            return 0;
        }

        @Override
        public InputStream getContent() {
            try {
                return new StringInputStream(body);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

        }

        @Override
        public void setContent(InputStream content) {
        }

        @Override
        public InputStream getContentUnwrapped() {
            return getContent();
        }

        @Override
        public ReadLimitInfo getReadLimitInfo() {
            return null;
        }

        @Override
        public Object getOriginalRequestObject() {
            return null;
        }
    }
}

