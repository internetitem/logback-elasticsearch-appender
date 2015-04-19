package com.internetitem.logback.elasticsearch;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

import java.io.IOException;
import java.net.URL;

public class ElasticsearchAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	private String url;
	private String index;
	private String type;

	private Settings settings;

	private ElasticsearchProperties properties;

	private ElasticsearchPublisher publisher;

	public ElasticsearchAppender() {
		this.settings = new Settings();
	}

	@Override
	public void start() {
		super.start();
		try {
			this.publisher = new ElasticsearchPublisher(getContext(), index, type, new URL(url), settings, properties);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		super.stop();
	}

	@Override
	protected void append(ILoggingEvent eventObject) {
		eventObject.prepareForDeferredProcessing();
		if (settings.isIncludeCallerData()) {
			eventObject.getCallerData();
		}

		publisher.addEvent(eventObject);
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setProperties(ElasticsearchProperties properties) {
		this.properties = properties;
	}

	public void setSleepTime(int sleepTime) {
		settings.setSleepTime(sleepTime);
	}

	public void setMaxRetries(int maxRetries) {
		settings.setMaxRetries(maxRetries);
	}

	public void setConnectTimeout(int connectTimeout) {
		settings.setConnectTimeout(connectTimeout);
	}

	public void setReadTimeout(int readTimeout) {
		settings.setReadTimeout(readTimeout);
	}

	public void setDebug(boolean debug) {
		settings.setDebug(debug);
	}

	public void setIncludeCallerData(boolean includeCallerData) {
		settings.setIncludeCallerData(includeCallerData);
	}

	public void setErrorsToStderr(boolean errorsToStderr) {
		settings.setErrorsToStderr(errorsToStderr);
	}

	public void setMaxQueueSize(int maxQueueSize) {
		settings.setMaxQueueSize(maxQueueSize);
	}

}
