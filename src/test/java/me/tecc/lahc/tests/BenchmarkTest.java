package me.tecc.lahc.tests;

import com.sun.net.httpserver.HttpServer;
import me.tecc.lahc.HttpClient;
import me.tecc.lahc.http.HttpRequest;
import org.apache.hc.client5.http.async.methods.*;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.asynchttpclient.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.*;
import java.util.concurrent.ExecutionException;

public class BenchmarkTest {
    public static int TIMES = 10;
    private static URL url;

    @BeforeAll
    static void setup() throws IOException {
        System.out.println("Iterations: " + TIMES);
        System.out.println("Times are shown in milliseconds");
        url = new URL("https://example.com/index.html");
    }

    @Test
    public void benchmarkLahc() {
        HttpClient client = new HttpClient();
        HttpRequest request = new HttpRequest()
                .url(url);

        benchmark("Lahc", () -> {
            try {
                client.execute(request).get();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void benchmarkApache5() throws URISyntaxException {
        final TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
                .useSystemProperties()
                .build();
        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(5))
                .build();
        final PoolingAsyncClientConnectionManager cm = PoolingAsyncClientConnectionManagerBuilder.create()
                .setTlsStrategy(tlsStrategy)
                .build();
        final CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)
                .setConnectionManager(cm)
                .build();
        client.start();
        final SimpleHttpRequest request = SimpleRequestBuilder.get()
                .setUri(url.toURI())
                .build();

        benchmark("Apache HTTPComponents Client 5", () -> {
            try {
                final Throwable[] t = new Throwable[]{null};
                client.execute(
                        SimpleRequestProducer.create(request), SimpleResponseConsumer.create(), new FutureCallback<SimpleHttpResponse>() {
                            @Override
                            public void completed(SimpleHttpResponse result) {
                            }

                            @Override
                            public void failed(Exception ex) {
                                t[0] = ex;
                            }

                            @Override
                            public void cancelled() {

                            }
                        })
                        .get();
                if (t[0] != null) throw t[0];
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void benchmarkAsyncHttpClient() {
        AsyncHttpClient client = new DefaultAsyncHttpClient();
        Request request = new RequestBuilder()
                .setUrl(url.toString())
                .setMethod("GET")
                .build();
        benchmark("AsyncHttpClient", () -> {
            try {
                client.executeRequest(request).get();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void benchmark(String name, Runnable runnable) {
        long totalNano = 0, worstNano = 0, bestNano = Long.MAX_VALUE;
        long[] results = new long[TIMES];
        Throwable exSample = null;
        int failed = 0;
        for (int i = 0; i < TIMES; i++) {
            long start = System.nanoTime();
            try {
                runnable.run();
            } catch (Throwable t) {
                if (exSample == null) exSample = t;
                failed++;
                continue;
            }
            long end = System.nanoTime();
            long result = end - start;
            totalNano += result;
            if (result > worstNano) worstNano = result;
            if (result < bestNano) bestNano = result;
            results[i] = result;
        }
        if (failed >= TIMES) {
            StringWriter writer = new StringWriter();
            if (exSample != null) {
                writer.append(" (st: ");
                exSample.printStackTrace(new PrintWriter(writer));
            }
            System.err.println("Benchmark " + name + " failed completely" + writer);
        } else {
            double times = TIMES;
            double failedPercentage = failed / times;
            double
                    average = totalNano / times / 1000000D,
                    worst = worstNano / 1000000D,
                    best = bestNano / 1000000D;
            put(name, average, worst, best);
        }
    }

    private static void put(String benchmark, double average, double worst, double best) {
        System.out.println("Benchmark " + benchmark + ": AVG " + average + ", WORST " + worst + ", BEST " + best);
    }
}
