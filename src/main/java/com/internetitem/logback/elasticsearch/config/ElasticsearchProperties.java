package com.internetitem.logback.elasticsearch.config;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class ElasticsearchProperties.
 */
public class ElasticsearchProperties {

    /** The properties. */
    private List<Property> properties;

    /**
     * Instantiates a new elasticsearch properties.
     */
    public ElasticsearchProperties() {
	this.properties = new ArrayList<Property>();
    }

    /**
     * Gets the properties.
     *
     * @return the properties
     */
    public List<Property> getProperties() {
	return properties;
    }

    /**
     * Adds the property.
     *
     * @param property the property
     */
    public void addProperty(Property property) {
	properties.add(property);
    }

}
