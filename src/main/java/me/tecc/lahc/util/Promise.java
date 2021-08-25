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

/**
 * This class represents some kind of 'Future' with some JavaScript like Promise methods.
 * See the {@link Future} documentation for further documentation.
 */
public class Promise<T> implements Future<T> {

    private Future<T> future;
    private PromiseConsumer<T> responseConsumer;
    private PromiseConsumer<Exception> exceptionConsumer;
    private Runnable concludeRunnable;

    /**
     * Creates a Promise from a given Future.
     *
     * @param future the Future to wrap
     */
    public Promise(Future<T> future) {
        this.future = future;
    }

    /**
     * The code to run when the {@link #get()} did not throw an exception.
     *
     * @param responseConsumer the behaviour to run
     * @return itself for better use
     */
    public Promise<T> then(PromiseConsumer<T> responseConsumer) {
        this.responseConsumer = responseConsumer;
        return this;
    }

    /**
     * The code to run when the {@link #get()} did throw an exception.
     *
     * @param exceptionConsumer the behaviour to run
     * @return itself for better use
     */
    public Promise<T> exception(PromiseConsumer<Exception> exceptionConsumer) {
        this.exceptionConsumer = exceptionConsumer;
        return this;
    }

    /**
     * The code to run after both {@link #then(PromiseConsumer)} and {@link #exception(PromiseConsumer)} methods.
     *
     * @param concludeRunnable the behaviour to run
     * @return itself for better use
     */
    public Promise<T> conclude(Runnable concludeRunnable) {
        this.concludeRunnable = concludeRunnable;
        return this;
    }

    /**
     * Evaluate this Future and run the corresponding {@link #then(PromiseConsumer)} or {@link #exception(PromiseConsumer)} and {@link #conclude(Runnable)} methods.
     * To use this not every behaviour needs to be initialised. Ones that are not will be skipped.
     */
    public void run() {
        if (isDone()) {
            throw new IllegalStateException("Already done");
        }
        try {
            T result = get();
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

    /**
     * A special {@link java.util.function.Consumer} with an exception that can be thrown inside the code.
     * @param <T> the type to consume
     */
    @FunctionalInterface
    public interface PromiseConsumer<T> {
        void accept(T value) throws Exception;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDone() {
        return future.isDone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (isDone()) {
            throw new IllegalStateException("Already done");
        }
        return future.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (isDone()) {
            throw new IllegalStateException("Already done");
        }
        return future.get(timeout, unit);
    }
}
