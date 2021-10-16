/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc;

import me.tecc.lahc.http.HttpRequest;
import me.tecc.lahc.http.HttpResponse;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;

public class Lahc {
    public static final String VERSION = "dev";

    /**
     * Gets the string that should be used for the {@code User-Agent} header by default.
     * @return The {@code User-Agent} header.
     */
    @Contract(pure = true)
    @NotNull
    public static String getUserAgentValue() {
        return "Lahc/" + VERSION + " (" + System.getProperty("os.name") + " " + System.getProperty("os.arch") + ")";
    }

    public static void main(String[] args) {
        HttpClient client = new HttpClient();
        long iter = 0;
        while (true) {
            iter++;
            long start = System.nanoTime();
            HttpResponse response = client.executeSync(new HttpRequest().url("https://httpbin.org/get"));
            long end = System.nanoTime();
            long time = end - start;
            double timeSeconds = time * 0.000000001;
            System.out.println(iter + ": " + timeSeconds + " (status: " + response.getStatus() + ")");

        }
    }
}
