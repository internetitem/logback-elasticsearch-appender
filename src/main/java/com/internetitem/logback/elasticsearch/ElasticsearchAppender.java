package com.internetitem.logback.elasticsearch;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Calendar;

public class ElasticsearchAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	private String url;
	private String index;
	private String type;

	private JsonGenerator gen;

	public ElasticsearchAppender() throws IOException {
		JsonFactory jf = new JsonFactory();
		this.gen = jf.createGenerator(System.out);
	}

	@Override
	protected void append(ILoggingEvent eventObject) {
		try {
			gen.setRootValueSeparator(new SerializedString("\n"));

			// header row
			gen.writeStartObject();
			gen.writeObjectFieldStart("index");
			gen.writeObjectField("_index", index);
			if (type != null) {
				gen.writeObjectField("_type", type);
			}
			gen.writeEndObject();
			gen.writeEndObject();

			// message row
			gen.writeStartObject();

			// timestamp
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(eventObject.getTimeStamp());
			String timestampString = DatatypeConverter.printDateTime(cal);
			gen.writeObjectField("@timestamp", timestampString);

			gen.writeObjectField("message", eventObject.getMessage());

			gen.writeEndObject();
			gen.flush();
		} catch (IOException e) {
			throw new RuntimeException("Failed to serialize log message: " + e.getMessage(), e);
		}
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public void setType(String type) {
		this.type = type;
	}
}
