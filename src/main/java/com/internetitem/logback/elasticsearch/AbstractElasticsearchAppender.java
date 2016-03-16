package com.internetitem.logback.elasticsearch;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import ch.qos.logback.core.UnsynchronizedAppenderBase;

import com.internetitem.logback.elasticsearch.config.ElasticsearchProperties;
import com.internetitem.logback.elasticsearch.config.HttpRequestHeaders;
import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.util.ErrorReporter;

/**
 * The Class AbstractElasticsearchAppender.
 * @param <T> the generic type
 */
public abstract class AbstractElasticsearchAppender<T> extends UnsynchronizedAppenderBase<T> {

    /** The settings. */
    protected Settings settings;

    /** The elasticsearch properties. */
    protected ElasticsearchProperties elasticsearchProperties;

    /** The publisher. */
    protected AbstractElasticsearchPublisher<T> publisher;

    /** The error reporter. */
    protected ErrorReporter errorReporter;

    /** The headers. */
    protected HttpRequestHeaders headers;

    /**
     * Instantiates a new abstract elasticsearch appender.
     */
    public AbstractElasticsearchAppender() {
	this.settings = new Settings();
	this.headers = new HttpRequestHeaders();
    }

    /**
     * Instantiates a new abstract elasticsearch appender.
     * @param settings the settings
     */
    public AbstractElasticsearchAppender(final Settings settings) {
	this.settings = settings;
    }

    /*
     * (non-Javadoc)
     * @see ch.qos.logback.core.UnsynchronizedAppenderBase#start()
     */
    @Override
    public void start() {
	super.start();
	this.errorReporter = getErrorReporter();
	try {
	    this.publisher = buildElasticsearchPublisher();
	} catch (final IOException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Publish event.
     * @param eventObject the event object
     */
    protected void publishEvent(final T eventObject) {
	publisher.addEvent(eventObject);
    }

    /**
     * Gets the error reporter.
     * @return the error reporter
     */
    // VisibleForTesting
    protected ErrorReporter getErrorReporter() {
	return new ErrorReporter(settings, getContext());
    }

    /**
     * Builds the elasticsearch publisher.
     * @return the abstract elasticsearch publisher
     * @throws IOException Signals that an I/O exception has occurred.
     */
    // VisibleForTesting
    protected abstract AbstractElasticsearchPublisher<T> buildElasticsearchPublisher() throws IOException;

    /*
     * (non-Javadoc)
     * @see ch.qos.logback.core.UnsynchronizedAppenderBase#stop()
     */
    @Override
    public void stop() {
	super.stop();
    }

    /*
     * (non-Javadoc)
     * @see ch.qos.logback.core.UnsynchronizedAppenderBase#append(java.lang.Object)
     */
    @Override
    protected void append(final T eventObject) {
	appendInternal(eventObject);
    }

    /**
     * Append internal.
     * @param eventObject the event object
     */
    protected abstract void appendInternal(T eventObject);

    /**
     * Sets the properties.
     * @param elasticsearchProperties the new properties
     */
    public void setProperties(final ElasticsearchProperties elasticsearchProperties) {
	this.elasticsearchProperties = elasticsearchProperties;
    }

    /**
     * Sets the sleep time.
     * @param sleepTime the new sleep time
     */
    public void setSleepTime(final int sleepTime) {
	settings.setSleepTime(sleepTime);
    }

    /**
     * Sets the max retries.
     * @param maxRetries the new max retries
     */
    public void setMaxRetries(final int maxRetries) {
	settings.setMaxRetries(maxRetries);
    }

    /**
     * Sets the connect timeout.
     * @param connectTimeout the new connect timeout
     */
    public void setConnectTimeout(final int connectTimeout) {
	settings.setConnectTimeout(connectTimeout);
    }

    /**
     * Sets the read timeout.
     * @param readTimeout the new read timeout
     */
    public void setReadTimeout(final int readTimeout) {
	settings.setReadTimeout(readTimeout);
    }

    /**
     * Sets the include caller data.
     * @param includeCallerData the new include caller data
     */
    public void setIncludeCallerData(final boolean includeCallerData) {
	settings.setIncludeCallerData(includeCallerData);
    }

    /**
     * Sets the errors to stderr.
     * @param errorsToStderr the new errors to stderr
     */
    public void setErrorsToStderr(final boolean errorsToStderr) {
	settings.setErrorsToStderr(errorsToStderr);
    }

    /**
     * Sets the logs to stderr.
     * @param logsToStderr the new logs to stderr
     */
    public void setLogsToStderr(final boolean logsToStderr) {
	settings.setLogsToStderr(logsToStderr);
    }

    /**
     * Sets the max queue size.
     * @param maxQueueSize the new max queue size
     */
    public void setMaxQueueSize(final int maxQueueSize) {
	settings.setMaxQueueSize(maxQueueSize);
    }

    /**
     * Sets the index.
     * @param index the new index
     */
    public void setIndex(final String index) {
	settings.setIndex(index);
    }

    /**
     * Sets the type.
     * @param type the new type
     */
    public void setType(final String type) {
	settings.setType(type);
    }

    /**
     * Sets the url.
     * @param url the new url
     * @throws MalformedURLException the malformed url exception
     */
    public void setUrl(final String url) throws MalformedURLException {
	settings.setUrl(new URL(url));
    }

    /**
     * Sets the logger name.
     * @param logger the new logger name
     */
    public void setLoggerName(final String logger) {
	settings.setLoggerName(logger);
    }

    /**
     * Sets the error logger name.
     * @param logger the new error logger name
     */
    public void setErrorLoggerName(final String logger) {
	settings.setErrorLoggerName(logger);
    }

    /**
     * Sets the headers.
     * @param httpRequestHeaders the new headers
     */
    public void setHeaders(final HttpRequestHeaders httpRequestHeaders) {
	this.headers = httpRequestHeaders;
    }

    /**
     * Sets the raw json message.
     * @param rawJsonMessage the new raw json message
     */
    public void setRawJsonMessage(final boolean rawJsonMessage) {
	settings.setRawJsonMessage(rawJsonMessage);
    }

    /**
     * Sets the time to live.
     * @param timeToLive the new time to live
     */
    public void setTimeToLive(final int timeToLive) {
	settings.setTimeToLive(timeToLive);
    }
}
