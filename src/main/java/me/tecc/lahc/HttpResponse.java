package me.tecc.lahc;

import org.jetbrains.annotations.Contract;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public interface HttpResponse {
    URL getRequestURL();
    Status getStatus();
    byte[] getBody();
    Map<String, List<String>> getHeaders();
    @Contract("null -> null; _ -> _")
    default List<String> getHeader(String header) {
        if (header == null) return null;
        return getHeaders().get(header);
    }
    default String getBodyString(Charset charset) {
        return new String(getBody(), charset);
    }
    default String getBodyString() {
        return getBodyString(StandardCharsets.UTF_8);
    }
}
