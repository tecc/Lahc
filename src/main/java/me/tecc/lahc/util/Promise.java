/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Promise<T> implements Future<T> {

    private Future<T> future;
    private PromiseConsumer<T> responseConsumer;
    private PromiseConsumer<Exception> exceptionConsumer;
    private Runnable concludeRunnable;

    public Promise(Future<T> future) {
        this.future = future;
    }

    public Promise then(PromiseConsumer<T> responseConsumer) {
        this.responseConsumer = responseConsumer;
        return this;
    }

    public Promise exception(PromiseConsumer<Exception> exceptionConsumer) {
        this.exceptionConsumer = exceptionConsumer;
        return this;
    }

    public Promise conclude(Runnable concludeRunnable) {
        this.concludeRunnable = concludeRunnable;
        return this;
    }

    public void run() {
        if (isDone()) {
            throw new IllegalStateException("Already done");
        }
        try {
            T result = future.get();
            if (responseConsumer != null) responseConsumer.accept(result);
        } catch (Exception e) {
            try {
                if (exceptionConsumer != null) exceptionConsumer.accept(e);
            } catch (Exception ex) {
                throw new SecurityException(ex.getMessage(), e);
            }
        } finally {
            if (concludeRunnable != null) concludeRunnable.run();
        }
    }

    @FunctionalInterface
    public interface PromiseConsumer<T> {
        void accept(T value) throws Exception;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (isDone()) {
            throw new IllegalStateException("Already done");
        }
        return future.get();
    }

    @Override
    public T get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (isDone()) {
            throw new IllegalStateException("Already done");
        }
        return future.get(timeout, unit);
    }
}
