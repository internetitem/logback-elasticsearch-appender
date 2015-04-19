package com.internetitem.logback.elasticsearch;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.internetitem.logback.elasticsearch.config.ElasticsearchProperties;
import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.util.ErrorReporter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ElasticsearchAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	private Settings settings;
	private ElasticsearchProperties properties;
	private ElasticsearchPublisher publisher;
	private ErrorReporter errorReporter;

	public ElasticsearchAppender() {
		this.settings = new Settings();
	}

	@Override
	public void start() {
		super.start();
		try {
			this.errorReporter = new ErrorReporter(getContext());
			this.publisher = new ElasticsearchPublisher(getContext(), errorReporter, settings, properties);
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
		String loggerName = settings.getLoggerName();
		if (loggerName != null && eventObject.getLoggerName().equals(loggerName)) {
			return;
		}

		eventObject.prepareForDeferredProcessing();
		if (settings.isIncludeCallerData()) {
			eventObject.getCallerData();
		}

		publisher.addEvent(eventObject);
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

	public void setIncludeCallerData(boolean includeCallerData) {
		settings.setIncludeCallerData(includeCallerData);
	}

	public void setErrorsToStderr(boolean errorsToStderr) {
		settings.setErrorsToStderr(errorsToStderr);
	}

	public void setLogsToStderr(boolean logsToStderr) {
		settings.setLogsToStderr(logsToStderr);
	}

	public void setMaxQueueSize(int maxQueueSize) {
		settings.setMaxQueueSize(maxQueueSize);
	}

	public void setIndex(String index) {
		settings.setIndex(index);
	}

	public void setType(String type) {
		settings.setType(type);
	}

	public void setUrl(String url) throws MalformedURLException {
		settings.setUrl(new URL(url));
	}

	public void setLoggerName(String logger) {
		settings.setLoggerName(logger);
	}
}
