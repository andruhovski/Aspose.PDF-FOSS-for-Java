package org.aspose.pdf.engine.filter;

import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * RunLengthDecode filter (§7.4.5, ISO 32000-1:2008).
 * <p>
 * Simple run-length encoding. A length byte controls interpretation:
 * <ul>
 *   <li>0–127: copy the next (length + 1) bytes literally</li>
 *   <li>129–255: repeat the next byte (257 − length) times</li>
 *   <li>128: end of data (EOD)</li>
 * </ul>
 * </p>
 */
public final class RunLengthFilter implements COSFilter {

    private static final Logger LOG = Logger.getLogger(RunLengthFilter.class.getName());

    private static final int EOD = 128;

    /**
     * Creates a RunLengthFilter instance.
     */
    public RunLengthFilter() {
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

        ByteArrayOutputStream out = new ByteArrayOutputStream(encoded.length * 2);
        int pos = 0;

        while (pos < encoded.length) {
            int length = encoded[pos++] & 0xFF;

            if (length < EOD) {
                // Literal run: copy next (length + 1) bytes
                int count = length + 1;
                if (pos + count > encoded.length) {
                    throw new IOException("RunLengthDecode: insufficient data for literal run of " +
                            count + " at position " + (pos - 1));
                }
                out.write(encoded, pos, count);
                pos += count;
            } else if (length > EOD) {
                // Repeat run: repeat next byte (257 - length) times
                if (pos >= encoded.length) {
                    throw new IOException("RunLengthDecode: missing byte for repeat run at position " + (pos - 1));
                }
                int count = 257 - length;
                byte val = encoded[pos++];
                for (int i = 0; i < count; i++) {
                    out.write(val);
                }
            } else {
                // EOD (128)
                break;
            }
        }

        LOG.fine(() -> "RunLengthDecode: " + encoded.length + " bytes → " + out.size() + " bytes");
        return out.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] encode(byte[] decoded, COSDictionary params) throws IOException {
        if (decoded == null || decoded.length == 0) {
            return new byte[]{(byte) EOD};
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(decoded.length + decoded.length / 128 + 2);
        int pos = 0;

        while (pos < decoded.length) {
            // Look ahead for a run of identical bytes
            int runStart = pos;
            byte current = decoded[pos];
            int runLen = 1;
            while (pos + runLen < decoded.length && decoded[pos + runLen] == current && runLen < 128) {
                runLen++;
            }

            if (runLen >= 3) {
                // Encode as repeat run
                out.write(257 - runLen);
                out.write(current);
                pos += runLen;
            } else {
                // Encode as literal run — gather non-repeating bytes
                int litStart = pos;
                int litLen = 0;
                while (pos + litLen < decoded.length && litLen < 128) {
                    // Check if a run of 3+ identical bytes starts here
                    if (pos + litLen + 2 < decoded.length &&
                            decoded[pos + litLen] == decoded[pos + litLen + 1] &&
                            decoded[pos + litLen] == decoded[pos + litLen + 2]) {
                        break;
                    }
                    litLen++;
                }
                if (litLen == 0) {
                    litLen = 1; // At least one byte
                }
                out.write(litLen - 1);
                out.write(decoded, litStart, litLen);
                pos += litLen;
            }
        }

        out.write(EOD);

        LOG.fine(() -> "RunLengthEncode: " + decoded.length + " bytes → " + out.size() + " bytes");
        return out.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public COSName getName() {
        return COSName.RUN_LENGTH_DECODE;
    }
}
