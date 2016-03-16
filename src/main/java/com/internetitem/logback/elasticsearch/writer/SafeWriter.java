package com.internetitem.logback.elasticsearch.writer;

import java.io.IOException;

/**
 * The Interface SafeWriter.
 */
public interface SafeWriter {

    /**
     * Write.
     *
     * @param cbuf the cbuf
     * @param off the off
     * @param len the len
     */
    void write(char[] cbuf, int off, int len);

    /**
     * Send data.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void sendData() throws IOException;

    /**
     * Checks for pending data.
     *
     * @return true, if successful
     */
    boolean hasPendingData();
}
