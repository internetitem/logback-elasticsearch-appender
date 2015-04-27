package com.internetitem.logback.elasticsearch;


import com.internetitem.logback.elasticsearch.util.ErrorReporter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchAppenderTest {


    @Mock
    private ElasticsearchPublisher elasticsearchPublisher;
    @Mock
    private ErrorReporter errorReporter;

    private boolean publisherSet = false;
    private boolean errorReporterSet = false;

    @Test
    public void should_report_to_publisher_when_a_log_event_arrives() {
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



}