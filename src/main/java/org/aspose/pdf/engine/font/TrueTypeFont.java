package org.aspose.pdf.engine.font;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.font.ttf.TrueTypeReader;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * TrueType font (/Subtype /TrueType) - ISO 32000-1:2008, 9.6.3.
 * <p>
 * Wraps a TrueType font embedded in a PDF. Reads /Widths, /Encoding,
 * and optionally parses the embedded font program via {@link TrueTypeReader}
 * for glyph-level metrics and cmap decoding.
 * </p>
 */
public class TrueTypeFont extends PdfFont {

    private static final Logger LOG = Logger.getLogger(TrueTypeFont.class.getName());

    private TrueTypeReader ttReader;
    private double[] customWidths;
    private int firstChar;
    private int lastChar;
    private boolean hasExplicitEncoding;

    /**
     * Creates a TrueTypeFont from a font dictionary.
     *
     * @param fontDict the font dictionary (/Type /Font, /Subtype /TrueType)
     * @param parser   the PDF parser (may be null)
     * @throws IOException if reading the font data fails
     */
    public TrueTypeFont(COSDictionary fontDict, PDFParser parser) throws IOException {
        super(fontDict, parser);
        initEncoding();
        initWidths();
        initTrueTypeReader();
        LOG.fine(() -> "TrueTypeFont created: " + baseFont);
    }

    @Override
    public String decode(byte[] charCodes) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (byte b : charCodes) {
            int code = b & 0xFF;
            if (toUnicode != null) {
                String mapped = toUnicode.lookup(code);
                if (mapped != null) {
                    sb.append(mapped);
                    continue;
                }
            }

            if (!hasExplicitEncoding && shouldPreferEncodingFirst() && encoding != null) {
                int unicode = encoding.getUnicode(code);
                if (isReadableEncodingCodePoint(unicode)) {
                    sb.appendCodePoint(unicode);
                    continue;
                }
            }

            // Some subset TrueType fonts omit both /Encoding and /ToUnicode and
            // use glyph-local ids directly in text-showing operators. Recover
            // through the embedded /post table → Adobe Glyph List, then through
            // the embedded cmap, before falling back to the synthetic WinAnsi
            // default.
            if (!hasExplicitEncoding && ttReader != null) {
                // 1. Subset fonts: charCode IS the glyph id; resolve via post
                //    → AGL. This is the only reliable channel because subset
                //    cmaps tend to be either missing, symbolic, or carry the
                //    pre-subset Unicode codepoints which read back as garbage.
                int unicodeFromName = unicodeFromGlyphName(ttReader.getGlyphName(code));
                if (unicodeFromName > 0) {
                    sb.appendCodePoint(unicodeFromName);
                    continue;
                }

                int unicode = ttReader.getUnicodeForGlyphId(code);
                if (isReadableUnicodeCodePoint(unicode)) {
                    sb.appendCodePoint(unicode);
                    continue;
                }
                int glyphId = ttReader.getGlyphId(code);
                if (glyphId == 0 && fontDescriptor != null && fontDescriptor.isSymbolic()) {
                    glyphId = ttReader.getGlyphId(0xF000 + code);
                }
                if (glyphId > 0) {
                    int unicodeFromGid = unicodeFromGlyphName(ttReader.getGlyphName(glyphId));
                    if (unicodeFromGid > 0) {
                        sb.appendCodePoint(unicodeFromGid);
                        continue;
                    }
                    unicode = ttReader.getUnicodeForGlyphId(glyphId);
                    if (isReadableUnicodeCodePoint(unicode)) {
                        sb.appendCodePoint(unicode);
                        continue;
                    }
                }
            }

            if (encoding != null) {
                int unicode = encoding.getUnicode(code);
                if (unicode > 0) {
                    sb.append((char) unicode);
                    continue;
                }
            }

            sb.append((char) code);
        }
        return sb.toString();
    }

    @Override
    public double getWidth(int charCode) {
        if (customWidths != null && charCode >= firstChar && charCode <= lastChar) {
            int idx = charCode - firstChar;
            if (idx < customWidths.length) {
                double w = customWidths[idx];
                if (w > 0) return w;
            }
        }
        if (ttReader != null) {
            int glyphId = ttReader.getGlyphId(charCode);
            if (glyphId == 0 && fontDescriptor != null && fontDescriptor.isSymbolic()) {
                glyphId = ttReader.getGlyphId(0xF000 + (charCode & 0xFF));
            }
            if (glyphId > 0) {
                int aw = ttReader.getAdvanceWidth(glyphId);
                return aw * 1000.0 / ttReader.getUnitsPerEm();
            }
        }
        if (fontDescriptor != null) {
            double mw = fontDescriptor.getMissingWidth();
            if (mw > 0) return mw;
        }
        return 1000;
    }

    /**
     * Returns the TrueTypeReader if the font program was loaded.
     *
     * @return the reader, or null
     */
    public TrueTypeReader getTrueTypeReader() {
        return ttReader;
    }

    /**
     * Resolves a PostScript glyph name (e.g. {@code "C"}, {@code "germandbls"},
     * {@code "uni0041"}, {@code "u00041"}) to a Unicode codepoint via the
     * Adobe Glyph List, with the standard {@code uniXXXX} / {@code uXXXXX}
     * fallbacks. Returns 0 if the name is null, empty, or unrecognised.
     */
    private static int unicodeFromGlyphName(String name) {
        if (name == null || name.isEmpty() || ".notdef".equals(name)) return 0;
        // Strip a textual variant suffix ("A.alt" → "A") because subset fonts
        // commonly suffix glyph names per OpenType conventions.
        int dot = name.indexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        int u = AdobeGlyphList.getUnicode(base);
        if (u > 0) return u;
        // Fallback: uXXXX / uniXXXX numeric forms.
        if (base.startsWith("uni") && base.length() == 7) {
            try { return Integer.parseInt(base.substring(3), 16); }
            catch (NumberFormatException ignored) {}
        }
        if (base.startsWith("u") && base.length() >= 5 && base.length() <= 7) {
            try { return Integer.parseInt(base.substring(1), 16); }
            catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    /** Filters out C0/C1 control codes that pollute extracted text. */
    private static boolean isReadableUnicodeCodePoint(int unicode) {
        if (unicode <= 0) return false;
        if (Character.isWhitespace(unicode)) return true;
        return !Character.isISOControl(unicode);
    }

    private void initEncoding() {
        COSBase encValue = resolve(fontDict.get("Encoding"));
        if (encValue instanceof COSName) {
            FontEncoding named = FontEncoding.getInstance(((COSName) encValue).getName());
            if (named != null) {
                this.encoding = named;
                this.hasExplicitEncoding = true;
            }
        } else if (encValue instanceof COSDictionary) {
            this.encoding = FontEncoding.fromDictionary((COSDictionary) encValue);
            this.hasExplicitEncoding = true;
        }
        if (this.encoding == null) {
            this.encoding = FontEncoding.WIN_ANSI;
        }
    }

    private void initWidths() {
        COSBase widthsVal = resolve(fontDict.get("Widths"));
        if (widthsVal instanceof COSArray) {
            this.firstChar = fontDict.getInt("FirstChar", 0);
            this.lastChar = fontDict.getInt("LastChar", 255);
            COSArray arr = (COSArray) widthsVal;
            this.customWidths = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                customWidths[i] = getNumber(arr.get(i));
            }
        }
    }

    private void initTrueTypeReader() {
        if (fontDescriptor == null) return;
        COSStream fontFile = fontDescriptor.getFontFile2();
        if (fontFile == null) return;
        try {
            byte[] ttfData = fontFile.getDecodedData();
            if (ttfData != null && ttfData.length > 0) {
                this.ttReader = new TrueTypeReader(ttfData);
            }
        } catch (IOException e) {
            LOG.fine(() -> "Failed to parse embedded TrueType font: " + e.getMessage());
        }
    }

    private boolean shouldPreferEncodingFirst() {
        if (baseFont == null || baseFont.isEmpty()) {
            return false;
        }
        if (baseFont.indexOf('+') >= 0) {
            return false;
        }
        String normalized = baseFont.replace(",", "")
                .replace("-", "")
                .toLowerCase(Locale.ROOT);
        return normalized.contains("arial")
                || normalized.contains("trebuchet")
                || normalized.contains("helvetica")
                || normalized.contains("times")
                || normalized.contains("courier")
                || normalized.contains("shell")
                || normalized.contains("verdana")
                || normalized.contains("tahoma")
                || normalized.contains("calibri")
                || normalized.contains("cambria");
    }

    private boolean isReadableEncodingCodePoint(int unicode) {
        if (unicode <= 0) {
            return false;
        }
        if (Character.isWhitespace(unicode)) {
            return true;
        }
        return !Character.isISOControl(unicode);
    }
}
