package com.internetitem.logback.elasticsearch;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.internetitem.logback.elasticsearch.config.ElasticsearchProperties;
import com.internetitem.logback.elasticsearch.config.Property;
import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.writer.ElasticsearchWriter;
import com.internetitem.logback.elasticsearch.writer.LoggerWriter;
import com.internetitem.logback.elasticsearch.writer.StdErrWriter;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ElasticsearchPublisher implements Runnable {

	public static final String THREAD_NAME = "es-writer";

	private volatile List<ILoggingEvent> events;
	private ElasticsearchOutputAggregator spigot;
	private List<PropertyAndEncoder> propertyList;

	private String indexString;
	private JsonFactory jf;
	private JsonGenerator jsonGenerator;

	private ErrorReporter errorReporter;
	private Settings settings;

	private final Object lock;


	private volatile boolean working;


	public ElasticsearchPublisher(Context context, ErrorReporter errorReporter, Settings settings, ElasticsearchProperties properties) throws IOException {
		this.errorReporter = errorReporter;
		this.events = new ArrayList<ILoggingEvent>();
		this.lock = new Object();
		this.settings = settings;

		this.spigot = configureSpigot(settings, errorReporter);

		this.jf = new JsonFactory();
		this.jf.setRootValueSeparator(null);
		this.jsonGenerator = jf.createGenerator(spigot);

		this.indexString = generateIndexString(jf, settings.getIndex(), settings.getType());
		this.propertyList = generatePropertyList(context, properties);
	}

	private static ElasticsearchOutputAggregator configureSpigot(Settings settings, ErrorReporter errorReporter) throws IOException {
		ElasticsearchOutputAggregator spigot = new ElasticsearchOutputAggregator(settings, errorReporter);

		if (settings.isErrorsToStderr()) {
			spigot.addWriter(new StdErrWriter());
		}

		if (settings.getLoggerName() != null) {
			spigot.addWriter(new LoggerWriter(settings.getLoggerName()));
		}

		if (settings.getUrl() != null) {
			spigot.addWriter(new ElasticsearchWriter(errorReporter, settings));
		}

		return spigot;
	}

	private static List<PropertyAndEncoder> generatePropertyList(Context context, ElasticsearchProperties properties) {
		List<PropertyAndEncoder> list = new ArrayList<PropertyAndEncoder>();
		if (properties != null) {
			for (Property property : properties.getProperties()) {
				list.add(new PropertyAndEncoder(property, context));
			}
		}
		return list;
	}


	private static String generateIndexString(JsonFactory jf, String index, String type) throws IOException {
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
		int currentTry = 1;
		int maxRetries = settings.getMaxRetries();
		while (true) {
			try {
				Thread.sleep(settings.getSleepTime());

				List<ILoggingEvent> eventsCopy = null;
				synchronized (lock) {
					if (!events.isEmpty()) {
						eventsCopy = events;
						events = new ArrayList<ILoggingEvent>();
						currentTry = 1;
					}

					if (eventsCopy == null) {
						if (!spigot.hasPendingData()) {
							// all done
							working = false;
							return;
						} else {
							// Nothing new, must be a retry
							if (currentTry > maxRetries) {
								// Oh well, better luck next time
								working = false;
								return;
							}
						}
					}
				}

				if (eventsCopy != null) {
					serializeEvents(jsonGenerator, indexString, eventsCopy, propertyList);
				}

				if (!spigot.sendData()) {
					currentTry++;
				}
			} catch (Exception e) {
				errorReporter.addError("Internal error handling log data: " + e.getMessage(), e);
				currentTry++;
			}
		}
	}


	private static void serializeEvents(JsonGenerator gen, String indexString, List<ILoggingEvent> eventsCopy, List<PropertyAndEncoder> propertyList) throws IOException {
		for (ILoggingEvent event : eventsCopy) {
			gen.writeRaw(indexString);
			serializeEvent(gen, event, propertyList);
			gen.writeRaw('\n');
		}
		gen.flush();
	}

	private static void serializeEvent(JsonGenerator gen, ILoggingEvent event, List<PropertyAndEncoder> propertyList) throws IOException {
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

	private static String getTimestamp(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		return DatatypeConverter.printDateTime(cal);
	}

}
