package com.internetitem.logback.elasticsearch.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;
import com.internetitem.logback.elasticsearch.config.Settings;
import org.slf4j.LoggerFactory;

public class ErrorReporter extends ContextAwareBase {

	private Settings settings;

	public ErrorReporter(Settings settings, Context context) {
		setContext(context);
		this.settings = settings;
	}

	public void logError(String message, Throwable e) {
		String loggerName = settings.getErrorLoggerName();
		if (loggerName != null) {
			LoggerFactory.getLogger(loggerName).error(message, e);
		}
		addError(message, e);
	}

	public void logWarning(String message) {
		String loggerName = settings.getErrorLoggerName();
		if (loggerName != null) {
			LoggerFactory.getLogger(loggerName).warn(message);
		}
		addWarn(message);
	}

	public void logInfo(String message) {
		String loggerName = settings.getErrorLoggerName();
		if (loggerName != null) {
			LoggerFactory.getLogger(loggerName).info(message);
		}
		addInfo(message);
	}
}
