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
import java.util.function.Supplier;

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
        try {
            T result = get();
            if (responseConsumer != null) responseConsumer.accept(result);
        } catch (Exception e) {
            try {
                if (exceptionConsumer != null) exceptionConsumer.accept(e);
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage(), e);
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

    private T resultSuccess;
    private InterruptedException resultInterruptedEx;
    private ExecutionException resultExecutionEx;
    private TimeoutException resultTimeoutEx;
    private RuntimeException resultOtherEx;
    private int resultValue = 0;


    private T _get(PromiseFunction<T> fn) throws InterruptedException, ExecutionException, TimeoutException {
        if (this.resultValue == 0) {
            try {
                this.resultSuccess = fn.get();
                resultValue = 1;
            } catch (InterruptedException e) {
                this.resultInterruptedEx = e;
                resultValue = 2;
            } catch (ExecutionException e) {
                this.resultExecutionEx = e;
                resultValue = 3;
            } catch (TimeoutException e) {
                this.resultTimeoutEx = e;
                resultValue = 4;
            } catch (RuntimeException e) {
                this.resultOtherEx = e;
                resultValue = 5;
            }
        }
        switch (this.resultValue) {
            case 1:
                return this.resultSuccess;
            case 2:
                throw this.resultInterruptedEx;
            case 3:
                throw this.resultExecutionEx;
            case 4:
                throw this.resultTimeoutEx;
            case 5:
                throw this.resultOtherEx;
            default:
                throw new IllegalStateException("Result value has invalid value: " + this.resultValue);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public T get() throws InterruptedException, ExecutionException {
        try {
            return _get(this.future::get);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return _get(() -> this.future.get(timeout, unit));
    }

    @FunctionalInterface
    private interface PromiseFunction<T> {
        T get() throws InterruptedException, ExecutionException, TimeoutException;
    }
}
