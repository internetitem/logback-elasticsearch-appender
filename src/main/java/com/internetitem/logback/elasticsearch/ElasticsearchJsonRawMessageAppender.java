package com.internetitem.logback.elasticsearch;

import java.io.IOException;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.internetitem.logback.elasticsearch.config.Settings;

public class ElasticsearchJsonRawMessageAppender extends AbstractElasticsearchAppender<ILoggingEvent> {

    public ElasticsearchJsonRawMessageAppender() {
    }

    public ElasticsearchJsonRawMessageAppender(Settings settings) {
        super(settings);
    }

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

    protected ClassicElasticsearchJsonRawMessagePublisher buildElasticsearchPublisher() throws IOException {
        return new ClassicElasticsearchJsonRawMessagePublisher(getContext(), errorReporter, settings, elasticsearchProperties, headers);
    }


}
