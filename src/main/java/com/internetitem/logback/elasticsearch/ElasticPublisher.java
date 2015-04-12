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

	public static final String THREAD_NAME = "es-writer";

	private volatile List<ILoggingEvent> events;
	private String sendBuffer;

	private Object lock;
	private String indexString;
	private JsonFactory jf;

	private URL url;
	private int connectTimeout;
	private int readTimeout;
	private int sleepTime;
	private int maxRetries;

	private FieldMap fields;

	private volatile boolean working;

	public ElasticPublisher(int sleepTime, int maxRetries, String index, String type, URL url, int connectTimeout, int readTimeout, FieldMap fields) throws IOException {
		if (sleepTime < 100) {
			sleepTime = 100;
		}
		this.events = new ArrayList<ILoggingEvent>();
		this.lock = new Object();
		this.jf = new JsonFactory();
		this.jf.setRootValueSeparator(null);

		this.indexString = generateIndexString(index, type);

		this.url = url;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.fields = fields;
		this.sleepTime = sleepTime;
		this.maxRetries = maxRetries;
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
		synchronized (lock) {
			events.add(event);
			if (!working) {
				working = true;
				Thread thread = new Thread(this, THREAD_NAME);
				thread.start();
			}
		}
	}

	public void run() {
		int currentTry = 0;
		while (true) {
			try {
				Thread.sleep(sleepTime);

				List<ILoggingEvent> eventsCopy = null;
				synchronized (lock) {
					if (!events.isEmpty()) {
						eventsCopy = events;
						events = new ArrayList<ILoggingEvent>();
					}

					if (eventsCopy == null) {
						if (sendBuffer == null) {
							// all done
							working = false;
							return;
						} else {
							// Nothing new, must be a retry
							currentTry++;
							if (currentTry >= maxRetries) {
								// Oh well, better luck next time
								working = false;
								return;
							}
						}
					}
				}

				if (eventsCopy != null) {
					serializeEventsToBuffer(eventsCopy);
				}

				sendEvents();
			} catch (Exception e) {
				addError("Failed to send events to Elasticsearch (try " + currentTry + " of " + maxRetries + "): " + e.getMessage(), e);
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

}
