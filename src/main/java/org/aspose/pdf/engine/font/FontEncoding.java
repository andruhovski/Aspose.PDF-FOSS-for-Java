package org.aspose.pdf.engine.font;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfName;

import java.util.logging.Logger;

/**
 * Maps character codes (0-255) to glyph names and Unicode codepoints.
 * <p>
 * Provides three built-in encodings per ISO 32000-1:2008, §9.6.6 and Annex D:
 * WinAnsiEncoding, MacRomanEncoding, and StandardEncoding.
 * Supports /Differences array overlay for custom encoding modifications.
 * </p>
 *
 * @see AdobeGlyphList
 */
public class FontEncoding {

    private static final Logger LOG = Logger.getLogger(FontEncoding.class.getName());

    private final String name;
    private final String[] codeToName = new String[256];

    /** WinAnsiEncoding singleton (ISO 32000, Table D.1). */
    public static final FontEncoding WIN_ANSI;
    /** MacRomanEncoding singleton (ISO 32000, Table D.1). */
    public static final FontEncoding MAC_ROMAN;
    /** StandardEncoding singleton (ISO 32000, Table D.1). */
    public static final FontEncoding STANDARD;
    /** MacExpertEncoding singleton. */
    public static final FontEncoding MAC_EXPERT;
    /** Symbol encoding for Symbol font. */
    public static final FontEncoding SYMBOL;
    /** ZapfDingbats encoding. */
    public static final FontEncoding ZAPF_DINGBATS;

    static {
        WIN_ANSI = buildWinAnsi();
        MAC_ROMAN = buildMacRoman();
        STANDARD = buildStandard();
        MAC_EXPERT = buildMacExpert();
        SYMBOL = buildSymbol();
        ZAPF_DINGBATS = buildZapfDingbats();
    }

    /**
     * Creates a new FontEncoding with the given name.
     *
     * @param name the encoding name
     */
    public FontEncoding(String name) {
        this.name = name != null ? name : "Custom";
    }

    /**
     * Returns a built-in encoding by name.
     *
     * @param name the encoding name ("WinAnsiEncoding", "MacRomanEncoding", "StandardEncoding")
     * @return the encoding instance, or null if unknown
     */
    public static FontEncoding getInstance(String name) {
        if (name == null) return null;
        switch (name) {
            case "WinAnsiEncoding": return WIN_ANSI;
            case "MacRomanEncoding": return MAC_ROMAN;
            case "StandardEncoding": return STANDARD;
            case "MacExpertEncoding": return MAC_EXPERT;
            default: return null;
        }
    }

    /**
     * Creates a FontEncoding from a PDF /Encoding dictionary.
     * <p>
     * Reads /BaseEncoding name and /Differences array per §9.6.6.1.
     * </p>
     *
     * @param encDict the encoding dictionary
     * @return the constructed encoding
     */
    public static FontEncoding fromDictionary(PdfDictionary encDict) {
        if (encDict == null) return null;

        // Get base encoding
        FontEncoding base = STANDARD;
        String baseName = encDict.getNameAsString("BaseEncoding");
        if (baseName != null) {
            FontEncoding named = getInstance(baseName);
            if (named != null) {
                base = named;
            }
        }

        // Apply /Differences if present
        PdfBase diffVal = encDict.get("Differences");
        if (diffVal instanceof PdfArray) {
            return base.withDifferences((PdfArray) diffVal);
        }

        return base;
    }

    /**
     * Returns the glyph name for the given character code.
     *
     * @param charCode the character code (0-255)
     * @return the glyph name, or null if unmapped
     */
    public String getGlyphName(int charCode) {
        if (charCode < 0 || charCode > 255) return null;
        return codeToName[charCode];
    }

    /**
     * Returns the Unicode codepoint for the given character code.
     * <p>
     * Looks up the glyph name, then maps to Unicode via {@link AdobeGlyphList}.
     * Falls back to identity mapping if the glyph name is not found.
     * </p>
     *
     * @param charCode the character code (0-255)
     * @return the Unicode codepoint
     */
    public int getUnicode(int charCode) {
        if (charCode < 0 || charCode > 255) return charCode;
        String glyphName = codeToName[charCode];
        if (glyphName == null || ".notdef".equals(glyphName)) {
            return charCode; // identity fallback
        }
        int unicode = AdobeGlyphList.getUnicode(glyphName);
        return unicode >= 0 ? unicode : charCode;
    }

    /**
     * Creates a new encoding by cloning this one and applying a /Differences array.
     * <p>
     * The /Differences array format is: [code1 name1 name2 ... codeN nameN ...],
     * where integer entries set the current code, and name entries assign glyph
     * names to consecutive codes (ISO 32000, §9.6.6.1).
     * </p>
     *
     * @param differences the /Differences PdfArray
     * @return a new encoding with differences applied
     */
    public FontEncoding withDifferences(PdfArray differences) {
        FontEncoding result = new FontEncoding(this.name + "+Diff");
        System.arraycopy(this.codeToName, 0, result.codeToName, 0, 256);
        int currentCode = 0;
        for (int i = 0; i < differences.size(); i++) {
            PdfBase elem = differences.get(i);
            if (elem instanceof PdfInteger) {
                currentCode = ((PdfInteger) elem).intValue();
            } else if (elem instanceof PdfName) {
                if (currentCode >= 0 && currentCode < 256) {
                    result.codeToName[currentCode] = ((PdfName) elem).getName();
                    currentCode++;
                }
            }
        }
        return result;
    }

    /**
     * Returns the encoding name.
     *
     * @return the encoding name
     */
    public String getName() {
        return name;
    }

    // ---- Builder methods for built-in encodings ----

    private static FontEncoding buildWinAnsi() {
        FontEncoding enc = new FontEncoding("WinAnsiEncoding");
        // ASCII range 0x20-0x7E
        setAsciiRange(enc);
        // Non-ASCII WinAnsiEncoding mappings (ISO 32000, Table D.1)
        enc.codeToName[0x80] = "Euro";
        enc.codeToName[0x82] = "quotesinglbase";
        enc.codeToName[0x83] = "florin";
        enc.codeToName[0x84] = "quotedblbase";
        enc.codeToName[0x85] = "ellipsis";
        enc.codeToName[0x86] = "dagger";
        enc.codeToName[0x87] = "daggerdbl";
        enc.codeToName[0x88] = "circumflex";
        enc.codeToName[0x89] = "perthousand";
        enc.codeToName[0x8A] = "Scaron";
        enc.codeToName[0x8B] = "guilsinglleft";
        enc.codeToName[0x8C] = "OE";
        enc.codeToName[0x8E] = "Zcaron";
        enc.codeToName[0x91] = "quoteleft";
        enc.codeToName[0x92] = "quoteright";
        enc.codeToName[0x93] = "quotedblleft";
        enc.codeToName[0x94] = "quotedblright";
        enc.codeToName[0x95] = "bullet";
        enc.codeToName[0x96] = "endash";
        enc.codeToName[0x97] = "emdash";
        enc.codeToName[0x98] = "tilde";
        enc.codeToName[0x99] = "trademark";
        enc.codeToName[0x9A] = "scaron";
        enc.codeToName[0x9B] = "guilsinglright";
        enc.codeToName[0x9C] = "oe";
        enc.codeToName[0x9E] = "zcaron";
        enc.codeToName[0x9F] = "Ydieresis";
        enc.codeToName[0xA0] = "space"; // NBSP
        enc.codeToName[0xA1] = "exclamdown";
        enc.codeToName[0xA2] = "cent";
        enc.codeToName[0xA3] = "sterling";
        enc.codeToName[0xA4] = "currency";
        enc.codeToName[0xA5] = "yen";
        enc.codeToName[0xA6] = "brokenbar";
        enc.codeToName[0xA7] = "section";
        enc.codeToName[0xA8] = "dieresis";
        enc.codeToName[0xA9] = "copyright";
        enc.codeToName[0xAA] = "ordfeminine";
        enc.codeToName[0xAB] = "guillemotleft";
        enc.codeToName[0xAC] = "logicalnot";
        enc.codeToName[0xAD] = "softhyphen";
        enc.codeToName[0xAE] = "registered";
        enc.codeToName[0xAF] = "macron";
        enc.codeToName[0xB0] = "degree";
        enc.codeToName[0xB1] = "plusminus";
        enc.codeToName[0xB2] = "twosuperior";
        enc.codeToName[0xB3] = "threesuperior";
        enc.codeToName[0xB4] = "acute";
        enc.codeToName[0xB5] = "mu";
        enc.codeToName[0xB6] = "paragraph";
        enc.codeToName[0xB7] = "periodcentered";
        enc.codeToName[0xB8] = "cedilla";
        enc.codeToName[0xB9] = "onesuperior";
        enc.codeToName[0xBA] = "ordmasculine";
        enc.codeToName[0xBB] = "guillemotright";
        enc.codeToName[0xBC] = "onequarter";
        enc.codeToName[0xBD] = "onehalf";
        enc.codeToName[0xBE] = "threequarters";
        enc.codeToName[0xBF] = "questiondown";
        enc.codeToName[0xC0] = "Agrave";
        enc.codeToName[0xC1] = "Aacute";
        enc.codeToName[0xC2] = "Acircumflex";
        enc.codeToName[0xC3] = "Atilde";
        enc.codeToName[0xC4] = "Adieresis";
        enc.codeToName[0xC5] = "Aring";
        enc.codeToName[0xC6] = "AE";
        enc.codeToName[0xC7] = "Ccedilla";
        enc.codeToName[0xC8] = "Egrave";
        enc.codeToName[0xC9] = "Eacute";
        enc.codeToName[0xCA] = "Ecircumflex";
        enc.codeToName[0xCB] = "Edieresis";
        enc.codeToName[0xCC] = "Igrave";
        enc.codeToName[0xCD] = "Iacute";
        enc.codeToName[0xCE] = "Icircumflex";
        enc.codeToName[0xCF] = "Idieresis";
        enc.codeToName[0xD0] = "Eth";
        enc.codeToName[0xD1] = "Ntilde";
        enc.codeToName[0xD2] = "Ograve";
        enc.codeToName[0xD3] = "Oacute";
        enc.codeToName[0xD4] = "Ocircumflex";
        enc.codeToName[0xD5] = "Otilde";
        enc.codeToName[0xD6] = "Odieresis";
        enc.codeToName[0xD7] = "multiply";
        enc.codeToName[0xD8] = "Oslash";
        enc.codeToName[0xD9] = "Ugrave";
        enc.codeToName[0xDA] = "Uacute";
        enc.codeToName[0xDB] = "Ucircumflex";
        enc.codeToName[0xDC] = "Udieresis";
        enc.codeToName[0xDD] = "Yacute";
        enc.codeToName[0xDE] = "Thorn";
        enc.codeToName[0xDF] = "germandbls";
        enc.codeToName[0xE0] = "agrave";
        enc.codeToName[0xE1] = "aacute";
        enc.codeToName[0xE2] = "acircumflex";
        enc.codeToName[0xE3] = "atilde";
        enc.codeToName[0xE4] = "adieresis";
        enc.codeToName[0xE5] = "aring";
        enc.codeToName[0xE6] = "ae";
        enc.codeToName[0xE7] = "ccedilla";
        enc.codeToName[0xE8] = "egrave";
        enc.codeToName[0xE9] = "eacute";
        enc.codeToName[0xEA] = "ecircumflex";
        enc.codeToName[0xEB] = "edieresis";
        enc.codeToName[0xEC] = "igrave";
        enc.codeToName[0xED] = "iacute";
        enc.codeToName[0xEE] = "icircumflex";
        enc.codeToName[0xEF] = "idieresis";
        enc.codeToName[0xF0] = "eth";
        enc.codeToName[0xF1] = "ntilde";
        enc.codeToName[0xF2] = "ograve";
        enc.codeToName[0xF3] = "oacute";
        enc.codeToName[0xF4] = "ocircumflex";
        enc.codeToName[0xF5] = "otilde";
        enc.codeToName[0xF6] = "odieresis";
        enc.codeToName[0xF7] = "divide";
        enc.codeToName[0xF8] = "oslash";
        enc.codeToName[0xF9] = "ugrave";
        enc.codeToName[0xFA] = "uacute";
        enc.codeToName[0xFB] = "ucircumflex";
        enc.codeToName[0xFC] = "udieresis";
        enc.codeToName[0xFD] = "yacute";
        enc.codeToName[0xFE] = "thorn";
        enc.codeToName[0xFF] = "ydieresis";
        return enc;
    }

    private static FontEncoding buildMacRoman() {
        FontEncoding enc = new FontEncoding("MacRomanEncoding");
        setAsciiRange(enc);
        enc.codeToName[0x80] = "Adieresis";
        enc.codeToName[0x81] = "Aring";
        enc.codeToName[0x82] = "Ccedilla";
        enc.codeToName[0x83] = "Eacute";
        enc.codeToName[0x84] = "Ntilde";
        enc.codeToName[0x85] = "Odieresis";
        enc.codeToName[0x86] = "Udieresis";
        enc.codeToName[0x87] = "aacute";
        enc.codeToName[0x88] = "agrave";
        enc.codeToName[0x89] = "acircumflex";
        enc.codeToName[0x8A] = "adieresis";
        enc.codeToName[0x8B] = "atilde";
        enc.codeToName[0x8C] = "aring";
        enc.codeToName[0x8D] = "ccedilla";
        enc.codeToName[0x8E] = "eacute";
        enc.codeToName[0x8F] = "egrave";
        enc.codeToName[0x90] = "ecircumflex";
        enc.codeToName[0x91] = "edieresis";
        enc.codeToName[0x92] = "iacute";
        enc.codeToName[0x93] = "igrave";
        enc.codeToName[0x94] = "icircumflex";
        enc.codeToName[0x95] = "idieresis";
        enc.codeToName[0x96] = "ntilde";
        enc.codeToName[0x97] = "oacute";
        enc.codeToName[0x98] = "ograve";
        enc.codeToName[0x99] = "ocircumflex";
        enc.codeToName[0x9A] = "odieresis";
        enc.codeToName[0x9B] = "otilde";
        enc.codeToName[0x9C] = "uacute";
        enc.codeToName[0x9D] = "ugrave";
        enc.codeToName[0x9E] = "ucircumflex";
        enc.codeToName[0x9F] = "udieresis";
        enc.codeToName[0xA0] = "dagger";
        enc.codeToName[0xA1] = "degree";
        enc.codeToName[0xA2] = "cent";
        enc.codeToName[0xA3] = "sterling";
        enc.codeToName[0xA4] = "section";
        enc.codeToName[0xA5] = "bullet";
        enc.codeToName[0xA6] = "paragraph";
        enc.codeToName[0xA7] = "germandbls";
        enc.codeToName[0xA8] = "registered";
        enc.codeToName[0xA9] = "copyright";
        enc.codeToName[0xAA] = "trademark";
        enc.codeToName[0xAB] = "acute";
        enc.codeToName[0xAC] = "dieresis";
        enc.codeToName[0xAD] = "notequal";
        enc.codeToName[0xAE] = "AE";
        enc.codeToName[0xAF] = "Oslash";
        enc.codeToName[0xB0] = "infinity";
        enc.codeToName[0xB1] = "plusminus";
        enc.codeToName[0xB2] = "lessequal";
        enc.codeToName[0xB3] = "greaterequal";
        enc.codeToName[0xB4] = "yen";
        enc.codeToName[0xB5] = "mu";
        enc.codeToName[0xB6] = "partialdiff";
        enc.codeToName[0xB7] = "summation";
        enc.codeToName[0xB8] = "product";
        enc.codeToName[0xB9] = "pi";
        enc.codeToName[0xBA] = "integral";
        enc.codeToName[0xBB] = "ordfeminine";
        enc.codeToName[0xBC] = "ordmasculine";
        MAP_put_macRomanRemainder(enc);
        return enc;
    }

    private static void MAP_put_macRomanRemainder(FontEncoding enc) {
        enc.codeToName[0xBD] = "Omega";
        enc.codeToName[0xBE] = "ae";
        enc.codeToName[0xBF] = "oslash";
        enc.codeToName[0xC0] = "questiondown";
        enc.codeToName[0xC1] = "exclamdown";
        enc.codeToName[0xC2] = "logicalnot";
        enc.codeToName[0xC3] = "radical";
        enc.codeToName[0xC4] = "florin";
        enc.codeToName[0xC5] = "approxequal";
        enc.codeToName[0xC6] = "Delta";
        enc.codeToName[0xC7] = "guillemotleft";
        enc.codeToName[0xC8] = "guillemotright";
        enc.codeToName[0xC9] = "ellipsis";
        enc.codeToName[0xCA] = "space"; // NBSP
        enc.codeToName[0xCB] = "Agrave";
        enc.codeToName[0xCC] = "Atilde";
        enc.codeToName[0xCD] = "Otilde";
        enc.codeToName[0xCE] = "OE";
        enc.codeToName[0xCF] = "oe";
        enc.codeToName[0xD0] = "endash";
        enc.codeToName[0xD1] = "emdash";
        enc.codeToName[0xD2] = "quotedblleft";
        enc.codeToName[0xD3] = "quotedblright";
        enc.codeToName[0xD4] = "quoteleft";
        enc.codeToName[0xD5] = "quoteright";
        enc.codeToName[0xD6] = "divide";
        enc.codeToName[0xD7] = "lozenge";
        enc.codeToName[0xD8] = "ydieresis";
        enc.codeToName[0xD9] = "Ydieresis";
        enc.codeToName[0xDA] = "fraction";
        enc.codeToName[0xDB] = "Euro";
        enc.codeToName[0xDC] = "guilsinglleft";
        enc.codeToName[0xDD] = "guilsinglright";
        enc.codeToName[0xDE] = "fi";
        enc.codeToName[0xDF] = "fl";
        enc.codeToName[0xE0] = "daggerdbl";
        enc.codeToName[0xE1] = "periodcentered";
        enc.codeToName[0xE2] = "quotesinglbase";
        enc.codeToName[0xE3] = "quotedblbase";
        enc.codeToName[0xE4] = "perthousand";
        enc.codeToName[0xE5] = "Acircumflex";
        enc.codeToName[0xE6] = "Ecircumflex";
        enc.codeToName[0xE7] = "Aacute";
        enc.codeToName[0xE8] = "Edieresis";
        enc.codeToName[0xE9] = "Egrave";
        enc.codeToName[0xEA] = "Iacute";
        enc.codeToName[0xEB] = "Icircumflex";
        enc.codeToName[0xEC] = "Idieresis";
        enc.codeToName[0xED] = "Igrave";
        enc.codeToName[0xEE] = "Oacute";
        enc.codeToName[0xEF] = "Ocircumflex";
        enc.codeToName[0xF0] = "apple"; // Apple logo (mapped to 0xF8FF in Mac)
        enc.codeToName[0xF1] = "Ograve";
        enc.codeToName[0xF2] = "Uacute";
        enc.codeToName[0xF3] = "Ucircumflex";
        enc.codeToName[0xF4] = "Ugrave";
        enc.codeToName[0xF5] = "dotlessi";
        enc.codeToName[0xF6] = "circumflex";
        enc.codeToName[0xF7] = "tilde";
        enc.codeToName[0xF8] = "macron";
        enc.codeToName[0xF9] = "breve";
        enc.codeToName[0xFA] = "dotaccent";
        enc.codeToName[0xFB] = "ring";
        enc.codeToName[0xFC] = "cedilla";
        enc.codeToName[0xFD] = "hungarumlaut";
        enc.codeToName[0xFE] = "ogonek";
        enc.codeToName[0xFF] = "caron";
    }

    private static FontEncoding buildStandard() {
        FontEncoding enc = new FontEncoding("StandardEncoding");
        // Standard Encoding (ISO 32000, Table D.1)
        enc.codeToName[0x20] = "space";
        enc.codeToName[0x21] = "exclam";
        enc.codeToName[0x22] = "quotedbl";
        enc.codeToName[0x23] = "numbersign";
        enc.codeToName[0x24] = "dollar";
        enc.codeToName[0x25] = "percent";
        enc.codeToName[0x26] = "ampersand";
        enc.codeToName[0x27] = "quoteright";
        enc.codeToName[0x28] = "parenleft";
        enc.codeToName[0x29] = "parenright";
        enc.codeToName[0x2A] = "asterisk";
        enc.codeToName[0x2B] = "plus";
        enc.codeToName[0x2C] = "comma";
        enc.codeToName[0x2D] = "hyphen";
        enc.codeToName[0x2E] = "period";
        enc.codeToName[0x2F] = "slash";
        // 0x30-0x39: digits
        for (int i = 0; i <= 9; i++) {
            enc.codeToName[0x30 + i] = new String[] {
                "zero", "one", "two", "three", "four",
                "five", "six", "seven", "eight", "nine"
            }[i];
        }
        enc.codeToName[0x3A] = "colon";
        enc.codeToName[0x3B] = "semicolon";
        enc.codeToName[0x3C] = "less";
        enc.codeToName[0x3D] = "equal";
        enc.codeToName[0x3E] = "greater";
        enc.codeToName[0x3F] = "question";
        enc.codeToName[0x40] = "at";
        // 0x41-0x5A: A-Z
        for (int i = 0; i < 26; i++) {
            enc.codeToName[0x41 + i] = String.valueOf((char) ('A' + i));
        }
        enc.codeToName[0x5B] = "bracketleft";
        enc.codeToName[0x5C] = "backslash";
        enc.codeToName[0x5D] = "bracketright";
        enc.codeToName[0x5E] = "asciicircum";
        enc.codeToName[0x5F] = "underscore";
        enc.codeToName[0x60] = "quoteleft";
        // 0x61-0x7A: a-z
        for (int i = 0; i < 26; i++) {
            enc.codeToName[0x61 + i] = String.valueOf((char) ('a' + i));
        }
        enc.codeToName[0x7B] = "braceleft";
        enc.codeToName[0x7C] = "bar";
        enc.codeToName[0x7D] = "braceright";
        enc.codeToName[0x7E] = "asciitilde";
        // High codes in StandardEncoding
        enc.codeToName[0xA1] = "exclamdown";
        enc.codeToName[0xA2] = "cent";
        enc.codeToName[0xA3] = "sterling";
        enc.codeToName[0xA4] = "fraction";
        enc.codeToName[0xA5] = "yen";
        enc.codeToName[0xA6] = "florin";
        enc.codeToName[0xA7] = "section";
        enc.codeToName[0xA8] = "currency";
        enc.codeToName[0xA9] = "quotesingle";
        enc.codeToName[0xAA] = "quotedblleft";
        enc.codeToName[0xAB] = "guillemotleft";
        enc.codeToName[0xAC] = "guilsinglleft";
        enc.codeToName[0xAD] = "guilsinglright";
        enc.codeToName[0xAE] = "fi";
        enc.codeToName[0xAF] = "fl";
        enc.codeToName[0xB1] = "endash";
        enc.codeToName[0xB2] = "dagger";
        enc.codeToName[0xB3] = "daggerdbl";
        enc.codeToName[0xB4] = "periodcentered";
        enc.codeToName[0xB6] = "paragraph";
        enc.codeToName[0xB7] = "bullet";
        enc.codeToName[0xB8] = "quotesinglbase";
        enc.codeToName[0xB9] = "quotedblbase";
        enc.codeToName[0xBA] = "quotedblright";
        enc.codeToName[0xBB] = "guillemotright";
        enc.codeToName[0xBC] = "ellipsis";
        enc.codeToName[0xBD] = "perthousand";
        enc.codeToName[0xBF] = "questiondown";
        enc.codeToName[0xC1] = "grave";
        enc.codeToName[0xC2] = "acute";
        enc.codeToName[0xC3] = "circumflex";
        enc.codeToName[0xC4] = "tilde";
        enc.codeToName[0xC5] = "macron";
        enc.codeToName[0xC6] = "breve";
        enc.codeToName[0xC7] = "dotaccent";
        enc.codeToName[0xC8] = "dieresis";
        enc.codeToName[0xCA] = "ring";
        enc.codeToName[0xCB] = "cedilla";
        enc.codeToName[0xCC] = "hungarumlaut";
        enc.codeToName[0xCD] = "ogonek";
        enc.codeToName[0xCE] = "caron";
        enc.codeToName[0xD0] = "emdash";
        enc.codeToName[0xE1] = "AE";
        enc.codeToName[0xE3] = "ordfeminine";
        enc.codeToName[0xE8] = "Lslash";
        enc.codeToName[0xE9] = "Oslash";
        enc.codeToName[0xEA] = "OE";
        enc.codeToName[0xEB] = "ordmasculine";
        enc.codeToName[0xF1] = "ae";
        enc.codeToName[0xF5] = "dotlessi";
        enc.codeToName[0xF8] = "lslash";
        enc.codeToName[0xF9] = "oslash";
        enc.codeToName[0xFA] = "oe";
        enc.codeToName[0xFB] = "germandbls";
        return enc;
    }

    private static FontEncoding buildMacExpert() {
        // Minimal stub — MacExpertEncoding is rare; provide enough for basic operation
        FontEncoding enc = new FontEncoding("MacExpertEncoding");
        setAsciiRange(enc);
        return enc;
    }

    private static FontEncoding buildSymbol() {
        FontEncoding enc = new FontEncoding("SymbolEncoding");
        // Symbol font uses its own encoding (ISO 32000, Table D.5)
        enc.codeToName[0x20] = "space";
        enc.codeToName[0x21] = "exclam";
        enc.codeToName[0x22] = "universal";
        enc.codeToName[0x23] = "numbersign";
        enc.codeToName[0x24] = "existential";
        enc.codeToName[0x25] = "percent";
        enc.codeToName[0x26] = "ampersand";
        enc.codeToName[0x27] = "suchthat";
        enc.codeToName[0x28] = "parenleft";
        enc.codeToName[0x29] = "parenright";
        enc.codeToName[0x2A] = "asteriskmath";
        enc.codeToName[0x2B] = "plus";
        enc.codeToName[0x2C] = "comma";
        enc.codeToName[0x2D] = "minus";
        enc.codeToName[0x2E] = "period";
        enc.codeToName[0x2F] = "slash";
        for (int i = 0; i <= 9; i++) {
            enc.codeToName[0x30 + i] = new String[] {
                "zero", "one", "two", "three", "four",
                "five", "six", "seven", "eight", "nine"
            }[i];
        }
        enc.codeToName[0x3A] = "colon";
        enc.codeToName[0x3B] = "semicolon";
        enc.codeToName[0x3C] = "less";
        enc.codeToName[0x3D] = "equal";
        enc.codeToName[0x3E] = "greater";
        enc.codeToName[0x3F] = "question";
        enc.codeToName[0x40] = "congruent";
        enc.codeToName[0x41] = "Alpha"; enc.codeToName[0x42] = "Beta";
        enc.codeToName[0x43] = "Chi"; enc.codeToName[0x44] = "Delta";
        enc.codeToName[0x45] = "Epsilon"; enc.codeToName[0x46] = "Phi";
        enc.codeToName[0x47] = "Gamma"; enc.codeToName[0x48] = "Eta";
        enc.codeToName[0x49] = "Iota"; enc.codeToName[0x4B] = "Kappa";
        enc.codeToName[0x4C] = "Lambda"; enc.codeToName[0x4D] = "Mu";
        enc.codeToName[0x4E] = "Nu"; enc.codeToName[0x4F] = "Omicron";
        enc.codeToName[0x50] = "Pi"; enc.codeToName[0x51] = "Theta";
        enc.codeToName[0x52] = "Rho"; enc.codeToName[0x53] = "Sigma";
        enc.codeToName[0x54] = "Tau"; enc.codeToName[0x55] = "Upsilon";
        enc.codeToName[0x57] = "Omega"; enc.codeToName[0x58] = "Xi";
        enc.codeToName[0x59] = "Psi"; enc.codeToName[0x5A] = "Zeta";
        enc.codeToName[0x5B] = "bracketleft";
        enc.codeToName[0x5D] = "bracketright";
        enc.codeToName[0x61] = "alpha"; enc.codeToName[0x62] = "beta";
        enc.codeToName[0x63] = "chi"; enc.codeToName[0x64] = "delta";
        enc.codeToName[0x65] = "epsilon"; enc.codeToName[0x66] = "phi";
        enc.codeToName[0x67] = "gamma"; enc.codeToName[0x68] = "eta";
        enc.codeToName[0x69] = "iota"; enc.codeToName[0x6B] = "kappa";
        enc.codeToName[0x6C] = "lambda"; enc.codeToName[0x6D] = "mu";
        enc.codeToName[0x6E] = "nu"; enc.codeToName[0x6F] = "omicron";
        enc.codeToName[0x70] = "pi"; enc.codeToName[0x71] = "theta";
        enc.codeToName[0x72] = "rho"; enc.codeToName[0x73] = "sigma";
        enc.codeToName[0x74] = "tau"; enc.codeToName[0x75] = "upsilon";
        enc.codeToName[0x77] = "omega"; enc.codeToName[0x78] = "xi";
        enc.codeToName[0x79] = "psi"; enc.codeToName[0x7A] = "zeta";
        enc.codeToName[0x7B] = "braceleft";
        enc.codeToName[0x7C] = "bar";
        enc.codeToName[0x7D] = "braceright";
        return enc;
    }

    private static FontEncoding buildZapfDingbats() {
        // ZapfDingbats has a unique encoding; minimal for now
        FontEncoding enc = new FontEncoding("ZapfDingbatsEncoding");
        enc.codeToName[0x20] = "space";
        // ZapfDingbats character codes map to specific dingbat glyphs
        // For text extraction, these typically don't produce readable text
        return enc;
    }

    /**
     * Sets the standard ASCII glyph names for codes 0x20-0x7E.
     */
    private static void setAsciiRange(FontEncoding enc) {
        enc.codeToName[0x20] = "space";
        enc.codeToName[0x21] = "exclam";
        enc.codeToName[0x22] = "quotedbl";
        enc.codeToName[0x23] = "numbersign";
        enc.codeToName[0x24] = "dollar";
        enc.codeToName[0x25] = "percent";
        enc.codeToName[0x26] = "ampersand";
        enc.codeToName[0x27] = "quotesingle";
        enc.codeToName[0x28] = "parenleft";
        enc.codeToName[0x29] = "parenright";
        enc.codeToName[0x2A] = "asterisk";
        enc.codeToName[0x2B] = "plus";
        enc.codeToName[0x2C] = "comma";
        enc.codeToName[0x2D] = "hyphen";
        enc.codeToName[0x2E] = "period";
        enc.codeToName[0x2F] = "slash";
        String[] digitNames = {"zero", "one", "two", "three", "four",
                "five", "six", "seven", "eight", "nine"};
        for (int i = 0; i <= 9; i++) {
            enc.codeToName[0x30 + i] = digitNames[i];
        }
        enc.codeToName[0x3A] = "colon";
        enc.codeToName[0x3B] = "semicolon";
        enc.codeToName[0x3C] = "less";
        enc.codeToName[0x3D] = "equal";
        enc.codeToName[0x3E] = "greater";
        enc.codeToName[0x3F] = "question";
        enc.codeToName[0x40] = "at";
        for (int i = 0; i < 26; i++) {
            enc.codeToName[0x41 + i] = String.valueOf((char) ('A' + i));
        }
        enc.codeToName[0x5B] = "bracketleft";
        enc.codeToName[0x5C] = "backslash";
        enc.codeToName[0x5D] = "bracketright";
        enc.codeToName[0x5E] = "asciicircum";
        enc.codeToName[0x5F] = "underscore";
        enc.codeToName[0x60] = "grave";
        for (int i = 0; i < 26; i++) {
            enc.codeToName[0x61 + i] = String.valueOf((char) ('a' + i));
        }
        enc.codeToName[0x7B] = "braceleft";
        enc.codeToName[0x7C] = "bar";
        enc.codeToName[0x7D] = "braceright";
        enc.codeToName[0x7E] = "asciitilde";
    }
}
