/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.util;

import org.jetbrains.annotations.Contract;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class Util {
    public static final String CRLF = new String(new byte[]{0x0D, 0x0A}, StandardCharsets.UTF_8);

    public static boolean isBlank(String s) {
        if (s == null) return true;
        if (s.isEmpty()) return true;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) return false;
        }
        return true;
    }
    public static String nonBlank(String s, String def) {
        return isBlank(s) ? def : s;
    }
    public static String headersString(Map<String, String> headers) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String value = entry.getValue();
            if (value == null) continue;
            builder.append(entry.getKey()).append(": ").append(value).append(Util.CRLF);
        }
        return builder.toString();
    }

    @SuppressWarnings("ConstantConditions")
    @Contract("null -> null; !null -> !null")
    public static Integer[] objectify(int[] a) {
        if (a == null) return null;
        return (Integer[]) ((Object) a);
    }

    @SafeVarargs
    public static <T> T firstValid(Predicate<T> invalid, T... ts) {
        T last = null;
        for (T t : ts) {
            last = t;
            if (invalid.test(last)) continue;
            return last;
        }
        return last;
    }

    public static void close(Closeable closeable) throws IOException {
        if (closeable != null) closeable.close();
    }

    public static URL url(String spec) {
        try {
            return new URL(spec);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static byte[] join(byte[] a, byte[] b, int maxLength) {
        byte[] c = new byte[maxLength];
        int w = Math.min(a.length, maxLength);
        System.arraycopy(a, 0, c, 0, w);
        System.arraycopy(b, 0, c, w, Math.min(w + b.length, maxLength - w));
        return c;
    }
}
