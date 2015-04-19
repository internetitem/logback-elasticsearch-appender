package com.internetitem.logback.elasticsearch;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import com.internetitem.logback.elasticsearch.config.Property;

public class PropertyAndEncoder {
	private Property property;
	private PatternLayout layout;

	public PropertyAndEncoder(Property property, Context context) {
		this.property = property;

		this.layout = new PatternLayout();
		this.layout.setContext(context);
		this.layout.setPattern(property.getValue());
		this.layout.setPostCompileProcessor(null);
		this.layout.start();
	}

	public String encode(ILoggingEvent event) {
		return layout.doLayout(event);
	}

	public String getName() {
		return property.getName();
	}

	public boolean allowEmpty() {
		return property.isAllowEmpty();
	}
}
