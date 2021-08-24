/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc.tests;

import me.tecc.lahc.HttpClient;
import me.tecc.lahc.http.HttpRequest;
import me.tecc.lahc.http.HttpResponse;
import org.asynchttpclient.Request;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class GeneralTest {
    HttpClient client = new HttpClient();
    Logger logger = LoggerFactory.getLogger(GeneralTest.class);

    @Test
    void google() {
        HttpRequest request = new HttpRequest()
                .url("http://google.com");
        dr(request);
    }
    @Test
    void example() {
        HttpRequest request = new HttpRequest()
                .url("http://example.com/index.html");
        dr(request);
    }

    void dr(HttpRequest request) {
        Future<HttpResponse> responseFuture = client.execute(request);
        Assertions.assertDoesNotThrow(() -> {
            HttpResponse response;
            try {
                response = responseFuture.get();
            } catch (CompletionException | ExecutionException e) {
                if (e.getCause() != null) throw e.getCause();
                else throw e;
            }
            Assertions.assertTrue(response.isSuccessful(), "Response is not successful: " + response.getStatus());
            logger.info(() -> "Raw response of unsecure: \n" + new String(response.getRawResponse()));
        });
        Future<HttpResponse> responseFutureSecure = client.execute(request.makeSecure());
        Assertions.assertDoesNotThrow(() -> {
            HttpResponse response;
            try {
                response = responseFutureSecure.get();
            } catch (CompletionException | ExecutionException e) {
                if (e.getCause() != null) throw e.getCause();
                else throw e;
            }
            Assertions.assertTrue(response.isSuccessful(), "Response is not successful: \n" + response);
            logger.info(response::toString);
        });
    }
}
