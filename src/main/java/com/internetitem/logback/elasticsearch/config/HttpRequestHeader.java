package com.internetitem.logback.elasticsearch.config;

/**
 * A key value pair for the http header.
 */
public class HttpRequestHeader {

    /** The name. */
    private String name;
    
    /** The value. */
    private String value;

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
	return value;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(String value) {
	this.value = value;
    }
}
