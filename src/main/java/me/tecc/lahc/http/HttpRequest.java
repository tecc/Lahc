/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.http;

import me.tecc.lahc.HttpClient;
import me.tecc.lahc.Lahc;
import me.tecc.lahc.util.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

public class HttpRequest {
    private HttpMethod method = HttpMethod.GET;
    private URL url;
    private byte[] body = new byte[0];
    private HttpVersion version = HttpVersion.V1_0;
    private Map<String, String> headers = new HashMap<>();

    public HttpRequest() {
        header("User-Agent", Lahc.getUserAgentValue());
        header("Accept", "*/*;");
    }

    public HttpRequest(HttpRequest base) {
        this.version = base.getHttpVersion();
        this.method = base.getMethod();
        this.url = base.getURL();
        this.headers = new HashMap<>(base.getHeaders());
        byte[] body = base.getBody();
        if (body != null) this.body = Arrays.copyOf(body, body.length);
        else this.body = new byte[0];
    }

    public boolean validate() {
        if (this.getMethod() == null) return false;
        if (this.getURL() == null) return false;
        if (this.getTarget() == null) return false;
        // TODO: Expand on this function
        return true;
    }

    public String getRequestLine() {
        return "GET " + Util.nonBlank(getURL().getPath(), "/") + " HTTP/" + getHttpVersion() + Util.CRLF;
    }

    public String getHeadersString() {
        return Util.headersString(this.getHeaders());
    }

    @Override
    public String toString() {
        return getRequestLine() + getHeadersString();
    }

    public HttpRequest resolve(HttpClient.Options options) {
        HttpRequest request = new HttpRequest(this);
        byte[] body = request.getBody();
        MimeType accepts = request.accepts();
        if (accepts == null || accepts.getCharset() == null) {
            // if accepts is null, default to allowing *anything* - as long as it's in that type
            accepts = MimeType.ANY.withCharset(options.getDefaultCharset());
            request.accept(accepts);
        }
        if (body.length > 0) {
            request.header(Headers.CONTENT_LENGTH, body.length);
            if (request.getContentType() == null) {
                request.header(Headers.CONTENT_TYPE, MimeType.ANY.withCharset(options.getDefaultCharset()));
            }
        } else {
            request.removeHeader(Headers.CONTENT_LENGTH);
            if (request.getContentType() != null) {
                request.removeHeader(Headers.CONTENT_TYPE);
            }
        }
        request.header(Headers.HOST, this.getURL().getAuthority());
        if (options.shouldUsePersistentConnections()) {
            request.header(Headers.CONNECTION, Headers.Connection.KEEP_ALIVE);
        } else {
            request.header(Headers.CONNECTION, Headers.Connection.CLOSE);
        }
        return request;
    }

    public byte[] toHTTPRequest() {
        byte[] meta = (toString() + Util.CRLF).getBytes(StandardCharsets.UTF_8);
        byte[] request;
        if (body != null && body.length > 0) {
            request = Util.join(meta, body, meta.length + body.length);
        } else {
            request = meta;
        }
        return request;
    }

    public HttpRequest body(byte[] bytes) {
        if (bytes != null) {
            this.body = bytes;
        }
        return this;
    }

    public HttpRequest body(String str, Charset charset) {
        if (charset == null) charset = StandardCharsets.UTF_8;
        return body(str.getBytes(charset));
    }
    public HttpRequest body(String str) {
        return body(str, StandardCharsets.UTF_8);
    }
    public byte[] getBody() {
        return this.body;
    }
    public HttpVersion getHttpVersion() {
        return this.version;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public HttpMethod getMethod() {
        return this.method;
    }

    public HttpRequest method(HttpMethod method) {
        this.method = method;
        return this;
    }
    public HttpRequest method(String method) {
        if (method == null) throw new IllegalArgumentException("Method may not be null!");
        return method(HttpMethod.valueOf(method.toUpperCase()));
    }
    public URL getURL() {
        return url;
    }

    public HttpRequest url(URL url) {
        this.url = url;
        return this;
    }
    public HttpRequest url(String url) {
        try {
            return url(new URL(url));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
    /**
     * Sets the header to the specified value. Any existing value is overwritten.
     * If the value is null, it clears the value.
     *
     * @param header
     * @param value
     * @return
     */
    public HttpRequest header(String header, List<String> value) {
        if (header == null) return this;
        if (value == null) return removeHeader(header);
        else this.headers.put(header, String.join(", ", value));
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
    public HttpRequest header(String header, @Nullable String... value) {
        if (value == null) return header(header, (List<String>) null);
        return header(header, Arrays.asList(value));
    }

    @Contract("_ -> this")
    public HttpRequest removeHeader(String header) {
        if (header == null) {
            return this;
        }
        this.headers.remove(header);
        return this;
    }

    @Contract("_, _ -> this")
    public HttpRequest header(String name, Number value) {
        if (value == null) return header(name, (List<String>) null);
        return header(name, value.toString());
    }

    @Contract("_, _ -> this")
    public HttpRequest header(String header, HeaderValue value) {
        if (value == null) return header(header, (List<String>) null);
        return header(header, value.getHeaderValue());
    }
    public String getHeader(String header) {
        return this.getHeaders().get(header);
    }

    public <T> T getHeader(String header, Function<String, T> converter) {
        String value = getHeader(header);
        if (header == null) return null;
        try {
            return converter.apply(value);
        } catch (Throwable t) {
            return null;
        }
    }
    @Contract("_ -> this")
    public HttpRequest accept(MimeType type) {
        if (type == null) {
            return this.removeHeader(Headers.ACCEPT);
        }
        return this.header(Headers.ACCEPT, type.toString());
    }

    public MimeType getContentType() {
        return getHeader(Headers.CONTENT_TYPE, MimeType::parse);
    }

    public MimeType accepts() {
        return getHeader(Headers.ACCEPT, MimeType::parse);
    }

    public ConnectionTarget getTarget() {
        return new ConnectionTarget(url);
    }

    /**
     * Attempts to make the request secure automatically, e.g. by switching "http" to "https".
     */
    @Contract("-> this")
    public HttpRequest makeSecure() {
        Protocols.Protocol protocol = Protocols.get(url);
        if (!protocol.canMakeSecure()) return this; // fail silently
        protocol = protocol.makeSecure();
        try {
            return this.url(new URL(protocol.getProtocol(), url.getHost(), url.getPort(), url.getFile()));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error trying to make request URL use secure protocol", e);
        }
    }

    public int getContentLength() {
        Integer i = getHeader(Headers.CONTENT_LENGTH, Integer::parseInt);
        if (i == null) return -1;
        return i;
    }
}
