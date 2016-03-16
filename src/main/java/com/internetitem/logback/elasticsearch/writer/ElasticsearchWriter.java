package com.internetitem.logback.elasticsearch.writer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Collections;

import com.internetitem.logback.elasticsearch.config.HttpRequestHeader;
import com.internetitem.logback.elasticsearch.config.HttpRequestHeaders;
import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.util.ErrorReporter;

/**
 * The Class ElasticsearchWriter.
 */
public class ElasticsearchWriter implements SafeWriter {

    /** The send buffer. */
    private final StringBuilder sendBuffer;

    /** The error reporter. */
    private final ErrorReporter errorReporter;

    /** The settings. */
    private final Settings settings;

    /** The header list. */
    private final Collection<HttpRequestHeader> headerList;

    /** The buffer exceeded. */
    private boolean bufferExceeded;

    /**
     * Instantiates a new elasticsearch writer.
     *
     * @param errorReporter the error reporter
     * @param settings the settings
     * @param headers the headers
     */
    public ElasticsearchWriter(final ErrorReporter errorReporter, final Settings settings, final HttpRequestHeaders headers) {
	this.errorReporter = errorReporter;
	this.settings = settings;
	headerList = headers != null && headers.getHeaders() != null ? headers.getHeaders() : Collections.<HttpRequestHeader> emptyList();
	sendBuffer = new StringBuilder();
    }

    /* (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.writer.SafeWriter#write(char[], int, int)
     */
    public void write(final char[] cbuf, final int off, final int len) {
	if (bufferExceeded) {
	    return;
	}

	sendBuffer.append(cbuf, off, len);

	if (sendBuffer.length() >= settings.getMaxQueueSize()) {
	    errorReporter.logWarning("Send queue maximum size exceeded - log messages will be lost until the buffer is cleared");
	    bufferExceeded = true;
	}
    }

    /* (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.writer.SafeWriter#sendData()
     */
    public void sendData() throws IOException {
	if (sendBuffer.length() <= 0) {
	    return;
	}

	final HttpURLConnection urlConnection = (HttpURLConnection) (settings.getUrl().openConnection());
	try {
	    urlConnection.setDoInput(true);
	    urlConnection.setDoOutput(true);
	    urlConnection.setReadTimeout(settings.getReadTimeout());
	    urlConnection.setConnectTimeout(settings.getConnectTimeout());
	    urlConnection.setRequestMethod("POST");

	    if (!headerList.isEmpty()) {
		for (final HttpRequestHeader header : headerList) {
		    urlConnection.setRequestProperty(header.getName(), header.getValue());
		}
	    }
	    final Writer writer = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
	    writer.write(sendBuffer.toString());
	    writer.flush();
	    writer.close();

	    final int rc = urlConnection.getResponseCode();
	    if (rc != 200) {
		final String data = slurpErrors(urlConnection);
		throw new IOException("Got response code [" + rc + "] from server with data " + data);
	    }
	} finally {
	    urlConnection.disconnect();
	}

	sendBuffer.setLength(0);
	if (bufferExceeded) {
	    errorReporter.logInfo("Send queue cleared - log messages will no longer be lost");
	    bufferExceeded = false;
	}
    }

    /* (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.writer.SafeWriter#hasPendingData()
     */
    public boolean hasPendingData() {
	return sendBuffer.length() != 0;
    }

    /**
     * Slurp errors.
     *
     * @param urlConnection the url connection
     * @return the string
     */
    private static String slurpErrors(final HttpURLConnection urlConnection) {
	try {
	    final InputStream stream = urlConnection.getErrorStream();
	    if (stream == null) {
		return "<no data>";
	    }

	    final StringBuilder builder = new StringBuilder();
	    final InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
	    final char[] buf = new char[2048];
	    int numRead;
	    while ((numRead = reader.read(buf)) > 0) {
		builder.append(buf, 0, numRead);
	    }
	    return builder.toString();
	} catch (final Exception e) {
	    return "<error retrieving data: " + e.getMessage() + ">";
	}
    }

}
