package com.internetitem.logback.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.DatatypeConverter;

import ch.qos.logback.core.Context;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.internetitem.logback.elasticsearch.config.ElasticsearchProperties;
import com.internetitem.logback.elasticsearch.config.HttpRequestHeaders;
import com.internetitem.logback.elasticsearch.config.Property;
import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.util.AbstractPropertyAndEncoder;
import com.internetitem.logback.elasticsearch.util.ErrorReporter;
import com.internetitem.logback.elasticsearch.writer.ElasticsearchWriter;
import com.internetitem.logback.elasticsearch.writer.LoggerWriter;
import com.internetitem.logback.elasticsearch.writer.StdErrWriter;

/**
 * The Class AbstractElasticsearchPublisher.
 * @param <T> the generic type
 */
public abstract class AbstractElasticsearchPublisher<T> implements Runnable {

    /** The Constant THREAD_COUNTER. */
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);

    /** The Constant THREAD_NAME_PREFIX. */
    public static final String THREAD_NAME_PREFIX = "es-writer-";

    /** The events. */
    private volatile List<T> events;

    /** The output aggregator. */
    private final ElasticsearchOutputAggregator outputAggregator;

    /** The property list. */
    private final List<AbstractPropertyAndEncoder<T>> propertyList;

    /** The index pattern. */
    private final AbstractPropertyAndEncoder<T> indexPattern;

    /** The jf. */
    private final JsonFactory jf;

    /** The json generator. */
    private final JsonGenerator jsonGenerator;

    /** The error reporter. */
    private final ErrorReporter errorReporter;

    /** The settings. */
    protected Settings settings;

    /** The lock. */
    private final Object lock;

    /** The working. */
    private volatile boolean working;

    /**
     * Instantiates a new abstract elasticsearch publisher.
     * @param context the context
     * @param errorReporter the error reporter
     * @param settings the settings
     * @param properties the properties
     * @param headers the headers
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public AbstractElasticsearchPublisher(final Context context, final ErrorReporter errorReporter, final Settings settings,
	    final ElasticsearchProperties properties, final HttpRequestHeaders headers) throws IOException {
	this.errorReporter = errorReporter;
	this.events = new ArrayList<T>();
	this.lock = new Object();
	this.settings = settings;

	this.outputAggregator = configureOutputAggregator(settings, errorReporter, headers);

	this.jf = new JsonFactory();
	this.jf.setRootValueSeparator(null);
	this.jsonGenerator = jf.createGenerator(outputAggregator);

	this.indexPattern = buildPropertyAndEncoder(context, new Property("<index>", settings.getIndex(), false));
	this.propertyList = generatePropertyList(context, properties);
    }

    /**
     * Configures the output aggregator.
     * @param settings the settings
     * @param errorReporter the error reporter
     * @param httpRequestHeaders the http request headers
     * @return the elasticsearch output aggregator
     */
    private static ElasticsearchOutputAggregator configureOutputAggregator(final Settings settings, final ErrorReporter errorReporter,
	    final HttpRequestHeaders httpRequestHeaders) {
	final ElasticsearchOutputAggregator spigot = new ElasticsearchOutputAggregator(settings, errorReporter);

	if (settings.isLogsToStderr()) {
	    spigot.addWriter(new StdErrWriter());
	}

	if (settings.getLoggerName() != null) {
	    spigot.addWriter(new LoggerWriter(settings.getLoggerName()));
	}

	if (settings.getUrl() != null) {
	    spigot.addWriter(new ElasticsearchWriter(errorReporter, settings, httpRequestHeaders));
	}

	return spigot;
    }

    /**
     * Generates the properties list.
     * @param context the context
     * @param properties the properties
     * @return the list
     */
    private List<AbstractPropertyAndEncoder<T>> generatePropertyList(final Context context, final ElasticsearchProperties properties) {
	final List<AbstractPropertyAndEncoder<T>> list = new ArrayList<AbstractPropertyAndEncoder<T>>();
	if (properties != null) {
	    for (final Property property : properties.getProperties()) {
		list.add(buildPropertyAndEncoder(context, property));
	    }
	}
	return list;
    }

    /**
     * Builds the property and encoder.
     * @param context the context
     * @param property the property
     * @return the abstract property and encoder
     */
    protected abstract AbstractPropertyAndEncoder<T> buildPropertyAndEncoder(Context context, Property property);

    /**
     * Adds the event.
     * @param event the event
     */
    public void addEvent(final T event) {
	if (!outputAggregator.hasOutputs()) {
	    return;
	}

	synchronized (lock) {
	    events.add(event);
	    if (!working) {
		working = true;
		final Thread thread = new Thread(this, THREAD_NAME_PREFIX + THREAD_COUNTER.getAndIncrement());
		thread.start();
	    }
	}
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
	int currentTry = 1;
	final int maxRetries = settings.getMaxRetries();
	while (true) {
	    try {
		Thread.sleep(settings.getSleepTime());

		List<T> eventsCopy = null;
		synchronized (lock) {
		    if (!events.isEmpty()) {
			eventsCopy = events;
			events = new ArrayList<T>();
			currentTry = 1;
		    }

		    if (eventsCopy == null) {
			if (!outputAggregator.hasPendingData()) {
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
		    serializeEvents(jsonGenerator, eventsCopy, propertyList);
		}

		if (!outputAggregator.sendData()) {
		    currentTry++;
		}
	    } catch (final Exception e) {
		errorReporter.logError("Internal error handling log data: " + e.getMessage(), e);
		currentTry++;
	    }
	}
    }

    /**
     * Serializes the events.
     * @param gen the generator
     * @param eventsCopy the events copy
     * @param propertyList the property list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void serializeEvents(final JsonGenerator gen, final List<T> eventsCopy, final List<AbstractPropertyAndEncoder<T>> propertyList) throws IOException {
	for (final T event : eventsCopy) {
	    serializeIndexString(gen, event);
	    gen.writeRaw('\n');
	    serializeEvent(gen, event, propertyList);
	    gen.writeRaw('\n');
	}
	gen.flush();
    }

    /**
     * Serializes the index string.
     * @param gen the generator
     * @param event the event
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void serializeIndexString(final JsonGenerator gen, final T event) throws IOException {
	gen.writeStartObject();
	gen.writeObjectFieldStart("index");
	gen.writeObjectField("_index", indexPattern.encode(event));
	final String type = settings.getType();
	if (type != null) {
	    gen.writeObjectField("_type", type);
	}
	if (settings.getTimeToLive() > 0) {
	    gen.writeObjectField("_ttl", Long.valueOf(settings.getTimeToLive()));
	}
	gen.writeEndObject();
	gen.writeEndObject();
    }

    /**
     * Serializes the event.
     * @param gen the generator
     * @param event the event
     * @param propertyList the property list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void serializeEvent(final JsonGenerator gen, final T event, final List<AbstractPropertyAndEncoder<T>> propertyList) throws IOException {
	gen.writeStartObject();

	serializeCommonFields(gen, event);

	for (final AbstractPropertyAndEncoder<T> pae : propertyList) {
	    final String value = pae.encode(event);
	    if (pae.allowEmpty() || (value != null && !value.isEmpty())) {
		gen.writeObjectField(pae.getName(), value);
	    }
	}

	gen.writeEndObject();
    }

    /**
     * Serializes the common fields.
     * @param gen the generator
     * @param event the event
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected abstract void serializeCommonFields(JsonGenerator gen, T event) throws IOException;

    /**
     * Gets the timestamp.
     * @param timestamp the timestamp
     * @return the timestamp
     */
    protected static String getTimestamp(final long timestamp) {
	final Calendar cal = Calendar.getInstance();
	cal.setTimeInMillis(timestamp);
	return DatatypeConverter.printDateTime(cal);
    }

}
