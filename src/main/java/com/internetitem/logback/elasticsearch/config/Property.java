package com.internetitem.logback.elasticsearch.config;

/**
 * The Class Property.
 */
public class Property {
    
    /** The name. */
    private String name;
    
    /** The value. */
    private String value;
    
    /** The allow empty. */
    private boolean allowEmpty;

    /**
     * Instantiates a new property.
     */
    public Property() {
    }

    /**
     * Instantiates a new property.
     *
     * @param name the name
     * @param value the value
     * @param allowEmpty the allow empty
     */
    public Property(String name, String value, boolean allowEmpty) {
	this.name = name;
	this.value = value;
	this.allowEmpty = allowEmpty;
    }

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

    /**
     * Checks if is allow empty.
     *
     * @return true, if is allow empty
     */
    public boolean isAllowEmpty() {
	return allowEmpty;
    }

    /**
     * Sets the allow empty.
     *
     * @param allowEmpty the new allow empty
     */
    public void setAllowEmpty(boolean allowEmpty) {
	this.allowEmpty = allowEmpty;
    }
}
