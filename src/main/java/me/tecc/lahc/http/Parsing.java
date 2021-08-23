package me.tecc.lahc.http;

import me.tecc.lahc.Status;
import me.tecc.lahc.util.Headers;
import me.tecc.lahc.util.MimeType;
import me.tecc.lahc.util.Util;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Parsing {
    public static HttpResponse response(HttpRequest request, InputStream stream) throws IOException {
        return response(request, new InputStreamReader(stream));
    }

    public static HttpResponse response(HttpRequest request, Reader reader) throws IOException {
        // keeps track of whether the http part of the response has been parsed
        ParseStage stage = ParseStage.FINDING_START;
        int current;
        int headerProgress = 0;
        boolean cr = false;
        boolean firstIter = false;

        StringBuilder
                version = new StringBuilder(),
                statusMessage = new StringBuilder();
        StringBuilder
                headerName = new StringBuilder(),
                headerValue = new StringBuilder();
        Map<String, String> headers = new HashMap<>();
        int statusCode = 0;
        ByteArrayOutputStream bodyOutput = new ByteArrayOutputStream();
        // quick access to headers when parsing
        MimeType contentType = request.accepts();
        int contentLength = 0;

        while ((current = reader.read()) >= 0 && stage != ParseStage.BODY) {
            // if it hasn't started, look for header
            switch (stage) {
                case FINDING_START: {
                    // look for header by checking how many characters into the HTTP/ it is and matching the characters
                    // sure, slightly complicated but the simple version assumes that the start is
                    if (headerProgress == 0 && current == 'H') {
                        headerProgress++;
                        break;
                    }
                    if ((headerProgress == 1 || headerProgress == 2) && current == 'T') {
                        headerProgress++;
                        break;
                    }
                    if (headerProgress == 3 && current == 'P') {
                        headerProgress++;
                        break;
                    }
                    if (headerProgress == 4 && current == '/') {
                        stage = ParseStage.VERSION;
                        break;
                    }
                    headerProgress = 0;
                    break;
                }
                case VERSION: {
                    if (current == ' ') stage = ParseStage.STATUS_CODE;
                    else version.append((char) current);
                    break;
                }
                case STATUS_CODE: {
                    if (current == ' ') stage = ParseStage.STATUS_MESSAGE;
                    else statusCode = addDigit(statusCode, current - '0');
                    break;
                }
                case STATUS_MESSAGE: {
                    // handle CRLF
                    if (current == '\r') cr = true;
                    else if (current == '\n' && cr) {
                        cr = false;
                        stage = ParseStage.WAIT_FOR_DIRECTION;
                        break;
                    } else statusMessage.append((char) current);
                    break;
                }
                case WAIT_FOR_DIRECTION: {
                    if (current == '\r') cr = true;
                    else if (cr && current == '\n') {
                        cr = false;
                        stage = ParseStage.BODY;
                    } else {
                        stage = ParseStage.HEADER_NAME;
                        headerName = new StringBuilder().append((char) current);
                        headerValue = new StringBuilder();
                    }
                    break;
                }
                case HEADER_NAME: {
                    if (current == ':') {
                        stage = ParseStage.HEADER_VALUE;
                        firstIter = true;
                    } else headerName.append((char) current);
                    break;
                }
                case HEADER_VALUE: {
                    if (current == '\r') cr = true;
                    else if (cr && current == '\n') {
                        String name = headerName.toString();
                        String value = headerValue.toString();
                        headers.put(name, value);
                        // for optimisation reasons
                        // ik, stupid, but eh, it'll work
                        switch (name) {
                            case Headers.CONTENT_LENGTH: {
                                if (!Util.isBlank(value)) {
                                    try {
                                        contentLength = Integer.parseInt(value);
                                    } catch (NumberFormatException ignored) {}
                                }
                                break;
                            }
                            case Headers.CONTENT_TYPE: {
                                if (!Util.isBlank(value)) {
                                    contentType = MimeType.parse(value);
                                } else contentType = MimeType.ANY;
                            }
                        }
                        cr = false;
                        stage = ParseStage.WAIT_FOR_DIRECTION;
                    } else if (firstIter) {
                        firstIter = false;
                        continue;
                    }
                    else headerValue.append((char) current);
                    break;
                }
            }
        }

        if (contentType.getCharset() == null) contentType.withCharset(request.accepts().getCharset());

        Charset charset = contentType.getNioCharset();
        if (charset == null) charset = Charset.defaultCharset();
        if (stage == ParseStage.BODY && contentLength != 0) {
            // buffered reading, reading 128 chars at a time
            int jumps = contentLength >> 7; // division of 128
            int remaining = contentLength & 0x7f; // remaining will always be last 6 bits
            for (int i = 0; i < jumps; i++) {
                char[] chars = new char[128];
                if (reader.read(chars) < 128) throw new IOException("Length prediction was incorrect!");
                bodyOutput.write(toBytes(chars, charset));
            }
        }

        Status status = new Status(statusCode, statusMessage.toString(), null);
        HttpVersion responseVersion = HttpVersion.byString(version.toString());
        byte[] body = bodyOutput.toByteArray();
        return new HttpResponse() {
            @Override
            public HttpRequest getRequest() {
                return request;
            }

            @Override
            public @NotNull String getStatusLine() {
                Status status = getStatus();
                return "HTTP/" + getResponseVersion() + " " + status.getCode() + " " + status.getMessage();
            }

            @Override
            @NotNull
            public HttpVersion getResponseVersion() {
                return responseVersion;
            }

            @Override
            @NotNull
            public Status getStatus() {
                return status;
            }

            @Override
            public byte[] getBody() {
                return body;
            }

            @Override
            @NotNull
            public Map<String, String> getHeaders() {
                return Collections.unmodifiableMap(headers);
            }
        };
    }


    private static byte[] toBytes(char[] chars, @NotNull Charset nioCharset) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = nioCharset.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }


    public static byte[] toByteArray(int val) {
        byte
                b1 = (byte) (val >> 24 & 0xff),
                b2 = (byte) (val >> 16 & 0xff),
                b3 = (byte) (val >> 8 & 0xff),
                b4 = (byte) (val & 0xff);
        int use;
        if (b1 != 0x0) use = 4;
        else if (b2 != 0x0) use = 3;
        else if (b3 != 0x0) use = 2;
        else use = 1;
        byte[] bytes = new byte[use];
        switch (use) {
            case 4:
                bytes[0] = b1;
            case 3:
                bytes[use - 3] = b2;
            case 2:
                bytes[use - 2] = b3;
            case 1:
                bytes[use - 1] = b4;
        }
        return bytes;
    }

    private static int addDigit(int i, int d) {
        if (i == 0) return d;
        return (i << 3) + (i << 1) + d;
    }

    enum ParseStage {
        FINDING_START,
        VERSION,
        STATUS_CODE,
        STATUS_MESSAGE,
        WAIT_FOR_DIRECTION,
        HEADER_NAME, HEADER_VALUE,
        BODY;
    }
}
