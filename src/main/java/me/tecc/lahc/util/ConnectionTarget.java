/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.util;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getPort());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConnectionTarget)) return false;
        ConnectionTarget tgt = (ConnectionTarget) obj;
        if (tgt.isSecure() != this.isSecure()) return false;
        if (!tgt.getAddress().equals(this.getAddress())) return false;
        return true;
    }
}
