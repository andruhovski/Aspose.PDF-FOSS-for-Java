package org.aspose.pdf.engine.filter;

import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * FlateDecode filter (§7.4.4, ISO 32000-1:2008).
 * <p>
 * Compresses and decompresses data using the Deflate algorithm (RFC 1951).
 * This is the most commonly used filter in PDF — approximately 90% of all
 * streams use FlateDecode. Supports optional PNG/TIFF predictors via
 * {@link PredictorDecoder}.
 * </p>
 */
public final class FlateFilter implements COSFilter {

    private static final Logger LOG = Logger.getLogger(FlateFilter.class.getName());

    private static final int BUFFER_SIZE = 8192;

    /**
     * Creates a FlateFilter instance.
     */
    public FlateFilter() {
        // Stateless
    }

    /**
     * {@inheritDoc}
     * <p>
     * Decompresses Flate-encoded data. If the data lacks a zlib header (raw deflate),
     * a fallback with {@code nowrap=true} is attempted automatically.
     * </p>
     */
    @Override
    public byte[] decode(byte[] encoded, COSDictionary params) throws IOException {
        if (encoded == null || encoded.length == 0) {
            return new byte[0];
        }

        byte[] inflated;
        try {
            inflated = inflate(encoded, false);
        } catch (IOException e) {
            // Fallback: some PDFs use raw deflate without zlib header
            LOG.fine("Flate decode failed with zlib header, retrying with nowrap=true");
            try {
                inflated = inflate(encoded, true);
            } catch (IOException e2) {
                byte[] salvaged = trySalvageInflate(encoded);
                if (salvaged != null) {
                    inflated = salvaged;
                } else {
                    throw new IOException("FlateDecode failed: " + e.getMessage(), e);
                }
            }
        }

        // Apply predictor if specified
        byte[] result = applyPredictorDecode(inflated, params);

        final int inLen = encoded.length;
        final int outLen = result.length;
        LOG.fine(() -> "FlateDecode: " + inLen + " bytes -> " + outLen + " bytes");
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] encode(byte[] decoded, COSDictionary params) throws IOException {
        if (decoded == null || decoded.length == 0) {
            return new byte[0];
        }

        // Apply predictor encoding if specified
        byte[] toCompress = applyPredictorEncode(decoded, params);

        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
        try {
            deflater.setInput(toCompress);
            deflater.finish();

            ByteArrayOutputStream out = new ByteArrayOutputStream(toCompress.length);
            byte[] buf = new byte[BUFFER_SIZE];
            while (!deflater.finished()) {
                int n = deflater.deflate(buf);
                out.write(buf, 0, n);
            }

            LOG.fine(() -> "FlateEncode: " + decoded.length + " bytes → " + out.size() + " bytes");
            return out.toByteArray();
        } finally {
            deflater.end();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public COSName getName() {
        return COSName.FLATE_DECODE;
    }

    // ---- Internal ----

    private static byte[] inflate(byte[] data, boolean nowrap) throws IOException {
        Inflater inflater = new Inflater(nowrap);
        try {
            inflater.setInput(data);
            ByteArrayOutputStream out = new ByteArrayOutputStream(data.length * 2);
            byte[] buf = new byte[BUFFER_SIZE];
            while (!inflater.finished()) {
                try {
                    int n = inflater.inflate(buf);
                    if (n == 0) {
                        if (inflater.needsInput()) {
                            break;
                        }
                        if (inflater.needsDictionary()) {
                            throw new IOException("FlateDecode: preset dictionary required but not supported");
                        }
                    }
                    out.write(buf, 0, n);
                } catch (DataFormatException e) {
                    // If we already decompressed some data, return what we have
                    // (some PDFs have truncated or corrupt streams but partial data is usable)
                    if (out.size() > 0) {
                        LOG.warning("FlateDecode: partial decompression (" + out.size()
                                + " bytes recovered) due to: " + e.getMessage());
                        return out.toByteArray();
                    }
                    throw new IOException("FlateDecode: invalid deflate data: " + e.getMessage(), e);
                }
            }
            return out.toByteArray();
        } finally {
            inflater.end();
        }
    }

    private static byte[] trySalvageInflate(byte[] data) {
        if (data == null || data.length < 32) {
            return null;
        }
        byte[] best = null;
        int minLength = Math.max(32, data.length - 65536);

        for (boolean nowrap : new boolean[] {false, true}) {
            for (int trim = 1; trim <= 4096 && data.length - trim >= minLength; trim *= 2) {
                byte[] candidate = tryInflatePrefix(data, data.length - trim, nowrap);
                if (candidate != null && candidate.length > 0
                        && (best == null || candidate.length > best.length)) {
                    best = candidate;
                }
            }
            if (best == null) {
                for (int len = data.length - 1; len >= minLength; len -= 256) {
                    byte[] candidate = tryInflatePrefix(data, len, nowrap);
                    if (candidate != null && candidate.length > 0
                            && (best == null || candidate.length > best.length)) {
                        best = candidate;
                    }
                }
            }
        }

        if (best != null) {
            LOG.warning("FlateDecode: salvaged " + best.length
                    + " bytes by truncating corrupt stream tail");
        }
        return best;
    }

    private static byte[] tryInflatePrefix(byte[] data, int length, boolean nowrap) {
        if (length <= 0 || length > data.length) {
            return null;
        }
        byte[] prefix = new byte[length];
        System.arraycopy(data, 0, prefix, 0, length);
        try {
            return inflate(prefix, nowrap);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static byte[] applyPredictorDecode(byte[] data, COSDictionary params) {
        if (params == null) {
            return data;
        }
        int predictor = params.getInt(COSName.of("Predictor"), 1);
        if (predictor <= 1) {
            return data;
        }
        int columns = params.getInt(COSName.of("Columns"), 1);
        int colors = params.getInt(COSName.of("Colors"), 1);
        int bpc = params.getInt(COSName.of("BitsPerComponent"), 8);
        return PredictorDecoder.decode(data, predictor, columns, colors, bpc);
    }

    private static byte[] applyPredictorEncode(byte[] data, COSDictionary params) {
        if (params == null) {
            return data;
        }
        int predictor = params.getInt(COSName.of("Predictor"), 1);
        if (predictor <= 1) {
            return data;
        }
        int columns = params.getInt(COSName.of("Columns"), 1);
        int colors = params.getInt(COSName.of("Colors"), 1);
        int bpc = params.getInt(COSName.of("BitsPerComponent"), 8);
        return PredictorDecoder.encode(data, predictor, columns, colors, bpc);
    }
}
