/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.io.connectors;

import me.tecc.lahc.io.CachedConnector;
import me.tecc.lahc.io.Connection;
import me.tecc.lahc.util.ConnectionTarget;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;

@ApiStatus.Experimental
public final class NonBlockingConnector extends CachedConnector<NonBlockingConnector.CachedConnectionImpl> {

    @Override
    protected CachedConnectionImpl instantiate(ConnectionTarget target) throws IOException {
        return new CachedConnectionImpl(this, target);
    }

    public static final class CachedConnectionImpl implements Connection {
        private final NonBlockingConnector connector;
        private SocketChannel channel;
        private ConnectionTarget target;
        private final Object lock = new Object();
        private OutputStream output;
        private InputStream input;

        public CachedConnectionImpl(NonBlockingConnector instance, ConnectionTarget target) {
            this.connector = instance;
            this.target = target;
        }

        @Override
        public boolean dead() {
            return channel == null || !channel.isConnected() || !channel.isOpen();
        }

        @Override
        public void open() throws IOException {
            if (dead()) {
                channel = SocketChannel.open(target.getAddress());
            }
        }

        @Override
        public @NotNull Object lock() {
            return this.lock;
        }

        @Override
        public OutputStream output() throws IOException {
            if (this.output == null) {
                this.output = Channels.newOutputStream(this.channel);
            }
            return this.output;
        }

        @Override
        public InputStream input() {
            if (this.input == null) {
                this.input = Channels.newInputStream(this.channel);
            }
            return this.input;
        }

        @Override
        public void done() {

        }

        @Override
        public void close() throws IOException {
            this.channel.close();
            this.channel = null;
        }
    }
}
