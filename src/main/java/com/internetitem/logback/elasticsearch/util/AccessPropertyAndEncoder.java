package com.internetitem.logback.elasticsearch.util;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import com.internetitem.logback.elasticsearch.config.Property;

/**
 * The Class AccessPropertyAndEncoder.
 */
public class AccessPropertyAndEncoder extends AbstractPropertyAndEncoder<IAccessEvent> {

    /**
     * Instantiates a new access property and encoder.
     *
     * @param property the property
     * @param context the context
     */
    public AccessPropertyAndEncoder(Property property, Context context) {
	super(property, context);
    }

    /* (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.util.AbstractPropertyAndEncoder#getLayout()
     */
    @Override
    protected PatternLayoutBase<IAccessEvent> getLayout() {
	return new PatternLayout();
    }
}
