package me.tecc.lahc.util;

import java.nio.charset.StandardCharsets;
import java.util.Map;
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
            builder.append(entry.getKey()).append(": ").append(value).append("\n");
        }
        return builder.toString();
    }

    public static <T> T firstValid(Predicate<T> invalid, T... ts) {
        T last = null;
        for (T t : ts) {
            last = t;
            if (invalid.test(last)) continue;
            return last;
        }
        return last;
    }
}
