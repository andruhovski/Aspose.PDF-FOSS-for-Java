package org.aspose.pdf.engine.pdfa.fixes;

import org.aspose.pdf.ConvertErrorAction;
import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectKey;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.cos.COSString;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;

/**
 * Font-related fixes for PDF/A compliance.
 * <p>
 * Because we cannot embed font programs we do not have access to, these fixes
 * are limited in scope.  The primary capabilities are:
 * </p>
 * <ul>
 *   <li>Generate a {@code /ToUnicode} CMap for standard-encoded fonts that lack one</li>
 *   <li>Generate a {@code /CharSet} string for subset Type 1 fonts</li>
 *   <li>Generate a {@code /CIDSet} stream for subset CID fonts</li>
 *   <li>Log warnings for unembedded fonts that cannot be fixed automatically</li>
 * </ul>
 */
public final class FontFixes {

    private static final Logger LOG = Logger.getLogger(FontFixes.class.getName());

    /** WinAnsiEncoding mapping: byte value (32..255) -> Unicode code point. */
    private static final Map<Integer, Integer> WIN_ANSI_MAP = new HashMap<>();

    static {
        // Standard ASCII range 32..126 maps to itself
        for (int i = 32; i <= 126; i++) {
            WIN_ANSI_MAP.put(i, i);
        }
        // Windows-1252 specific mappings for 128..159 and 160..255
        WIN_ANSI_MAP.put(128, 0x20AC); // Euro sign
        WIN_ANSI_MAP.put(130, 0x201A);
        WIN_ANSI_MAP.put(131, 0x0192);
        WIN_ANSI_MAP.put(132, 0x201E);
        WIN_ANSI_MAP.put(133, 0x2026);
        WIN_ANSI_MAP.put(134, 0x2020);
        WIN_ANSI_MAP.put(135, 0x2021);
        WIN_ANSI_MAP.put(136, 0x02C6);
        WIN_ANSI_MAP.put(137, 0x2030);
        WIN_ANSI_MAP.put(138, 0x0160);
        WIN_ANSI_MAP.put(139, 0x2039);
        WIN_ANSI_MAP.put(140, 0x0152);
        WIN_ANSI_MAP.put(142, 0x017D);
        WIN_ANSI_MAP.put(145, 0x2018);
        WIN_ANSI_MAP.put(146, 0x2019);
        WIN_ANSI_MAP.put(147, 0x201C);
        WIN_ANSI_MAP.put(148, 0x201D);
        WIN_ANSI_MAP.put(149, 0x2022);
        WIN_ANSI_MAP.put(150, 0x2013);
        WIN_ANSI_MAP.put(151, 0x2014);
        WIN_ANSI_MAP.put(152, 0x02DC);
        WIN_ANSI_MAP.put(153, 0x2122);
        WIN_ANSI_MAP.put(154, 0x0161);
        WIN_ANSI_MAP.put(155, 0x203A);
        WIN_ANSI_MAP.put(156, 0x0153);
        WIN_ANSI_MAP.put(158, 0x017E);
        WIN_ANSI_MAP.put(159, 0x0178);
        // 160..255 map to their Unicode equivalents (Latin-1 Supplement)
        for (int i = 160; i <= 255; i++) {
            WIN_ANSI_MAP.put(i, i);
        }
    }

    /**
     * Creates a new FontFixes instance.
     */
    public FontFixes() {
        // default
    }

    /**
     * Generates a {@code /ToUnicode} CMap for fonts that use WinAnsiEncoding or
     * MacRomanEncoding but lack a {@code /ToUnicode} entry.
     * <p>
     * PDF/A Level A requires every font to have a way to map character codes to
     * Unicode (ISO 19005-1:2005, 6.3.8).
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void generateToUnicodeCMap(PDFParser parser, PdfFormat format,
                                      ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        for (COSObjectKey key : parser.getAllObjectKeys()) {
            COSBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                continue;
            }
            if (!(obj instanceof COSDictionary)) {
                continue;
            }
            COSDictionary dict = (COSDictionary) obj;
            String type = dict.getNameAsString("Type");
            if (!"Font".equals(type)) {
                continue;
            }
            // Skip if already has ToUnicode
            if (dict.get("ToUnicode") != null) {
                continue;
            }

            String encoding = dict.getNameAsString("Encoding");
            if (!"WinAnsiEncoding".equals(encoding) && !"MacRomanEncoding".equals(encoding)) {
                continue;
            }

            int firstChar = dict.getInt("FirstChar", 0);
            int lastChar = dict.getInt("LastChar", 255);

            LOG.fine(() -> "Generating ToUnicode CMap for font at object " + key.getObjectNumber()
                    + " (" + encoding + ")");

            byte[] cmapData;
            if ("WinAnsiEncoding".equals(encoding)) {
                cmapData = buildWinAnsiCMap(firstChar, lastChar);
            } else {
                // MacRomanEncoding — use a simplified identity mapping for now
                cmapData = buildWinAnsiCMap(firstChar, lastChar);
            }

            COSStream cmapStream = new COSStream();
            cmapStream.setDecodedData(cmapData);
            cmapStream.setFilter(COSName.FLATE_DECODE);

            int maxObj = findMaxObjectNumber(parser);
            COSObjectKey cmapKey = new COSObjectKey(maxObj + 1, 0);
            COSObjectReference cmapRef = new COSObjectReference(cmapKey, k -> cmapStream);

            dict.set("ToUnicode", cmapRef);
            result.addWarning("font.1", "Generated /ToUnicode CMap for " + encoding + " font",
                    "obj " + key.getObjectNumber(), "ISO 19005-1:2005, 6.3.8");
        }
    }

    /**
     * Generates a {@code /CharSet} string for subset Type 1 fonts that lack one.
     * <p>
     * The /CharSet string lists the glyph names present in the font. For PDF/A-1
     * this is required in the font descriptor of subset Type 1 fonts
     * (ISO 19005-1:2005, 6.3.5).
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void generateCharSet(PDFParser parser, PdfFormat format,
                                ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        for (COSObjectKey key : parser.getAllObjectKeys()) {
            COSBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                continue;
            }
            if (!(obj instanceof COSDictionary)) {
                continue;
            }
            COSDictionary dict = (COSDictionary) obj;
            if (!"Font".equals(dict.getNameAsString("Type"))) {
                continue;
            }
            String subtype = dict.getNameAsString("Subtype");
            if (!"Type1".equals(subtype)) {
                continue;
            }

            // Check if it's a subset font (name contains '+')
            String baseFontName = dict.getNameAsString("BaseFont");
            if (baseFontName == null || !baseFontName.contains("+")) {
                continue;
            }

            // Get font descriptor
            COSBase fdRef = dict.get("FontDescriptor");
            if (fdRef == null) {
                continue;
            }
            COSBase fdObj = parser.resolveReference(fdRef);
            if (!(fdObj instanceof COSDictionary)) {
                continue;
            }
            COSDictionary fontDesc = (COSDictionary) fdObj;
            if (fontDesc.get("CharSet") != null) {
                continue;
            }

            // Generate CharSet from encoding differences or standard encoding
            int firstChar = dict.getInt("FirstChar", 0);
            int lastChar = dict.getInt("LastChar", 255);
            String charSet = buildCharSetString(firstChar, lastChar);

            fontDesc.set("CharSet", new COSString(charSet));
            result.addWarning("font.2", "Generated /CharSet for subset Type1 font " + baseFontName,
                    "obj " + key.getObjectNumber(), "ISO 19005-1:2005, 6.3.5");
        }
    }

    /**
     * Generates a {@code /CIDSet} stream for subset CID fonts that lack one.
     * <p>
     * For PDF/A, CIDFont subsets must include a CIDSet stream that indicates
     * which CIDs are present (ISO 19005-1:2005, 6.3.6).
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void generateCIDSet(PDFParser parser, PdfFormat format,
                               ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        for (COSObjectKey key : parser.getAllObjectKeys()) {
            COSBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                continue;
            }
            if (!(obj instanceof COSDictionary)) {
                continue;
            }
            COSDictionary dict = (COSDictionary) obj;
            String subtype = dict.getNameAsString("Subtype");
            if (!"CIDFontType0".equals(subtype) && !"CIDFontType2".equals(subtype)) {
                continue;
            }

            // Get font descriptor
            COSBase fdRef = dict.get("FontDescriptor");
            if (fdRef == null) {
                continue;
            }
            COSBase fdObj = parser.resolveReference(fdRef);
            if (!(fdObj instanceof COSDictionary)) {
                continue;
            }
            COSDictionary fontDesc = (COSDictionary) fdObj;
            if (fontDesc.get("CIDSet") != null) {
                continue;
            }

            // Check if subset
            String baseFontName = dict.getNameAsString("BaseFont");
            if (baseFontName == null || !baseFontName.contains("+")) {
                continue;
            }

            LOG.fine(() -> "Generating CIDSet for CIDFont " + baseFontName);

            // Generate a CIDSet that marks all CIDs 0..lastCid as present
            // This is a conservative approach; ideally we'd analyze the actual CIDs used
            int lastCid = 255; // default
            COSBase wArray = dict.get("W");
            if (wArray instanceof COSArray) {
                lastCid = estimateMaxCidFromW((COSArray) wArray);
            }

            byte[] cidSetData = buildCidSetBitmap(lastCid);

            COSStream cidSetStream = new COSStream();
            cidSetStream.setDecodedData(cidSetData);
            cidSetStream.setFilter(COSName.FLATE_DECODE);

            int maxObj = findMaxObjectNumber(parser);
            COSObjectKey cidSetKey = new COSObjectKey(maxObj + 1, 0);
            COSObjectReference cidSetRef = new COSObjectReference(cidSetKey, k -> cidSetStream);

            fontDesc.set("CIDSet", cidSetRef);
            result.addWarning("font.3", "Generated /CIDSet for subset CIDFont " + baseFontName,
                    "obj " + key.getObjectNumber(), "ISO 19005-1:2005, 6.3.6");
        }
    }

    /**
     * Logs warnings for fonts that are not embedded and cannot be fixed
     * automatically (we lack the font program data).
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void logUnembeddedFonts(PDFParser parser, PdfFormat format,
                                   ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        for (COSObjectKey key : parser.getAllObjectKeys()) {
            COSBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                continue;
            }
            if (!(obj instanceof COSDictionary)) {
                continue;
            }
            COSDictionary dict = (COSDictionary) obj;
            if (!"Font".equals(dict.getNameAsString("Type"))) {
                continue;
            }
            String subtype = dict.getNameAsString("Subtype");
            // Type3 fonts are always embedded
            if ("Type3".equals(subtype)) {
                continue;
            }

            COSBase fdRef = dict.get("FontDescriptor");
            if (fdRef == null) {
                // Standard 14 fonts may lack FontDescriptor
                String baseFontName = dict.getNameAsString("BaseFont");
                if (baseFontName != null && isStandard14(baseFontName)) {
                    result.addWarning("font.4",
                            "Standard 14 font '" + baseFontName + "' is not embedded (may need embedding for strict PDF/A)",
                            "obj " + key.getObjectNumber(), "ISO 19005-1:2005, 6.3.3");
                }
                continue;
            }

            COSBase fdObj = parser.resolveReference(fdRef);
            if (!(fdObj instanceof COSDictionary)) {
                continue;
            }
            COSDictionary fontDesc = (COSDictionary) fdObj;

            // Check for embedded font program
            boolean embedded = fontDesc.get("FontFile") != null
                    || fontDesc.get("FontFile2") != null
                    || fontDesc.get("FontFile3") != null;
            if (!embedded) {
                String baseFontName = dict.getNameAsString("BaseFont");
                result.addWarning("font.4",
                        "Font '" + (baseFontName != null ? baseFontName : "unknown")
                                + "' is not embedded (cannot fix automatically)",
                        "obj " + key.getObjectNumber(), "ISO 19005-1:2005, 6.3.3");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a ToUnicode CMap for WinAnsiEncoding covering firstChar..lastChar.
     */
    private static byte[] buildWinAnsiCMap(int firstChar, int lastChar) {
        StringBuilder sb = new StringBuilder(4096);
        sb.append("/CIDInit /ProcSet findresource begin\n");
        sb.append("12 dict begin\n");
        sb.append("begincmap\n");
        sb.append("/CIDSystemInfo << /Registry (Adobe) /Ordering (UCS) /Supplement 0 >> def\n");
        sb.append("/CMapName /Adobe-Identity-UCS def\n");
        sb.append("/CMapType 2 def\n");
        sb.append("1 begincodespacerange\n");
        sb.append("<00> <FF>\n");
        sb.append("endcodespacerange\n");

        // Collect valid mappings
        int count = 0;
        StringBuilder entries = new StringBuilder(2048);
        for (int code = firstChar; code <= lastChar; code++) {
            Integer unicode = WIN_ANSI_MAP.get(code);
            if (unicode != null) {
                entries.append(String.format("<%02X> <%04X>\n", code, unicode));
                count++;
            }
        }

        // Write in batches of 100 (CMap spec limit)
        String[] lines = entries.toString().split("\n");
        int offset = 0;
        while (offset < lines.length) {
            int batch = Math.min(100, lines.length - offset);
            sb.append(batch).append(" beginbfchar\n");
            for (int i = 0; i < batch; i++) {
                sb.append(lines[offset + i]).append('\n');
            }
            sb.append("endbfchar\n");
            offset += batch;
        }

        sb.append("endcmap\n");
        sb.append("CMapName currentdict /CMap defineresource pop\n");
        sb.append("end\n");
        sb.append("end\n");
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Builds a /CharSet string listing standard glyph names for codes firstChar..lastChar.
     */
    private static String buildCharSetString(int firstChar, int lastChar) {
        // Standard glyph names for WinAnsi codes 32..126
        String[] stdNames = {
                "space", "exclam", "quotedbl", "numbersign", "dollar", "percent",
                "ampersand", "quotesingle", "parenleft", "parenright", "asterisk",
                "plus", "comma", "hyphen", "period", "slash",
                "zero", "one", "two", "three", "four", "five", "six", "seven",
                "eight", "nine", "colon", "semicolon", "less", "equal", "greater",
                "question", "at",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
                "bracketleft", "backslash", "bracketright", "asciicircum", "underscore",
                "grave",
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
                "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
                "braceleft", "bar", "braceright", "asciitilde"
        };

        StringBuilder sb = new StringBuilder();
        for (int code = Math.max(firstChar, 32); code <= Math.min(lastChar, 126); code++) {
            int idx = code - 32;
            if (idx >= 0 && idx < stdNames.length) {
                sb.append('/').append(stdNames[idx]);
            }
        }
        return sb.toString();
    }

    /**
     * Estimates the maximum CID from a /W (widths) array.
     */
    private static int estimateMaxCidFromW(COSArray wArray) {
        int maxCid = 255;
        for (int i = 0; i < wArray.size(); i++) {
            COSBase item = wArray.get(i);
            if (item instanceof COSInteger) {
                int val = (int) ((COSInteger) item).longValue();
                if (val > maxCid) {
                    maxCid = val;
                }
            }
        }
        return maxCid;
    }

    /**
     * Builds a CIDSet bitmap marking CIDs 0..lastCid as present.
     * The CIDSet is a byte array where bit N represents CID N (MSB first).
     */
    private static byte[] buildCidSetBitmap(int lastCid) {
        int byteCount = (lastCid / 8) + 1;
        byte[] bitmap = new byte[byteCount];
        // Mark all CIDs 0..lastCid as present
        for (int cid = 0; cid <= lastCid; cid++) {
            int byteIdx = cid / 8;
            int bitIdx = 7 - (cid % 8); // MSB first
            bitmap[byteIdx] |= (1 << bitIdx);
        }
        return bitmap;
    }

    /**
     * Checks if a font name is one of the standard 14 PDF fonts.
     */
    private static boolean isStandard14(String name) {
        // Strip subset prefix if present
        String n = name.contains("+") ? name.substring(name.indexOf('+') + 1) : name;
        switch (n) {
            case "Courier": case "Courier-Bold": case "Courier-Oblique": case "Courier-BoldOblique":
            case "Helvetica": case "Helvetica-Bold": case "Helvetica-Oblique": case "Helvetica-BoldOblique":
            case "Times-Roman": case "Times-Bold": case "Times-Italic": case "Times-BoldItalic":
            case "Symbol": case "ZapfDingbats":
                return true;
            default:
                return false;
        }
    }

    /**
     * Finds the maximum object number currently in the parser.
     */
    private static int findMaxObjectNumber(PDFParser parser) {
        int maxObj = 0;
        for (COSObjectKey k : parser.getAllObjectKeys()) {
            maxObj = Math.max(maxObj, k.getObjectNumber());
        }
        return maxObj;
    }
}
