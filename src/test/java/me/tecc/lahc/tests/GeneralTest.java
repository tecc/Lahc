package me.tecc.lahc.tests;

import me.tecc.lahc.HttpClient;
import me.tecc.lahc.HttpRequest;
import me.tecc.lahc.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class GeneralTest {
    HttpClient client = new HttpClient();
    Logger logger = LoggerFactory.getLogger(GeneralTest.class);

    @Test
    void text() {
        HttpRequest request = new HttpRequest()
                .setURL("https://status.mojang.com/check");
        Future<HttpResponse> responseFuture = client.execute(request);
        try {
            HttpResponse response = responseFuture.get();
            logger.info(() -> "Status: " + response.getStatus().toString());
            logger.info(() -> "Content type: " + response.getHeader("Content-Type"));
            logger.info(() -> "Body: " + response.getBodyString());
        } catch (CompletionException e) {
            logger.error(e.getCause(), () -> "Error in completion");
            throw new RuntimeException(e);
        } catch (Throwable e) {
            logger.error(e, () -> "Other error");
            throw new RuntimeException(e);
        }
    }
}
