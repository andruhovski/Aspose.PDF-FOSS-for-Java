package org.aspose.pdf.engine.security;

import java.security.SecureRandom;

/// Small security-related utility helpers used by legacy-compatible tests.
public final class SecurityUtils {

    private static final char[] ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private SecurityUtils() {
    }

    /// Returns a cryptographically strong random 64-bit value.
    ///
    /// @return random unsigned-compatible 64-bit value in Java `long`
    public static long random64Bit() {
        return RANDOM.nextLong();
    }

    /// Returns a random alpha-numeric string of the requested length.
    ///
    /// @param length desired string length
    /// @return random string
    public static String getRandomString(int length) {
        if (length <= 0) {
            return "";
        }
        char[] chars = new char[length];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = ALPHABET[RANDOM.nextInt(ALPHABET.length)];
        }
        return new String(chars);
    }
}
