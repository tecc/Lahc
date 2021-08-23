package me.tecc.lahc;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class HttpClient {
    private Executor executor;
    private static Options DEFAULT_OPTIONS = new Options();

    public HttpClient() {
        this(DEFAULT_OPTIONS);
    }
    public HttpClient(Options options) {
        if (options.shouldPoolThreads()) executor = Executors.newCachedThreadPool();
        else {
            if (options.getThreads() < 1) throw new IllegalArgumentException("'threads' option must be positive!");

            executor = Executors.newFixedThreadPool(options.getThreads());
        }
    }

    public Future<HttpResponse> execute(HttpRequest request) {

        return CompletableFuture.supplyAsync(() -> {
            // make sure it's a valid request
            if (!request.validate()) throw new IllegalArgumentException("Request is invalid!");

            try {
                HttpURLConnection conn = (HttpURLConnection) request.getURL().openConnection();

                conn.setRequestMethod(request.getMethod().name());
                int code = conn.getResponseCode();



                Status status = Status.getByCode(code);

                InputStream input = conn.getInputStream();
                byte[] body = input.readAllBytes();
                input.close();

                Map<String, List<String>> headers = conn.getHeaderFields();

                // return result
                return new HttpResponse() {
                    @Override
                    public URL getRequestURL() {
                        return request.getURL();
                    }

                    @Override
                    public Status getStatus() {
                        return status;
                    }

                    @Override
                    public byte[] getBody() {
                        return body;
                    }

                    @Override
                    public Map<String, List<String>> getHeaders() {
                        return headers;
                    }
                };
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    public static final class Options {
        private boolean shouldPoolThreads = true;
        private int threads = 2;
        private String defaultCharset;

        public boolean shouldPoolThreads() {
            return shouldPoolThreads;
        }

        public void setShouldPoolThreads(boolean shouldPoolThreads) {
            this.shouldPoolThreads = shouldPoolThreads;
        }

        public int getThreads() {
            return threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }

        public String getDefaultCharset() {
            return defaultCharset;
        }

        public Options setDefaultCharset(String defaultCharset) {
            this.defaultCharset = defaultCharset;
            return this;
        }
        public Options setDefaultCharset(Charset charset) {
            return setDefaultCharset(charset.name());
        }
    }
}
