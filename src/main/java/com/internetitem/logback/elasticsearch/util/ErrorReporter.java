package com.internetitem.logback.elasticsearch.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;
import com.internetitem.logback.elasticsearch.config.Settings;
import org.slf4j.LoggerFactory;

/**
 * The Class ErrorReporter.
 */
public class ErrorReporter extends ContextAwareBase {

    /** The settings. */
    private Settings settings;

    /**
     * Instantiates a new error reporter.
     *
     * @param settings the settings
     * @param context the context
     */
    public ErrorReporter(Settings settings, Context context) {
	setContext(context);
	this.settings = settings;
    }

    /**
     * Log error.
     *
     * @param message the message
     * @param e the e
     */
    public void logError(String message, Throwable e) {
	String loggerName = settings.getErrorLoggerName();
	if (loggerName != null) {
	    LoggerFactory.getLogger(loggerName).error(message, e);
	}
	addError(message, e);
    }

    /**
     * Log warning.
     *
     * @param message the message
     */
    public void logWarning(String message) {
	String loggerName = settings.getErrorLoggerName();
	if (loggerName != null) {
	    LoggerFactory.getLogger(loggerName).warn(message);
	}
	addWarn(message);
    }

    /**
     * Log info.
     *
     * @param message the message
     */
    public void logInfo(String message) {
	String loggerName = settings.getErrorLoggerName();
	if (loggerName != null) {
	    LoggerFactory.getLogger(loggerName).info(message);
	}
	addInfo(message);
    }
}
