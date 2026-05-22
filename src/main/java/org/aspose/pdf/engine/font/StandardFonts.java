package org.aspose.pdf.engine.font;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of the 14 Standard PDF Fonts (ISO 32000-1:2008, §9.6.2.2).
 * <p>
 * These fonts do not require embedding — every conforming PDF viewer must support them.
 * Provides predefined width tables and default encoding for each standard font.
 * </p>
 */
public final class StandardFonts {

    private static final Map<String, int[]> WIDTH_MAP = new HashMap<>(20);
    private static final Map<String, FontEncoding> ENCODING_MAP = new HashMap<>(20);

    // Standard 14 font names
    private static final String[] STANDARD_NAMES = {
        "Courier", "Courier-Bold", "Courier-Oblique", "Courier-BoldOblique",
        "Helvetica", "Helvetica-Bold", "Helvetica-Oblique", "Helvetica-BoldOblique",
        "Times-Roman", "Times-Bold", "Times-Italic", "Times-BoldItalic",
        "Symbol", "ZapfDingbats"
    };

    static {
        // Courier (all widths = 600, fixed-pitch)
        int[] courier = new int[256];
        for (int i = 0; i < 256; i++) courier[i] = 600;
        WIDTH_MAP.put("Courier", courier);
        WIDTH_MAP.put("Courier-Bold", courier);
        WIDTH_MAP.put("Courier-Oblique", courier);
        WIDTH_MAP.put("Courier-BoldOblique", courier);

        // Helvetica
        WIDTH_MAP.put("Helvetica", buildHelveticaWidths());
        WIDTH_MAP.put("Helvetica-Bold", buildHelveticaBoldWidths());
        WIDTH_MAP.put("Helvetica-Oblique", buildHelveticaWidths()); // same metrics
        WIDTH_MAP.put("Helvetica-BoldOblique", buildHelveticaBoldWidths());

        // Times-Roman
        WIDTH_MAP.put("Times-Roman", buildTimesRomanWidths());
        WIDTH_MAP.put("Times-Bold", buildTimesBoldWidths());
        WIDTH_MAP.put("Times-Italic", buildTimesItalicWidths());
        WIDTH_MAP.put("Times-BoldItalic", buildTimesBoldItalicWidths());

        // Symbol and ZapfDingbats
        WIDTH_MAP.put("Symbol", buildSymbolWidths());
        WIDTH_MAP.put("ZapfDingbats", buildZapfDingbatsWidths());

        // Default encodings
        for (String name : STANDARD_NAMES) {
            if ("Symbol".equals(name)) {
                ENCODING_MAP.put(name, FontEncoding.SYMBOL);
            } else if ("ZapfDingbats".equals(name)) {
                ENCODING_MAP.put(name, FontEncoding.ZAPF_DINGBATS);
            } else {
                ENCODING_MAP.put(name, FontEncoding.STANDARD);
            }
        }
    }

    private StandardFonts() {
        // Utility class
    }

    /**
     * Returns whether the given font name is one of the Standard 14 fonts.
     *
     * @param fontName the base font name (e.g., "Helvetica", "Courier-Bold")
     * @return true if it's a standard font
     */
    public static boolean isStandard(String fontName) {
        return fontName != null && WIDTH_MAP.containsKey(normalizeStandardName(fontName));
    }

    /**
     * Returns the 256-entry width table for the given standard font.
     * Widths are in units of 1/1000 of text space.
     *
     * @param fontName the standard font name
     * @return the width array (256 entries, 0-based by char code), or null if not standard
     */
    public static int[] getWidths(String fontName) {
        if (fontName == null) return null;
        int[] w = WIDTH_MAP.get(normalizeStandardName(fontName));
        return w != null ? w.clone() : null;
    }

    /**
     * Returns the default encoding for the given standard font.
     *
     * @param fontName the standard font name
     * @return the encoding, or null if not a standard font
     */
    public static FontEncoding getEncoding(String fontName) {
        if (fontName == null) return null;
        return ENCODING_MAP.get(normalizeStandardName(fontName));
    }

    /**
     * Normalizes common alternative names to Standard 14 names.
     */
    private static String normalizeStandardName(String name) {
        // Handle common aliases
        switch (name) {
            case "TimesNewRomanPSMT": return "Times-Roman";
            case "TimesNewRomanPS-BoldMT": return "Times-Bold";
            case "TimesNewRomanPS-ItalicMT": return "Times-Italic";
            case "TimesNewRomanPS-BoldItalicMT": return "Times-BoldItalic";
            case "ArialMT": return "Helvetica";
            case "Arial-BoldMT": return "Helvetica-Bold";
            case "Arial-ItalicMT": return "Helvetica-Oblique";
            case "Arial-BoldItalicMT": return "Helvetica-BoldOblique";
            case "CourierNewPSMT": return "Courier";
            case "CourierNewPS-BoldMT": return "Courier-Bold";
            case "CourierNewPS-ItalicMT": return "Courier-Oblique";
            case "CourierNewPS-BoldItalicMT": return "Courier-BoldOblique";
            default: return name;
        }
    }

    // ---- Width tables for Helvetica ----
    private static int[] buildHelveticaWidths() {
        int[] w = new int[256];
        // Default width for undefined codes
        for (int i = 0; i < 256; i++) w[i] = 278; // default to space width
        // ASCII punctuation and digits
        w[32] = 278; // space
        w[33] = 278; w[34] = 355; w[35] = 556; w[36] = 556; w[37] = 889;
        w[38] = 667; w[39] = 191; w[40] = 333; w[41] = 333; w[42] = 389;
        w[43] = 584; w[44] = 278; w[45] = 333; w[46] = 278; w[47] = 278;
        // Digits 0-9
        w[48] = 556; w[49] = 556; w[50] = 556; w[51] = 556; w[52] = 556;
        w[53] = 556; w[54] = 556; w[55] = 556; w[56] = 556; w[57] = 556;
        w[58] = 278; w[59] = 278; w[60] = 584; w[61] = 584; w[62] = 584;
        w[63] = 556; w[64] = 1015;
        // Uppercase A-Z
        w[65] = 667; w[66] = 667; w[67] = 722; w[68] = 722; w[69] = 667;
        w[70] = 611; w[71] = 778; w[72] = 722; w[73] = 278; w[74] = 500;
        w[75] = 667; w[76] = 556; w[77] = 833; w[78] = 722; w[79] = 778;
        w[80] = 667; w[81] = 778; w[82] = 722; w[83] = 667; w[84] = 611;
        w[85] = 722; w[86] = 667; w[87] = 944; w[88] = 667; w[89] = 667;
        w[90] = 611;
        w[91] = 278; w[92] = 278; w[93] = 278; w[94] = 469; w[95] = 556;
        w[96] = 333;
        // Lowercase a-z
        w[97] = 556; w[98] = 556; w[99] = 500; w[100] = 556; w[101] = 556;
        w[102] = 278; w[103] = 556; w[104] = 556; w[105] = 222; w[106] = 222;
        w[107] = 500; w[108] = 222; w[109] = 833; w[110] = 556; w[111] = 556;
        w[112] = 556; w[113] = 556; w[114] = 333; w[115] = 500; w[116] = 278;
        w[117] = 556; w[118] = 500; w[119] = 722; w[120] = 500; w[121] = 500;
        w[122] = 500;
        w[123] = 334; w[124] = 260; w[125] = 334; w[126] = 584;
        // Latin-1 Supplement (high bytes via WinAnsiEncoding)
        w[0x80] = 556; // Euro
        w[0x85] = 1000; // ellipsis
        w[0x91] = 222; w[0x92] = 222; w[0x93] = 333; w[0x94] = 333;
        w[0x95] = 350; w[0x96] = 556; w[0x97] = 1000;
        w[0x99] = 1000; // trademark
        w[0xA0] = 278; w[0xA1] = 333; w[0xA2] = 556; w[0xA3] = 556;
        w[0xA4] = 556; w[0xA5] = 556; w[0xA6] = 260; w[0xA7] = 556;
        w[0xA8] = 333; w[0xA9] = 737; w[0xAA] = 370; w[0xAB] = 556;
        w[0xAC] = 584; w[0xAD] = 333; w[0xAE] = 737; w[0xAF] = 333;
        w[0xB0] = 400; w[0xB1] = 584; w[0xB2] = 333; w[0xB3] = 333;
        w[0xB4] = 333; w[0xB5] = 556; w[0xB6] = 537; w[0xB7] = 278;
        w[0xB8] = 333; w[0xB9] = 333; w[0xBA] = 365; w[0xBB] = 556;
        w[0xBC] = 834; w[0xBD] = 834; w[0xBE] = 834; w[0xBF] = 611;
        // Accented uppercase
        w[0xC0] = 667; w[0xC1] = 667; w[0xC2] = 667; w[0xC3] = 667;
        w[0xC4] = 667; w[0xC5] = 667; w[0xC6] = 1000; w[0xC7] = 722;
        w[0xC8] = 667; w[0xC9] = 667; w[0xCA] = 667; w[0xCB] = 667;
        w[0xCC] = 278; w[0xCD] = 278; w[0xCE] = 278; w[0xCF] = 278;
        w[0xD0] = 722; w[0xD1] = 722; w[0xD2] = 778; w[0xD3] = 778;
        w[0xD4] = 778; w[0xD5] = 778; w[0xD6] = 778; w[0xD7] = 584;
        w[0xD8] = 778; w[0xD9] = 722; w[0xDA] = 722; w[0xDB] = 722;
        w[0xDC] = 722; w[0xDD] = 667; w[0xDE] = 667; w[0xDF] = 611;
        // Accented lowercase
        w[0xE0] = 556; w[0xE1] = 556; w[0xE2] = 556; w[0xE3] = 556;
        w[0xE4] = 556; w[0xE5] = 556; w[0xE6] = 889; w[0xE7] = 500;
        w[0xE8] = 556; w[0xE9] = 556; w[0xEA] = 556; w[0xEB] = 556;
        w[0xEC] = 278; w[0xED] = 278; w[0xEE] = 278; w[0xEF] = 278;
        w[0xF0] = 556; w[0xF1] = 556; w[0xF2] = 556; w[0xF3] = 556;
        w[0xF4] = 556; w[0xF5] = 556; w[0xF6] = 556; w[0xF7] = 584;
        w[0xF8] = 611; w[0xF9] = 556; w[0xFA] = 556; w[0xFB] = 556;
        w[0xFC] = 556; w[0xFD] = 500; w[0xFE] = 556; w[0xFF] = 500;
        return w;
    }

    private static int[] buildHelveticaBoldWidths() {
        int[] w = new int[256];
        for (int i = 0; i < 256; i++) w[i] = 278;
        w[32] = 278; w[33] = 333; w[34] = 474; w[35] = 556; w[36] = 556;
        w[37] = 889; w[38] = 722; w[39] = 238; w[40] = 333; w[41] = 333;
        w[42] = 389; w[43] = 584; w[44] = 278; w[45] = 333; w[46] = 278;
        w[47] = 278;
        for (int i = 48; i <= 57; i++) w[i] = 556;
        w[58] = 333; w[59] = 333; w[60] = 584; w[61] = 584; w[62] = 584;
        w[63] = 611; w[64] = 975;
        w[65] = 722; w[66] = 722; w[67] = 722; w[68] = 722; w[69] = 667;
        w[70] = 611; w[71] = 778; w[72] = 722; w[73] = 278; w[74] = 556;
        w[75] = 722; w[76] = 611; w[77] = 833; w[78] = 722; w[79] = 778;
        w[80] = 667; w[81] = 778; w[82] = 722; w[83] = 667; w[84] = 611;
        w[85] = 722; w[86] = 667; w[87] = 944; w[88] = 667; w[89] = 667;
        w[90] = 611;
        w[91] = 333; w[92] = 278; w[93] = 333; w[94] = 584; w[95] = 556;
        w[96] = 333;
        w[97] = 556; w[98] = 611; w[99] = 556; w[100] = 611; w[101] = 556;
        w[102] = 333; w[103] = 611; w[104] = 611; w[105] = 278; w[106] = 278;
        w[107] = 556; w[108] = 278; w[109] = 889; w[110] = 611; w[111] = 611;
        w[112] = 611; w[113] = 611; w[114] = 389; w[115] = 556; w[116] = 333;
        w[117] = 611; w[118] = 556; w[119] = 778; w[120] = 556; w[121] = 556;
        w[122] = 500;
        w[123] = 389; w[124] = 280; w[125] = 389; w[126] = 584;
        return w;
    }

    private static int[] buildTimesRomanWidths() {
        int[] w = new int[256];
        for (int i = 0; i < 256; i++) w[i] = 250;
        w[32] = 250; w[33] = 333; w[34] = 408; w[35] = 500; w[36] = 500;
        w[37] = 833; w[38] = 778; w[39] = 180; w[40] = 333; w[41] = 333;
        w[42] = 500; w[43] = 564; w[44] = 250; w[45] = 333; w[46] = 250;
        w[47] = 278;
        w[48] = 500; w[49] = 500; w[50] = 500; w[51] = 500; w[52] = 500;
        w[53] = 500; w[54] = 500; w[55] = 500; w[56] = 500; w[57] = 500;
        w[58] = 278; w[59] = 278; w[60] = 564; w[61] = 564; w[62] = 564;
        w[63] = 444; w[64] = 921;
        w[65] = 722; w[66] = 667; w[67] = 667; w[68] = 722; w[69] = 611;
        w[70] = 556; w[71] = 722; w[72] = 722; w[73] = 333; w[74] = 389;
        w[75] = 722; w[76] = 611; w[77] = 889; w[78] = 722; w[79] = 722;
        w[80] = 556; w[81] = 722; w[82] = 667; w[83] = 556; w[84] = 611;
        w[85] = 722; w[86] = 722; w[87] = 944; w[88] = 722; w[89] = 722;
        w[90] = 611;
        w[91] = 333; w[92] = 278; w[93] = 333; w[94] = 469; w[95] = 500;
        w[96] = 333;
        w[97] = 444; w[98] = 500; w[99] = 444; w[100] = 500; w[101] = 444;
        w[102] = 333; w[103] = 500; w[104] = 500; w[105] = 278; w[106] = 278;
        w[107] = 500; w[108] = 278; w[109] = 778; w[110] = 500; w[111] = 500;
        w[112] = 500; w[113] = 500; w[114] = 333; w[115] = 389; w[116] = 278;
        w[117] = 500; w[118] = 500; w[119] = 722; w[120] = 500; w[121] = 500;
        w[122] = 444;
        w[123] = 480; w[124] = 200; w[125] = 480; w[126] = 541;
        return w;
    }

    private static int[] buildTimesBoldWidths() {
        int[] w = new int[256];
        for (int i = 0; i < 256; i++) w[i] = 250;
        w[32] = 250; w[33] = 333; w[34] = 555; w[35] = 500; w[36] = 500;
        w[37] = 1000; w[38] = 833; w[39] = 278; w[40] = 333; w[41] = 333;
        w[42] = 500; w[43] = 570; w[44] = 250; w[45] = 333; w[46] = 250;
        w[47] = 278;
        for (int i = 48; i <= 57; i++) w[i] = 500;
        w[58] = 333; w[59] = 333; w[60] = 570; w[61] = 570; w[62] = 570;
        w[63] = 500; w[64] = 930;
        w[65] = 722; w[66] = 667; w[67] = 722; w[68] = 722; w[69] = 667;
        w[70] = 611; w[71] = 778; w[72] = 778; w[73] = 389; w[74] = 500;
        w[75] = 778; w[76] = 667; w[77] = 944; w[78] = 722; w[79] = 778;
        w[80] = 611; w[81] = 778; w[82] = 722; w[83] = 556; w[84] = 667;
        w[85] = 722; w[86] = 722; w[87] = 1000; w[88] = 722; w[89] = 722;
        w[90] = 667;
        w[91] = 333; w[92] = 278; w[93] = 333; w[94] = 581; w[95] = 500;
        w[96] = 333;
        w[97] = 500; w[98] = 556; w[99] = 444; w[100] = 556; w[101] = 444;
        w[102] = 333; w[103] = 500; w[104] = 556; w[105] = 278; w[106] = 333;
        w[107] = 556; w[108] = 278; w[109] = 833; w[110] = 556; w[111] = 500;
        w[112] = 556; w[113] = 556; w[114] = 444; w[115] = 389; w[116] = 333;
        w[117] = 556; w[118] = 500; w[119] = 722; w[120] = 500; w[121] = 500;
        w[122] = 444;
        w[123] = 394; w[124] = 220; w[125] = 394; w[126] = 520;
        return w;
    }

    private static int[] buildTimesItalicWidths() {
        int[] w = new int[256];
        for (int i = 0; i < 256; i++) w[i] = 250;
        w[32] = 250; w[33] = 333; w[34] = 420; w[35] = 500; w[36] = 500;
        w[37] = 833; w[38] = 778; w[39] = 214; w[40] = 333; w[41] = 333;
        w[42] = 500; w[43] = 675; w[44] = 250; w[45] = 333; w[46] = 250;
        w[47] = 278;
        for (int i = 48; i <= 57; i++) w[i] = 500;
        w[58] = 333; w[59] = 333; w[60] = 675; w[61] = 675; w[62] = 675;
        w[63] = 500; w[64] = 920;
        w[65] = 611; w[66] = 611; w[67] = 667; w[68] = 722; w[69] = 611;
        w[70] = 611; w[71] = 722; w[72] = 722; w[73] = 333; w[74] = 444;
        w[75] = 667; w[76] = 556; w[77] = 833; w[78] = 667; w[79] = 722;
        w[80] = 611; w[81] = 722; w[82] = 611; w[83] = 500; w[84] = 556;
        w[85] = 722; w[86] = 611; w[87] = 833; w[88] = 611; w[89] = 556;
        w[90] = 556;
        w[91] = 389; w[92] = 278; w[93] = 389; w[94] = 422; w[95] = 500;
        w[96] = 333;
        w[97] = 500; w[98] = 500; w[99] = 444; w[100] = 500; w[101] = 444;
        w[102] = 278; w[103] = 500; w[104] = 500; w[105] = 278; w[106] = 278;
        w[107] = 444; w[108] = 278; w[109] = 722; w[110] = 500; w[111] = 500;
        w[112] = 500; w[113] = 500; w[114] = 389; w[115] = 389; w[116] = 278;
        w[117] = 500; w[118] = 444; w[119] = 667; w[120] = 444; w[121] = 444;
        w[122] = 389;
        w[123] = 400; w[124] = 275; w[125] = 400; w[126] = 541;
        return w;
    }

    private static int[] buildTimesBoldItalicWidths() {
        int[] w = new int[256];
        for (int i = 0; i < 256; i++) w[i] = 250;
        w[32] = 250; w[33] = 389; w[34] = 555; w[35] = 500; w[36] = 500;
        w[37] = 833; w[38] = 778; w[39] = 278; w[40] = 333; w[41] = 333;
        w[42] = 500; w[43] = 570; w[44] = 250; w[45] = 333; w[46] = 250;
        w[47] = 278;
        for (int i = 48; i <= 57; i++) w[i] = 500;
        w[58] = 333; w[59] = 333; w[60] = 570; w[61] = 570; w[62] = 570;
        w[63] = 500; w[64] = 832;
        w[65] = 667; w[66] = 667; w[67] = 667; w[68] = 722; w[69] = 667;
        w[70] = 667; w[71] = 722; w[72] = 778; w[73] = 389; w[74] = 500;
        w[75] = 667; w[76] = 611; w[77] = 889; w[78] = 722; w[79] = 722;
        w[80] = 611; w[81] = 722; w[82] = 667; w[83] = 556; w[84] = 611;
        w[85] = 722; w[86] = 667; w[87] = 889; w[88] = 667; w[89] = 611;
        w[90] = 611;
        w[91] = 333; w[92] = 278; w[93] = 333; w[94] = 570; w[95] = 500;
        w[96] = 333;
        w[97] = 500; w[98] = 500; w[99] = 444; w[100] = 500; w[101] = 444;
        w[102] = 333; w[103] = 500; w[104] = 556; w[105] = 278; w[106] = 278;
        w[107] = 500; w[108] = 278; w[109] = 778; w[110] = 556; w[111] = 500;
        w[112] = 500; w[113] = 500; w[114] = 389; w[115] = 389; w[116] = 278;
        w[117] = 556; w[118] = 444; w[119] = 667; w[120] = 500; w[121] = 444;
        w[122] = 389;
        w[123] = 348; w[124] = 220; w[125] = 348; w[126] = 570;
        return w;
    }

    private static int[] buildSymbolWidths() {
        int[] w = new int[256];
        for (int i = 0; i < 256; i++) w[i] = 250;
        w[32] = 250; w[33] = 333; w[34] = 713; w[35] = 500; w[36] = 549;
        w[37] = 833; w[38] = 778; w[39] = 439; w[40] = 333; w[41] = 333;
        w[42] = 500; w[43] = 549; w[44] = 250; w[45] = 549; w[46] = 250;
        w[47] = 278;
        for (int i = 48; i <= 57; i++) w[i] = 500;
        return w;
    }

    private static int[] buildZapfDingbatsWidths() {
        int[] w = new int[256];
        for (int i = 0; i < 256; i++) w[i] = 278;
        w[32] = 278;
        return w;
    }
}
