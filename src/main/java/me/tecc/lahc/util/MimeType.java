package me.tecc.lahc.util;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;

public class MimeType implements HeaderValue {
    private String type;
    private String charset;

    public MimeType(String type, String charset) {
        if (type == null) throw new IllegalArgumentException("Type may not be null!");

        this.type = type;
        this.charset = charset;
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
        return getType() + ";charset=" + getCharset();
    }

    @Override
    public List<String> getHeaderValue() {
        return List.of(toString());
    }

    /* Put predefined mime types here */

}
