package org.aspose.pdf.engine.font.ttf;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/// Generates a tiny but valid TrueType font **in memory** for tests — a licence-clean, deterministic
/// fixture (no proprietary font binary is committed). The font defines `.notdef` plus the glyphs
/// for the requested characters, each a filled square outline, with caller-chosen advance widths so
/// tests can assert that embedding derives `/W` from the font's own `hmtx`.
///
/// Only the tables [TrueTypeReader] needs are emitted: `head, maxp, hhea, hmtx, cmap`
/// (format 4), `loca` (long), `glyf`, `name`, `post` (3.0). unitsPerEm = 1000.
public final class MinimalTtf {

    private MinimalTtf() {
    }

    /// @param familyName the font family name (name table id 1)
    /// @param glyphs     map of char → advance width (font units, unitsPerEm=1000), document order
    /// @return a standalone TTF byte array
    public static byte[] build(String familyName, Map<Character, Integer> glyphs) {
        int nGlyphs = glyphs.size() + 1; // + .notdef
        char[] chars = new char[glyphs.size()];
        int[] advances = new int[nGlyphs];
        advances[0] = 500; // .notdef advance
        int gi = 1;
        for (Map.Entry<Character, Integer> e : glyphs.entrySet()) {
            chars[gi - 1] = e.getKey();
            advances[gi] = e.getValue();
            gi++;
        }

        // ---- glyf + loca (each non-notdef glyph = one square contour; .notdef empty) ----
        ByteArrayOutputStream glyf = new ByteArrayOutputStream();
        int[] locaOff = new int[nGlyphs + 1];
        locaOff[0] = 0; // .notdef empty
        locaOff[1] = glyf.size();
        for (int g = 1; g < nGlyphs; g++) {
            byte[] sq = squareGlyph();
            glyf.write(sq, 0, sq.length);
            locaOff[g + 1] = glyf.size();
        }
        byte[] glyfData = glyf.toByteArray();

        // ---- tables ----
        Map<String, byte[]> tables = new LinkedHashMap<>();
        tables.put("cmap", cmap(chars));
        tables.put("glyf", glyfData);
        tables.put("head", head());
        tables.put("hhea", hhea(nGlyphs));
        tables.put("hmtx", hmtx(advances));
        tables.put("loca", locaLong(locaOff));
        tables.put("maxp", maxp(nGlyphs));
        tables.put("name", name(familyName));
        tables.put("post", post());

        return assemble(tables);
    }

    /* ------------------------------- tables ------------------------------- */

    private static byte[] squareGlyph() {
        // simple glyph: 1 contour, 4 on-curve points forming a 100..900 box
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        w16(b, 1);            // numberOfContours
        w16(b, 100); w16(b, 0); w16(b, 900); w16(b, 800); // xMin yMin xMax yMax
        w16(b, 3);            // endPtsOfContours[0] (4 points: 0..3)
        w16(b, 0);            // instructionLength
        // flags: 4 points, all on-curve (0x01), x/y as signed 16 (no SHORT flags)
        b.write(0x01); b.write(0x01); b.write(0x01); b.write(0x01);
        // xCoords (delta, signed16): 100, 800, 0, -800  → 100,900,900,100
        w16(b, 100); w16(b, 800); w16(b, 0); w16(b, -800);
        // yCoords: 0, 0, 800, 0 → 0,0,800,800
        w16(b, 0); w16(b, 0); w16(b, 800); w16(b, 0);
        byte[] d = b.toByteArray();
        // pad to even length
        if ((d.length & 1) == 1) {
            byte[] p = new byte[d.length + 1];
            System.arraycopy(d, 0, p, 0, d.length);
            d = p;
        }
        return d;
    }

    private static byte[] cmap(char[] chars) {
        // single format-4 subtable; assumes contiguous ascending chars mapped to gid 1..n
        int segCount = 2; // one real segment + the 0xFFFF terminator
        int start = chars.length > 0 ? chars[0] : 0xFFFF;
        int end = chars.length > 0 ? chars[chars.length - 1] : 0xFFFF;
        int idDelta = (1 - start) & 0xFFFF; // gid = char + idDelta (mod 65536), first char → gid 1
        ByteArrayOutputStream sub = new ByteArrayOutputStream();
        w16(sub, 4);                 // format
        w16(sub, 32);                // length
        w16(sub, 0);                 // language
        w16(sub, segCount * 2);      // segCountX2
        int sr = 2 * largestPow2(segCount);
        w16(sub, sr);                // searchRange
        w16(sub, log2(largestPow2(segCount))); // entrySelector
        w16(sub, segCount * 2 - sr); // rangeShift
        w16(sub, end); w16(sub, 0xFFFF);       // endCode[]
        w16(sub, 0);                 // reservedPad
        w16(sub, start); w16(sub, 0xFFFF);     // startCode[]
        w16(sub, idDelta); w16(sub, 1);        // idDelta[]
        w16(sub, 0); w16(sub, 0);              // idRangeOffset[]
        byte[] subt = sub.toByteArray();

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        w16(b, 0);   // version
        w16(b, 1);   // numTables
        w16(b, 3); w16(b, 1);        // platform 3 (Windows), encoding 1 (Unicode BMP)
        w32(b, 12);  // offset to subtable (4 + 8)
        b.write(subt, 0, subt.length);
        return b.toByteArray();
    }

    private static byte[] head() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        w32(b, 0x00010000); // version
        w32(b, 0x00010000); // fontRevision
        w32(b, 0);          // checkSumAdjustment
        w32(b, 0x5F0F3CF5); // magicNumber
        w16(b, 0x000B);     // flags
        w16(b, 1000);       // unitsPerEm
        w32(b, 0); w32(b, 0); // created
        w32(b, 0); w32(b, 0); // modified
        w16(b, 0); w16(b, 0); w16(b, 900); w16(b, 800); // bbox
        w16(b, 0);          // macStyle
        w16(b, 8);          // lowestRecPPEM
        w16(b, 2);          // fontDirectionHint
        w16(b, 1);          // indexToLocFormat = 1 (long)
        w16(b, 0);          // glyphDataFormat
        return b.toByteArray();
    }

    private static byte[] maxp(int nGlyphs) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        w32(b, 0x00010000); // version 1.0
        w16(b, nGlyphs);
        for (int i = 0; i < 13; i++) {
            w16(b, i == 0 ? 16 : 0); // maxPoints etc. — generous/zero
        }
        return b.toByteArray();
    }

    private static byte[] hhea(int nGlyphs) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        w32(b, 0x00010000); // version
        w16(b, 800);  // ascent
        w16(b, -200); // descent
        w16(b, 0);    // lineGap
        w16(b, 1000); // advanceWidthMax
        w16(b, 0); w16(b, 0); w16(b, 1000); // minLSB, minRSB, xMaxExtent
        w16(b, 1); w16(b, 0); // caretSlopeRise, Run
        w16(b, 0);            // caretOffset
        w16(b, 0); w16(b, 0); w16(b, 0); w16(b, 0); // reserved
        w16(b, 0);            // metricDataFormat
        w16(b, nGlyphs);      // numberOfHMetrics
        return b.toByteArray();
    }

    private static byte[] hmtx(int[] advances) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        for (int a : advances) {
            w16(b, a); // advanceWidth
            w16(b, 0); // lsb
        }
        return b.toByteArray();
    }

    private static byte[] locaLong(int[] off) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        for (int o : off) {
            w32(b, o);
        }
        return b.toByteArray();
    }

    private static byte[] name(String family) {
        byte[] fam = family.getBytes(StandardCharsets.UTF_16BE);
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        w16(b, 0);  // format
        w16(b, 1);  // count
        w16(b, 18); // stringOffset (6 + 12)
        // one record: platform 3, encoding 1, language 0x409, nameID 1 (family)
        w16(b, 3); w16(b, 1); w16(b, 0x409); w16(b, 1);
        w16(b, fam.length); w16(b, 0); // length, offset
        b.write(fam, 0, fam.length);
        return b.toByteArray();
    }

    private static byte[] post() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        w32(b, 0x00030000); // version 3.0 (no glyph names)
        w32(b, 0);          // italicAngle
        w16(b, -100); w16(b, 50); // underlinePosition, thickness
        w32(b, 0);          // isFixedPitch
        w32(b, 0); w32(b, 0); w32(b, 0); w32(b, 0); // mem usage
        return b.toByteArray();
    }

    /* ------------------------------ assembly ------------------------------ */

    private static byte[] assemble(Map<String, byte[]> tables) {
        int n = tables.size();
        int sr = 16 * largestPow2(n);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        w32(out, 0x00010000); // sfnt version
        w16(out, n);
        w16(out, sr);
        w16(out, log2(largestPow2(n)));
        w16(out, n * 16 - sr);

        int offset = 12 + n * 16;
        // table records must be sorted by tag
        java.util.List<String> tags = new java.util.ArrayList<>(tables.keySet());
        java.util.Collections.sort(tags);
        Map<String, Integer> offsets = new LinkedHashMap<>();
        for (String tag : tags) {
            int len = tables.get(tag).length;
            offsets.put(tag, offset);
            offset += (len + 3) & ~3; // 4-byte aligned
        }
        for (String tag : tags) {
            byte[] t = tag.getBytes(StandardCharsets.US_ASCII);
            out.write(t, 0, 4);
            w32(out, 0);                 // checksum (ignored by reader)
            w32(out, offsets.get(tag));
            w32(out, tables.get(tag).length);
        }
        for (String tag : tags) {
            byte[] data = tables.get(tag);
            out.write(data, 0, data.length);
            int pad = ((data.length + 3) & ~3) - data.length;
            for (int i = 0; i < pad; i++) {
                out.write(0);
            }
        }
        return out.toByteArray();
    }

    private static int largestPow2(int n) {
        int p = 1;
        while (p * 2 <= n) {
            p *= 2;
        }
        return p;
    }

    private static int log2(int n) {
        int l = 0;
        while ((1 << (l + 1)) <= n) {
            l++;
        }
        return l;
    }

    private static void w16(ByteArrayOutputStream b, int v) {
        b.write((v >> 8) & 0xFF);
        b.write(v & 0xFF);
    }

    private static void w32(ByteArrayOutputStream b, int v) {
        b.write((v >> 24) & 0xFF);
        b.write((v >> 16) & 0xFF);
        b.write((v >> 8) & 0xFF);
        b.write(v & 0xFF);
    }
}
