package org.aspose.pdf.engine.font.cmap;

import org.aspose.pdf.engine.cos.COSStream;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Parses CMap data into a {@link ToUnicodeCMap} (ISO 32000-1:2008, §9.10).
 * <p>
 * Handles both {@code beginbfchar}/{@code endbfchar} and
 * {@code beginbfrange}/{@code endbfrange} sections. Supports hex-encoded
 * character codes and Unicode values, including multi-codepoint mappings
 * for ligatures.
 * </p>
 */
public final class CMapParser {

    private static final Logger LOG = Logger.getLogger(CMapParser.class.getName());

    private CMapParser() {
        // Utility class
    }

    /**
     * Parses a ToUnicode CMap from raw byte data.
     *
     * @param cmapData the raw CMap stream bytes
     * @return the parsed ToUnicodeCMap
     * @throws IOException if parsing fails
     */
    public static ToUnicodeCMap parseToUnicode(byte[] cmapData) throws IOException {
        if (cmapData == null || cmapData.length == 0) {
            return new ToUnicodeCMap(new HashMap<>());
        }
        String text = new String(cmapData, java.nio.charset.StandardCharsets.ISO_8859_1);
        Map<Integer, String> mappings = new HashMap<>();
        parseBfChars(text, mappings);
        parseBfRanges(text, mappings);
        LOG.fine(() -> "Parsed ToUnicode CMap: " + mappings.size() + " mappings");
        return new ToUnicodeCMap(mappings);
    }

    /**
     * Parses a ToUnicode CMap from a COSStream.
     *
     * @param stream the CMap stream
     * @return the parsed ToUnicodeCMap
     * @throws IOException if parsing or decoding fails
     */
    public static ToUnicodeCMap parseToUnicode(COSStream stream) throws IOException {
        if (stream == null) {
            return new ToUnicodeCMap(new HashMap<>());
        }
        byte[] data = stream.getDecodedData();
        return parseToUnicode(data);
    }

    /**
     * Parses beginbfchar/endbfchar sections.
     * Format: beginbfchar <srcCode> <dstUnicode> endbfchar
     */
    private static void parseBfChars(String text, Map<Integer, String> mappings) {
        int searchFrom = 0;
        while (true) {
            int begin = text.indexOf("beginbfchar", searchFrom);
            if (begin < 0) break;
            int end = text.indexOf("endbfchar", begin);
            if (end < 0) break;

            String block = text.substring(begin + "beginbfchar".length(), end);
            parseBfCharBlock(block, mappings);
            searchFrom = end + "endbfchar".length();
        }
    }

    /**
     * Parses beginbfrange/endbfrange sections.
     * Format: beginbfrange <srcCodeLo> <srcCodeHi> <dstUnicodeStart> endbfrange
     * Or:     beginbfrange <srcCodeLo> <srcCodeHi> [<dst1> <dst2> ...] endbfrange
     */
    private static void parseBfRanges(String text, Map<Integer, String> mappings) {
        int searchFrom = 0;
        while (true) {
            int begin = text.indexOf("beginbfrange", searchFrom);
            if (begin < 0) break;
            int end = text.indexOf("endbfrange", begin);
            if (end < 0) break;

            String block = text.substring(begin + "beginbfrange".length(), end);
            parseBfRangeBlock(block, mappings);
            searchFrom = end + "endbfrange".length();
        }
    }

    private static void parseBfCharBlock(String block, Map<Integer, String> mappings) {
        int pos = 0;
        while (pos < block.length()) {
            // Find source code
            int srcStart = block.indexOf('<', pos);
            if (srcStart < 0) break;
            int srcEnd = block.indexOf('>', srcStart);
            if (srcEnd < 0) break;
            String srcHex = block.substring(srcStart + 1, srcEnd).trim();

            // Find destination unicode
            int dstStart = block.indexOf('<', srcEnd);
            if (dstStart < 0) break;
            int dstEnd = block.indexOf('>', dstStart);
            if (dstEnd < 0) break;
            String dstHex = block.substring(dstStart + 1, dstEnd).trim();

            try {
                int srcCode = Integer.parseInt(srcHex, 16);
                String unicode = hexToUnicodeString(dstHex);
                mappings.put(srcCode, unicode);
            } catch (NumberFormatException e) {
                LOG.fine(() -> "Skipping invalid bfchar entry: " + srcHex + " -> " + dstHex);
            }

            pos = dstEnd + 1;
        }
    }

    private static void parseBfRangeBlock(String block, Map<Integer, String> mappings) {
        int pos = 0;
        while (pos < block.length()) {
            // Find source range lo
            int loStart = block.indexOf('<', pos);
            if (loStart < 0) break;
            int loEnd = block.indexOf('>', loStart);
            if (loEnd < 0) break;
            String loHex = block.substring(loStart + 1, loEnd).trim();

            // Find source range hi
            int hiStart = block.indexOf('<', loEnd);
            if (hiStart < 0) break;
            int hiEnd = block.indexOf('>', hiStart);
            if (hiEnd < 0) break;
            String hiHex = block.substring(hiStart + 1, hiEnd).trim();

            pos = hiEnd + 1;

            // Skip whitespace
            while (pos < block.length() && Character.isWhitespace(block.charAt(pos))) {
                pos++;
            }

            try {
                int lo = Integer.parseInt(loHex, 16);
                int hi = Integer.parseInt(hiHex, 16);

                if (pos < block.length() && block.charAt(pos) == '[') {
                    // Array of destination values
                    int arrayEnd = block.indexOf(']', pos);
                    if (arrayEnd < 0) break;
                    String arrayContent = block.substring(pos + 1, arrayEnd);
                    parseArrayRange(lo, hi, arrayContent, mappings);
                    pos = arrayEnd + 1;
                } else {
                    // Single destination start value
                    int dstStart = block.indexOf('<', pos - 1);
                    if (dstStart < pos - 1) dstStart = block.indexOf('<', pos);
                    if (dstStart < 0) break;
                    int dstEnd = block.indexOf('>', dstStart);
                    if (dstEnd < 0) break;
                    String dstHex = block.substring(dstStart + 1, dstEnd).trim();

                    int dstBase = Integer.parseInt(dstHex, 16);
                    for (int code = lo; code <= hi; code++) {
                        int dstCode = dstBase + (code - lo);
                        mappings.put(code, String.valueOf((char) dstCode));
                    }
                    pos = dstEnd + 1;
                }
            } catch (NumberFormatException e) {
                LOG.fine(() -> "Skipping invalid bfrange entry");
            }
        }
    }

    private static void parseArrayRange(int lo, int hi, String arrayContent,
                                         Map<Integer, String> mappings) {
        int pos = 0;
        int code = lo;
        while (pos < arrayContent.length() && code <= hi) {
            int dstStart = arrayContent.indexOf('<', pos);
            if (dstStart < 0) break;
            int dstEnd = arrayContent.indexOf('>', dstStart);
            if (dstEnd < 0) break;
            String dstHex = arrayContent.substring(dstStart + 1, dstEnd).trim();
            try {
                mappings.put(code, hexToUnicodeString(dstHex));
            } catch (NumberFormatException e) {
                // skip
            }
            code++;
            pos = dstEnd + 1;
        }
    }

    /**
     * Converts a hex string to a Unicode string.
     * Each 4 hex digits = one UTF-16 code unit.
     * Supports multi-codepoint mappings (8+ hex digits = ligature).
     */
    private static String hexToUnicodeString(String hex) {
        hex = hex.replaceAll("\\s", "");
        if (hex.length() <= 4) {
            // Single codepoint
            int codepoint = Integer.parseInt(hex, 16);
            return String.valueOf((char) codepoint);
        }
        // Multi-codepoint: each 4 hex digits = one char
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i + 3 < hex.length(); i += 4) {
            int cp = Integer.parseInt(hex.substring(i, i + 4), 16);
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
