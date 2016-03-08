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

public class ClassicElasticsearchPublisher extends AbstractElasticsearchPublisher<ILoggingEvent> {

	public ClassicElasticsearchPublisher(Context context, ErrorReporter errorReporter, Settings settings, ElasticsearchProperties properties, HttpRequestHeaders headers) throws IOException {
		super(context, errorReporter, settings, properties, headers);
	}

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
                        gen.writeObjectField("message", event.getFormattedMessage());
                }
	}
}
