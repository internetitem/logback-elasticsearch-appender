package com.internetitem.logback.elasticsearch;

import java.util.ArrayList;
import java.util.List;

public class ElasticsearchProperties {

	private List<Property> properties;

	public ElasticsearchProperties() {
		this.properties = new ArrayList<Property>();
	}

	public List<Property> getProperties() {
		return properties;
	}

	public void addProperty(Property property) {
		properties.add(property);
	}

}
