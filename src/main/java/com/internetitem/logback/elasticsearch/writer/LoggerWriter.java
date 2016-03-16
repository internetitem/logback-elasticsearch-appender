package com.internetitem.logback.elasticsearch.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LoggerWriter.
 */
public class LoggerWriter implements SafeWriter {

    /** The logger name. */
    private String loggerName;

    /** The logger. */
    private Logger logger;

    /**
     * Instantiates a new logger writer.
     *
     * @param loggerName the logger name
     */
    public LoggerWriter(String loggerName) {
	this.loggerName = loggerName;
    }

    /* (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.writer.SafeWriter#write(char[], int, int)
     */
    public void write(char[] cbuf, int off, int len) {
	if (logger == null) {
	    logger = LoggerFactory.getLogger(loggerName);
	}
	logger.info(new String(cbuf, 0, len));
    }

    /* (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.writer.SafeWriter#sendData()
     */
    public void sendData() {
	// No-op
    }

    /* (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.writer.SafeWriter#hasPendingData()
     */
    public boolean hasPendingData() {
	return false;
    }
}
