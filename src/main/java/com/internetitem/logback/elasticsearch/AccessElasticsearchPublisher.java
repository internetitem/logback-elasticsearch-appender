package com.internetitem.logback.elasticsearch;

import java.io.IOException;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.Context;

import com.fasterxml.jackson.core.JsonGenerator;
import com.internetitem.logback.elasticsearch.config.ElasticsearchProperties;
import com.internetitem.logback.elasticsearch.config.HttpRequestHeaders;
import com.internetitem.logback.elasticsearch.config.Property;
import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.util.AbstractPropertyAndEncoder;
import com.internetitem.logback.elasticsearch.util.AccessPropertyAndEncoder;
import com.internetitem.logback.elasticsearch.util.ErrorReporter;

/**
 * The Class AccessElasticsearchPublisher.
 */
public class AccessElasticsearchPublisher extends AbstractElasticsearchPublisher<IAccessEvent> {

    /**
     * Instantiates a new access elasticsearch publisher.
     * @param context the context
     * @param errorReporter the error reporter
     * @param settings the settings
     * @param properties the properties
     * @param httpRequestHeaders the http request headers
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public AccessElasticsearchPublisher(final Context context, final ErrorReporter errorReporter, final Settings settings,
	    final ElasticsearchProperties properties, final HttpRequestHeaders httpRequestHeaders) throws IOException {
	super(context, errorReporter, settings, properties, httpRequestHeaders);
    }

    /*
     * (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.AbstractElasticsearchPublisher#buildPropertyAndEncoder(ch.qos.logback.core.Context,
     * com.internetitem.logback.elasticsearch.config.Property)
     */
    @Override
    protected AbstractPropertyAndEncoder<IAccessEvent> buildPropertyAndEncoder(final Context context, final Property property) {
	return new AccessPropertyAndEncoder(property, context);
    }

    /*
     * (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.AbstractElasticsearchPublisher#serializeCommonFields(com.fasterxml.jackson.core.JsonGenerator,
     * java.lang.Object)
     */
    @Override
    protected void serializeCommonFields(final JsonGenerator gen, final IAccessEvent event) throws IOException {
	gen.writeObjectField("@timestamp", getTimestamp(event.getTimeStamp()));

	if (settings.getTimeToLive() > 0) {
	    // seems to be unused in ES 2.2.0
	    gen.writeObjectField("@ttl", Long.valueOf(settings.getTimeToLive()));
	}
    }
}
