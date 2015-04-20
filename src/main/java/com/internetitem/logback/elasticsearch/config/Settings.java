package com.internetitem.logback.elasticsearch.config;

import java.net.URL;

public class Settings {

	private String index;
	private String type;
	private URL url;

	private String loggerName;
	private String errorLoggerName;

	private int sleepTime = 250;
	private int maxRetries = 3;
	private int connectTimeout = 30000;
	private int readTimeout = 30000;
	private boolean logsToStderr;
	private boolean errorsToStderr;
	private boolean includeCallerData;
	private int maxQueueSize = 100 * 1024 * 1024;

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(int sleepTime) {
		if (sleepTime < 100) {
			sleepTime = 100;
		}
		this.sleepTime = sleepTime;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public boolean isLogsToStderr() {
		return logsToStderr;
	}

	public void setLogsToStderr(boolean logsToStderr) {
		this.logsToStderr = logsToStderr;
	}

	public boolean isErrorsToStderr() {
		return errorsToStderr;
	}

	public void setErrorsToStderr(boolean errorsToStderr) {
		this.errorsToStderr = errorsToStderr;
	}

	public boolean isIncludeCallerData() {
		return includeCallerData;
	}

	public void setIncludeCallerData(boolean includeCallerData) {
		this.includeCallerData = includeCallerData;
	}

	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	public void setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getErrorLoggerName() {
		return errorLoggerName;
	}

	public void setErrorLoggerName(String errorLoggerName) {
		this.errorLoggerName = errorLoggerName;
	}
}
