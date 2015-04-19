package com.internetitem.logback.elasticsearch.writer;

import com.internetitem.logback.elasticsearch.util.ErrorReporter;
import com.internetitem.logback.elasticsearch.config.Settings;

import java.io.*;
import java.net.HttpURLConnection;

public class ElasticsearchWriter implements SafeWriter {

	private StringBuilder sendBuffer;

	private ErrorReporter errorReporter;
	private Settings settings;


	private boolean bufferExceeded;

	public ElasticsearchWriter(ErrorReporter errorReporter, Settings settings) throws IOException {
		this.errorReporter = errorReporter;
		this.settings = settings;

		this.sendBuffer = new StringBuilder();
	}

	public void write(char[] cbuf, int off, int len) {
		if (bufferExceeded) {
			return;
		}

		sendBuffer.append(cbuf, off, len);

		if (sendBuffer.length() >= settings.getMaxQueueSize()) {
			errorReporter.addWarn("Send queue maximum size exceeded - log messages will be lost until the buffer is cleared");
			bufferExceeded = true;
		}
	}

	public void sendData() throws IOException {
		if (sendBuffer.length() <= 0) {
			return;
		}

		HttpURLConnection urlConnection = (HttpURLConnection)(settings.getUrl().openConnection());
		try {
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.setReadTimeout(settings.getReadTimeout());
			urlConnection.setConnectTimeout(settings.getConnectTimeout());
			urlConnection.setRequestMethod("POST");

			Writer writer = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
			writer.write(sendBuffer.toString());
			writer.flush();
			writer.close();

			int rc = urlConnection.getResponseCode();
			if (rc != 200) {
				String data = slurpErrors(urlConnection);
				throw new IOException("Got response code [" + rc + "] from server with data " + data);
			}
		} finally {
			urlConnection.disconnect();
		}

		sendBuffer.setLength(0);
		if (bufferExceeded) {
			errorReporter.addInfo("Send queue cleared - log messages will no longer be lost");
			bufferExceeded = false;
		}
	}

	public boolean hasPendingData() {
		return sendBuffer.length() != 0;
	}

	private static String slurpErrors(HttpURLConnection urlConnection) {
		try {
			InputStream stream = urlConnection.getErrorStream();
			if (stream == null) {
				return "<no data>";
			}

			StringBuilder builder = new StringBuilder();
			InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
			char[] buf = new char[2048];
			int numRead;
			while ((numRead = reader.read(buf)) > 0) {
				builder.append(buf, 0, numRead);
			}
			return builder.toString();
		} catch (Exception e) {
			return "<error retrieving data: " + e.getMessage() + ">";
		}
	}

}
