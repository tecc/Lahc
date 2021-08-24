/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.io.connectors;

import me.tecc.lahc.io.Connection;
import me.tecc.lahc.util.ConnectionTarget;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class DefaultConnector implements Connector {
    private Map<ConnectionTarget, DefaultConnectionImpl> connections = new HashMap<>();

    @Override
    public Connection connect(ConnectionTarget target) throws IOException {
        DefaultConnectionImpl connection = connections.get(target);
        if (connection == null) {
            connection = new DefaultConnectionImpl(target);
            connections.put(target, connection);
        }
        return connection;
    }

    @Override
    public void destroy() {
    }

    public static final class DefaultConnectionImpl implements Connection {
        private Object lock;
        private ConnectionTarget target;
        private Socket socket;
        private OutputStream output;
        private InputStream input;

        public DefaultConnectionImpl(ConnectionTarget target) throws IOException {
            this.target = target;
            this.lock = new Object();
        }

        @Override
        public void open() throws IOException {
            if (this.socket != null) {
                if (!this.socket.isClosed() && this.socket.isConnected() && this.socket.isBound()) return;
            }
            if (this.socket != null) {
                this.socket.close();
                this.socket = null;
                this.output = null;
                this.input = null;
            }
            if (this.target.isSecure()) {
                this.socket = SSLSocketFactory.getDefault().createSocket(target.getAddress(), target.getPort());
            } else {
                this.socket = new Socket(target.getAddress(), target.getPort());
            }
        }

        @Override
        public OutputStream output() throws IOException {
            if (output == null) output = socket.getOutputStream();
            return output;
        }

        @Override
        public InputStream input() throws IOException {
            if (input == null) input = socket.getInputStream();
            return input;
        }

        @Override
        public void close() throws IOException {
            if (input != null) input.close();
            if (output != null) output.close();
            socket.close();
        }

        @Override
        @NotNull
        public Object lock() {
            return lock;
        }
    }
}
