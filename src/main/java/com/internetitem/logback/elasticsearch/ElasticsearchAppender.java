package com.internetitem.logback.elasticsearch;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

import java.io.IOException;

public class ElasticsearchAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	private String url;
	private String index;
	private String type;

	private int sleepTime = 100;
	private int shutdownRetries = 3;

	private ElasticPublisher publisher;
	private Thread publisherThread;

	public ElasticsearchAppender() {
	}

	@Override
	public void start() {
		super.start();
		try {
			this.publisher = new ElasticPublisher(sleepTime, shutdownRetries, index, type);
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

	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}

	public void setShutdownRetries(int shutdownRetries) {
		this.shutdownRetries = shutdownRetries;
	}
}
