package me.tecc.lahc;

import me.tecc.lahc.util.HeaderValue;
import me.tecc.lahc.util.Headers;
import me.tecc.lahc.util.MimeType;
import org.jetbrains.annotations.Contract;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class HttpRequest {
    private HttpMethod method = HttpMethod.GET;
    private URL url;
    private byte[] body;
    private Map<String, List<String>> headers = new HashMap<>();

    public HttpRequest() {
        setHeader("User-Agent", Lahc.getUserAgentValue());
        setHeader("Accept", "*/*;");
    }

    public boolean validate() {
        if (this.getMethod() == null) return false;
        if (this.url == null) return false;

        switch (this.url.getProtocol()) {
            case "http":
            case "https":
                break;
            default:
                return false;
        }

        return true;
    }

    public HttpMethod getMethod() {
        return this.method;
    }
    public HttpRequest setMethod(HttpMethod method) {
        this.method = method;
        return this;
    }
    public HttpRequest setMethod(String method) {
        if (method == null) throw new IllegalArgumentException("Method may not be null!");
        return setMethod(HttpMethod.valueOf(method.toUpperCase()));
    }

    public URL getURL() {
        return url;
    }
    public HttpRequest setURL(URL url) {
        this.url = url;
        return this;
    }
    public HttpRequest setURL(String url) {
        try {
            return setURL(new URL(url));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public HttpRequest setHeader(String header, List<String> value) {
        if (header == null) return this;
        if (value == null) this.headers.remove(header);
        else this.headers.put(header, new ArrayList<>(value));
        return this;
    }

    /**
     * Sets a header for this request.
     *
     * @param header The header to set. If it's null, fails quietly.
     * @param value  The value to set it to. If it's null, it removes any previous value.
     * @return This request.
     */
    @Contract("_, _ -> this")
    public HttpRequest setHeader(String header, String... value) {
        if (value == null) setHeader(header, (List<String>) null);
        return setHeader(header, Arrays.asList(value));
    }

    public HttpRequest setHeader(String header, HeaderValue value) {
        if (value == null) setHeader(header, (List<String>) null);
        return setHeader(header, value.getHeaderValue());
    }

    public HttpRequest accept(MimeType type) {
        if (type == null) setHeader(Headers.CONTENT_TYPE, (List<String>) null);
        return setHeader(Headers.CONTENT_TYPE, type);
    }
}
