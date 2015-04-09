package com.internetitem.logback.elasticsearch;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.ContextAwareBase;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ElasticPublisher extends ContextAwareBase implements Runnable {

	private int shutdownRetries;
	private int sleepTime;
	private List<ILoggingEvent> events;
	private String sendBuffer;
	private volatile boolean shutdown;
	private volatile boolean pendingEvents;

	private URL url;
	private int connectTimeout;
	private int readTimeout;

	private String indexString;

	private JsonFactory jf;

	public ElasticPublisher(int sleepTime, int shutdownRetries, String index, String type, URL url, int connectTimeout, int readTimeout) throws IOException {
		if (sleepTime < 100) {
			sleepTime = 100;
		}
		this.sleepTime = sleepTime;
		this.shutdownRetries = shutdownRetries;
		this.events = new ArrayList<ILoggingEvent>();

		this.jf = new JsonFactory();
		this.jf.setRootValueSeparator(null);

		this.indexString = generateIndexString(index, type);

		this.url = url;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
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
								e.printStackTrace();
								addError("Failed to send events to Elasticsearch (" + shutdownRetries + " more retries): " + e.getMessage(), e);
								shutdownRetries--;
							} else {
								addError("Failed to send events to Elasticsearch (no more retries): " + e.getMessage(), e);
								return;
							}
						} else {
							addError("Failed to send events to Elasticsearch: " + e.getMessage(), e);
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
				addError("Error processing logs: " + e.getMessage(), e);
			}
		}
	}

	private void sendEvents() throws IOException {
		HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
		try {
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.setReadTimeout(readTimeout);
			urlConnection.setConnectTimeout(connectTimeout);
			urlConnection.setRequestMethod("POST");

			Writer writer = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
			writer.write(sendBuffer);
			writer.flush();
			writer.close();

			int rc = urlConnection.getResponseCode();
			if (rc != 200) {
				throw new IOException("Got response code [" + rc + "] from server");
			}
		} finally {
			urlConnection.disconnect();
		}

		sendBuffer = null;
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
}
