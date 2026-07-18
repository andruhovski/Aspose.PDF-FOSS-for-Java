package org.aspose.pdf.engine.linearization;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/// Generates hint tables for linearized PDF.
/// ISO 32000-1:2008 §F.4: hint tables are binary bit-packed structures
/// stored in the primary hint stream.
///
/// Per §F.4: "This byte stream shall be treated as a bit stream, high-order
/// bit first, which shall then be subdivided into fields of arbitrary width
/// without regard to byte boundaries."
public final class HintTableGenerator {

    private static final Logger LOG = Logger.getLogger(HintTableGenerator.class.getName());

    private HintTableGenerator() {}

    /// Generates the combined hint stream data (page offset table + shared object table).
    ///
    /// @param plan             the linearization plan
    /// @param pageOffsets      byte offset of each page's first object in the output
    /// @param pageLengths      total byte length of each page's objects
    /// @param pageObjectCounts number of objects per page
    /// @param sharedOffsets    byte offset of each shared object
    /// @param sharedLengths    byte length of each shared object
    /// @param sharedObjRefs    per-page list of shared object indices referenced
    /// @return the raw hint stream bytes
    public static byte[] generate(
            LinearizationPlan plan,
            long[] pageOffsets, int[] pageLengths,
            int[] pageObjectCounts,
            long[] sharedOffsets, int[] sharedLengths,
            List<List<Integer>> sharedObjRefs) {

        BitOutputStream bits = new BitOutputStream();

        // Page Offset Hint Table (§F.4.1, Tables F.3 and F.4)
        generatePageOffsetTable(bits, plan, pageOffsets, pageLengths,
                pageObjectCounts, sharedObjRefs);

        // Align to byte boundary between tables
        bits.alignToByte();

        // Shared Object Hint Table (§F.4.2, Tables F.5 and F.6)
        generateSharedObjectTable(bits, sharedOffsets, sharedLengths);

        return bits.toByteArray();
    }

    /// Page Offset Hint Table: header (Table F.3) + per-page entries (Table F.4).
    private static void generatePageOffsetTable(BitOutputStream bits,
            LinearizationPlan plan, long[] pageOffsets, int[] pageLengths,
            int[] pageObjectCounts, List<List<Integer>> sharedObjRefs) {

        int numPages = plan.getNumPages();
        if (numPages == 0) return;

        int minObjects = minInt(pageObjectCounts);
        int maxObjects = maxInt(pageObjectCounts);
        int minLength = minInt(pageLengths);
        int maxLength = maxInt(pageLengths);

        int objectsBits = bitsNeeded(maxObjects - minObjects);
        int lengthBits = bitsNeeded(maxLength - minLength);

        int maxSharedRefs = 0;
        for (List<Integer> refs : sharedObjRefs) {
            maxSharedRefs = Math.max(maxSharedRefs, refs.size());
        }
        int sharedRefBits = bitsNeeded(maxSharedRefs);
        int numShared = plan.getSharedObjects().size() + plan.getFirstPageShared().size();
        int sharedObjIdBits = bitsNeeded(numShared);

        // ─── Header (Table F.3) ──────────────────────────────────
        // Item 1: least number of objects in a page (32 bits)
        bits.writeBits(minObjects, 32);
        // Item 2: location of first page's page object (32 bits)
        bits.writeBits((int) pageOffsets[plan.getFirstPageIndex()], 32);
        // Item 3: bits needed for delta objects per page (16 bits)
        bits.writeBits(objectsBits, 16);
        // Item 4: least page length (32 bits)
        bits.writeBits(minLength, 32);
        // Item 5: bits needed for delta page length (16 bits)
        bits.writeBits(lengthBits, 16);
        // Item 6: least content stream offset in page (32 bits)
        bits.writeBits(0, 32);
        // Item 7: bits for delta content stream offset (16 bits)
        bits.writeBits(0, 16);
        // Item 8: least content stream length (32 bits)
        bits.writeBits(0, 32);
        // Item 9: bits for delta content stream length (16 bits)
        bits.writeBits(0, 16);
        // Item 10: bits for number of shared object refs (16 bits)
        bits.writeBits(sharedRefBits, 16);
        // Item 11: bits for shared object identifier (16 bits)
        bits.writeBits(sharedObjIdBits, 16);
        // Item 12: bits for numerator of content stream fraction (16 bits)
        bits.writeBits(0, 16);
        // Item 13: denominator of content stream fraction (16 bits)
        bits.writeBits(0, 16);

        // ─── Per-page entries (Table F.4) ────────────────────────
        // Item 1: delta objects per page
        if (objectsBits > 0) {
            for (int i = 0; i < numPages; i++) {
                bits.writeBits(pageObjectCounts[i] - minObjects, objectsBits);
            }
        }
        // Item 2: delta page length
        if (lengthBits > 0) {
            for (int i = 0; i < numPages; i++) {
                bits.writeBits(pageLengths[i] - minLength, lengthBits);
            }
        }
        // Item 3: number of shared object refs per page
        if (sharedRefBits > 0) {
            for (int i = 0; i < numPages; i++) {
                bits.writeBits(sharedObjRefs.get(i).size(), sharedRefBits);
            }
        }
        // Item 4: shared object identifiers
        if (sharedObjIdBits > 0) {
            for (int i = 0; i < numPages; i++) {
                for (int ref : sharedObjRefs.get(i)) {
                    bits.writeBits(ref, sharedObjIdBits);
                }
            }
        }
        // Items 5-7 (content stream numerators): all 0, omitted when denominator=0
    }

    /// Shared Object Hint Table: header (Table F.5) + per-object entries (Table F.6).
    private static void generateSharedObjectTable(BitOutputStream bits,
            long[] sharedOffsets, int[] sharedLengths) {

        int numShared = sharedLengths.length;

        int minLength = numShared > 0 ? minInt(sharedLengths) : 0;
        int maxLength = numShared > 0 ? maxInt(sharedLengths) : 0;
        int lengthBits = bitsNeeded(maxLength - minLength);

        // ─── Header (Table F.5) ──────────────────────────────────
        // Item 1: first shared object number (32 bits)
        bits.writeBits(0, 32);
        // Item 2: location of first shared object (32 bits)
        bits.writeBits(numShared > 0 ? (int) sharedOffsets[0] : 0, 32);
        // Item 3: number of shared object entries (32 bits)
        bits.writeBits(numShared, 32);
        // Item 4: greatest objects in shared group (32 bits) — 0 for simple case
        bits.writeBits(0, 32);
        // Item 5: least object length (32 bits)
        bits.writeBits(minLength, 32);
        // Item 6: bits for delta length (16 bits)
        bits.writeBits(lengthBits, 16);

        // ─── Per-object entries (Table F.6) ──────────────────────
        // Item 1: delta object length
        if (lengthBits > 0) {
            for (int i = 0; i < numShared; i++) {
                bits.writeBits(sharedLengths[i] - minLength, lengthBits);
            }
        }
        // Item 2: signature flag (1 bit each) — all 0
        for (int i = 0; i < numShared; i++) {
            bits.writeBits(0, 1);
        }
    }

    /// Returns the number of bits needed to represent the given non-negative value.
    /// Returns 0 if value <= 0.
    public static int bitsNeeded(int value) {
        if (value <= 0) return 0;
        return 32 - Integer.numberOfLeadingZeros(value);
    }

    private static int minInt(int[] arr) {
        if (arr.length == 0) return 0;
        int m = arr[0];
        for (int v : arr) if (v < m) m = v;
        return m;
    }

    private static int maxInt(int[] arr) {
        if (arr.length == 0) return 0;
        int m = arr[0];
        for (int v : arr) if (v > m) m = v;
        return m;
    }

    // ═══════════════════════════════════════════════════════════════
    //  BitOutputStream — MSB-first bit-packed writer
    // ═══════════════════════════════════════════════════════════════

    /// Writes bits to a byte array, MSB first, crossing byte boundaries.
    /// Per §F.4: "treated as a bit stream, high-order bit first."
    public static final class BitOutputStream {
        private byte[] buffer = new byte[4096];
        private int bytePos = 0;
        private int bitPos = 7; // MSB first: 7 = highest bit

        /// Writes `numBits` bits from the low-order bits of `value`.
        ///
        /// @param value   the value to write (low-order bits used)
        /// @param numBits number of bits to write (0–32)
        public void writeBits(int value, int numBits) {
            if (numBits == 0) return;
            for (int i = numBits - 1; i >= 0; i--) {
                if (bytePos >= buffer.length) grow();
                if (((value >> i) & 1) == 1) {
                    buffer[bytePos] |= (byte) (1 << bitPos);
                }
                bitPos--;
                if (bitPos < 0) {
                    bitPos = 7;
                    bytePos++;
                }
            }
        }

        /// Writes `numBits` bits from a long value.
        void writeBitsLong(long value, int numBits) {
            if (numBits <= 32) {
                writeBits((int) value, numBits);
                return;
            }
            // Write high bits then low bits
            writeBits((int) (value >>> 32), numBits - 32);
            writeBits((int) value, 32);
        }

        /// Advances to the next byte boundary.
        public void alignToByte() {
            if (bitPos != 7) {
                bytePos++;
                bitPos = 7;
            }
        }

        /// Returns the number of bytes written (including partial).
        int getByteCount() {
            return bitPos == 7 ? bytePos : bytePos + 1;
        }

        /// Returns a copy of the written bytes.
        public byte[] toByteArray() {
            return Arrays.copyOf(buffer, getByteCount());
        }

        private void grow() {
            buffer = Arrays.copyOf(buffer, buffer.length * 2);
        }
    }
}
