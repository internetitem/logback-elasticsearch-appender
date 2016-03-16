package com.internetitem.logback.elasticsearch.config;

import java.util.LinkedList;
import java.util.List;

/**
 * A container for the headers which will be sent to elasticsearch.
 */
public class HttpRequestHeaders {

    /** The headers. */
    private List<HttpRequestHeader> headers = new LinkedList<HttpRequestHeader>();

    /**
     * Gets the headers.
     *
     * @return the headers
     */
    public List<HttpRequestHeader> getHeaders() {
	return headers;
    }

    /**
     * Adds the header.
     *
     * @param header the header
     */
    public void addHeader(HttpRequestHeader header) {
	this.headers.add(header);
    }
}
