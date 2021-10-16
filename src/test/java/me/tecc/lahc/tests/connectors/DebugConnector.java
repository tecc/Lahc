package me.tecc.lahc.tests.connectors;

import me.tecc.lahc.http.HttpResponse;
import me.tecc.lahc.io.CachedConnector;
import me.tecc.lahc.io.Connection;
import me.tecc.lahc.util.ConnectionTarget;
import me.tecc.lahc.util.Util;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class DebugConnector extends CachedConnector {
    @Override
    protected Connection instantiate(ConnectionTarget target) throws IOException {
        return new DebugConnectionImpl();
    }

    public static class DebugConnectionImpl implements Connection {
        private ByteArrayOutputStream outputStream;
        private final Object lock = new Object();
        private static final byte[] response;

        static {
            String s = "HTTP/1.0 200 OK" + Util.CRLF + "Server: Lahc/Debug" + Util.CRLF + Util.CRLF;
            response = s.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public boolean dead() {
            return false;
        }

        @Override
        public void open() throws IOException {
            this.outputStream = new ByteArrayOutputStream();
        }

        @Override
        public @NotNull Object lock() {
            return this.lock;
        }

        @Override
        public OutputStream output() throws IOException {
            return this.outputStream;
        }

        @Override
        public InputStream input() throws IOException {
            return new ByteArrayInputStream(response);
        }

        @Override
        public void done() {
            byte[] received = this.outputStream.toByteArray();
            this.outputStream.reset();
            String s = new String(received, StandardCharsets.UTF_8);
            System.out.println(s);
        }

        @Override
        public void close() throws IOException {

        }
    }
}
