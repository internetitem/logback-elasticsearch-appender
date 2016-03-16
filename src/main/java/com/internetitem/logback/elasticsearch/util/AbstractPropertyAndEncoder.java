package com.internetitem.logback.elasticsearch.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import com.internetitem.logback.elasticsearch.config.Property;

/**
 * The Class AbstractPropertyAndEncoder.
 *
 * @param <T> the generic type
 */
public abstract class AbstractPropertyAndEncoder<T> {
    
    /** The property. */
    private Property property;
    
    /** The layout. */
    private PatternLayoutBase<T> layout;

    /**
     * Instantiates a new abstract property and encoder.
     *
     * @param property the property
     * @param context the context
     */
    public AbstractPropertyAndEncoder(Property property, Context context) {
	this.property = property;

	this.layout = getLayout();
	this.layout.setContext(context);
	this.layout.setPattern(property.getValue());
	this.layout.setPostCompileProcessor(null);
	this.layout.start();
    }

    /**
     * Gets the layout.
     *
     * @return the layout
     */
    protected abstract PatternLayoutBase<T> getLayout();

    /**
     * Encode.
     *
     * @param event the event
     * @return the string
     */
    public String encode(T event) {
	return layout.doLayout(event);
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
	return property.getName();
    }

    /**
     * Allow empty.
     *
     * @return true, if successful
     */
    public boolean allowEmpty() {
	return property.isAllowEmpty();
    }
}
