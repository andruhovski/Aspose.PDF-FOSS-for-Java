package org.aspose.pdf.engine.font.ttf;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.cos.COSString;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Builds the PDF object graph required to embed a TrueType font as a
 * {@code /Type0} composite font with {@code /Identity-H} encoding
 * (ISO 32000-1:2008, §9.7). The resulting root dictionary is suitable to
 * register under a page's {@code /Resources/Font}; content streams that
 * reference it must emit two-byte glyph IDs (big-endian) inside
 * {@code Tj}/{@code TJ}.
 *
 * <p>Mandatory PDF structure assembled here:</p>
 * <pre>
 *   Type0 root          /Type /Font /Subtype /Type0
 *      └─ /DescendantFonts [ CIDFontType2 ]
 *      └─ /Encoding /Identity-H
 *      └─ /ToUnicode &lt;CMap stream&gt;
 *   CIDFontType2        /Type /Font /Subtype /CIDFontType2
 *      └─ /FontDescriptor
 *      └─ /CIDSystemInfo (Adobe / Identity / 0)
 *      └─ /W [...] (optional, omitted — default width 1000 covers CJK)
 *   FontDescriptor      /Type /FontDescriptor
 *      └─ /FontFile2 &lt;TTF bytes&gt;
 *      └─ /Flags 4 (Symbolic) etc.
 * </pre>
 *
 * <p>For simplicity this MVP embeds the <em>entire</em> TTF without
 * subsetting and skips emitting a {@code /W} width array — viewers fall
 * back to the {@code /DW} default width (1000), which matches the CJK
 * em square the regression-test templates were rendered against. Width
 * accuracy would require glyph-usage tracking; not implemented yet.</p>
 */
public final class Type0FontBuilder {

    /**
     * Result tuple from {@link #build}: the {@code /Type0} font dict to
     * register under /Resources/Font, plus the {@link TrueTypeReader} the
     * caller will need to map Unicode → glyph IDs when encoding text.
     */
    public static final class Result {
        public final COSDictionary type0Font;
        public final TrueTypeReader reader;
        public Result(COSDictionary type0Font, TrueTypeReader reader) {
            this.type0Font = type0Font;
            this.reader = reader;
        }
    }

    private Type0FontBuilder() {}

    /**
     * Builds the Type0 font object graph for the supplied TrueType bytes.
     *
     * @param baseFontName the {@code /BaseFont} value to use on the Type0
     *                     root and the CIDFontType2 descendant; pass the
     *                     PDF-friendly form (no spaces).
     * @param ttfBytes     standalone TrueType bytes (TTC must already be
     *                     unpacked via {@link FontDiskLookup}).
     * @return the assembled Type0 dict + parsed reader for encoding-side use
     * @throws java.io.IOException if the TrueType bytes are malformed
     */
    public static Result build(String baseFontName, byte[] ttfBytes) throws java.io.IOException {
        TrueTypeReader reader = new TrueTypeReader(ttfBytes);
        String pdfName = pdfSafeName(baseFontName);

        // /FontFile2 — embedded raw TTF as a stream.
        COSStream fontFile = new COSStream();
        fontFile.set(COSName.of("Length1"), COSInteger.valueOf(ttfBytes.length));
        fontFile.setDecodedData(ttfBytes);

        // /FontDescriptor — minimal viable: required keys + FontFile2.
        // Real ascent/descent would come from hhea/OS/2; the bbox from head;
        // we use sane CJK defaults that PDF viewers tolerate.
        COSDictionary descriptor = new COSDictionary();
        descriptor.set(COSName.of("Type"), COSName.of("FontDescriptor"));
        descriptor.set(COSName.of("FontName"), COSName.of(pdfName));
        descriptor.set(COSName.of("Flags"), COSInteger.valueOf(4));   // Symbolic
        COSArray bbox = new COSArray();
        bbox.add(COSInteger.valueOf(-200));
        bbox.add(COSInteger.valueOf(-200));
        bbox.add(COSInteger.valueOf(1100));
        bbox.add(COSInteger.valueOf(1100));
        descriptor.set(COSName.of("FontBBox"), bbox);
        descriptor.set(COSName.of("ItalicAngle"), COSInteger.valueOf(0));
        descriptor.set(COSName.of("Ascent"), COSInteger.valueOf(880));
        descriptor.set(COSName.of("Descent"), COSInteger.valueOf(-120));
        descriptor.set(COSName.of("CapHeight"), COSInteger.valueOf(700));
        descriptor.set(COSName.of("StemV"), COSInteger.valueOf(80));
        descriptor.set(COSName.of("FontFile2"), fontFile);

        // CIDSystemInfo — Adobe/Identity/0 is the conventional pairing for
        // Identity-H encoded composite fonts.
        COSDictionary cidSysInfo = new COSDictionary();
        cidSysInfo.set(COSName.of("Registry"), new COSString("Adobe"));
        cidSysInfo.set(COSName.of("Ordering"), new COSString("Identity"));
        cidSysInfo.set(COSName.of("Supplement"), COSInteger.valueOf(0));

        // CIDFontType2 descendant
        COSDictionary cidFont = new COSDictionary();
        cidFont.set(COSName.of("Type"), COSName.of("Font"));
        cidFont.set(COSName.of("Subtype"), COSName.of("CIDFontType2"));
        cidFont.set(COSName.of("BaseFont"), COSName.of(pdfName));
        cidFont.set(COSName.of("CIDSystemInfo"), cidSysInfo);
        cidFont.set(COSName.of("FontDescriptor"), descriptor);
        cidFont.set(COSName.of("DW"), COSInteger.valueOf(1000));
        // CIDToGIDMap defaults to /Identity for CIDFontType2 — that's what we want.

        // Type0 root
        COSDictionary type0 = new COSDictionary();
        type0.set(COSName.of("Type"), COSName.of("Font"));
        type0.set(COSName.of("Subtype"), COSName.of("Type0"));
        type0.set(COSName.of("BaseFont"), COSName.of(pdfName));
        type0.set(COSName.of("Encoding"), COSName.of("Identity-H"));
        COSArray descendantArr = new COSArray();
        descendantArr.add(cidFont);
        type0.set(COSName.of("DescendantFonts"), descendantArr);
        type0.set(COSName.of("ToUnicode"), buildToUnicodeCMap(reader, pdfName));

        return new Result(type0, reader);
    }

    /**
     * Generates a {@code /ToUnicode} CMap stream that maps every glyph ID in
     * the font's cmap back to a single Unicode codepoint. Lets PDF readers
     * extract text and copy-paste it correctly even though the content
     * stream carries raw glyph IDs (Identity-H).
     */
    private static COSStream buildToUnicodeCMap(TrueTypeReader reader, String pdfName) {
        // Invert the cmap: glyph_id → first Unicode that maps to it.
        Map<Integer, Integer> entries = reader.getCmapEntries();
        TreeMap<Integer, Integer> gidToUnicode = new TreeMap<>();
        for (Map.Entry<Integer, Integer> e : entries.entrySet()) {
            int unicode = e.getKey();
            int gid = e.getValue();
            if (gid == 0) continue;
            gidToUnicode.putIfAbsent(gid, unicode);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("/CIDInit /ProcSet findresource begin\n");
        sb.append("12 dict begin\n");
        sb.append("begincmap\n");
        sb.append("/CIDSystemInfo << /Registry (Adobe) /Ordering (UCS) /Supplement 0 >> def\n");
        sb.append("/CMapName /Adobe-Identity-UCS def\n");
        sb.append("/CMapType 2 def\n");
        sb.append("1 begincodespacerange\n<0000> <FFFF>\nendcodespacerange\n");

        // beginbfchar groups (up to 100 entries per group per spec). The CMap
        // format encodes supplementary-plane codepoints as UTF-16 surrogate
        // pairs inside the bfchar value (Adobe CMap spec, §3.9.4).
        List<int[]> chunk = new ArrayList<>(100);
        for (Map.Entry<Integer, Integer> e : gidToUnicode.entrySet()) {
            int gid = e.getKey();
            int u = e.getValue();
            // Lone surrogate halves never map to anything meaningful; skip.
            if (u >= 0xD800 && u <= 0xDFFF) continue;
            chunk.add(new int[]{gid, u});
            if (chunk.size() == 100) flushBfchar(sb, chunk);
        }
        if (!chunk.isEmpty()) flushBfchar(sb, chunk);

        sb.append("endcmap\n");
        sb.append("CMapName currentdict /CMap defineresource pop\n");
        sb.append("end\nend\n");

        byte[] body = sb.toString().getBytes(StandardCharsets.US_ASCII);
        COSStream cmap = new COSStream();
        cmap.setDecodedData(body);
        return cmap;
    }

    private static void flushBfchar(StringBuilder sb, List<int[]> chunk) {
        sb.append(chunk.size()).append(" beginbfchar\n");
        for (int[] pair : chunk) {
            sb.append('<').append(hex4(pair[0])).append("> <")
                    .append(hexUtf16(pair[1])).append(">\n");
        }
        sb.append("endbfchar\n");
        chunk.clear();
    }

    private static String hex4(int v) {
        return String.format("%04X", v & 0xFFFF);
    }

    /**
     * Emits {@code codePoint} as its UTF-16 hex representation: 4 hex digits
     * for BMP, 8 hex digits (surrogate pair) for supplementary planes — the
     * Adobe ToUnicode CMap value format (§3.9.4 of the CMap spec).
     */
    private static String hexUtf16(int codePoint) {
        if (codePoint <= 0xFFFF) {
            return String.format("%04X", codePoint);
        }
        int v = codePoint - 0x10000;
        int high = 0xD800 | (v >>> 10);
        int low  = 0xDC00 | (v & 0x3FF);
        return String.format("%04X%04X", high, low);
    }

    /**
     * Replaces characters that PDF doesn't allow inside a /Name with the
     * placeholder {@code _}. Spaces in particular have to go (e.g.
     * {@code "Arial Unicode MS"} → {@code "Arial_Unicode_MS"}). The result
     * is opaque to viewers — what matters is that the same name is used
     * consistently on the Type0 root and descendant.
     */
    private static String pdfSafeName(String name) {
        if (name == null || name.isEmpty()) return "Font";
        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == ' ' || c == '#' || c == '(' || c == ')'
                    || c == '<' || c == '>' || c == '[' || c == ']'
                    || c == '{' || c == '}' || c == '/' || c == '%') {
                sb.append('_');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
