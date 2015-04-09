package com.internetitem.logback.elasticsearch;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class ElasticsearchAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	private String url;
	private String index;
	private String type;

	private FieldMap fields;

	private int sleepTime = 100;
	private int shutdownRetries = 3;
	private int connectTimeout = 30000;
	private int readTimeout = 30000;

	private ElasticPublisher publisher;
	private Thread publisherThread;

	public ElasticsearchAppender() {
	}

	@Override
	public void start() {
		super.start();
		try {
			this.publisher = new ElasticPublisher(sleepTime, shutdownRetries, index, type, new URL(url), connectTimeout, readTimeout, fields);
			publisher.setContext(getContext());
			this.publisherThread = new Thread(publisher);
			publisherThread.setName("es-publisher");
			publisherThread.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		super.stop();
		publisher.shutdown();
	}

	@Override
	protected void append(ILoggingEvent eventObject) {
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

	public void setFields(FieldMap fields) {
		this.fields = fields;
	}

	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}

	public void setShutdownRetries(int shutdownRetries) {
		this.shutdownRetries = shutdownRetries;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
}
