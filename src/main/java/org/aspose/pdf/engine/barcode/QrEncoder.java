package org.aspose.pdf.engine.barcode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/// A dependency-free QR Code (Model 2) encoder implementing ISO/IEC 18004. Produces the boolean module
/// matrix for a payload at a chosen error-correction level, selecting the smallest fitting version
/// (1–40) and the best data-mask by the standard penalty rules.
///
/// Three encoding modes are supported and auto-selected by content: numeric (digits only),
/// alphanumeric (the 45-character QR set), and byte (UTF-8) for anything else — byte mode alone covers
/// arbitrary binary, the others only tighten density. Reed–Solomon error correction is computed
/// over GF(2<sup>8</sup>) with primitive polynomial `0x11D`; data and ECC codewords are split into
/// the version/level block structure and interleaved per the spec.
///
/// Used by the XFA renderer to paint a `<ui><barcode type="QRCode">` field. The class is pure
/// (`java.*` only) and stateless apart from the working buffers of a single [#encode] call.
public final class QrEncoder {

    /// Error-correction level. Ordinal `L,M,Q,H` = 0..3 indexes the spec block tables.
    public enum Ecc {
        /// \~7% recovery.
        LOW(0b01),
        /// \~15% recovery.
        MEDIUM(0b00),
        /// \~25% recovery.
        QUARTILE(0b11),
        /// \~30% recovery.
        HIGH(0b10);

        /// The 2-bit value this level contributes to the format information.
        final int formatBits;

        Ecc(int formatBits) {
            this.formatBits = formatBits;
        }
    }

    private static final int MIN_VERSION = 1;
    private static final int MAX_VERSION = 40;

    private QrEncoder() {
    }

    /// Encodes `text` as a QR Code at error-correction level `ecc`, auto-selecting the mode.
    ///
    /// @param text the payload (UTF-8 for byte mode)
    /// @param ecc  the error-correction level
    /// @return the module matrix, `matrix[y][x]` = `true` for a dark module
    /// @throws IllegalArgumentException if the payload exceeds version-40 capacity at `ecc`
    public static boolean[][] encode(String text, Ecc ecc) {
        return encode(text.getBytes(StandardCharsets.UTF_8), text, ecc);
    }

    /// Encodes raw `bytes` as a byte-mode QR Code at level `ecc`.
    ///
    /// @param bytes the binary payload
    /// @param ecc   the error-correction level
    /// @return the module matrix (`true` = dark)
    public static boolean[][] encodeBytes(byte[] bytes, Ecc ecc) {
        return encode(bytes, null, ecc);
    }

    /* ------------------------------------------------------------------ encoding */

    private static boolean[][] encode(byte[] bytes, String text, Ecc ecc) {
        Encoded e = buildCodewords(bytes, text, ecc);
        return new Matrix(e.version, ecc, e.codewords).build();
    }

    /// The selected version plus the final (data + ECC, interleaved) codeword stream.
    static final class Encoded {
        final int version;
        final byte[] codewords;

        Encoded(int version, byte[] codewords) {
            this.version = version;
            this.codewords = codewords;
        }
    }

    /// Runs mode/version selection, bit packing, padding and Reed-Solomon — the pre-matrix pipeline.
    static Encoded buildCodewords(byte[] bytes, String text, Ecc ecc) {
        // Choose the densest applicable mode: numeric < alphanumeric < byte.
        Mode mode;
        if (text != null && isNumeric(text)) {
            mode = Mode.NUMERIC;
        } else if (text != null && isAlphanumeric(text)) {
            mode = Mode.ALPHANUMERIC;
        } else {
            mode = Mode.BYTE;
        }
        int charCount = mode == Mode.BYTE ? bytes.length : text.length();

        // Smallest version whose data capacity (in bits) holds mode + count + payload at this level.
        int version = -1;
        for (int v = MIN_VERSION; v <= MAX_VERSION; v++) {
            int capacityBits = numDataCodewords(v, ecc) * 8;
            int needed = 4 + mode.charCountBits(v) + dataBitLength(mode, charCount, bytes, text);
            if (needed <= capacityBits) {
                version = v;
                break;
            }
        }
        if (version < 0) {
            throw new IllegalArgumentException("QR payload too large (" + charCount
                    + " chars) for version 40 at " + ecc);
        }

        BitBuffer bb = new BitBuffer();
        bb.append(mode.indicator, 4);
        bb.append(charCount, mode.charCountBits(version));
        appendData(bb, mode, bytes, text);

        // Terminator (up to four 0 bits), pad to a byte boundary, then alternate pad bytes 0xEC/0x11.
        int capacityBits = numDataCodewords(version, ecc) * 8;
        bb.append(0, Math.min(4, capacityBits - bb.length()));
        bb.append(0, (8 - bb.length() % 8) % 8);
        for (int pad = 0xEC; bb.length() < capacityBits; pad ^= 0xEC ^ 0x11) {
            bb.append(pad, 8);
        }

        return new Encoded(version, addEccAndInterleave(bb.toBytes(), version, ecc));
    }

    private static void appendData(BitBuffer bb, Mode mode, byte[] bytes, String text) {
        switch (mode) {
            case NUMERIC:
                for (int i = 0; i < text.length(); ) {
                    int n = Math.min(3, text.length() - i);
                    int val = Integer.parseInt(text.substring(i, i + n));
                    bb.append(val, n * 3 + 1); // 3 digits=10b, 2=7b, 1=4b
                    i += n;
                }
                break;
            case ALPHANUMERIC:
                for (int i = 0; i < text.length(); i += 2) {
                    if (i + 1 < text.length()) {
                        bb.append(alnum(text.charAt(i)) * 45 + alnum(text.charAt(i + 1)), 11);
                    } else {
                        bb.append(alnum(text.charAt(i)), 6);
                    }
                }
                break;
            default:
                for (byte b : bytes) {
                    bb.append(b & 0xFF, 8);
                }
        }
    }

    private static int dataBitLength(Mode mode, int charCount, byte[] bytes, String text) {
        switch (mode) {
            case NUMERIC:
                return 10 * (charCount / 3) + (charCount % 3 == 0 ? 0 : charCount % 3 == 1 ? 4 : 7);
            case ALPHANUMERIC:
                return 11 * (charCount / 2) + (charCount % 2) * 6;
            default:
                return 8 * bytes.length;
        }
    }

    /* --------------------------------------------------- Reed-Solomon + interleave */

    /// Splits data codewords into the version/level blocks, appends per-block ECC, and interleaves.
    private static byte[] addEccAndInterleave(byte[] data, int version, Ecc ecc) {
        int numBlocks = NUM_BLOCKS[ecc.ordinal()][version];
        int eccLen = ECC_PER_BLOCK[ecc.ordinal()][version];
        int totalCodewords = numRawDataModules(version) / 8;
        int numShortBlocks = numBlocks - totalCodewords % numBlocks;
        int shortBlockDataLen = totalCodewords / numBlocks - eccLen;

        byte[] generator = rsGenerator(eccLen);
        List<byte[]> dataBlocks = new ArrayList<>();
        List<byte[]> eccBlocks = new ArrayList<>();
        for (int i = 0, k = 0; i < numBlocks; i++) {
            int datLen = shortBlockDataLen + (i < numShortBlocks ? 0 : 1);
            byte[] dat = new byte[datLen];
            System.arraycopy(data, k, dat, 0, datLen);
            k += datLen;
            dataBlocks.add(dat);
            eccBlocks.add(rsRemainder(dat, generator));
        }

        // Interleave: column-major over data codewords (short blocks skip the final column), then ECC.
        byte[] result = new byte[totalCodewords];
        int idx = 0;
        int maxDataLen = shortBlockDataLen + 1;
        for (int col = 0; col < maxDataLen; col++) {
            for (int blk = 0; blk < numBlocks; blk++) {
                if (col < dataBlocks.get(blk).length) {
                    result[idx++] = dataBlocks.get(blk)[col];
                }
            }
        }
        for (int col = 0; col < eccLen; col++) {
            for (int blk = 0; blk < numBlocks; blk++) {
                result[idx++] = eccBlocks.get(blk)[col];
            }
        }
        return result;
    }

    /// The Reed-Solomon divisor polynomial of the given degree (coefficients, highest term dropped).
    static byte[] rsGenerator(int degree) {
        byte[] result = new byte[degree];
        result[degree - 1] = 1; // start with the monomial x^0
        int root = 1;
        for (int i = 0; i < degree; i++) {
            for (int j = 0; j < degree; j++) {
                result[j] = (byte) gfMul(result[j] & 0xFF, root);
                if (j + 1 < degree) {
                    result[j] ^= result[j + 1];
                }
            }
            root = gfMul(root, 0x02);
        }
        return result;
    }

    /// The RS remainder (ECC codewords) of `data` divided by `generator`.
    static byte[] rsRemainder(byte[] data, byte[] generator) {
        byte[] result = new byte[generator.length];
        for (byte b : data) {
            int factor = (b ^ result[0]) & 0xFF;
            System.arraycopy(result, 1, result, 0, result.length - 1);
            result[result.length - 1] = 0;
            for (int j = 0; j < result.length; j++) {
                result[j] ^= (byte) gfMul(generator[j] & 0xFF, factor);
            }
        }
        return result;
    }

    /// Multiplies two GF(2^8) elements modulo the QR primitive polynomial `0x11D`.
    private static int gfMul(int x, int y) {
        int z = 0;
        for (int i = 7; i >= 0; i--) {
            z = (z << 1) ^ ((z >>> 7) * 0x11D);
            z ^= ((y >>> i) & 1) * x;
        }
        return z & 0xFF;
    }

    /* ------------------------------------------------------------- capacity tables */

    /// The number of data codewords available at `version`/`ecc` (raw modules − ECC).
    static int numDataCodewords(int version, Ecc ecc) {
        return numRawDataModules(version) / 8
                - ECC_PER_BLOCK[ecc.ordinal()][version] * NUM_BLOCKS[ecc.ordinal()][version];
    }

    /// The centre coordinates of the alignment patterns for `version` (empty for version 1). The
    /// first is always 6; the rest are evenly spaced (the standard step) back from the symbol edge, so
    /// the first gap may be shorter than the others — e.g. v16 = {6, 26, 50, 74}.
    static int[] alignmentPositions(int version) {
        if (version == 1) {
            return new int[0];
        }
        int numAlign = version / 7 + 2;
        int step = (version == 32) ? 26
                : (version * 4 + numAlign * 2 + 1) / (numAlign * 2 - 2) * 2;
        int size = version * 4 + 17;
        int[] pos = new int[numAlign];
        pos[0] = 6;
        for (int i = numAlign - 1, p = size - 7; i >= 1; i--, p -= step) {
            pos[i] = p;
        }
        return pos;
    }

    /// The number of data-carrying modules in a symbol of `version` (total minus function pattern).
    static int numRawDataModules(int version) {
        int size = version * 4 + 17;
        int result = size * size;
        result -= 8 * 8 * 3;                 // three finder patterns + separators
        result -= 15 * 2 + 1;                // two format-info strips + the dark module
        result -= (size - 16) * 2;           // two timing patterns (excluding finder overlap)
        if (version >= 2) {
            int numAlign = version / 7 + 2;
            result -= (numAlign - 1) * (numAlign - 1) * 25;   // alignment patterns
            result -= (numAlign - 2) * 2 * 20;                // alignment/timing overlaps
            if (version >= 7) {
                result -= 6 * 3 * 2;        // two version-info blocks
            }
        }
        return result;
    }

    /* --------------------------------------------------------------- char helpers */

    private static final String ALNUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:";

    private static boolean isNumeric(String s) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) < '0' || s.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }

    private static boolean isAlphanumeric(String s) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (ALNUM.indexOf(s.charAt(i)) < 0) {
                return false;
            }
        }
        return true;
    }

    private static int alnum(char c) {
        return ALNUM.indexOf(c);
    }

    /* ----------------------------------------------------------------------- mode */

    private enum Mode {
        NUMERIC(0b0001, 10, 12, 14),
        ALPHANUMERIC(0b0010, 9, 11, 13),
        BYTE(0b0100, 8, 16, 16);

        final int indicator;
        private final int small;  // versions 1-9
        private final int medium; // versions 10-26
        private final int large;  // versions 27-40

        Mode(int indicator, int small, int medium, int large) {
            this.indicator = indicator;
            this.small = small;
            this.medium = medium;
            this.large = large;
        }

        int charCountBits(int version) {
            return version <= 9 ? small : version <= 26 ? medium : large;
        }
    }

    /// A most-significant-bit-first bit accumulator.
    private static final class BitBuffer {
        private final List<Byte> bits = new ArrayList<>();

        void append(int value, int len) {
            for (int i = len - 1; i >= 0; i--) {
                bits.add((byte) ((value >>> i) & 1));
            }
        }

        int length() {
            return bits.size();
        }

        byte[] toBytes() {
            byte[] out = new byte[(bits.size() + 7) / 8];
            for (int i = 0; i < bits.size(); i++) {
                out[i >>> 3] |= bits.get(i) << (7 - (i & 7));
            }
            return out;
        }
    }

    /* --------------------------------------------------------------- module matrix */

    /// Builds the symbol module grid: function patterns, masked data, format/version info.
    private static final class Matrix {
        private final int version;
        private final Ecc ecc;
        private final byte[] codewords;
        private final int size;
        private final boolean[][] modules;
        private final boolean[][] reserved;

        Matrix(int version, Ecc ecc, byte[] codewords) {
            this.version = version;
            this.ecc = ecc;
            this.codewords = codewords;
            this.size = version * 4 + 17;
            this.modules = new boolean[size][size];
            this.reserved = new boolean[size][size];
        }

        boolean[][] build() {
            drawFunctionPatterns();
            drawCodewords();
            int mask = chooseMask();
            applyMask(mask);
            drawFormatBits(mask);
            return modules;
        }

        private void drawFunctionPatterns() {
            // Timing patterns.
            for (int i = 0; i < size; i++) {
                setFunction(6, i, i % 2 == 0);
                setFunction(i, 6, i % 2 == 0);
            }
            // Finder patterns (with separators) at three corners.
            drawFinder(3, 3);
            drawFinder(size - 4, 3);
            drawFinder(3, size - 4);
            // Alignment patterns.
            int[] pos = alignmentPositions();
            for (int i = 0; i < pos.length; i++) {
                for (int j = 0; j < pos.length; j++) {
                    boolean corner = (i == 0 && j == 0) || (i == 0 && j == pos.length - 1)
                            || (i == pos.length - 1 && j == 0);
                    if (!corner) {
                        drawAlignment(pos[i], pos[j]);
                    }
                }
            }
            // Reserve format-info areas (filled by drawFormatBits) and the dark module.
            reserveFormatAreas();
            // Version information (v >= 7).
            if (version >= 7) {
                drawVersionInfo();
            }
        }

        private void drawFinder(int cx, int cy) {
            for (int dy = -4; dy <= 4; dy++) {
                for (int dx = -4; dx <= 4; dx++) {
                    int x = cx + dx;
                    int y = cy + dy;
                    if (x < 0 || x >= size || y < 0 || y >= size) {
                        continue;
                    }
                    int cd = Math.max(Math.abs(dx), Math.abs(dy));
                    setFunction(x, y, cd != 2 && cd != 4); // dark ring at distance 0-1 and 3
                }
            }
        }

        private void drawAlignment(int cx, int cy) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dx = -2; dx <= 2; dx++) {
                    setFunction(cx + dx, cy + dy, Math.max(Math.abs(dx), Math.abs(dy)) != 1);
                }
            }
        }

        private void reserveFormatAreas() {
            for (int i = 0; i < 9; i++) {
                reserve(i, 8);
                reserve(8, i);
            }
            for (int i = 0; i < 8; i++) {
                reserve(size - 1 - i, 8);
                reserve(8, size - 1 - i);
            }
            setFunction(8, size - 8, true); // dark module
        }

        private void drawVersionInfo() {
            int rem = version;
            for (int i = 0; i < 12; i++) {
                rem = (rem << 1) ^ ((rem >>> 11) * 0x1F25);
            }
            int bits = (version << 12) | rem; // 18 bits
            for (int i = 0; i < 18; i++) {
                boolean bit = ((bits >>> i) & 1) != 0;
                int a = size - 11 + i % 3;
                int b = i / 3;
                setFunction(a, b, bit);
                setFunction(b, a, bit);
            }
        }

        /// Places the interleaved codewords in the zig-zag data region, skipping function modules.
        private void drawCodewords() {
            int bit = 0;
            int total = codewords.length * 8;
            for (int right = size - 1; right >= 1; right -= 2) {
                if (right == 6) {
                    right = 5; // skip the vertical timing column
                }
                for (int v = 0; v < size; v++) {
                    for (int j = 0; j < 2; j++) {
                        int x = right - j;
                        boolean upward = ((right + 1) & 2) == 0;
                        int y = upward ? size - 1 - v : v;
                        if (!reserved[y][x] && bit < total) {
                            modules[y][x] = ((codewords[bit >>> 3] >>> (7 - (bit & 7))) & 1) != 0;
                            bit++;
                        }
                    }
                }
            }
        }

        /// Applies data mask `m` over non-function modules.
        private void applyMask(int m) {
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (reserved[y][x]) {
                        continue;
                    }
                    boolean invert;
                    switch (m) {
                        case 0: invert = (x + y) % 2 == 0; break;
                        case 1: invert = y % 2 == 0; break;
                        case 2: invert = x % 3 == 0; break;
                        case 3: invert = (x + y) % 3 == 0; break;
                        case 4: invert = (x / 3 + y / 2) % 2 == 0; break;
                        case 5: invert = x * y % 2 + x * y % 3 == 0; break;
                        case 6: invert = (x * y % 2 + x * y % 3) % 2 == 0; break;
                        default: invert = ((x + y) % 2 + x * y % 3) % 2 == 0; break;
                    }
                    modules[y][x] ^= invert;
                }
            }
        }

        private int chooseMask() {
            int best = 0;
            int bestPenalty = Integer.MAX_VALUE;
            for (int m = 0; m < 8; m++) {
                applyMask(m);
                drawFormatBits(m);
                int penalty = penaltyScore();
                if (penalty < bestPenalty) {
                    bestPenalty = penalty;
                    best = m;
                }
                applyMask(m); // undo (mask is its own inverse over the data region)
            }
            return best;
        }

        private void drawFormatBits(int mask) {
            int data = ecc.formatBits << 3 | mask; // 5 bits
            int rem = data;
            for (int i = 0; i < 10; i++) {
                rem = (rem << 1) ^ ((rem >>> 9) * 0x537);
            }
            int bits = ((data << 10) | rem) ^ 0x5412; // 15 bits, XOR mask
            // Top-left and the wrap around the other two finders.
            for (int i = 0; i <= 5; i++) {
                setFunction(8, i, bit(bits, i));
            }
            setFunction(8, 7, bit(bits, 6));
            setFunction(8, 8, bit(bits, 7));
            setFunction(7, 8, bit(bits, 8));
            for (int i = 9; i < 15; i++) {
                setFunction(14 - i, 8, bit(bits, i));
            }
            for (int i = 0; i < 8; i++) {
                setFunction(size - 1 - i, 8, bit(bits, i));
            }
            for (int i = 8; i < 15; i++) {
                setFunction(8, size - 15 + i, bit(bits, i));
            }
        }

        private static boolean bit(int v, int i) {
            return ((v >>> i) & 1) != 0;
        }

        /* ----------------------------------------------------- mask penalty rules */

        private int penaltyScore() {
            int penalty = 0;
            // Rule 1: runs of five+ same-colour modules in rows and columns.
            for (int y = 0; y < size; y++) {
                penalty += lineRunPenalty(y, true);
            }
            for (int x = 0; x < size; x++) {
                penalty += lineRunPenalty(x, false);
            }
            // Rule 2: 2x2 blocks of one colour.
            for (int y = 0; y < size - 1; y++) {
                for (int x = 0; x < size - 1; x++) {
                    boolean c = modules[y][x];
                    if (c == modules[y][x + 1] && c == modules[y + 1][x] && c == modules[y + 1][x + 1]) {
                        penalty += 3;
                    }
                }
            }
            // Rule 3: finder-like 1:1:3:1:1 patterns in rows and columns.
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (x <= size - 11 && finderLike(y, x, true)) {
                        penalty += 40;
                    }
                    if (y <= size - 11 && finderLike(y, x, false)) {
                        penalty += 40;
                    }
                }
            }
            // Rule 4: deviation of the dark-module proportion from 50%.
            int dark = 0;
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (modules[y][x]) {
                        dark++;
                    }
                }
            }
            int total = size * size;
            int k = (Math.abs(dark * 20 - total * 10) + total - 1) / total; // ceil(|pct-50|/5)
            penalty += k * 10;
            return penalty;
        }

        private int lineRunPenalty(int line, boolean row) {
            int penalty = 0;
            int runColor = -1;
            int runLen = 0;
            for (int i = 0; i < size; i++) {
                boolean c = row ? modules[line][i] : modules[i][line];
                if ((c ? 1 : 0) == runColor) {
                    runLen++;
                    if (runLen == 5) {
                        penalty += 3;
                    } else if (runLen > 5) {
                        penalty++;
                    }
                } else {
                    runColor = c ? 1 : 0;
                    runLen = 1;
                }
            }
            return penalty;
        }

        private static final boolean[] FINDER = {true, false, true, true, true, false, true};

        private boolean finderLike(int y, int x, boolean row) {
            // 1:1:3:1:1 dark pattern with a 4-module light margin on one side (either orientation).
            for (int i = 0; i < 7; i++) {
                boolean c = row ? modules[y][x + i] : modules[y + i][x];
                if (c != FINDER[i]) {
                    return false;
                }
            }
            boolean lightBefore = true;
            boolean lightAfter = true;
            for (int i = 1; i <= 4; i++) {
                int before = row ? x - i : y - i;
                int after = row ? x + 6 + i : y + 6 + i;
                if (before >= 0) {
                    lightBefore &= !(row ? modules[y][before] : modules[before][x]);
                }
                if (after < size) {
                    lightAfter &= !(row ? modules[y][after] : modules[after][x]);
                }
            }
            return lightBefore || lightAfter;
        }

        /* --------------------------------------------------------------- helpers */

        private int[] alignmentPositions() {
            return QrEncoder.alignmentPositions(version);
        }

        private void setFunction(int x, int y, boolean dark) {
            modules[y][x] = dark;
            reserved[y][x] = true;
        }

        private void reserve(int x, int y) {
            reserved[y][x] = true;
        }
    }

    /* --------------------------------------------------------------- spec tables */

    // Index: [ecc.ordinal()][version]; index 0 of each row is unused padding.
    private static final byte[][] ECC_PER_BLOCK = {
        {-1, 7, 10, 15, 20, 26, 18, 20, 24, 30, 18, 20, 24, 26, 30, 22, 24, 28, 30, 28, 28, 28, 28, 30, 30, 26, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30}, // L
        {-1, 10, 16, 26, 18, 24, 16, 18, 22, 22, 26, 30, 22, 22, 24, 24, 28, 28, 26, 26, 26, 26, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28}, // M
        {-1, 13, 22, 18, 26, 18, 24, 18, 22, 20, 24, 28, 26, 24, 20, 30, 24, 28, 28, 26, 30, 28, 30, 30, 30, 30, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30}, // Q
        {-1, 17, 28, 22, 16, 22, 28, 26, 26, 24, 28, 24, 28, 22, 24, 24, 30, 28, 28, 26, 28, 30, 24, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30}, // H
    };

    private static final byte[][] NUM_BLOCKS = {
        {-1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 4, 4, 4, 4, 4, 6, 6, 6, 6, 7, 8, 8, 9, 9, 10, 12, 12, 12, 13, 14, 15, 16, 17, 18, 19, 19, 20, 21, 22, 24, 25}, // L
        {-1, 1, 1, 1, 2, 2, 4, 4, 4, 5, 5, 5, 8, 9, 9, 10, 10, 11, 13, 14, 16, 17, 17, 18, 20, 21, 23, 25, 26, 28, 29, 31, 33, 35, 37, 38, 40, 43, 45, 47, 49}, // M
        {-1, 1, 1, 2, 2, 4, 4, 6, 6, 8, 8, 8, 10, 12, 16, 12, 17, 16, 18, 21, 20, 23, 23, 25, 27, 29, 34, 34, 35, 38, 40, 43, 45, 48, 51, 53, 56, 59, 62, 65, 68}, // Q
        {-1, 1, 1, 2, 4, 4, 4, 5, 6, 8, 8, 11, 11, 16, 16, 18, 16, 19, 21, 25, 25, 25, 34, 30, 32, 35, 37, 40, 42, 45, 48, 51, 54, 57, 60, 63, 66, 70, 74, 77, 81}, // H
    };
}
