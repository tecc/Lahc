/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.tests;

import me.tecc.lahc.HttpClient;
import me.tecc.lahc.http.HttpRequest;
import me.tecc.lahc.http.HttpResponse;
import me.tecc.lahc.tests.connectors.DebugConnector;
import me.tecc.lahc.util.Promise;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class GeneralTest {
    HttpClient client;
    Logger logger = LoggerFactory.getLogger(GeneralTest.class);
    public static final boolean useDebugConnector = false;

    {
        HttpClient.Options opts = new HttpClient.Options();
        if (useDebugConnector) {
            opts.setConnector(new DebugConnector());
        }
        this.client = new HttpClient(opts);
    }

    @Test
    void google() {
        logger.info("google"::toString);
        HttpRequest request = new HttpRequest()
                .url("http://google.com");
        dr(request, true);
    }
    @Test
    void example() {
        logger.info("example"::toString);
        HttpRequest request = new HttpRequest()
                .url("http://example.com/index.html");
        dr(request, true);
    }

    @Test
    void twoConsecutive() {
        logger.info("twoConsecutive"::toString);
        HttpRequest request = new HttpRequest()
                .url("http://httpbin.org/get");
        dr(request, false);
        dr(request, false);
    }

    void dr(HttpRequest request, boolean makeSecure) {

        Promise<HttpResponse> responseFuture = client.execute(request);
        AtomicReference<HttpResponse> response = new AtomicReference<>();
        Assertions.assertDoesNotThrow(() -> {
            try {
                response.set(responseFuture.get());
            } catch (CompletionException | ExecutionException e) {
                if (e.getCause() != null) throw e.getCause();
                else throw e;
            }
            HttpResponse res = response.get();
            String raw = new String(res.getRawResponse());
            logger.info(() -> "Raw response of unsecure: \n" + raw);
            logger.info(() -> "Last 4 bytes: " + Arrays.toString(raw.substring(raw.length() - 4).getBytes()));
        });
        Assertions.assertTrue(response.get().isSuccessful(), "Response is not successful: " + response.get().getStatus());
        if (!makeSecure) return;
        Promise<HttpResponse> responseFutureSecure = client.execute(request.makeSecure());
        AtomicReference<HttpResponse> responseSecure = new AtomicReference<>();
        Assertions.assertDoesNotThrow(() -> {
            try {
                responseSecure.set(responseFutureSecure.get());
            } catch (CompletionException | ExecutionException e) {
                if (e.getCause() != null) throw e.getCause();
                else throw e;
            }
        });
        HttpResponse res = responseSecure.get();
        String raw = new String(res.getRawResponse());
        logger.info(() -> "Raw response of secure:\n" + raw);
        logger.info(() -> "Last 4 bytes: " + Arrays.toString(raw.substring(raw.length() - 4).getBytes()));
        Assertions.assertTrue(res.isSuccessful(), "Secure response is not successful: \n" + raw);
    }
}
