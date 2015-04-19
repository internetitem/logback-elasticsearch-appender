package com.internetitem.logback.elasticsearch;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ElasticsearchPublisher extends ContextAwareBase implements Runnable {

	public static final String THREAD_NAME = "es-writer";

	private volatile List<ILoggingEvent> events;
	private ElasticsearchFileSpigot spigot;
	private StringWriter sendBuffer;

	private final Object lock;
	private String indexString;
	private JsonFactory jf;

	private URL url;
	private Settings settings;
	private FileAppenderSettings fileAppenderSettings;

	private List<PropertyAndEncoder> propertyList;

	private volatile boolean working;
	private boolean bufferExceeded;

	private Date nextRolloverTime;
	private FileWriter fileWriter;
	private File currentFile;

	public ElasticsearchPublisher(Context context, String index, String type, URL url, Settings settings, FileAppenderSettings fileAppenderSettings, ElasticsearchProperties properties) throws IOException {
		setContext(context);
		this.events = new ArrayList<ILoggingEvent>();
		this.lock = new Object();
		this.jf = new JsonFactory();
		this.jf.setRootValueSeparator(null);

		this.indexString = generateIndexString(index, type);
		this.sendBuffer = new StringWriter();
		this.spigot = new ElasticsearchFileSpigot(sendBuffer);

		this.url = url;
		this.settings = settings;
		this.fileAppenderSettings = fileAppenderSettings;
		this.propertyList = setupPropertyList(getContext(), properties);
	}

	private static List<PropertyAndEncoder> setupPropertyList(Context context, ElasticsearchProperties properties) {
		List<PropertyAndEncoder> list = new ArrayList<PropertyAndEncoder>();
		if (properties != null) {
			for (Property property : properties.getProperties()) {
				list.add(new PropertyAndEncoder(property, context));
			}
		}
		return list;
	}

	private String generateIndexString(String index, String type) throws IOException {
		StringWriter writer = new StringWriter();
		JsonGenerator gen = jf.createGenerator(writer);
		gen.writeStartObject();
		gen.writeObjectFieldStart("index");
		gen.writeObjectField("_index", index);
		if (type != null) {
			gen.writeObjectField("_type", type);
		}
		gen.writeEndObject();
		gen.writeEndObject();
		gen.writeRaw('\n');
		gen.close();
		return writer.toString();
	}

	public void addEvent(ILoggingEvent event) {
		synchronized (lock) {
			events.add(event);
			if (!working) {
				working = true;
				Thread thread = new Thread(this, THREAD_NAME);
				thread.start();
			}
		}
	}

	public void run() {
		int currentTry = 1;
		int maxRetries = settings.getMaxRetries();
		while (true) {
			try {
				Thread.sleep(settings.getSleepTime());

				List<ILoggingEvent> eventsCopy = null;
				synchronized (lock) {
					if (!events.isEmpty()) {
						eventsCopy = events;
						events = new ArrayList<ILoggingEvent>();
						currentTry = 1;
					}

					if (eventsCopy == null) {
						if (sendBuffer == null) {
							// all done
							working = false;
							return;
						} else {
							// Nothing new, must be a retry
							if (currentTry > maxRetries) {
								// Oh well, better luck next time
								working = false;
								return;
							}
						}
					}
				}

				if (eventsCopy != null) {
					serializeEventsToBuffer(eventsCopy);
				}

				sendEvents();
			} catch (Exception e) {
				addError("Failed to send events to Elasticsearch (try " + currentTry + " of " + maxRetries + "): " + e.getMessage(), e);
				if (settings.isErrorsToStderr()) {
					System.err.println("[" + new Date().toString() + "] Failed to send events to Elasticsearch (try " + currentTry + " of " + maxRetries + "): " + e.getMessage());
				}
				currentTry++;
			}
		}
	}

	private void sendEvents() throws IOException {
		if (settings.isDebug()) {
			System.err.println(sendBuffer);
			sendBuffer = null;
			return;
		}

		HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
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

		sendBuffer.getBuffer().setLength(0);
		if (bufferExceeded) {
			addInfo("Send queue cleared - log messages will no longer be lost");
			bufferExceeded = false;
			spigot.setDisableBuffer(false);
		}
	}

	private String slurpErrors(HttpURLConnection urlConnection) {
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

	private void serializeEventsToBuffer(List<ILoggingEvent> eventsCopy) throws IOException {
		JsonGenerator gen = jf.createGenerator(spigot);
		for (ILoggingEvent event : eventsCopy) {
			setupFileAppender();

			gen.writeRaw(indexString);
			serializeEvent(gen, event);
			gen.writeRaw('\n');
		}
		gen.close();

		if (sendBuffer.getBuffer().length() > settings.getMaxQueueSize() && !bufferExceeded) {
			addWarn("Send queue maximum size exceeded - log messages will be lost until the buffer is cleared");
			bufferExceeded = true;
			spigot.setDisableBuffer(true);
		}
	}

	private void setupFileAppender() throws IOException {
		if (fileAppenderSettings == null || fileAppenderSettings.getFilename() == null) {
			return;
		}

		Date now = new Date();
		if (now.after(nextRolloverTime)) {
			rollFile();
		}

		if (fileWriter == null) {
			String filename = getFilenameForDate(now);
			currentFile = new File(filename);
			fileWriter = new FileWriter(currentFile, true);
			spigot.setFileWriter(fileWriter);
			nextRolloverTime = calculateNextRolloverTime(now);
		}
	}

	private void rollFile() throws IOException {
		fileWriter.close();

		// Delete old files, check up to one week back from the earliest date
		int maxDays = fileAppenderSettings.getMaxDays();
		if (maxDays > 0) {
			Date today = DateUtil.clearTime(new Date());
			Date earliestDate = DateUtil.addDays(today, -maxDays);
			Date oneWeekEarlier = DateUtil.addDays(earliestDate, -7);
			for (Date cur = earliestDate; cur.compareTo(oneWeekEarlier) >= 0; cur = DateUtil.addDays(cur, -1)) {
				String filenameForDate = getFilenameForDate(cur);
				if (fileAppenderSettings.isArchive()) {
					filenameForDate += ".zip";
				}
				File fileForDate = new File(filenameForDate);
				if (fileForDate.exists()) {
					fileForDate.delete();
				}
			}
		}

		// Zip current file if necessary
		if (fileAppenderSettings.isArchive()) {
			String currentName = currentFile.getName();
			File zipFile = new File(currentFile.getParentFile(), currentName + ".zip");
			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);
			zos.putNextEntry(new ZipEntry(currentName));

			byte[] buf = new byte[4096];
			int numRead;
			FileInputStream fis = new FileInputStream(currentFile);
			while ((numRead = fis.read(buf)) > 0) {
				zos.write(buf, 0, numRead);
			}
			zos.closeEntry();
			zos.close();
		}

		spigot.setFileWriter(null);
		fileWriter = null;
		currentFile = null;
	}

	private static Date calculateNextRolloverTime(Date now) {
		return DateUtil.addDays(DateUtil.clearTime(now), 1);
	}

	private String getFilenameForDate(Date date) {
		String rawFilename = fileAppenderSettings.getFilename();
		String dateString = getDateString(date);
		String filename = rawFilename.replaceAll("%d", dateString);
		return filename;
	}

	private String getDateString(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}

	private void serializeEvent(JsonGenerator gen, ILoggingEvent event) throws IOException {
		gen.writeStartObject();

		gen.writeObjectField("@timestamp", getTimestamp(event.getTimeStamp()));
		gen.writeObjectField("message", event.getMessage());

		for (PropertyAndEncoder pae : propertyList) {
			String value = pae.encode(event);
			if (pae.allowEmpty() || (value != null && !value.isEmpty())) {
				gen.writeObjectField(pae.getName(), value);
			}
		}

		gen.writeEndObject();
	}

	private String getTimestamp(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		return DatatypeConverter.printDateTime(cal);
	}

}
