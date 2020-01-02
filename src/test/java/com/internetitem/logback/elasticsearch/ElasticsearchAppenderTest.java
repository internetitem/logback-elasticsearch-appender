package com.internetitem.logback.elasticsearch;


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import com.internetitem.logback.elasticsearch.config.ElasticsearchProperties;
import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.util.ErrorReporter;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchAppenderTest {


    @Mock
    private ClassicElasticsearchPublisher elasticsearchPublisher;
    @Mock
    private ErrorReporter errorReporter;
    @Mock
    private Settings settings;
    @Mock
    private ElasticsearchProperties elasticsearchProperties;
    @Mock
    private Context mockedContext;

    private boolean publisherSet = false;
    private boolean errorReporterSet = false;
    private AbstractElasticsearchAppender appender;

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

    @Test
    public void should_set_the_collaborators_when_started() {
        appender.start();


        assertThat(publisherSet, is(true));
        assertThat(errorReporterSet, is(true));
    }

    @Test
    public void should_throw_error_when_publisher_setup_fails_during_startup() {
        ElasticsearchAppender appender = new ElasticsearchAppender() {
            @Override
            protected ClassicElasticsearchPublisher buildElasticsearchPublisher() throws IOException {
                throw new IOException("Failed to start Publisher");
            }
        };

        try {
            appender.start();
        } catch (Exception e) {
            assertThat(e, IsInstanceOf.instanceOf(RuntimeException.class));
            assertThat(e.getMessage(), is("java.io.IOException: Failed to start Publisher"));
        }


    }

    @Test
    public void should_not_publish_events_when_logger_set() {
        String loggerName = "elastic-debug-log";
        ILoggingEvent eventToLog = mock(ILoggingEvent.class);
        given(eventToLog.getLoggerName()).willReturn(loggerName);


        appender.setLoggerName(loggerName);
        appender.start();


        appender.append(eventToLog);

        verifyZeroInteractions(elasticsearchPublisher);
    }


    @Test
    public void should_not_publish_events_when_errorlogger_set() {
        String errorLoggerName = "elastic-error-log";
        ILoggingEvent eventToLog = mock(ILoggingEvent.class);
        given(eventToLog.getLoggerName()).willReturn(errorLoggerName);


        appender.setErrorLoggerName(errorLoggerName);
        appender.start();


        appender.append(eventToLog);

        verifyZeroInteractions(elasticsearchPublisher);
    }


    @Test
    public void should_publish_events_when_loggername_is_null() {
        ILoggingEvent eventToPublish = mock(ILoggingEvent.class);
        given(eventToPublish.getLoggerName()).willReturn(null);
        String errorLoggerName = "es-error";

        appender.setErrorLoggerName(errorLoggerName);
        appender.start();


        appender.append(eventToPublish);

        verify(elasticsearchPublisher, times(1)).addEvent(eventToPublish);
    }


    @Test
    public void should_publish_events_when_loggername_is_different_from_the_elasticsearch_loggers() {
        ILoggingEvent eventToPublish = mock(ILoggingEvent.class);
        String differentLoggerName = "different-logger";
        String errorLoggerName = "es-errors";
        given(eventToPublish.getLoggerName()).willReturn(differentLoggerName);


        appender.setErrorLoggerName(errorLoggerName);
        appender.start();


        appender.append(eventToPublish);

        verify(elasticsearchPublisher, times(1)).addEvent(eventToPublish);
    }

    @Test
    public void should_create_error_reporter_with_same_context() {
        ElasticsearchAppender appender = new ElasticsearchAppender(){
            @Override
            public Context getContext() {
                return mockedContext;
            }
        };

        ErrorReporter errorReporter = appender.getErrorReporter();

        assertThat(errorReporter.getContext(), is(mockedContext));
    }


    @Test
    public void should_delegate_setters_to_settings() throws MalformedURLException {
        ElasticsearchAppender appender = new ElasticsearchAppender(settings);
        boolean includeCallerData = false;
        boolean errorsToStderr = false;
        boolean rawJsonMessage = false;
        boolean includeMdc = true;
        String excludedMdcKeys = "stacktrace,url";
        String index = "app-logs";
        String type = "appenderType";
        int maxQueueSize = 10;
        String logger = "es-logger";
        String url = "http://myelasticsearch.mycompany.com";
        String errorLogger = "es-error-logger";
        int maxRetries = 10000;
        int aSleepTime = 10000;
        int readTimeout = 10000;
        int connectTimeout = 5000;

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
        appender.setRawJsonMessage(rawJsonMessage);
        appender.setIncludeMdc(includeMdc);
        appender.setExcludedMdcKeys(excludedMdcKeys);

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
        verify(settings, times(1)).setRawJsonMessage(rawJsonMessage);
        verify(settings, times(1)).setIncludeMdc(includeMdc);
        verify(settings, times(1)).setExcludedMdcKeys(excludedMdcKeys);
    }


}