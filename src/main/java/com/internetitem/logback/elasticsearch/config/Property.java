package com.internetitem.logback.elasticsearch.config;

public class Property {
	private String name;
	private String value;
	private boolean allowEmpty;

	public Property() {
	}

	public Property(String name, String value, boolean allowEmpty) {
		this.name = name;
		this.value = value;
		this.allowEmpty = allowEmpty;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isAllowEmpty() {
		return allowEmpty;
	}

	public void setAllowEmpty(boolean allowEmpty) {
		this.allowEmpty = allowEmpty;
	}
}
