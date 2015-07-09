package com.internetitem.logback.elasticsearch;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.Context;
import com.fasterxml.jackson.core.JsonGenerator;
import com.internetitem.logback.elasticsearch.config.ElasticsearchProperties;
import com.internetitem.logback.elasticsearch.config.Property;
import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.util.AbstractPropertyAndEncoder;
import com.internetitem.logback.elasticsearch.util.AccessPropertyAndEncoder;
import com.internetitem.logback.elasticsearch.util.ErrorReporter;

import java.io.IOException;

public class AccessElasticsearchPublisher extends AbstractElasticsearchPublisher<IAccessEvent> {

	public AccessElasticsearchPublisher(Context context, ErrorReporter errorReporter, Settings settings, ElasticsearchProperties properties) throws IOException {
		super(context, errorReporter, settings, properties);
	}

	@Override
	protected AbstractPropertyAndEncoder<IAccessEvent> buildPropertyAndEncoder(Context context, Property property) {
		return new AccessPropertyAndEncoder(property, context);
	}

	@Override
	protected void serializeCommonFields(JsonGenerator gen, IAccessEvent event) throws IOException {
		gen.writeObjectField("@timestamp", getTimestamp(event.getTimeStamp()));
	}
}
