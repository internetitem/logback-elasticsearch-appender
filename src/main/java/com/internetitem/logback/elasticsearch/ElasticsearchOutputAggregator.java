package com.internetitem.logback.elasticsearch;

import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.util.ErrorReporter;
import com.internetitem.logback.elasticsearch.writer.SafeWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ElasticsearchOutputAggregator extends Writer {

	private Settings settings;
	private ErrorReporter errorReporter;
	private List<SafeWriter> writers;

	public ElasticsearchOutputAggregator(Settings settings, ErrorReporter errorReporter) {
		this.writers = new ArrayList<SafeWriter>();
		this.settings = settings;
		this.errorReporter = errorReporter;
	}

	public void addWriter(SafeWriter writer) {
		writers.add(writer);
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		for (SafeWriter writer : writers) {
			writer.write(cbuf, off, len);
		}
	}

	public boolean hasPendingData() {
		for (SafeWriter writer : writers) {
			if (writer.hasPendingData()) {
				return true;
			}
		}
		return false;
	}

	public boolean hasOutputs() {
		return !writers.isEmpty();
	}

	public boolean sendData() {
		boolean success = true;
		for (SafeWriter writer : writers) {
			try {
				writer.sendData();
			} catch (Exception e) {
				success = false;
				errorReporter.addError("Failed to send events to Elasticsearch: " + e.getMessage(), e);
				if (settings.isErrorsToStderr()) {
					System.err.println("[" + new Date().toString() + "] Failed to send events to Elasticsearch: " + e.getMessage());
				}

			}
		}
		return success;
	}

	@Override
	public void flush() throws IOException {
		// No-op
	}

	@Override
	public void close() throws IOException {
		// No-op
	}

}
