package com.internetitem.logback.elasticsearch.writer;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public interface SafeWriter {

	void write(char[] cbuf, int off, int len);

	void sendData() throws IOException, NoSuchAlgorithmException, KeyManagementException;

	boolean hasPendingData();
}
