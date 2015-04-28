package com.internetitem.logback.elasticsearch;


import ch.qos.logback.core.Context;
import com.internetitem.logback.elasticsearch.config.ElasticsearchProperties;
import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.util.ErrorReporter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchAppenderTest {


    @Mock
    private ElasticsearchPublisher elasticsearchPublisher;
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

    @Test
    public void should_set_the_collaborators_when_started() {
        ElasticsearchAppender appender = new ElasticsearchAppender() {
            @Override
            protected ElasticsearchPublisher getElasticsearchPublisher() throws IOException {
                publisherSet = true;
                return elasticsearchPublisher;
            }

            @Override
            protected ErrorReporter getErrorReporter() {
                errorReporterSet = true;
                return errorReporter;
            }
        };


        appender.start();


        assertThat(publisherSet, is(true));
        assertThat(errorReporterSet, is(true));
    }

    @Test
    public void should_create_error_reporter_with_same_context() {
        ElasticsearchAppender appender = new ElasticsearchAppender(settings) {
            @Override
            public Context getContext() {
                return mockedContext;
            }
        };

        ErrorReporter errorReporter = appender.getErrorReporter();


        assertThat(errorReporter.getContext(), is(mockedContext));
    }


    @Test
    public void should_delegate_setIncludeCallerData_to_settings() throws MalformedURLException {
        ElasticsearchAppender appender = new ElasticsearchAppender(settings);
        boolean includeCallerData = false;
        boolean errorsToStderr = false;
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
    }


}