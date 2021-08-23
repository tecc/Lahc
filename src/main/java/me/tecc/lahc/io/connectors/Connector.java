package me.tecc.lahc.io.connectors;

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
