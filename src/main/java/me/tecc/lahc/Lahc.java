package me.tecc.lahc;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Lahc {
    public static final String VERSION = "${project.version}";

    /**
     * Gets the string that should be used for the {@code User-Agent} header by default.
     * @return The {@code User-Agent} header.
     */
    @Contract(pure = true)
    @NotNull
    public static String getUserAgentValue() {
        return "Lahc/" + VERSION + " (" + System.getProperty("os.name") + " " + System.getProperty("os.arch") + ")";
    }
}
