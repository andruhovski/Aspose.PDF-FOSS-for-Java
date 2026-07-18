package org.aspose.pdf.engine.security.sasl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.Objects;

/// Minimal SASLprep implementation used by security-related regression tests.
///
/// The implementation covers mapping to nothing, NFKC normalization, prohibited
/// output checks, and the bidirectional rule from RFC 4013 / RFC 3454.
///
public class Stringprep {

    private final String source;
    private String result;

    /// Creates a processor from a source string.
    ///
    /// @param source source text
    public Stringprep(String source) {
        this.source = Objects.requireNonNull(source, "source");
    }

    /// Creates a processor from a UTF-8 input stream.
    ///
    /// @param stream source stream
    /// @throws IOException if the stream cannot be read
    public Stringprep(InputStream stream) throws IOException {
        this.source = new String(readAllBytes(stream), java.nio.charset.StandardCharsets.UTF_8);
    }

    /// Processes the input according to SASLprep.
    ///
    /// @return normalized result
    public String process() {
        String mapped = mapToNothing(source);
        String normalized = Normalizer.normalize(mapped, Normalizer.Form.NFKC);
        checkProhibited(normalized);
        checkBidirectional(normalized);
        this.result = normalized;
        return normalized;
    }

    /// Returns the last processed result.
    ///
    /// @return the processed result, or `null` if [#process()] was not called
    public String getResult() {
        return result;
    }

    private String mapToNothing(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        input.codePoints().forEach(cp -> {
            if (!isMappedToNothing(cp)) {
                sb.appendCodePoint(cp);
            }
        });
        return sb.toString();
    }

    private boolean isMappedToNothing(int cp) {
        return cp == 0x00AD
                || cp == 0x034F
                || cp == 0x1806
                || cp == 0x180B
                || cp == 0x180C
                || cp == 0x180D
                || cp == 0x200B
                || cp == 0x2060
                || cp == 0xFE00
                || cp == 0xFE01
                || cp == 0xFE02
                || cp == 0xFE03
                || cp == 0xFE04
                || cp == 0xFE05
                || cp == 0xFE06
                || cp == 0xFE07
                || cp == 0xFE08
                || cp == 0xFE09
                || cp == 0xFE0A
                || cp == 0xFE0B
                || cp == 0xFE0C
                || cp == 0xFE0D
                || cp == 0xFE0E
                || cp == 0xFE0F
                || cp == 0xFEFF;
    }

    private void checkProhibited(String input) {
        input.codePoints().forEach(cp -> {
            if (isProhibited(cp)) {
                throw new StringprepException("Prohibited code point: U+" + Integer.toHexString(cp).toUpperCase());
            }
        });
    }

    private boolean isProhibited(int cp) {
        if (cp == 0x0000) {
            return true;
        }
        if (Character.isISOControl(cp) && cp != 0x0020) {
            return true;
        }
        int type = Character.getType(cp);
        if (type == Character.PRIVATE_USE || type == Character.SURROGATE) {
            return true;
        }
        if (type == Character.SPACE_SEPARATOR && cp != 0x0020) {
            return true;
        }
        if (cp == 0x200E || cp == 0x200F) {
            return true;
        }
        if (cp >= 0x202A && cp <= 0x202E) {
            return true;
        }
        if (cp >= 0x206A && cp <= 0x206F) {
            return true;
        }
        if (cp == 0x180E) {
            return true;
        }
        if (cp >= 0x1D173 && cp <= 0x1D17A) {
            return true;
        }
        if (cp >= 0xE0001 && cp <= 0xE007F) {
            return true;
        }
        return false;
    }

    private void checkBidirectional(String input) {
        boolean hasRandAL = false;
        boolean hasL = false;
        int[] cps = input.codePoints().toArray();
        for (int cp : cps) {
            byte dir = Character.getDirectionality(cp);
            if (dir == Character.DIRECTIONALITY_LEFT_TO_RIGHT) {
                hasL = true;
            }
            if (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT
                    || dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC) {
                hasRandAL = true;
            }
        }
        if (!hasRandAL) {
            return;
        }
        if (hasL) {
            throw new StringprepException("Bidirectional check failed");
        }
        if (cps.length == 0) {
            return;
        }
        byte first = Character.getDirectionality(cps[0]);
        byte last = Character.getDirectionality(cps[cps.length - 1]);
        if (!isRandAL(first) || !isRandAL(last)) {
            throw new StringprepException("Bidirectional check failed");
        }
    }

    private boolean isRandAL(byte dir) {
        return dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT
                || dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    private byte[] readAllBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = stream.read(buffer)) >= 0) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }
}
