package com.internetitem.logback.elasticsearch;

public class Settings {

	private int sleepTime = 250;
	private int maxRetries = 3;
	private int connectTimeout = 30000;
	private int readTimeout = 30000;
	private boolean debug;
	private boolean errorsToStderr;
	private boolean includeCallerData;
	private int maxQueueSize = 100 * 1024 * 1024;

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

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
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
}
