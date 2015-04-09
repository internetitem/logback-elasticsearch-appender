package com.internetitem.logback.elasticsearch;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ElasticPublisher implements Runnable {

	private static Logger logger;

	private int shutdownRetries;
	private int sleepTime;
	private List<ILoggingEvent> events;
	private String sendBuffer;
	private volatile boolean shutdown;
	private volatile boolean pendingEvents;

	private String indexString;

	private JsonFactory jf;

	public ElasticPublisher(int sleepTime, int shutdownRetries, String index, String type) throws IOException {
		if (sleepTime < 100) {
			sleepTime = 100;
		}
		this.sleepTime = sleepTime;
		this.shutdownRetries = shutdownRetries;
		this.events = new ArrayList<ILoggingEvent>();

		this.jf = new JsonFactory();
		this.jf.setRootValueSeparator(null);

		this.indexString = generateIndexString(index, type);
	}

	private String generateIndexString(String index, String type) throws IOException {
		StringWriter writer = new StringWriter();
		JsonGenerator gen = jf.createGenerator(writer);
		gen.writeStartObject();
		gen.writeObjectFieldStart("index");
		gen.writeObjectField("_index", index);
		if (type != null) {
			gen.writeObjectField("_type", type);
		}
		gen.writeEndObject();
		gen.writeEndObject();
		gen.writeRaw('\n');
		gen.close();
		return writer.toString();
	}

	public void addEvent(ILoggingEvent event) {
		synchronized (events) {
			pendingEvents = true;
			events.add(event);
		}
	}

	public void run() {
		while (true) {
			try {
				List<ILoggingEvent> eventsCopy = getEventsToProcess();
				if (eventsCopy != null) {
					serializeEventsToBuffer(eventsCopy);
				}
				if (sendBuffer != null) {
					try {
						sendEvents();
					} catch (Exception e) {
						if (shutdown) {
							if (shutdownRetries > 0) {
								getLogger().warn("Failed to send events to Elasticsearch (" + shutdownRetries + " more retries): " + e.getMessage(), e);
								shutdownRetries--;
							} else {
								getLogger().error("Failed to send events to Elasticsearch (no more retries): " + e.getMessage(), e);
								return;
							}
						} else {
							getLogger().warn("Failed to send events to Elasticsearch: " + e.getMessage(), e);
						}
					}
				}

				if (shutdown && !pendingEvents && sendBuffer == null) {
					return;
				}

				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				shutdown = true;
			} catch (Exception e) {
				getLogger().error("Error processing logs: " + e.getMessage(), e);
			}
		}
	}

	private void sendEvents() {
		// TODO: Send events
	}

	private void serializeEventsToBuffer(List<ILoggingEvent> eventsCopy) throws IOException {
		StringWriter writer = new StringWriter();
		JsonGenerator gen = jf.createGenerator(writer);
		for (ILoggingEvent event : eventsCopy) {
			gen.writeRaw(indexString);
			serializeEvent(gen, event);
			gen.writeRaw('\n');
		}
		gen.close();

		if (sendBuffer != null) {
			sendBuffer = sendBuffer + writer.toString();
		} else {
			sendBuffer = writer.toString();
		}
	}

	private void serializeEvent(JsonGenerator gen, ILoggingEvent event) throws IOException {
		gen.writeStartObject();

		gen.writeObjectField("@timestamp", getTimestamp(event.getTimeStamp()));
		gen.writeObjectField("message", event.getMessage());

		gen.writeEndObject();
	}

	private String getTimestamp(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		return DatatypeConverter.printDateTime(cal);
	}

	private List<ILoggingEvent> getEventsToProcess() {
		List<ILoggingEvent> eventsCopy = null;
		synchronized (events) {
			if (!events.isEmpty()) {
				eventsCopy = new ArrayList<ILoggingEvent>(events);
				events.clear();
				pendingEvents = false;
			}
		}
		return eventsCopy;
	}

	public void shutdown() {
		shutdown = true;
	}

	private static Logger getLogger() {
		if (logger == null) {
			logger = LoggerFactory.getLogger(ElasticPublisher.class);
		}
		return logger;
	}
}
