package me.tecc.lahc.tests;

import me.tecc.lahc.util.MimeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MimeTypeTest {
    @Test
    void simple() {
        MimeType mt = MimeType.parse("application/json");
        assertEquals(mt.getType(), "application/json");
        assertNull(mt.getCharset());
    }

    @Test
    void withCharset() {
        MimeType mt = MimeType.parse("application/json;      charset=utf-8");
        assertEquals(mt.getType(), "application/json");
        assertEquals(mt.getCharset(), "utf-8");
    }
}
