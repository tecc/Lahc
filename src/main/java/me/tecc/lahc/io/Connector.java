/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.io;

import me.tecc.lahc.io.Connection;
import me.tecc.lahc.util.ConnectionTarget;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Interface for a Connector, whose job is to manage connections to hosts.
 */
public interface Connector {
    Connection connect(ConnectionTarget target) throws IOException;
    void destroy();
}
