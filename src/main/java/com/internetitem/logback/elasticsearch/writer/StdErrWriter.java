package com.internetitem.logback.elasticsearch.writer;

/**
 * The Class StdErrWriter.
 */
public class StdErrWriter implements SafeWriter {

    /* (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.writer.SafeWriter#write(char[], int, int)
     */
    public void write(char[] cbuf, int off, int len) {
	System.err.println(new String(cbuf, 0, len));
    }

    /* (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.writer.SafeWriter#sendData()
     */
    public void sendData() {
	// No-op
    }

    /* (non-Javadoc)
     * @see com.internetitem.logback.elasticsearch.writer.SafeWriter#hasPendingData()
     */
    public boolean hasPendingData() {
	return false;
    }
}
