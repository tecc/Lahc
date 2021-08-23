/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.io;

import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Connection extends Closeable, AutoCloseable {
    /**
     * Opens the connection. If the connection is already open, it should do nothing.
     * @throws IOException If the connection couldn't be opened.
     */
    void open() throws IOException;

    /**
     * The lock for this connection.
     * @return a lock
     */
    @NotNull
    Object lock();

    /**
     * Gets the output stream to write the request to.
     * This method will block until the previous request has been fully written.
     * @return The output stream of this connection.
     * @throws IOException If an error occurs whilst trying to get the output stream.
     */
    @Blocking
    OutputStream output() throws IOException;

    /**
     * Gets the input stream to read the response from.
     * This method will block until the previous response has been fully read.
     * @return The input stream of this connection.
     * @throws IOException If any error occurs whilst trying to get the input stream.
     */
    @Blocking
    InputStream input() throws IOException;
}
