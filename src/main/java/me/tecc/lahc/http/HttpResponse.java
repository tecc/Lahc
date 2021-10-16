/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.http;

import me.tecc.lahc.Status;
import me.tecc.lahc.util.Headers;
import me.tecc.lahc.util.MimeType;
import me.tecc.lahc.util.Util;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

/**
 * An abstract class for an HTTP response.
 */
public abstract class HttpResponse {
    public abstract HttpRequest getRequest();
    @NotNull
    public abstract String getStatusLine();
    @NotNull
    public abstract HttpVersion getResponseVersion();
    @NotNull
    public abstract Status getStatus();
    @Contract("-> !null")
    public abstract byte[] getBody();
    @NotNull
    public abstract Map<String, String> getHeaders();
    @Contract("null -> null; _ -> _")
    public String getHeader(String header) {
        if (header == null) return null;
        return getHeaders().get(header);
    }
    @Contract("null, _ -> null; _, null -> null; _, _ -> _")
    public <T> T getHeader(String header, Function<String, T> converter) {
        if (header == null || converter == null) return null;
        String value = getHeader(header);
        return converter.apply(value);
    }
    public String getBodyString(Charset charset) {
        if (charset == null) charset = Charset.defaultCharset();
        return new String(getBody(), charset);
    }
    public String getBodyString() {
        MimeType contentType = getContentType();
        if (contentType != null) return getBodyString(contentType.getNioCharset());
        return getBodyString(StandardCharsets.UTF_8);
    }

    public MimeType getContentType() {
        return getHeader(Headers.CONTENT_TYPE, MimeType::parse);
    }

    public Headers.Connection getConnectionHeader() {
        String value = getHeader(Headers.CONNECTION);
        if (value == null) return null;
        return Headers.Connection.getByHeaderValue(value);
    }

    public String getHeadersString() {
        return Util.headersString(this.getHeaders());
    }

    @Override
    public String toString() {
        return "HTTP/" + getResponseVersion() + " " + getStatus() + "\n"
                + getHeadersString() + "\n"
                + getBodyString();
    }

    public boolean isSuccessful() {
        return this.getStatus().isSuccessful();
    }

    public abstract byte[] getRawResponse();
}
