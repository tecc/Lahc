/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc;

import me.tecc.lahc.http.HttpRequest;
import me.tecc.lahc.http.HttpResponse;
import me.tecc.lahc.http.Parsing;
import me.tecc.lahc.io.Connection;
import me.tecc.lahc.io.Connectors;
import me.tecc.lahc.io.connectors.Connector;
import me.tecc.lahc.util.Promise;
import me.tecc.lahc.io.Connector;
import me.tecc.lahc.util.MimeType;

import java.io.OutputStream;
import java.nio.charset.Charset;
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

    public Promise<HttpResponse> execute(HttpRequest source) {
        final HttpRequest request = new HttpRequest(source); // make a copy of the request as to make sure that nothing bad happens
        MimeType accepts = request.accepts();
        if (accepts == null) {
            // if accepts is null, default to allowing *anything* - as long as it's in that type
            accepts = MimeType.ANY.withCharset(this.options.getDefaultCharset());
            request.accept(accepts);
        }
        if (accepts.getCharset() == null && this.options.getDefaultCharset() != null) {
            // if it doesn't have a charset specified, use the default charset
            request.accept(accepts.withCharset(this.options.getDefaultCharset()));
        }
        return new Promise<>(CompletableFuture.supplyAsync(() -> {
            // make sure it's a valid request
            if (!request.validate()) throw new IllegalArgumentException("Request is invalid!");

            try {
                Connection connection = getConnector().connect(request.getTarget());

                synchronized (connection.lock()) {
                    try {
                        // make sure connection is open
                        connection.open();

                        // get output stream and write request bytes
                        OutputStream output = connection.output();
                        // TODO: Add checks to make sure output is valid
                        output.write(request.toHTTPRequest());

                        // return result
                        // TODO: Add checks to make sure input is valid
                        return Parsing.response(request, connection.input());
                    } catch (Throwable t) {
                        throw new CompletionException(t);
                    }
                }
            } catch (CompletionException e) {
                throw e;
            } catch (Throwable e) {
                throw new CompletionException(e);
            }
        }, executor));
    }

    private Connector connector;
    public Connector getConnector() {
        if (connector == null) {
            connector = options.connector;
            if (connector == null) {
                connector = Connectors.createDefault();
            }
        }
        return connector;
    }

    public static final class Options {
        private boolean shouldPoolThreads = true;
        private int threads = 2;
        private String defaultCharset = StandardCharsets.UTF_8.name();
        private int connectionTimeout = -1;
        private int readTimeout = -1;
        private int timeout = 10000;
        private Connector connector = Connectors.createDefault();

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
            if (connectionTimeout < 1) return getTimeout();
            return connectionTimeout;
        }

        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public int getReadTimeout() {
            if (readTimeout < 1) return getTimeout();
            else return readTimeout;
        }

        public Connector getConnector() {
            return this.connector;
        }
    }
}
