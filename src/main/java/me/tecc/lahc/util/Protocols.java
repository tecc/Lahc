/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.util;

import jdk.internal.access.JavaIOFileDescriptorAccess;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class Protocols {
    private static Map<String, Protocol> protocols = new HashMap<>();

    static {
        register("http", 80, false, "https");
        register("https", 443, true, null);
    }

    public static void register(String protocol, int port, boolean isSecure, String secured) {
        protocols.put(protocol, new Protocol() {
            @Override
            public String getProtocol() {
                return protocol;
            }

            @Override
            public int getPort() {
                return port;
            }

            @Override
            public boolean isSecure() {
                return isSecure;
            }

            @Override
            public boolean canMakeSecure() {
                return isSecure || secured != null;
            }

            @Override
            public Protocol makeSecure() {
                if (isSecure) return this;
                else return Protocols.get(secured);
            }
        });
    }

    @Contract("null -> null; !null -> _")
    @Nullable
    public static Protocol get(String name) {
        if (name == null) return null;
        return protocols.get(name);
    }
    public static Protocol get(URL url) {
        if (url == null) return null;
        return get(url.getProtocol());
    }

    public interface Protocol {
        String getProtocol();
        int getPort();
        boolean isSecure();
        boolean canMakeSecure();
        Protocol makeSecure();
    }
}
