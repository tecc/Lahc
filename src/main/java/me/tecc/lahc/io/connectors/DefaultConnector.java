/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.io.connectors;

import me.tecc.lahc.io.CachedConnector;
import me.tecc.lahc.io.Connection;
import me.tecc.lahc.io.Connector;
import me.tecc.lahc.util.ConnectionTarget;
import me.tecc.lahc.util.Util;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class DefaultConnector extends CachedConnector<DefaultConnector.DefaultConnectionImpl> {
    private Map<ConnectionTarget, DefaultConnectionImpl> connections = new HashMap<>();
    private SSLSocketFactory sslSocketFactory;

    {
        try {
            sslSocketFactory = SSLContext.getDefault().getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected DefaultConnectionImpl instantiate(ConnectionTarget target) throws IOException {
        return new DefaultConnectionImpl(this, target);
    }

    public static final class DefaultConnectionImpl implements Connection {
        private Object lock;
        private DefaultConnector connector;
        private ConnectionTarget target;
        private Socket socket;

        public DefaultConnectionImpl(DefaultConnector connector, ConnectionTarget target) throws IOException {
            this.connector = connector;
            this.target = target;
            this.lock = new Object();
        }

        @Override
        public boolean dead() {
            if (this.socket == null) return true;
            return
                    this.socket.isClosed() || !this.socket.isConnected() || !this.socket.isBound() ||
                    this.socket.isInputShutdown() || this.socket.isOutputShutdown();
        }

        @Override
        public void open() throws IOException {
            synchronized (lock()) {
                if (!dead()) {
                    return;
                }
                if (dead() && socket != null) {
                    socket.close();
                }
                if (this.target.isSecure()) {
                    SSLSocket socket = (SSLSocket) connector.sslSocketFactory.createSocket();
                    socket.setUseClientMode(true);
                    this.socket = socket;
                } else {
                    this.socket = new Socket();
                }
                this.socket.setTcpNoDelay(true);
                this.socket.setReuseAddress(true);
                this.socket.setTrafficClass(0x10 | 0x08 | 0x04);
                this.socket.connect(target.getAddress(), connector.connectionTimeout);
            }
        }

        @Override
        public InputStream input() throws IOException {
            return socket.getInputStream();
        }

        @Override
        public void close() throws IOException {
            synchronized (lock()) {
                socket.close();
            }
        }

        @Override
        public void done() {
        }

        @Override
        @NotNull
        public Object lock() {
            return lock;
        }

        @Override
        public OutputStream output() throws IOException {
            return this.socket.getOutputStream();
        }
    }
}
