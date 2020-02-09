package com.internetitem.logback.elasticsearch;

import java.io.IOException;
import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import com.fasterxml.jackson.core.JsonGenerator;
import com.internetitem.logback.elasticsearch.config.ElasticsearchProperties;
import com.internetitem.logback.elasticsearch.config.HttpRequestHeaders;
import com.internetitem.logback.elasticsearch.config.Property;
import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.util.AbstractPropertyAndEncoder;
import com.internetitem.logback.elasticsearch.util.ClassicPropertyAndEncoder;
import com.internetitem.logback.elasticsearch.util.ContextMapWriter;
import com.internetitem.logback.elasticsearch.util.ErrorReporter;

public class ClassicElasticsearchPublisher extends AbstractElasticsearchPublisher<ILoggingEvent> {

    public ClassicElasticsearchPublisher(Context context, ErrorReporter errorReporter, Settings settings, ElasticsearchProperties properties, HttpRequestHeaders headers) throws IOException {
        super(context, errorReporter, settings, properties, headers);
        contextMapWriter = new ContextMapWriter();
    }

    protected ContextMapWriter contextMapWriter;

    @Override
    protected AbstractPropertyAndEncoder<ILoggingEvent> buildPropertyAndEncoder(Context context, Property property) {
        return new ClassicPropertyAndEncoder(property, context);
    }

    @Override
    protected void serializeCommonFields(JsonGenerator gen, ILoggingEvent event) throws IOException {
        gen.writeObjectField("@timestamp", getTimestamp(event.getTimeStamp()));

        if (settings.isRawJsonMessage()) {
            gen.writeFieldName("message");
            gen.writeRawValue(event.getFormattedMessage());
        } else {
            String formattedMessage = event.getFormattedMessage();
            if (settings.getMaxMessageSize() > 0 && formattedMessage.length() > settings.getMaxMessageSize()) {
                formattedMessage = formattedMessage.substring(0, settings.getMaxMessageSize()) + "..";
            }
            gen.writeObjectField("message", formattedMessage);
        }

        if (settings.isIncludeMdc()) {
            for (Map.Entry<String, String> entry : event.getMDCPropertyMap().entrySet()) {
                gen.writeObjectField(entry.getKey(), entry.getValue());
            }
        }

        if (settings.isEnableContextMap()) {
            contextMapWriter.writeContextMap(gen, event);
        }
    }

}
