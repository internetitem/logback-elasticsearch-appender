package com.internetitem.logback.elasticsearch;

import java.io.IOException;

import ch.qos.logback.access.spi.IAccessEvent;
import com.internetitem.logback.elasticsearch.config.Settings;

/**
 * The Class ElasticsearchAccessAppender.
 */
public class ElasticsearchAccessAppender extends AbstractElasticsearchAppender<IAccessEvent> {

    /**
     * Instantiates a new elasticsearch access appender.
     */
    public ElasticsearchAccessAppender() {
    }

    /**
     * Instantiates a new elasticsearch access appender.
     * @param settings the settings
     */
    public ElasticsearchAccessAppender(Settings settings) {
	super(settings);
    }

    /*
     * (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.AbstractElasticsearchAppender#appendInternal(java.lang.Object)
     */
    @Override
    protected void appendInternal(IAccessEvent eventObject) {
	eventObject.prepareForDeferredProcessing();
	publishEvent(eventObject);
    }

    /*
     * (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.AbstractElasticsearchAppender#buildElasticsearchPublisher()
     */
    protected AccessElasticsearchPublisher buildElasticsearchPublisher() throws IOException {
	return new AccessElasticsearchPublisher(getContext(), errorReporter, settings, elasticsearchProperties, headers);
    }

}
