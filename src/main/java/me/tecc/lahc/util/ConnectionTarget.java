/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.util;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

public class ConnectionTarget {
    private final InetAddress address;
    private final int port;
    private boolean secure;

    public ConnectionTarget(URL url) throws UnknownHostException {
        this(InetAddress.getByName(url.getHost()), Util.firstValid((i) -> i == -1, url.getPort(), url.getDefaultPort(), Protocols.port(url.getProtocol())), Protocols.secure(url.getProtocol()));
    }
    public ConnectionTarget(InetAddress address, int port, boolean secure) {
        this.address = address;
        this.port = port;
        this.secure = secure;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public boolean isSecure() {
        return secure;
    }
}
