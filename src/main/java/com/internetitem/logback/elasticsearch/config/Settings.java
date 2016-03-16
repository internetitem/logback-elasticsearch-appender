package com.internetitem.logback.elasticsearch.config;

import java.net.URL;

/**
 * The Class Settings.
 */
public class Settings {

    /** The index. */
    private String index;

    /** The type. */
    private String type;

    /** The url. */
    private URL url;

    /** The logger name. */
    private String loggerName;

    /** The error logger name. */
    private String errorLoggerName;

    /** The sleep time. */
    private int sleepTime = 250;

    /** The max retries. */
    private int maxRetries = 3;

    /** The connect timeout. */
    private int connectTimeout = 30000;

    /** The read timeout. */
    private int readTimeout = 30000;

    /** The time to live. */
    private long timeToLive = 0;

    /** The logs to stderr. */
    private boolean logsToStderr;

    /** The errors to stderr. */
    private boolean errorsToStderr;

    /** The include caller data. */
    private boolean includeCallerData;

    /** The raw json message. */
    private boolean rawJsonMessage;

    /** The max queue size. */
    private int maxQueueSize = 100 * 1024 * 1024;

    /**
     * Gets the index.
     * @return the index
     */
    public String getIndex() {
	return index;
    }

    /**
     * Sets the index.
     * @param index the new index
     */
    public void setIndex(final String index) {
	this.index = index;
    }

    /**
     * Gets the type.
     * @return the type
     */
    public String getType() {
	return type;
    }

    /**
     * Sets the type.
     * @param type the new type
     */
    public void setType(final String type) {
	this.type = type;
    }

    /**
     * Gets the sleep time.
     * @return the sleep time
     */
    public int getSleepTime() {
	return sleepTime;
    }

    /**
     * Sets the sleep time.
     * @param sleepTime the new sleep time
     */
    public void setSleepTime(final int sleepTime) {
	if (sleepTime < 100) {
	    this.sleepTime = 100;
	} else {
	    this.sleepTime = sleepTime;
	}
    }

    /**
     * Gets the max retries.
     * @return the max retries
     */
    public int getMaxRetries() {
	return maxRetries;
    }

    /**
     * Sets the max retries.
     * @param maxRetries the new max retries
     */
    public void setMaxRetries(final int maxRetries) {
	this.maxRetries = maxRetries;
    }

    /**
     * Gets the connect timeout.
     * @return the connect timeout
     */
    public int getConnectTimeout() {
	return connectTimeout;
    }

    /**
     * Sets the connect timeout.
     * @param connectTimeout the new connect timeout
     */
    public void setConnectTimeout(final int connectTimeout) {
	this.connectTimeout = connectTimeout;
    }

    /**
     * Gets the read timeout.
     * @return the read timeout
     */
    public int getReadTimeout() {
	return readTimeout;
    }

    /**
     * Sets the read timeout.
     * @param readTimeout the new read timeout
     */
    public void setReadTimeout(final int readTimeout) {
	this.readTimeout = readTimeout;
    }

    /**
     * Checks if is logs to stderr.
     * @return true, if is logs to stderr
     */
    public boolean isLogsToStderr() {
	return logsToStderr;
    }

    /**
     * Sets the logs to stderr.
     * @param logsToStderr the new logs to stderr
     */
    public void setLogsToStderr(final boolean logsToStderr) {
	this.logsToStderr = logsToStderr;
    }

    /**
     * Checks if is errors to stderr.
     * @return true, if is errors to stderr
     */
    public boolean isErrorsToStderr() {
	return errorsToStderr;
    }

    /**
     * Sets the errors to stderr.
     * @param errorsToStderr the new errors to stderr
     */
    public void setErrorsToStderr(final boolean errorsToStderr) {
	this.errorsToStderr = errorsToStderr;
    }

    /**
     * Checks if is include caller data.
     * @return true, if is include caller data
     */
    public boolean isIncludeCallerData() {
	return includeCallerData;
    }

    /**
     * Sets the include caller data.
     * @param includeCallerData the new include caller data
     */
    public void setIncludeCallerData(final boolean includeCallerData) {
	this.includeCallerData = includeCallerData;
    }

    /**
     * Gets the max queue size.
     * @return the max queue size
     */
    public int getMaxQueueSize() {
	return maxQueueSize;
    }

    /**
     * Sets the max queue size.
     * @param maxQueueSize the new max queue size
     */
    public void setMaxQueueSize(final int maxQueueSize) {
	this.maxQueueSize = maxQueueSize;
    }

    /**
     * Gets the logger name.
     * @return the logger name
     */
    public String getLoggerName() {
	return loggerName;
    }

    /**
     * Sets the logger name.
     * @param loggerName the new logger name
     */
    public void setLoggerName(final String loggerName) {
	this.loggerName = loggerName;
    }

    /**
     * Gets the URL.
     * @return the URL
     */
    public URL getUrl() {
	return url;
    }

    /**
     * Sets the URL.
     * @param url the new URL
     */
    public void setUrl(final URL url) {
	this.url = url;
    }

    /**
     * Gets the error logger name.
     * @return the error logger name
     */
    public String getErrorLoggerName() {
	return errorLoggerName;
    }

    /**
     * Sets the error logger name.
     * @param errorLoggerName the new error logger name
     */
    public void setErrorLoggerName(final String errorLoggerName) {
	this.errorLoggerName = errorLoggerName;
    }

    /**
     * Checks if is raw JSON message.
     * @return true, if is raw JSON message
     */
    public boolean isRawJsonMessage() {
	return rawJsonMessage;
    }

    /**
     * Sets the raw JSON message.
     * @param rawJsonMessage the new raw JSON message
     */
    public void setRawJsonMessage(final boolean rawJsonMessage) {
	this.rawJsonMessage = rawJsonMessage;
    }

    /**
     * Gets the time to live.
     * @return the time to live
     */
    public long getTimeToLive() {
	return timeToLive;
    }

    /**
     * Sets the time to live.
     * @param timeToLive the new time to live
     */
    public void setTimeToLive(final long timeToLive) {
	this.timeToLive = timeToLive;
    }
}
