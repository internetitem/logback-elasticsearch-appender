package com.internetitem.logback.elasticsearch;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class ElasticsearchFileSpigot extends Writer {

	private boolean disableBuffer;
	private FileWriter fileWriter;
	private StringWriter stringWriter;

	public ElasticsearchFileSpigot(StringWriter stringWriter) {
		this.stringWriter = stringWriter;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		if (fileWriter != null) {
			fileWriter.write(cbuf, off, len);
		}
		if (!disableBuffer) {
			stringWriter.write(cbuf, off, len);
		}
	}

	@Override
	public void flush() throws IOException {
		if (fileWriter != null) {
			fileWriter.flush();
		}
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

	public void setFileWriter(FileWriter fileWriter) {
		this.fileWriter = fileWriter;
	}


}
