package com.internetitem.logback.elasticsearch.writer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;

import com.internetitem.logback.elasticsearch.config.HttpRequestHeader;
import com.internetitem.logback.elasticsearch.config.HttpRequestHeaders;
import com.internetitem.logback.elasticsearch.config.Settings;
import com.internetitem.logback.elasticsearch.util.ErrorReporter;

public class ElasticsearchWriter implements SafeWriter {

	private final Queue<String> sendQueue;

	private final ErrorReporter errorReporter;
	private final Settings settings;
	private final Collection<HttpRequestHeader> headerList;

	private boolean bufferExceeded;

	public ElasticsearchWriter(ErrorReporter errorReporter, Settings settings, HttpRequestHeaders headers) {
		this.errorReporter = errorReporter;
		this.settings = settings;
		this.headerList = headers != null && headers.getHeaders() != null
			? headers.getHeaders()
			: Collections.<HttpRequestHeader>emptyList();

		this.sendQueue = new ArrayDeque<>();
	}

	public void write(char[] cbuf, int off, int len) {
		if (bufferExceeded) {
			return;
		}

		sendQueue.add(String.valueOf(cbuf, off, len));
	}

	public void sendData() throws IOException {
		if (!hasPendingData()) {
			return;
		}

		doSend();

		sendQueue.clear();
		if (bufferExceeded) {
			errorReporter.logInfo("Send queue cleared - log messages will no longer be lost");
			bufferExceeded = false;
		}
	}

	private void doSend() throws IOException {
		boolean sent = false;
		int dropSize = 1;
		HttpURLConnection urlConnection = (HttpURLConnection)(settings.getUrl().openConnection());
		try {
			while(!sent && hasPendingData()) {
				urlConnection.setDoInput(true);
				urlConnection.setDoOutput(true);
				urlConnection.setReadTimeout(settings.getReadTimeout());
				urlConnection.setConnectTimeout(settings.getConnectTimeout());
				urlConnection.setRequestMethod("POST");

				StringBuilder bodyBuilder = new StringBuilder();
				for (String chunk : sendQueue) {
					bodyBuilder.append(chunk);
				}
				String body = bodyBuilder.toString();
				if (body.length() > settings.getMaxQueueSize()) {
					errorReporter.logWarning("Max queue size exceeded. Further messages will be dropped");
					bufferExceeded = true;
				}

				if (!headerList.isEmpty()) {
					for (HttpRequestHeader header : headerList) {
						urlConnection.setRequestProperty(header.getName(), header.getValue());
					}
				}

				if (settings.getAuthentication() != null) {
					settings.getAuthentication().addAuth(urlConnection, body);
				}

				Writer writer = new OutputStreamWriter(urlConnection.getOutputStream(), StandardCharsets.UTF_8);
				writer.write(body);
				writer.flush();
				writer.close();

				int rc = urlConnection.getResponseCode();
				if (rc != 200) {
					if (rc == 413) {
						// 413 - Request entity too large, drop the head of the queue and try again
						errorReporter.logWarning("413 error received - dropping messages from the head of the queue");
						for(int i=0 ; i < dropSize; i++) {
							sendQueue.poll();
						}
						dropSize = dropSize * 2;
					} else {
						String data = slurpErrors(urlConnection);
						throw new IOException("Got response code [" + rc + "] from server with data " + data);
					}
				} else {
					sent = true;
				}
			}
		} finally {
			urlConnection.disconnect();
		}
	}

	public boolean hasPendingData() {
		return sendQueue.size() != 0;
	}

	private static String slurpErrors(HttpURLConnection urlConnection) {
		try {
			InputStream stream = urlConnection.getErrorStream();
			if (stream == null) {
				return "<no data>";
			}

			StringBuilder builder = new StringBuilder();
			InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
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
