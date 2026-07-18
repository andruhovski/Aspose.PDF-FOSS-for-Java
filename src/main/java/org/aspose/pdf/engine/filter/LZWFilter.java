package org.aspose.pdf.engine.filter;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/// LZWDecode filter (§7.4.4.2, ISO 32000-1:2008).
///
/// Decompresses LZW-encoded data as found in older PDF files (pre-1.4).
/// Uses variable-width codes (9–12 bits), MSB-first bit ordering, with
/// clear-table code 256 and EOD code 257.
///
/// Encoding is not supported — modern PDFs should use FlateDecode instead.
///
public final class LZWFilter implements PdfFilter {

    private static final Logger LOG = Logger.getLogger(LZWFilter.class.getName());

    private static final int CLEAR_TABLE = 256;
    private static final int EOD = 257;
    private static final int INITIAL_CODE_SIZE = 9;
    private static final int MAX_CODE_SIZE = 12;
    private static final int MAX_TABLE_SIZE = 1 << MAX_CODE_SIZE; // 4096

    /// Creates an LZWFilter instance.
    public LZWFilter() {
        // Stateless
    }

    /// {@inheritDoc}
    ///
    /// Decodes LZW-compressed data per the PDF specification.
    ///
    @Override
    public byte[] decode(byte[] encoded, PdfDictionary params) throws IOException {
        if (encoded == null || encoded.length == 0) {
            return new byte[0];
        }

        // Determine early change behavior (default true for PDF)
        boolean earlyChange = true;
        if (params != null) {
            earlyChange = params.getInt(PdfName.of("EarlyChange"), 1) != 0;
        }

        // encoded.length * 3 overflows int for raw streams over ~715 MB and
        // pre-allocates large buffers for big-but-legal streams; clamp the
        // initial capacity — BAOS doubles on demand anyway.
        ByteArrayOutputStream out = new ByteArrayOutputStream(
                (int) Math.min(Math.max(encoded.length * 3L, 64), 1L << 20));
        BitReader bits = new BitReader(encoded);

        // Initialize table
        byte[][] table = new byte[MAX_TABLE_SIZE][];
        int tableSize;
        int codeSize;

        // Reset table
        for (int i = 0; i < 256; i++) {
            table[i] = new byte[]{(byte) i};
        }
        table[CLEAR_TABLE] = null; // marker
        table[EOD] = null; // marker
        tableSize = 258;
        codeSize = INITIAL_CODE_SIZE;

        // Read first code — must be CLEAR_TABLE per spec
        int code = bits.readBits(codeSize);
        if (code != CLEAR_TABLE) {
            // Some encoders skip the initial CLEAR, treat as data
            if (code >= 0 && code < 256) {
                out.write(table[code], 0, table[code].length);
            }
        } else {
            // Read actual first data code
            code = bits.readBits(codeSize);
            if (code == EOD || code < 0) {
                return applyPredictor(out.toByteArray(), params);
            }
            if (code < 256) {
                out.write(table[code], 0, table[code].length);
            }
        }

        int oldCode = code;
        long limit = DecodeLimits.maxDecodedBytes();

        while (true) {
            DecodeLimits.check(out.size(), limit, "LZWDecode");
            code = bits.readBits(codeSize);
            if (code < 0 || code == EOD) {
                break;
            }

            if (code == CLEAR_TABLE) {
                // Reset
                tableSize = 258;
                codeSize = INITIAL_CODE_SIZE;

                code = bits.readBits(codeSize);
                if (code < 0 || code == EOD) {
                    break;
                }
                if (code < 256) {
                    out.write(table[code], 0, table[code].length);
                }
                oldCode = code;
                continue;
            }

            // Defend against corrupt LZW streams: a bad code can reference an
            // uninitialised table slot (e.g. oldCode pointing at the reserved
            // CLEAR/EOD positions). Return what we have decoded so far instead of
            // throwing NPE — mirrors FlateFilter's partial-recovery behaviour.
            if (oldCode < 0 || oldCode >= MAX_TABLE_SIZE || table[oldCode] == null
                    || (code < tableSize && table[code] == null)) {
                LOG.warning("LZWDecode: corrupt stream (invalid back-reference oldCode="
                        + oldCode + ", code=" + code + "); returning "
                        + out.size() + " bytes decoded so far");
                return applyPredictor(out.toByteArray(), params);
            }

            byte[] entry;
            if (code < tableSize) {
                entry = table[code];
            } else if (code == tableSize) {
                // Special case: code not yet in table
                byte[] oldEntry = table[oldCode];
                entry = new byte[oldEntry.length + 1];
                System.arraycopy(oldEntry, 0, entry, 0, oldEntry.length);
                entry[oldEntry.length] = oldEntry[0];
            } else {
                throw new IOException("LZWDecode: invalid code " + code + " (table size=" + tableSize + ")");
            }

            out.write(entry, 0, entry.length);

            // Add new entry to table
            if (tableSize < MAX_TABLE_SIZE) {
                byte[] oldEntry = table[oldCode];
                byte[] newEntry = new byte[oldEntry.length + 1];
                System.arraycopy(oldEntry, 0, newEntry, 0, oldEntry.length);
                newEntry[oldEntry.length] = entry[0];
                table[tableSize] = newEntry;
                tableSize++;

                // Increase code size when needed
                int threshold = earlyChange ? (1 << codeSize) - 1 : (1 << codeSize);
                if (tableSize >= threshold && codeSize < MAX_CODE_SIZE) {
                    codeSize++;
                }
            }

            oldCode = code;
        }

        byte[] decoded = out.toByteArray();
        byte[] result = applyPredictor(decoded, params);

        LOG.fine(() -> "LZWDecode: " + encoded.length + " bytes → " + result.length + " bytes");
        return result;
    }

    /// {@inheritDoc}
    ///
    /// LZW encoding is not supported. Use FlateDecode for new PDF streams.
    ///
    /// @throws UnsupportedOperationException always
    @Override
    public byte[] encode(byte[] decoded, PdfDictionary params) throws IOException {
        throw new IOException("LZW encoding not supported. Use FlateDecode.");
    }

    /// {@inheritDoc}
    @Override
    public PdfName getName() {
        return PdfName.LZW_DECODE;
    }

    private static byte[] applyPredictor(byte[] data, PdfDictionary params) {
        if (params == null) {
            return data;
        }
        int predictor = params.getInt(PdfName.of("Predictor"), 1);
        if (predictor <= 1) {
            return data;
        }
        int columns = params.getInt(PdfName.of("Columns"), 1);
        int colors = params.getInt(PdfName.of("Colors"), 1);
        int bpc = params.getInt(PdfName.of("BitsPerComponent"), 8);
        return PredictorDecoder.decode(data, predictor, columns, colors, bpc);
    }

    /// MSB-first bit reader for LZW decoding.
    private static final class BitReader {
        private final byte[] data;
        private int bytePos;
        private int bitPos; // bits remaining in current byte (8..1)

        BitReader(byte[] data) {
            this.data = data;
            this.bytePos = 0;
            this.bitPos = 8;
        }

        /// Reads the specified number of bits as an integer value.
        ///
        /// @param numBits number of bits to read (1–12)
        /// @return the value, or -1 if not enough data
        int readBits(int numBits) {
            int result = 0;
            int bitsNeeded = numBits;

            while (bitsNeeded > 0) {
                if (bytePos >= data.length) {
                    return -1; // EOF
                }

                int bitsAvailable = bitPos;
                int bitsToRead = Math.min(bitsNeeded, bitsAvailable);

                // Extract bits from current byte (MSB first)
                int shift = bitsAvailable - bitsToRead;
                int mask = ((1 << bitsToRead) - 1) << shift;
                int bits = (data[bytePos] & mask) >>> shift;

                result = (result << bitsToRead) | bits;
                bitsNeeded -= bitsToRead;
                bitPos -= bitsToRead;

                if (bitPos == 0) {
                    bytePos++;
                    bitPos = 8;
                }
            }

            return result;
        }
    }
}
