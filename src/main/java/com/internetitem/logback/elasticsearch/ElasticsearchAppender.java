package com.internetitem.logback.elasticsearch;

import java.io.IOException;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.internetitem.logback.elasticsearch.config.Settings;

/**
 * The Class ElasticsearchAppender.
 */
public class ElasticsearchAppender extends AbstractElasticsearchAppender<ILoggingEvent> {

    /**
     * Instantiates a new elasticsearch appender.
     */
    public ElasticsearchAppender() {
    }

    /**
     * Instantiates a new elasticsearch appender.
     * @param settings the settings
     */
    public ElasticsearchAppender(Settings settings) {
	super(settings);
    }

    /*
     * (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.AbstractElasticsearchAppender#appendInternal(java.lang.Object)
     */
    @Override
    protected void appendInternal(ILoggingEvent eventObject) {

	String targetLogger = eventObject.getLoggerName();

	String loggerName = settings.getLoggerName();
	if (loggerName != null && loggerName.equals(targetLogger)) {
	    return;
	}

	String errorLoggerName = settings.getErrorLoggerName();
	if (errorLoggerName != null && errorLoggerName.equals(targetLogger)) {
	    return;
	}

	eventObject.prepareForDeferredProcessing();
	if (settings.isIncludeCallerData()) {
	    eventObject.getCallerData();
	}

	publishEvent(eventObject);
    }

    /*
     * (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.AbstractElasticsearchAppender#buildElasticsearchPublisher()
     */
    protected ClassicElasticsearchPublisher buildElasticsearchPublisher() throws IOException {
	return new ClassicElasticsearchPublisher(getContext(), errorReporter, settings, elasticsearchProperties, headers);
    }

}
