package com.internetitem.logback.elasticsearch;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class ElasticsearchFileSpigot extends Writer {

	private boolean disableBuffer;
	private StringWriter stringWriter;

	public ElasticsearchFileSpigot(StringWriter stringWriter) {
		this.stringWriter = stringWriter;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		if (!disableBuffer) {
			stringWriter.write(cbuf, off, len);
		}
	}

	@Override
	public void flush() throws IOException {
		if (!disableBuffer) {
			stringWriter.flush();
		}
	}

	@Override
	public void close() throws IOException {
		flush();
	}

	public void setDisableBuffer(boolean disableBuffer) {
		this.disableBuffer = disableBuffer;
	}

}
