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

    public ConnectionTarget(URL url) {
        if (url == null) throw new IllegalArgumentException("'url' parameter may not be null!");
        try {
            this.address = InetAddress.getByName(url.getHost());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        Protocols.Protocol protocol = Protocols.get(url);
        this.port = Util.firstValid((i) -> i == -1,
                        url.getPort(),
                        url.getDefaultPort());
        this.secure = protocol.isSecure();
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

    @Override
    public String toString() {
        return "ConnTgt{" + this.getAddress().toString() + " port " + this.getPort() + "}";
    }
}
