package com.internetitem.logback.elasticsearch.config;

import java.util.ArrayList;
import java.util.List;

public class ElasticsearchProperties {

	private List<EsProperty> properties;

	public ElasticsearchProperties() {
		this.properties = new ArrayList<EsProperty>();
	}

	public List<EsProperty> getProperties() {
		return properties;
	}

	public void addEsProperty(EsProperty property) {
		properties.add(property);
	}

}
