package com.internetitem.logback.elasticsearch;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;

public class ErrorReporter extends ContextAwareBase {
	public ErrorReporter(Context context) {
		setContext(context);
	}

}
