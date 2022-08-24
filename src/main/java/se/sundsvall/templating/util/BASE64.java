package se.sundsvall.templating.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class BASE64 {

    private BASE64() { }

    public static String encode(final String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    public static String encode(final byte[] buf) {
        return Base64.getEncoder().encodeToString(buf);

    }
}
