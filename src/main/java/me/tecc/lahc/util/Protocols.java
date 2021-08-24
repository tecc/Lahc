/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.util;

import java.util.HashMap;
import java.util.Map;

public final class Protocols {
    private static Map<String, Integer> ports = new HashMap<>();
    private static Map<String, Boolean> secure = new HashMap<>();

    static {
        register("http", 80, false);
        register("https", 443, true);
    }

    public static void register(String protocol, int port, boolean isSecure) {
        ports.put(protocol, port);
        secure.put(protocol, isSecure);
    }

    public static int port(String protocol) {
        Integer val = ports.get(protocol);
        if (val == null) return -1;
        return val;
    }

    public static boolean secure(String protocol) {
        Boolean val = secure.get(protocol);
        if (val == null) return false;
        return val;
    }
}
