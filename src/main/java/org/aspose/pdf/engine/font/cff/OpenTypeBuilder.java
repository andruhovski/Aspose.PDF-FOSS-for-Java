package org.aspose.pdf.engine.font.cff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/// Builds a synthetic CFF-flavored OpenType (`.otf`) file in memory from
/// a parsed CFF font + a PDF /Encoding + PDF /Widths. The resulting OTF can be
/// fed to [java.awt.Font#createFont(int, java.io.InputStream)] with
/// `Font.TRUETYPE_FONT` (Java's font engine accepts both TT-outlined and
/// CFF-outlined OTF).
///
/// Tables emitted, in OpenType-required ordering:
///
/// <pre>
///   'CFF '  — the raw CFF data verbatim
///   'OS/2'  — minimal OS/2 metrics (xAvgCharWidth, weight, range bits)
///   'cmap'  — format 4 segmented map from Unicode → glyph-id
///   'head'  — font header (units/em, bbox, flags)
///   'hhea'  — horizontal header (ascent/descent + numberOfHMetrics)
///   'hmtx'  — per-glyph advance widths
///   'maxp'  — version 0.5 (CFF), numGlyphs only
///   'name'  — minimum name records (FontFamily, FullName, PSName)
///   'post'  — version 3.0 (no glyph names, since CFF provides them)
/// </pre>
///
/// The cmap maps PDF charcodes (after passing through the PDF /Encoding to
/// get a glyph name, then looking that name up in the CFF Charset to get a
/// glyph id) to Unicode codepoints. When Java's text engine calls
/// drawString(text) the Unicode is mapped back to the glyph id via this cmap,
/// and the CFF rasteriser does the actual outline painting.
public final class OpenTypeBuilder {

    private static final int UNITS_PER_EM = 1000;

    /// Inputs needed to build the OTF.
    public static final class Inputs {
        public CFFParser cff;
        /// Glyph name → glyph-id reverse map of [CFFParser#glyphNames].
        public Map<String, Integer> nameToGid;
        /// Unicode codepoint → glyph-id (built from PDF /Encoding + name→gid).
        public Map<Integer, Integer> unicodeToGid;
        /// Per-glyph advance width in 1/1000 em (length == numGlyphs).
        public int[] advanceWidths;
        /// Display name (FontFamily / FullName).
        public String displayName;
        /// PostScript name (PSName).
        public String psName;
    }

    /// Wraps a CFF font into an OTF and returns the bytes ready for
    /// [java.awt.Font#createFont].
    public static byte[] build(Inputs in) throws IOException {
        Map<String, byte[]> tables = new HashMap<>();
        tables.put("CFF ", in.cff.cffBytes);
        tables.put("OS/2", buildOS2(in));
        tables.put("cmap", buildCmap(in.unicodeToGid));
        tables.put("head", buildHead());
        tables.put("hhea", buildHhea(in));
        tables.put("hmtx", buildHmtx(in.advanceWidths));
        tables.put("maxp", buildMaxp(in.cff.numGlyphs));
        tables.put("name", buildName(in.displayName, in.psName));
        tables.put("post", buildPost());

        // OpenType requires the table directory sorted alphabetically by tag.
        String[] tags = tables.keySet().toArray(new String[0]);
        Arrays.sort(tags);
        int numTables = tags.length;

        // Compute the search-range constants the spec demands (binary-search hints).
        int entrySelector = 31 - Integer.numberOfLeadingZeros(numTables);
        int searchRange = (1 << entrySelector) * 16;
        int rangeShift = numTables * 16 - searchRange;

        // Offset table (12 bytes) + table directory (16 bytes/entry) + each
        // table aligned to a 4-byte boundary.
        int directorySize = 12 + 16 * numTables;
        int[] tableOffsets = new int[numTables];
        int[] tableLengths = new int[numTables];
        int totalSize = directorySize;
        for (int i = 0; i < numTables; i++) {
            byte[] tbl = tables.get(tags[i]);
            tableOffsets[i] = totalSize;
            tableLengths[i] = tbl.length;
            totalSize += alignUp(tbl.length, 4);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(totalSize);
        // Offset Table
        writeU32(out, 0x4F54544F);              // 'OTTO' magic — CFF-flavored OTF
        writeU16(out, numTables);
        writeU16(out, searchRange);
        writeU16(out, entrySelector);
        writeU16(out, rangeShift);
        // Table directory
        for (int i = 0; i < numTables; i++) {
            byte[] tbl = tables.get(tags[i]);
            writeTag(out, tags[i]);
            writeU32(out, computeChecksum(tbl));
            writeU32(out, tableOffsets[i]);
            writeU32(out, tableLengths[i]);
        }
        // Table data
        for (int i = 0; i < numTables; i++) {
            byte[] tbl = tables.get(tags[i]);
            out.write(tbl);
            int pad = alignUp(tbl.length, 4) - tbl.length;
            for (int p = 0; p < pad; p++) out.write(0);
        }
        return out.toByteArray();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Individual tables
    // ────────────────────────────────────────────────────────────────────────

    private static byte[] buildHead() throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        writeFixed(b, 1, 0);                    // version 1.0
        writeFixed(b, 1, 0);                    // fontRevision 1.0
        writeU32(b, 0);                          // checkSumAdjustment (patched later? — Java tolerates 0)
        writeU32(b, 0x5F0F3CF5);                 // magic number
        writeU16(b, 0x000B);                     // flags (baseline at y=0, lsb at x=0, instr may depend on point size)
        writeU16(b, UNITS_PER_EM);
        writeI64(b, 0);                           // created
        writeI64(b, 0);                           // modified
        writeI16(b, -200);                        // xMin (rough)
        writeI16(b, -200);                        // yMin
        writeI16(b, 1100);                        // xMax
        writeI16(b, 1100);                        // yMax
        writeU16(b, 0);                           // macStyle
        writeU16(b, 8);                           // lowestRecPPEM
        writeI16(b, 2);                           // fontDirectionHint (deprecated, set to 2)
        writeI16(b, 0);                           // indexToLocFormat (0 for short loca — irrelevant for CFF)
        writeI16(b, 0);                           // glyphDataFormat
        return b.toByteArray();
    }

    private static byte[] buildHhea(Inputs in) throws IOException {
        int numGlyphs = in.cff.numGlyphs;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        writeFixed(b, 1, 0);
        writeI16(b, 800);                         // ascender
        writeI16(b, -200);                        // descender
        writeI16(b, 100);                         // lineGap
        // advanceWidthMax
        int maxAdv = 1000;
        for (int w : in.advanceWidths) if (w > maxAdv) maxAdv = w;
        writeU16(b, maxAdv);
        writeI16(b, 0);                           // minLeftSideBearing
        writeI16(b, 0);                           // minRightSideBearing
        writeI16(b, maxAdv);                      // xMaxExtent
        writeI16(b, 1);                           // caretSlopeRise
        writeI16(b, 0);                           // caretSlopeRun
        writeI16(b, 0);                           // caretOffset
        for (int i = 0; i < 4; i++) writeI16(b, 0); // reserved
        writeI16(b, 0);                           // metricDataFormat
        writeU16(b, numGlyphs);                   // numberOfHMetrics (we emit metric for every glyph)
        return b.toByteArray();
    }

    private static byte[] buildHmtx(int[] widths) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        for (int w : widths) {
            writeU16(b, Math.max(0, Math.min(0xFFFF, w)));
            writeI16(b, 0);                       // lsb
        }
        return b.toByteArray();
    }

    private static byte[] buildMaxp(int numGlyphs) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        writeFixed(b, 0, 0x5000);                 // 0.5 — minimal version for CFF
        writeU16(b, numGlyphs);
        return b.toByteArray();
    }

    private static byte[] buildName(String displayName, String psName) throws IOException {
        if (displayName == null || displayName.isEmpty()) displayName = "PdfFont";
        if (psName == null || psName.isEmpty()) psName = displayName.replace(' ', '-');

        // Each record needs UTF-16BE encoded strings
        byte[] dispBE = displayName.getBytes(java.nio.charset.StandardCharsets.UTF_16BE);
        byte[] psBE = psName.getBytes(java.nio.charset.StandardCharsets.UTF_16BE);

        // Records: nameID 1 (Family), 2 (Subfamily=Regular), 4 (Full), 6 (PS)
        // platformID 3 (Microsoft), encodingID 1 (Unicode BMP), langID 0x0409 (en-US)
        String regular = "Regular";
        byte[] regularBE = regular.getBytes(java.nio.charset.StandardCharsets.UTF_16BE);

        int count = 4;
        ByteArrayOutputStream stringData = new ByteArrayOutputStream();
        int[] lengths = new int[count];
        int[] offsets = new int[count];

        offsets[0] = stringData.size(); stringData.write(dispBE);    lengths[0] = dispBE.length;
        offsets[1] = stringData.size(); stringData.write(regularBE); lengths[1] = regularBE.length;
        offsets[2] = stringData.size(); stringData.write(dispBE);    lengths[2] = dispBE.length;
        offsets[3] = stringData.size(); stringData.write(psBE);      lengths[3] = psBE.length;

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        writeU16(b, 0);                            // format
        writeU16(b, count);                        // count
        writeU16(b, 6 + count * 12);               // offset to string storage
        int[] nameIds = {1, 2, 4, 6};
        for (int i = 0; i < count; i++) {
            writeU16(b, 3);                        // platformID = Microsoft
            writeU16(b, 1);                        // encodingID = Unicode BMP
            writeU16(b, 0x0409);                   // langID = en-US
            writeU16(b, nameIds[i]);
            writeU16(b, lengths[i]);
            writeU16(b, offsets[i]);
        }
        b.write(stringData.toByteArray());
        return b.toByteArray();
    }

    private static byte[] buildOS2(Inputs in) throws IOException {
        int avg = 0;
        if (in.advanceWidths.length > 0) {
            long sum = 0;
            for (int w : in.advanceWidths) sum += w;
            avg = (int) (sum / in.advanceWidths.length);
        }
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        writeU16(b, 4);                            // version 4
        writeI16(b, avg);                          // xAvgCharWidth
        writeU16(b, 400);                          // usWeightClass = normal
        writeU16(b, 5);                            // usWidthClass = medium
        writeU16(b, 0);                            // fsType
        writeI16(b, 650);                          // ySubscriptXSize
        writeI16(b, 700);                          // ySubscriptYSize
        writeI16(b, 0);                            // ySubscriptXOffset
        writeI16(b, 140);                          // ySubscriptYOffset
        writeI16(b, 650);                          // ySuperscriptXSize
        writeI16(b, 700);                          // ySuperscriptYSize
        writeI16(b, 0);                            // ySuperscriptXOffset
        writeI16(b, 480);                          // ySuperscriptYOffset
        writeI16(b, 50);                           // yStrikeoutSize
        writeI16(b, 260);                          // yStrikeoutPosition
        writeI16(b, 0);                            // sFamilyClass
        for (int i = 0; i < 10; i++) b.write(0);   // panose
        writeU32(b, 0);                            // ulUnicodeRange1 (Basic Latin)
        writeU32(b, 0); writeU32(b, 0); writeU32(b, 0);
        writeTag(b, "    ");                       // achVendID
        writeU16(b, 0);                            // fsSelection
        writeU16(b, 0x20);                         // usFirstCharIndex
        writeU16(b, 0xFFFD);                       // usLastCharIndex
        writeI16(b, 800);                          // sTypoAscender
        writeI16(b, -200);                         // sTypoDescender
        writeI16(b, 100);                          // sTypoLineGap
        writeU16(b, 1000);                         // usWinAscent
        writeU16(b, 300);                          // usWinDescent
        writeU32(b, 1);                            // ulCodePageRange1 (Latin 1)
        writeU32(b, 0);                            // ulCodePageRange2
        writeI16(b, 500);                          // sxHeight
        writeI16(b, 700);                          // sCapHeight
        writeU16(b, 0);                            // usDefaultChar
        writeU16(b, 0x20);                         // usBreakChar
        writeU16(b, 0);                            // usMaxContext
        return b.toByteArray();
    }

    private static byte[] buildPost() throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        writeFixed(b, 3, 0);                       // version 3.0 — no glyph names (CFF has them)
        writeFixed(b, 0, 0);                       // italicAngle
        writeI16(b, -100);                         // underlinePosition
        writeI16(b, 50);                           // underlineThickness
        writeU32(b, 0);                            // isFixedPitch
        writeU32(b, 0);                            // minMemType42
        writeU32(b, 0);                            // maxMemType42
        writeU32(b, 0);                            // minMemType1
        writeU32(b, 0);                            // maxMemType1
        return b.toByteArray();
    }

    /// Builds a format-4 cmap (segmented mapping to delta values) from the
    /// Unicode → glyph-id map.
    private static byte[] buildCmap(Map<Integer, Integer> unicodeToGid) throws IOException {
        // Sort code points + group consecutive runs of (cp, gid) where gid increases monotonically.
        List<int[]> segs = buildSegments(unicodeToGid);

        // Format-4 segment count is implicit — must include the sentinel
        // segment (0xFFFF, 0xFFFF, …) at the end.
        int segCount = segs.size() + 1;
        ByteArrayOutputStream subtable = new ByteArrayOutputStream();
        // Header
        writeU16(subtable, 4);                     // format
        // length will be patched
        int lengthPos = subtable.size(); writeU16(subtable, 0);
        writeU16(subtable, 0);                     // language
        writeU16(subtable, segCount * 2);
        int entrySelector = 31 - Integer.numberOfLeadingZeros(segCount);
        int searchRange = (1 << entrySelector) * 2;
        int rangeShift = 2 * segCount - searchRange;
        writeU16(subtable, searchRange);
        writeU16(subtable, entrySelector);
        writeU16(subtable, rangeShift);
        // endCode[]
        for (int[] s : segs) writeU16(subtable, s[1]);
        writeU16(subtable, 0xFFFF);                // sentinel
        writeU16(subtable, 0);                     // reservedPad
        // startCode[]
        for (int[] s : segs) writeU16(subtable, s[0]);
        writeU16(subtable, 0xFFFF);
        // idDelta[]
        for (int[] s : segs) writeI16(subtable, s[2]);
        writeI16(subtable, 1);
        // idRangeOffset[]
        for (int i = 0; i < segCount; i++) writeU16(subtable, 0);
        // Patch length
        byte[] sub = subtable.toByteArray();
        int len = sub.length;
        sub[lengthPos]     = (byte) ((len >> 8) & 0xFF);
        sub[lengthPos + 1] = (byte) (len & 0xFF);

        // cmap header + one encoding record pointing to subtable
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        writeU16(b, 0);                            // version
        writeU16(b, 1);                            // numTables
        writeU16(b, 3);                            // platformID = Microsoft
        writeU16(b, 1);                            // encodingID = Unicode BMP
        writeU32(b, 12);                           // offset to subtable
        b.write(sub);
        return b.toByteArray();
    }

    /// Coalesces a `(codepoint → glyphId)` map into format-4 segments
    /// where each segment is `(startCode, endCode, idDelta)` and
    /// `glyphId == (codepoint + idDelta) & 0xFFFF` for every codepoint
    /// in the range.
    private static List<int[]> buildSegments(Map<Integer, Integer> map) {
        Integer[] codes = map.keySet().stream()
                .filter(c -> c >= 0 && c <= 0xFFFE)
                .sorted()
                .toArray(Integer[]::new);
        List<int[]> segs = new ArrayList<>();
        int i = 0;
        while (i < codes.length) {
            int start = codes[i];
            int gid0 = map.get(start);
            int delta = (gid0 - start) & 0xFFFF;
            int end = start;
            int j = i + 1;
            while (j < codes.length && codes[j] == end + 1
                    && ((map.get(codes[j]) - codes[j]) & 0xFFFF) == delta) {
                end = codes[j];
                j++;
            }
            segs.add(new int[]{start, end, delta > 0x7FFF ? delta - 0x10000 : delta});
            i = j;
        }
        return segs;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Checksums + helpers
    // ────────────────────────────────────────────────────────────────────────

    private static long computeChecksum(byte[] table) {
        long sum = 0;
        int n = table.length;
        int padded = alignUp(n, 4);
        for (int i = 0; i < padded; i += 4) {
            int b0 = i     < n ? (table[i]     & 0xFF) : 0;
            int b1 = i + 1 < n ? (table[i + 1] & 0xFF) : 0;
            int b2 = i + 2 < n ? (table[i + 2] & 0xFF) : 0;
            int b3 = i + 3 < n ? (table[i + 3] & 0xFF) : 0;
            sum += ((long) b0 << 24) | ((long) b1 << 16) | ((long) b2 << 8) | b3;
            sum &= 0xFFFFFFFFL;
        }
        return sum;
    }

    private static int alignUp(int v, int boundary) {
        return (v + boundary - 1) & ~(boundary - 1);
    }

    private static void writeU16(ByteArrayOutputStream b, int v) {
        b.write((v >> 8) & 0xFF);
        b.write(v & 0xFF);
    }

    private static void writeI16(ByteArrayOutputStream b, int v) {
        writeU16(b, v & 0xFFFF);
    }

    private static void writeU32(ByteArrayOutputStream b, long v) {
        b.write((int) ((v >> 24) & 0xFF));
        b.write((int) ((v >> 16) & 0xFF));
        b.write((int) ((v >> 8) & 0xFF));
        b.write((int) (v & 0xFF));
    }

    private static void writeI64(ByteArrayOutputStream b, long v) {
        for (int i = 7; i >= 0; i--) b.write((int) ((v >> (i * 8)) & 0xFF));
    }

    private static void writeFixed(ByteArrayOutputStream b, int hi, int lo) {
        writeU16(b, hi);
        writeU16(b, lo);
    }

    private static void writeTag(ByteArrayOutputStream b, String tag) {
        byte[] tagBytes = tag.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        if (tagBytes.length != 4) throw new IllegalArgumentException("tag must be 4 chars: " + tag);
        b.write(tagBytes, 0, 4);
    }

    private OpenTypeBuilder() {}
}
