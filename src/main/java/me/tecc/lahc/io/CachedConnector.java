/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.io;

import me.tecc.lahc.util.ConnectionTarget;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class CachedConnector<T extends Connection> implements Connector {
    protected Map<ConnectionTarget, T> connections = new HashMap<>();
    protected int connectionTimeout;

    protected abstract T instantiate(ConnectionTarget target) throws IOException;

    @Override
    public Connection connect(ConnectionTarget target) throws IOException {
        T connection = connections.get(target);
        if (connection == null) {
            connection = instantiate(target);
            connections.put(target, connection);
        }
        return connection;
    }

    @Override
    public void setConnectionTimeout(int timeout) {

    }

    @Override
    public void destroy() {

    }
}
