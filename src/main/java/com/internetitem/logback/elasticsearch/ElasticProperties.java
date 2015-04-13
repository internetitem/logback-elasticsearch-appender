package com.internetitem.logback.elasticsearch;

import java.util.ArrayList;
import java.util.List;

public class ElasticProperties {

	private List<Property> properties;

	public ElasticProperties() {
		this.properties = new ArrayList<Property>();
	}

	public List<Property> getProperties() {
		return properties;
	}

	public void addProperty(Property property) {
		properties.add(property);
	}

}
