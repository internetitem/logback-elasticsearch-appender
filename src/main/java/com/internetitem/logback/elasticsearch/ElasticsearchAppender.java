package com.internetitem.logback.elasticsearch;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

import java.io.IOException;
import java.net.URL;

public class ElasticsearchAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	private String url;
	private String index;
	private String type;

	private ElasticsearchProperties properties;

	private int sleepTime = 250;
	private int maxRetries = 3;
	private int connectTimeout = 30000;
	private int readTimeout = 30000;
	private boolean debug;
	private boolean includeCallerData;

	private ElasticsearchPublisher publisher;

	public ElasticsearchAppender() {
	}

	@Override
	public void start() {
		super.start();
		try {
			this.publisher = new ElasticsearchPublisher(getContext(), sleepTime, maxRetries, index, type, new URL(url), connectTimeout, readTimeout, debug, properties);
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
		if (includeCallerData) {
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
		this.sleepTime = sleepTime;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setIncludeCallerData(boolean includeCallerData) {
		this.includeCallerData = includeCallerData;
	}
}
