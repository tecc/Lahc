/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.tests;

import me.tecc.lahc.HttpClient;
import me.tecc.lahc.http.HttpRequest;
import me.tecc.lahc.http.HttpResponse;
import me.tecc.lahc.util.Promise;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class GeneralTest {
    HttpClient client = new HttpClient();

    @Test
    void google() {
        HttpRequest request = new HttpRequest()
                .url("http://google.com");
        dr(request, true);
    }
    @Test
    void example() {
        HttpRequest request = new HttpRequest()
                .url("http://example.com/index.html");
        dr(request, true);
    }

    @Test
    void twoConsecutive() {
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
            // log.info(() -> "Raw response of unsecure: \n" + new String(response.get().getRawResponse()));
        });
        Assertions.assertTrue(response.get().isSuccessful(), "Response is not successful: " + response.get().getStatus());
        if (makeSecure) return;
        Promise<HttpResponse> responseFutureSecure = client.execute(request.makeSecure());
        AtomicReference<HttpResponse> responseSecure = new AtomicReference<>();
        Assertions.assertDoesNotThrow(() -> {
            try {
                responseSecure.set(responseFutureSecure.get());
            } catch (CompletionException | ExecutionException e) {
                if (e.getCause() != null) throw e.getCause();
                else throw e;
            }
            // log.info(responseSecure.get()::toString);
        });
        Assertions.assertTrue(responseSecure.get().isSuccessful(), "Secure response is not successful: \n" + responseSecure);
    }
}
