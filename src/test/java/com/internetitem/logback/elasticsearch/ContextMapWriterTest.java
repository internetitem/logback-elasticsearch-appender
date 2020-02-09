package com.internetitem.logback.elasticsearch;

import ch.qos.logback.classic.spi.LoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import com.internetitem.logback.elasticsearch.util.ContextMapWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class ContextMapWriterTest {

    @Mock
    private JsonGenerator jsonGenerator;


    private ContextMapWriter contextMapWriter;

    @Before
    public void setup() throws IOException {
        contextMapWriter = new ContextMapWriter();
    }

    @Test
    public void should_not_write_if_arguments_null_or_empty() throws IOException {
        LoggingEvent event = new LoggingEvent();
        contextMapWriter.writeContextMap(jsonGenerator, event);
        event.setArgumentArray(new Object[]{});
        contextMapWriter.writeContextMap(jsonGenerator, event);
        verifyZeroInteractions(jsonGenerator);
    }

    @Test
    public void should_not_write_if_last_element_not_map() throws IOException {
        LoggingEvent event = new LoggingEvent();
        event.setArgumentArray(new Object[]{"23", 3243});
        contextMapWriter.writeContextMap(jsonGenerator, event);
        verifyZeroInteractions(jsonGenerator);
    }
}
