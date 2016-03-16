package com.internetitem.logback.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;

import com.internetitem.logback.elasticsearch.config.ElasticsearchProperties;
import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.util.ErrorReporter;

/**
 * The Class ElasticsearchAppenderTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchAppenderTest {

    /** The elasticsearch publisher. */
    @Mock
    private ClassicElasticsearchPublisher elasticsearchPublisher;

    /** The error reporter. */
    @Mock
    private ErrorReporter errorReporter;

    /** The settings. */
    @Mock
    private Settings settings;

    /** The elasticsearch properties. */
    @Mock
    private ElasticsearchProperties elasticsearchProperties;

    /** The mocked context. */
    @Mock
    private Context mockedContext;

    /** The publisher set. */
    private boolean publisherSet = false;

    /** The error reporter set. */
    private boolean errorReporterSet = false;

    /** The appender. */
    private AbstractElasticsearchAppender<ILoggingEvent> appender;

    /**
     * Sets the up.
     */
    @Before
    public void setUp() {

	appender = new ElasticsearchAppender() {
	    @Override
	    protected ClassicElasticsearchPublisher buildElasticsearchPublisher() throws IOException {
		publisherSet = true;
		return elasticsearchPublisher;
	    }

	    @Override
	    protected ErrorReporter getErrorReporter() {
		errorReporterSet = true;
		return errorReporter;
	    }
	};
    }

    /**
     * Should_set_the_collaborators_when_started.
     */
    @Test
    public void should_set_the_collaborators_when_started() {
	appender.start();

	assertTrue(publisherSet);
	assertTrue(errorReporterSet);
    }

    /**
     * Should_throw_error_when_publisher_setup_fails_during_startup.
     */
    @Test
    public void should_throw_error_when_publisher_setup_fails_during_startup() {
	final ElasticsearchAppender appender = new ElasticsearchAppender() {
	    @Override
	    protected ClassicElasticsearchPublisher buildElasticsearchPublisher() throws IOException {
		throw new IOException("Failed to start Publisher");
	    }
	};

	try {
	    appender.start();
	} catch (final Exception e) {
	    assertTrue(e instanceof RuntimeException);
	    assertEquals("java.io.IOException: Failed to start Publisher", e.getMessage());
	}

    }

    /**
     * Should_not_publish_events_when_logger_set.
     */
    @Test
    public void should_not_publish_events_when_logger_set() {
	final String loggerName = "elastic-debug-log";
	final ILoggingEvent eventToLog = mock(ILoggingEvent.class);
	given(eventToLog.getLoggerName()).willReturn(loggerName);

	appender.setLoggerName(loggerName);
	appender.start();

	appender.append(eventToLog);

	verifyZeroInteractions(elasticsearchPublisher);
    }

    /**
     * Should_not_publish_events_when_errorlogger_set.
     */
    @Test
    public void should_not_publish_events_when_errorlogger_set() {
	final String errorLoggerName = "elastic-error-log";
	final ILoggingEvent eventToLog = mock(ILoggingEvent.class);
	given(eventToLog.getLoggerName()).willReturn(errorLoggerName);

	appender.setErrorLoggerName(errorLoggerName);
	appender.start();

	appender.append(eventToLog);

	verifyZeroInteractions(elasticsearchPublisher);
    }

    /**
     * Should_publish_events_when_loggername_is_null.
     */
    @Test
    public void should_publish_events_when_loggername_is_null() {
	final ILoggingEvent eventToPublish = mock(ILoggingEvent.class);
	given(eventToPublish.getLoggerName()).willReturn(null);
	final String errorLoggerName = "es-error";

	appender.setErrorLoggerName(errorLoggerName);
	appender.start();

	appender.append(eventToPublish);

	verify(elasticsearchPublisher, times(1)).addEvent(eventToPublish);
    }

    /**
     * Should_publish_events_when_loggername_is_different_from_the_elasticsearch_loggers.
     */
    @Test
    public void should_publish_events_when_loggername_is_different_from_the_elasticsearch_loggers() {
	final ILoggingEvent eventToPublish = mock(ILoggingEvent.class);
	final String differentLoggerName = "different-logger";
	final String errorLoggerName = "es-errors";
	given(eventToPublish.getLoggerName()).willReturn(differentLoggerName);

	appender.setErrorLoggerName(errorLoggerName);
	appender.start();

	appender.append(eventToPublish);

	verify(elasticsearchPublisher, times(1)).addEvent(eventToPublish);
    }

    /**
     * Should_create_error_reporter_with_same_context.
     */
    @Test
    public void should_create_error_reporter_with_same_context() {
	final ElasticsearchAppender appender = new ElasticsearchAppender() {
	    @Override
	    public Context getContext() {
		return mockedContext;
	    }
	};

	final ErrorReporter errorReporter = appender.getErrorReporter();

	assertEquals(mockedContext, errorReporter.getContext());
    }

    /**
     * Should_delegate_setters_to_settings.
     * @throws MalformedURLException the malformed url exception
     */
    @Test
    public void should_delegate_setters_to_settings() throws MalformedURLException {
	final ElasticsearchAppender appender = new ElasticsearchAppender(settings);
	final boolean includeCallerData = false;
	final boolean errorsToStderr = false;
	final boolean rawJsonMessage = false;
	final String index = "app-logs";
	final String type = "appenderType";
	final int maxQueueSize = 10;
	final String logger = "es-logger";
	final String url = "http://myelasticsearch.mycompany.com";
	final String errorLogger = "es-error-logger";
	final int maxRetries = 10000;
	final int aSleepTime = 10000;
	final int readTimeout = 10000;
	final int connectTimeout = 5000;
	final int ttl = 500000;

	appender.setIncludeCallerData(includeCallerData);
	appender.setSleepTime(aSleepTime);
	appender.setReadTimeout(readTimeout);
	appender.setErrorsToStderr(errorsToStderr);
	appender.setLogsToStderr(errorsToStderr);
	appender.setMaxQueueSize(maxQueueSize);
	appender.setIndex(index);
	appender.setType(type);
	appender.setUrl(url);
	appender.setLoggerName(logger);
	appender.setErrorLoggerName(errorLogger);
	appender.setMaxRetries(maxRetries);
	appender.setConnectTimeout(connectTimeout);
	appender.setTimeToLive(ttl);
	appender.setRawJsonMessage(rawJsonMessage);

	verify(settings, times(1)).setReadTimeout(readTimeout);
	verify(settings, times(1)).setSleepTime(aSleepTime);
	verify(settings, times(1)).setIncludeCallerData(includeCallerData);
	verify(settings, times(1)).setErrorsToStderr(errorsToStderr);
	verify(settings, times(1)).setLogsToStderr(errorsToStderr);
	verify(settings, times(1)).setMaxQueueSize(maxQueueSize);
	verify(settings, times(1)).setIndex(index);
	verify(settings, times(1)).setType(type);
	verify(settings, times(1)).setUrl(new URL(url));
	verify(settings, times(1)).setLoggerName(logger);
	verify(settings, times(1)).setErrorLoggerName(errorLogger);
	verify(settings, times(1)).setMaxRetries(maxRetries);
	verify(settings, times(1)).setConnectTimeout(connectTimeout);
	verify(settings, times(1)).setTimeToLive(ttl);
	verify(settings, times(1)).setRawJsonMessage(rawJsonMessage);
    }

}