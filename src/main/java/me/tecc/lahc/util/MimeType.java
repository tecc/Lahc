/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.util;

import org.jetbrains.annotations.Contract;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;

public class MimeType implements HeaderValue {
    private String type;
    private String charset;

    public MimeType(String type) {
        this(type, null);
    }

    public MimeType(String type, String charset) {
        if (type == null) throw new IllegalArgumentException("Type may not be null!");

        this.type = type;

        if (charset != null && !Util.isBlank(charset)) this.charset = charset;
        else this.charset = null;
    }

    @Contract("null -> null; _ -> _")
    public static MimeType parse(String str) {
        if (str == null) return null;
        StringBuilder type = new StringBuilder();
        StringBuilder charset = null;
        StringBuilder propName = null; // just for safety
        boolean semi = false;
        boolean eq = false;
        for (char c : str.toCharArray()) {
            if (eq) charset.append(c);
            else if (semi) {
                if (c == '=') {
                    if (!propName.toString().equals("charset")) throw new IllegalArgumentException("Invalid property name");
                    charset = new StringBuilder();
                    eq = true;
                } else propName.append(c);
            }
            else {
                if (c == ';') {
                    semi = true;
                    propName = new StringBuilder();
                } else {
                    type.append(c);
                }
            }
        }
        return new MimeType(type.toString(), charset == null ? null : charset.toString());
    }

    public String getType() {
        return this.type;
    }
    public String getCharset() {
        return charset;
    }
    public Charset getNioCharset() {
        String name = getCharset();
        if (name == null) return null;
        try {
            return Charset.forName(name);
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            return null;
        }
    }

    public MimeType withCharset(String charset) {
        return new MimeType(getType(), charset);
    }
    public MimeType withCharset(Charset charset) {
        return withCharset(charset.name());
    }

    @Override
    public String toString() {
        String type = getType();
        String charset = getCharset();
        if (charset == null || Util.isBlank(charset)) return getType();
        else return getType() + ";charset=" + charset;
    }

    @Override
    public String getHeaderValue() {
        return toString();
    }

    /* Put predefined mime types here */
    public static final MimeType ANY = new MimeType("*/*");
    public static final class Application {
        public static final MimeType JSON = new MimeType("application/json");
    }
}
