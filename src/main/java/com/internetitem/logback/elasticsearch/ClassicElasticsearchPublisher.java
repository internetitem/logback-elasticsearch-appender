package com.internetitem.logback.elasticsearch;

import java.io.IOException;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;

import com.fasterxml.jackson.core.JsonGenerator;
import com.internetitem.logback.elasticsearch.config.ElasticsearchProperties;
import com.internetitem.logback.elasticsearch.config.HttpRequestHeaders;
import com.internetitem.logback.elasticsearch.config.Property;
import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.util.AbstractPropertyAndEncoder;
import com.internetitem.logback.elasticsearch.util.ClassicPropertyAndEncoder;
import com.internetitem.logback.elasticsearch.util.ErrorReporter;

/**
 * The Class ClassicElasticsearchPublisher.
 */
public class ClassicElasticsearchPublisher extends AbstractElasticsearchPublisher<ILoggingEvent> {

    /**
     * Instantiates a new classic elasticsearch publisher.
     * @param context the context
     * @param errorReporter the error reporter
     * @param settings the settings
     * @param properties the properties
     * @param headers the headers
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public ClassicElasticsearchPublisher(final Context context, final ErrorReporter errorReporter, final Settings settings, final ElasticsearchProperties properties,
	    final HttpRequestHeaders headers) throws IOException {
	super(context, errorReporter, settings, properties, headers);
    }

    /*
     * (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.AbstractElasticsearchPublisher#buildPropertyAndEncoder(ch.qos.logback.core.Context,
     * com.internetitem.logback.elasticsearch.config.Property)
     */
    @Override
    protected AbstractPropertyAndEncoder<ILoggingEvent> buildPropertyAndEncoder(final Context context, final Property property) {
	return new ClassicPropertyAndEncoder(property, context);
    }

    /*
     * (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.AbstractElasticsearchPublisher#serializeCommonFields(com.fasterxml.jackson.core.JsonGenerator,
     * java.lang.Object)
     */
    @Override
    protected void serializeCommonFields(final JsonGenerator gen, final ILoggingEvent event) throws IOException {
	gen.writeObjectField("@timestamp", getTimestamp(event.getTimeStamp()));

	if (settings.getTimeToLive() > 0) {
	    // seems to be unused in ES 2.2.0
	    gen.writeObjectField("@ttl", Long.valueOf(settings.getTimeToLive()));
	}

	if (settings.isRawJsonMessage()) {
	    gen.writeFieldName("message");
	    gen.writeRawValue(event.getFormattedMessage());
	} else {
	    gen.writeObjectField("message", event.getFormattedMessage());
	}
    }
}
