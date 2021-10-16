/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc;

import me.tecc.lahc.http.HttpException;
import me.tecc.lahc.http.HttpRequest;
import me.tecc.lahc.http.HttpResponse;
import me.tecc.lahc.http.Parsing;
import me.tecc.lahc.io.Connection;
import me.tecc.lahc.io.Connectors;
import me.tecc.lahc.util.Headers;
import me.tecc.lahc.util.Promise;
import me.tecc.lahc.io.Connector;
import me.tecc.lahc.util.MimeType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Blocking;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HttpClient {
    private Executor executor;
    private Options options;
    private static Options DEFAULT_OPTIONS = new Options();

    public HttpClient() {
        this(DEFAULT_OPTIONS);
    }
    public HttpClient(Options options) {
        this.options = options;
        if (options.shouldPoolThreads()) executor = Executors.newCachedThreadPool();
        else {
            if (options.getThreads() < 1) throw new IllegalArgumentException("'threads' option must be positive!");

            executor = Executors.newFixedThreadPool(options.getThreads());
        }
    }

    public Promise<HttpResponse> execute(HttpRequest request) {
        return new Promise<>(CompletableFuture.supplyAsync(() -> this.executeSync(request), executor));
    }

    @Blocking
    @ApiStatus.Internal
    public HttpResponse executeSync(HttpRequest source) {
        // make a copy of the request as to make sure that nothing bad happens
        // note that it'll also prepare some other things behind the scenes neatly
        final HttpRequest request = source.resolve(this.options);

        // make sure it's a valid request
        if (!request.validate()) {
            throw new IllegalArgumentException("Request is invalid!");
        }

        HttpResponse response = null;
        Connection connection = null;
        try {
            connection = getConnector().connect(request.getTarget());

            synchronized (connection.lock()) {
                // make sure connection is open
                connection.open();

                // get output stream and write request bytes
                connection.write(request.toHTTPRequest());

                // return result
                // TODO: Add checks to make sure input is valid
                response = Parsing.response(request, connection.input());
                connection.done();

                if (!this.options.shouldUsePersistentConnections()) {
                    connection.close();
                } else if (response.getConnectionHeader() == null) {
                    switch (response.getResponseVersion()) {
                        case V1_0:
                            connection.close();
                        case V1_1:
                        case V2_0:
                            break;
                        default:
                    }
                } else if (response.getConnectionHeader() == Headers.Connection.CLOSE) {
                    connection.close();
                }
            }
        } catch (IOException e) {
            throw new HttpException(e);
        }
        return response;
    }

    private Connector connector;
    public Connector getConnector() {
        if (connector == null) {
            connector = options.getConnector();
            if (connector == null) {
                connector = Connectors.createDefault();
            }
        }
        return connector;
    }

    public Executor getExecutor() {
        return this.executor;
    }

    public static final class Options {
        private boolean shouldPoolThreads = true;
        private int threads = 2;
        private String defaultCharset = StandardCharsets.UTF_8.name();
        private int connectionTimeout = 10000;
        private int readTimeout = -1;
        private Connector connector = Connectors.getDefault();
        private boolean shouldUsePersistentConnections = true;

        public boolean shouldPoolThreads() {
            return shouldPoolThreads;
        }

        public void setShouldPoolThreads(boolean shouldPoolThreads) {
            this.shouldPoolThreads = shouldPoolThreads;
        }

        public int getThreads() {
            return threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }

        public String getDefaultCharset() {
            return defaultCharset;
        }

        public Options setDefaultCharset(String defaultCharset) {
            this.defaultCharset = defaultCharset;
            return this;
        }
        public Options setDefaultCharset(Charset charset) {
            return setDefaultCharset(charset.name());
        }

        public int getConnectionTimeout() {
            if (connectionTimeout < 0) return -1;
            return connectionTimeout;
        }

        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public int getReadTimeout() {
            if (readTimeout < 0) return -1;
            return readTimeout;
        }

        public Connector getConnector() {
            return this.connector;
        }

        public Options setConnector(Connector connector) {
            this.connector = connector;
            return this;
        }

        public boolean shouldUsePersistentConnections() {
            return this.shouldUsePersistentConnections;
        }
        public Options setShouldUsePersistentConnections(boolean value) {
            this.shouldUsePersistentConnections = value;
            return this;
        }
    }
}
