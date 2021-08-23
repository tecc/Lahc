package me.tecc.lahc.tests;

import me.tecc.lahc.HttpClient;
import me.tecc.lahc.http.HttpRequest;
import me.tecc.lahc.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;

public class GeneralTest {
    HttpClient client = new HttpClient();
    Logger logger = LoggerFactory.getLogger(GeneralTest.class);

    @Test
    void text() {
        HttpRequest request = new HttpRequest()
                .url("https://example.com/index.html");

        Future<HttpResponse> responseFuture = client.execute(request);
        Assertions.assertDoesNotThrow(() -> {
            HttpResponse response = responseFuture.get();
            Assertions.assertTrue(response.isSuccessful(), "Response is not successful: " + response.getStatus());
        });
    }
}
