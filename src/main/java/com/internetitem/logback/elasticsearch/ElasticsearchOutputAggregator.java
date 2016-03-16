package com.internetitem.logback.elasticsearch;

import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.util.ErrorReporter;
import com.internetitem.logback.elasticsearch.writer.SafeWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Class ElasticsearchOutputAggregator.
 */
public class ElasticsearchOutputAggregator extends Writer {

    /** The settings. */
    private Settings settings;

    /** The error reporter. */
    private ErrorReporter errorReporter;

    /** The writers. */
    private List<SafeWriter> writers;

    /**
     * Instantiates a new elasticsearch output aggregator.
     * @param settings the settings
     * @param errorReporter the error reporter
     */
    public ElasticsearchOutputAggregator(Settings settings, ErrorReporter errorReporter) {
	this.writers = new ArrayList<SafeWriter>();
	this.settings = settings;
	this.errorReporter = errorReporter;
    }

    /**
     * Adds the writer.
     * @param writer the writer
     */
    public void addWriter(SafeWriter writer) {
	writers.add(writer);
    }

    /*
     * (non-Javadoc)
     * @see java.io.Writer#write(char[], int, int)
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
	for (SafeWriter writer : writers) {
	    writer.write(cbuf, off, len);
	}
    }

    /**
     * Checks for pending data.
     * @return true, if successful
     */
    public boolean hasPendingData() {
	for (SafeWriter writer : writers) {
	    if (writer.hasPendingData()) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Checks for outputs.
     * @return true, if successful
     */
    public boolean hasOutputs() {
	return !writers.isEmpty();
    }

    /**
     * Send data.
     * @return true, if successful
     */
    public boolean sendData() {
	boolean success = true;
	for (SafeWriter writer : writers) {
	    try {
		writer.sendData();
	    } catch (IOException e) {
		success = false;
		errorReporter.logWarning("Failed to send events to Elasticsearch: " + e.getMessage());
		if (settings.isErrorsToStderr()) {
		    System.err.println("[" + new Date().toString() + "] Failed to send events to Elasticsearch: " + e.getMessage());
		}

	    }
	}
	return success;
    }

    /*
     * (non-Javadoc)
     * @see java.io.Writer#flush()
     */
    @Override
    public void flush() throws IOException {
	// No-op
    }

    /*
     * (non-Javadoc)
     * @see java.io.Writer#close()
     */
    @Override
    public void close() throws IOException {
	// No-op
    }

}
