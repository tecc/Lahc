/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.io;

import me.tecc.lahc.HttpClient;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents
 */
public interface Connection extends Closeable, AutoCloseable {
    /**
     *
     * @return
     */
    boolean dead();
    /**
     * Opens the connection and reset it If the connection is already open, it should do nothing.
     *
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
    default void write(byte[] bytes) throws IOException {
        OutputStream os = this.output();
        os.write(bytes);
        os.flush();
    }

    /**
     * Gets the input stream to read the response from.
     * This method will block until the previous response has been fully read.
     * @return The input stream of this connection.
     * @throws IOException If any error occurs whilst trying to get the input stream.
     */
    @Blocking
    InputStream input() throws IOException;

    /**
     * Marks this connection as done.
     * Note: This connection may be closed after a call to this.
     */
    void done();
}
