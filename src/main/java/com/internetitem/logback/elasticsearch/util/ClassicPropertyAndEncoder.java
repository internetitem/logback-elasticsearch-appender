package com.internetitem.logback.elasticsearch.util;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import com.internetitem.logback.elasticsearch.config.Property;

/**
 * The Class ClassicPropertyAndEncoder.
 */
public class ClassicPropertyAndEncoder extends AbstractPropertyAndEncoder<ILoggingEvent> {

    /**
     * Instantiates a new classic property and encoder.
     *
     * @param property the property
     * @param context the context
     */
    public ClassicPropertyAndEncoder(Property property, Context context) {
	super(property, context);
    }

    /* (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.util.AbstractPropertyAndEncoder#getLayout()
     */
    @Override
    protected PatternLayoutBase<ILoggingEvent> getLayout() {
	return new PatternLayout();
    }
}
