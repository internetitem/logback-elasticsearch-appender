package com.internetitem.logback.elasticsearch.writer;

import java.io.IOException;

public interface SafeWriter {

	void write(char[] cbuf, int off, int len);

	void sendData() throws IOException;

	boolean hasPendingData();

	boolean canSendData();
}
