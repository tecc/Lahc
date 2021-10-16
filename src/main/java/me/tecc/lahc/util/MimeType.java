/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.util;

import org.jetbrains.annotations.Contract;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String charset = getCharset();
        if (charset == null || Util.isBlank(charset)) return getType();
        else return getType() + ";charset=" + charset;
    }

    @Override
    public String getHeaderValue() {
        return toString();
    }

    @Contract("null -> null; _ -> _")
    public static MimeType parse(String str) {
        if (str == null) return null;
        StringBuilder type = new StringBuilder();
        StringBuilder propName = null, propValue = null;
        String propNameB = null;
        Map<String, String> props = new HashMap<>();
        boolean cr = false, needsPut = false;
        ParseStage stage = ParseStage.TYPE;
        loop:
        for (char c : str.toCharArray()) {
            switch (c) {
                case '\r':
                    cr = true;
                    continue loop;
                case '\n':
                    if (cr) {
                        break loop;
                    }
                default:
                    break;
            }
            switch (stage) {
                case TYPE: {
                    if (c == ';') {
                        propName = new StringBuilder();
                        stage = ParseStage.PROPERTY_NAME;
                    } else {
                        type.append(c);
                    }
                    continue loop;
                }
                case PROPERTY_NAME: {
                    if (c == '=') {
                        stage = ParseStage.PROPERTY_VALUE;
                        propNameB = propName.toString().trim();
                        propValue = new StringBuilder();
                        needsPut = true;
                    } else {
                        propName.append(c);
                    }
                    continue loop;
                }
                case PROPERTY_VALUE: {
                    switch (c) {
                        case ';': {
                            stage = ParseStage.PROPERTY_NAME;
                            continue loop;
                        }
                        default: {
                            propValue.append(c);
                        }
                    }
                }
            }
        }

        if (needsPut) {
            props.put(propNameB, propValue.toString());
        }

        String charset = props.get("charset");
        return new MimeType(type.toString(), charset);
    }

    private enum ParseStage {
        TYPE,
        PROPERTY_NAME,
        PROPERTY_VALUE
    }

    /* Put predefined mime types here */
    public static final MimeType ANY = new MimeType("*/*");

    public static final class Application {
        public static final MimeType JSON = new MimeType("application/json");
    }
}
