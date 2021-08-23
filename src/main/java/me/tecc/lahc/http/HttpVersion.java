/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.http;

import java.util.HashMap;
import java.util.Map;

public enum HttpVersion {
    V1_0("1.0"),
    V1_1("1.1"),
    V2_0("2.0");

    private final String version;
    HttpVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return this.version;
    }

    private static final Map<String, HttpVersion> byString = new HashMap<>();

    static {
        for (HttpVersion version : values()) {
            byString.put(version.getVersion(), version);
        }
    }

    public static HttpVersion byString(String str) {
        if (str == null) return null;
        return byString.get(str);
    }
}
