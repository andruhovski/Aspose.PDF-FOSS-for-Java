package org.aspose.pdf.engine.filter;

import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * ASCII85Decode filter (§7.4.3, ISO 32000-1:2008).
 * <p>
 * Encodes groups of 4 bytes as 5 ASCII characters in the range '!' (33) to 'u' (117).
 * A group of 4 zero bytes is encoded as the single character 'z'. The end-of-data
 * marker is {@code ~>}.
 * </p>
 */
public final class ASCII85Filter implements COSFilter {

    private static final Logger LOG = Logger.getLogger(ASCII85Filter.class.getName());

    private static final long[] POW85 = {85L * 85 * 85 * 85, 85L * 85 * 85, 85L * 85, 85, 1};

    /**
     * Creates an ASCII85Filter instance.
     */
    public ASCII85Filter() {
        // Stateless
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] decode(byte[] encoded, COSDictionary params) throws IOException {
        if (encoded == null || encoded.length == 0) {
            return new byte[0];
        }

        // Strip whitespace and find EOD
        ByteArrayOutputStream stripped = new ByteArrayOutputStream(encoded.length);
        for (int i = 0; i < encoded.length; i++) {
            byte b = encoded[i];
            char ch = (char) (b & 0xFF);

            // Check for EOD marker ~>
            if (ch == '~' && i + 1 < encoded.length && (encoded[i + 1] & 0xFF) == '>') {
                break;
            }

            // Skip whitespace
            if (ch == 0x00 || ch == 0x09 || ch == 0x0A || ch == 0x0C || ch == 0x0D || ch == 0x20) {
                continue;
            }

            stripped.write(b);
        }

        byte[] clean = stripped.toByteArray();
        ByteArrayOutputStream out = new ByteArrayOutputStream(clean.length * 4 / 5 + 4);

        int i = 0;
        while (i < clean.length) {
            char ch = (char) (clean[i] & 0xFF);

            // Special case: 'z' = four zero bytes
            if (ch == 'z') {
                out.write(0);
                out.write(0);
                out.write(0);
                out.write(0);
                i++;
                continue;
            }

            // Determine group size
            int groupLen = Math.min(5, clean.length - i);
            if (groupLen < 2) {
                throw new IOException("ASCII85Decode: incomplete group of size " + groupLen);
            }

            // Decode group
            long value = 0;
            int outputBytes;
            if (groupLen == 5) {
                // Full group
                for (int j = 0; j < 5; j++) {
                    int c = (clean[i + j] & 0xFF) - 33;
                    if (c < 0 || c > 84) {
                        throw new IOException("ASCII85Decode: invalid character '" +
                                (char) (clean[i + j] & 0xFF) + "' at position " + (i + j));
                    }
                    value += c * POW85[j];
                }
                outputBytes = 4;
            } else {
                // Partial group: pad with 'u' (84)
                for (int j = 0; j < 5; j++) {
                    int c;
                    if (j < groupLen) {
                        c = (clean[i + j] & 0xFF) - 33;
                        if (c < 0 || c > 84) {
                            throw new IOException("ASCII85Decode: invalid character '" +
                                    (char) (clean[i + j] & 0xFF) + "' at position " + (i + j));
                        }
                    } else {
                        c = 84; // 'u' - 33
                    }
                    value += c * POW85[j];
                }
                outputBytes = groupLen - 1;
            }

            // Extract bytes (big-endian)
            for (int j = 0; j < outputBytes; j++) {
                out.write((int) ((value >> (24 - j * 8)) & 0xFF));
            }

            i += groupLen;
        }

        LOG.fine(() -> "ASCII85Decode: " + encoded.length + " bytes → " + out.size() + " bytes");
        return out.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] encode(byte[] decoded, COSDictionary params) throws IOException {
        if (decoded == null || decoded.length == 0) {
            return "~>".getBytes();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(decoded.length * 5 / 4 + 10);

        int i = 0;
        while (i + 4 <= decoded.length) {
            long value = ((decoded[i] & 0xFFL) << 24) |
                    ((decoded[i + 1] & 0xFFL) << 16) |
                    ((decoded[i + 2] & 0xFFL) << 8) |
                    (decoded[i + 3] & 0xFFL);

            if (value == 0) {
                out.write('z');
            } else {
                for (int j = 0; j < 5; j++) {
                    out.write((int) (value / POW85[j] % 85 + 33));
                }
            }
            i += 4;
        }

        // Handle remaining bytes (1-3)
        int remaining = decoded.length - i;
        if (remaining > 0) {
            long value = 0;
            for (int j = 0; j < remaining; j++) {
                value |= (decoded[i + j] & 0xFFL) << (24 - j * 8);
            }
            for (int j = 0; j < remaining + 1; j++) {
                out.write((int) (value / POW85[j] % 85 + 33));
            }
        }

        // EOD marker
        out.write('~');
        out.write('>');

        LOG.fine(() -> "ASCII85Encode: " + decoded.length + " bytes → " + out.size() + " bytes");
        return out.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public COSName getName() {
        return COSName.ASCII85_DECODE;
    }
}
