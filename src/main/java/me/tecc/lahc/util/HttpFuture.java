/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.util;

import me.tecc.lahc.http.HttpResponse;

import java.util.concurrent.Future;

public class HttpFuture {

    private Future<HttpResponse> future;
    private boolean evaluated = false;
    private HttpConsumer<HttpResponse> httpResponseHttpConsumer;
    private HttpConsumer<Exception> exceptionHttpConsumer;

    public HttpFuture(Future<HttpResponse> future) {
        this.future = future;
    }

    public HttpFuture success(HttpConsumer<HttpResponse> httpResponseHttpConsumer) {
        this.httpResponseHttpConsumer = httpResponseHttpConsumer;
        return this;
    }

    public HttpFuture failure(HttpConsumer<Exception> exceptionHttpConsumer) {
        this.exceptionHttpConsumer = exceptionHttpConsumer;
        return this;
    }

    public void complete() {
        if (evaluated) {
            return;
        }
        evaluated = true;
        try {
            HttpResponse httpResponse = future.get();
            if (httpResponseHttpConsumer != null) {
                httpResponseHttpConsumer.accept(httpResponse);
            }
        } catch (Exception e) {
            if (exceptionHttpConsumer != null) {
                try {
                    exceptionHttpConsumer.accept(e);
                } catch (Exception ex) {
                    throw new SecurityException(ex.getMessage(), ex);
                }
            }
        }
    }

    public Future<HttpResponse> future() {
        if (evaluated) {
            throw new IllegalStateException("This HttpFuture is already evaluated");
        }
        evaluated = true;
        return future;
    }

    @FunctionalInterface
    public interface HttpConsumer<T> {
        void accept(T value) throws Exception;
    }
}
