package org.aspose.pdf.engine.filter;

import java.io.IOException;

/// Central guard against decompression bombs in stream decode filters
/// (FlateDecode, LZWDecode, RunLengthDecode).
///
/// A corrupt or malicious stream can legitimately-looking expand thousands of
/// times (Flate >1000:1, RunLength up to 64:1, LZW similar). Decoding such a
/// stream into an unbounded `ByteArrayOutputStream` ends in
/// [OutOfMemoryError], which poisons the whole JVM — every other thread
/// sharing the heap starts failing too (observed in mass corpus runs: one
/// bomb page produced hundreds of cascading OOMs in unrelated files).
///
/// The limit is deliberately generous: legitimate single streams (even raw
/// 40-megapixel RGB images at \~120 MB) stay far below the default 256 MB.
/// Override with the system property `aspose.pdf.maxDecodedStreamBytes`
/// (bytes; values <= 0 disable the guard).
///
public final class DecodeLimits {

    /// System property that overrides the decoded-stream size cap (bytes).
    public static final String PROPERTY = "aspose.pdf.maxDecodedStreamBytes";

    /// Default cap on a single decoded stream: 256 MB.
    public static final long DEFAULT_MAX_DECODED_BYTES = 256L << 20;

    private DecodeLimits() {
    }

    /// Returns the current cap on a single decoded stream in bytes,
    /// or [Long#MAX\_VALUE] when the guard is disabled.
    /// Read dynamically so tests (and embedders) can adjust it at runtime.
    ///
    /// @return the cap in bytes
    public static long maxDecodedBytes() {
        String v = System.getProperty(PROPERTY);
        if (v == null || v.isEmpty()) {
            return DEFAULT_MAX_DECODED_BYTES;
        }
        try {
            long parsed = Long.parseLong(v.trim());
            return parsed <= 0 ? Long.MAX_VALUE : parsed;
        } catch (NumberFormatException e) {
            return DEFAULT_MAX_DECODED_BYTES;
        }
    }

    /// Throws when `decodedSoFar` exceeds the cap.
    ///
    /// @param decodedSoFar bytes produced by the filter so far
    /// @param limit        the cap obtained from [#maxDecodedBytes()] once
    ///                     per decode call (avoids re-parsing in tight loops)
    /// @param filterName   filter name for the error message
    /// @throws IOException when the cap is exceeded
    public static void check(long decodedSoFar, long limit, String filterName) throws IOException {
        if (decodedSoFar > limit) {
            throw new DecodeSizeLimitException(filterName + ": decoded output exceeds "
                    + limit + " bytes - likely a corrupt stream or decompression bomb"
                    + " (override with -D" + PROPERTY + ")");
        }
    }
}
