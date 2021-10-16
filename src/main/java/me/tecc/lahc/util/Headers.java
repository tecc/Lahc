/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.util;

import java.util.HashMap;
import java.util.Map;

public class Headers {
    public static final String USER_AGENT = "User-Agent";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ACCEPT = "Accept";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String HOST = "Host";
    public static final String CONNECTION = "Connection";

    public enum Connection implements HeaderValue {
        UNKNOWN(null),
        KEEP_ALIVE("keep-alive"),
        CLOSE("close");

        private final String value;
        private static Map<String, Connection> byHeaderValue = new HashMap<>();

        static {
            for (Connection v : values()) {
                byHeaderValue.put(v.getHeaderValue(), v);
            }
        }

        Connection(String value) {
            this.value = value;
        }

        @Override
        public String getHeaderValue() {
            return this.value;
        }

        public static Connection getByHeaderValue(String value) {
            return byHeaderValue.get(value);
        }
    }
}
