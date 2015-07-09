package com.internetitem.logback.elasticsearch;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import com.fasterxml.jackson.core.JsonGenerator;
import com.internetitem.logback.elasticsearch.config.ElasticsearchProperties;
import com.internetitem.logback.elasticsearch.config.Property;
import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.util.AbstractPropertyAndEncoder;
import com.internetitem.logback.elasticsearch.util.ClassicPropertyAndEncoder;
import com.internetitem.logback.elasticsearch.util.ErrorReporter;

import java.io.IOException;

public class ClassicElasticsearchPublisher extends AbstractElasticsearchPublisher<ILoggingEvent> {

	public ClassicElasticsearchPublisher(Context context, ErrorReporter errorReporter, Settings settings, ElasticsearchProperties properties) throws IOException {
		super(context, errorReporter, settings, properties);
	}

	@Override
	protected AbstractPropertyAndEncoder<ILoggingEvent> buildPropertyAndEncoder(Context context, Property property) {
		return new ClassicPropertyAndEncoder(property, context);
	}

	@Override
	protected void serializeCommonFields(JsonGenerator gen, ILoggingEvent event) throws IOException {
		gen.writeObjectField("@timestamp", getTimestamp(event.getTimeStamp()));
		gen.writeObjectField("message", event.getMessage());
	}
}
