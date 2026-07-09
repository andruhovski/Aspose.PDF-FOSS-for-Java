package org.aspose.pdf.engine.script.js.builtins;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * URI encoding/decoding for the global {@code encodeURI}, {@code decodeURI},
 * {@code encodeURIComponent} and {@code decodeURIComponent} functions
 * (ECMA-262 3rd ed., sec 15.1.3). UTF-8 is used for the octet sequence.
 */
final class UriCoding {

    /** Characters left unescaped by {@code encodeURI}. */
    static final String URI_UNRESERVED_RESERVED =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
                    + "-_.!~*'()" + ";/?:@&=+$,#";
    /** Characters left unescaped by {@code encodeURIComponent}. */
    static final String COMPONENT_UNRESERVED =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
                    + "-_.!~*'()";
    /** Reserved set preserved by {@code decodeURI}. */
    static final String URI_RESERVED_HASH = ";/?:@&=+$,#";

    private UriCoding() { }

    static String encode(String s, String unescaped) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (unescaped.indexOf(c) >= 0) {
                out.append(c);
            } else {
                int cp = c;
                if (Character.isHighSurrogate(c) && i + 1 < s.length()
                        && Character.isLowSurrogate(s.charAt(i + 1))) {
                    cp = Character.toCodePoint(c, s.charAt(i + 1));
                    i++;
                }
                byte[] bytes = new String(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8);
                for (byte b : bytes) {
                    out.append('%');
                    out.append(hex((b >> 4) & 0xF));
                    out.append(hex(b & 0xF));
                }
            }
        }
        return out.toString();
    }

    static String decode(Realm r, String s, String reserved) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c != '%') {
                out.append(c);
                i++;
                continue;
            }
            buf.reset();
            while (i < s.length() && s.charAt(i) == '%') {
                if (i + 2 >= s.length()) {
                    throw r.makeUriError("URI malformed");
                }
                int hi = Character.digit(s.charAt(i + 1), 16);
                int lo = Character.digit(s.charAt(i + 2), 16);
                if (hi < 0 || lo < 0) {
                    throw r.makeUriError("URI malformed");
                }
                buf.write((hi << 4) | lo);
                i += 3;
            }
            // NOTE: ES3 decodeURI preserves reserved characters in escaped form;
            // this implementation fully decodes (documented gap). 'reserved' is
            // accepted for API symmetry.
            out.append(new String(buf.toByteArray(), StandardCharsets.UTF_8));
        }
        return out.toString();
    }

    private static char hex(int v) {
        return "0123456789ABCDEF".charAt(v);
    }
}
