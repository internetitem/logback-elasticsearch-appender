package com.internetitem.logback.elasticsearch;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import com.fasterxml.jackson.core.JsonGenerator;
import com.internetitem.logback.elasticsearch.config.ElasticsearchProperties;
import com.internetitem.logback.elasticsearch.config.HttpRequestHeaders;
import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.util.ErrorReporter;
import net.logstash.logback.marker.ObjectAppendingMarker;

import java.io.IOException;
import java.lang.reflect.Field;

public class StructuredArgsElasticsearchPublisher extends ClassicElasticsearchPublisher {
    private String keyPrefix;
    private Field field;
    private ErrorReporter errorReporter;

    public StructuredArgsElasticsearchPublisher(Context context, ErrorReporter errorReporter, Settings settings, ElasticsearchProperties properties,
                                                HttpRequestHeaders headers) throws IOException {
        super(context, errorReporter, settings, properties, headers);

        this.errorReporter = errorReporter;

        keyPrefix = "";
        if(settings != null && settings.getKeyPrefix() != null) {
            keyPrefix = settings.getKeyPrefix();
        }

        try {
            field = ObjectAppendingMarker.class.getDeclaredField("object");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // message will be logged without object
            errorReporter.logError("error in logging with object serialization", e);
        }
    }

    protected void serializeCommonFields(JsonGenerator gen, ILoggingEvent event) throws IOException {
        super.serializeCommonFields(gen, event);

        if(event.getArgumentArray() != null) {
            Object[] eventArgs = event.getArgumentArray();
            for(Object eventArg:eventArgs) {
                if(eventArg instanceof ObjectAppendingMarker) {
                    ObjectAppendingMarker marker = (ObjectAppendingMarker) eventArg;
                    if(field != null && settings != null && settings.isObjectSerialization() &&
                            marker.getFieldValue().toString().contains("@")) {
                        try {
                            Object obj = field.get(marker);
                            if(obj != null) {
                                gen.writeObjectField(keyPrefix + marker.getFieldName(), obj);
                            }
                        } catch (IllegalAccessException e) {
                            // message will be logged without object
                            errorReporter.logError("error in logging with object serialization", e);
                        }
                    }
                    else
                        gen.writeObjectField(keyPrefix + marker.getFieldName(), marker.getFieldValue());
                }
            }
        }
    }

}
