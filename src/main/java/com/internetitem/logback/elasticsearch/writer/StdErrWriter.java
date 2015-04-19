package com.internetitem.logback.elasticsearch.writer;

public class StdErrWriter implements SafeWriter {

	public void write(char[] cbuf, int off, int len) {
		System.err.println(new String(cbuf, 0, len));
	}

	public void sendData() {
		// No-op
	}

	public boolean hasPendingData() {
		return false;
	}
}
