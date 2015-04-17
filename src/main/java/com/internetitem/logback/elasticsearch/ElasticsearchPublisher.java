package com.internetitem.logback.elasticsearch;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ElasticsearchPublisher extends ContextAwareBase implements Runnable {

	public static final String THREAD_NAME = "es-writer";

	private volatile List<ILoggingEvent> events;
	private String sendBuffer;

	private Object lock;
	private String indexString;
	private JsonFactory jf;

	private URL url;
	private boolean debug;
	private int connectTimeout;
	private int readTimeout;
	private int sleepTime;
	private int maxRetries;

	private List<PropertyAndEncoder> propertyList;

	private volatile boolean working;

	public ElasticsearchPublisher(Context context, int sleepTime, int maxRetries, String index, String type, URL url, int connectTimeout, int readTimeout, boolean debug, ElasticsearchProperties properties) throws IOException {
		setContext(context);
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
		this.debug = debug;
		this.propertyList = setupPropertyList(getContext(), properties);
		this.sleepTime = sleepTime;
		this.maxRetries = maxRetries;
	}

	private static List<PropertyAndEncoder> setupPropertyList(Context context, ElasticsearchProperties properties) {
		List<PropertyAndEncoder> list = new ArrayList<PropertyAndEncoder>(properties.getProperties().size());
		if (properties != null) {
			for (Property property : properties.getProperties()) {
				list.add(new PropertyAndEncoder(property, context));
			}
		}
		return list;
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
		if (debug) {
			System.err.println(sendBuffer);
			sendBuffer = null;
			return;
		}

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
				String data = slurpErrors(urlConnection);
				throw new IOException("Got response code [" + rc + "] from server with data " + data);
			}
		} finally {
			urlConnection.disconnect();
		}

		sendBuffer = null;
	}

	private String slurpErrors(HttpURLConnection urlConnection) {
		try {
			InputStream stream = urlConnection.getErrorStream();
			if (stream == null) {
                return "<no data>";
            }

			StringBuilder builder = new StringBuilder();
			InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
			char[] buf = new char[2048];
			int numRead;
			while ((numRead = reader.read(buf)) > 0) {
                builder.append(buf, 0, numRead);
            }
			return builder.toString();
		} catch (Exception e) {
			return "<error retrieving data: " + e.getMessage() + ">";
		}
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

		for (PropertyAndEncoder pae : propertyList) {
			String value = pae.encode(event);
			if (pae.allowEmpty() || (value != null && !value.isEmpty())) {
				gen.writeObjectField(pae.getName(), value);
			}
		}

		gen.writeEndObject();
	}

	private String getTimestamp(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		return DatatypeConverter.printDateTime(cal);
	}

}
