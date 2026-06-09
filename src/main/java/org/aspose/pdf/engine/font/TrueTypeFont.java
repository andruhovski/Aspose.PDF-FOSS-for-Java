package org.aspose.pdf.engine.font;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
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
    public TrueTypeFont(PdfDictionary fontDict, PDFParser parser) throws IOException {
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
     * Maps a character code to a glyph id in the embedded program, following the
     * TrueType glyph-selection rules of ISO 32000-1:2008 §9.6.6.4:
     * <ol>
     *   <li>symbolic font with no explicit /Encoding → embedded (3,0)/(1,0) cmap
     *       keyed by the raw code (and the {@code 0xF000+code} fallback);</li>
     *   <li>otherwise → /Encoding code→Unicode, then the embedded Unicode cmap;</li>
     *   <li>last resort → the raw code (and {@code 0xF000+code}) through the cmap.</li>
     * </ol>
     *
     * @param code the 1-byte character code from the content stream
     * @return the glyph id, or 0 when it cannot be resolved
     */
    public int resolveGlyphId(int code) {
        if (ttReader == null) return 0;
        boolean symbolic = fontDescriptor != null && fontDescriptor.isSymbolic();
        if (symbolic && !hasExplicitEncoding) {
            int g = ttReader.getGlyphId(code);
            if (g == 0) g = ttReader.getGlyphId(0xF000 | code);
            if (g != 0) return g;
        }
        if (encoding != null) {
            int uni = encoding.getUnicode(code);
            if (uni > 0) {
                int g = ttReader.getGlyphId(uni);
                if (g != 0) return g;
            }
        }
        int g = ttReader.getGlyphId(code);
        if (g == 0) g = ttReader.getGlyphId(0xF000 | code);
        return g;
    }

    /**
     * Returns the em-normalised, Y-up outline of the glyph selected by
     * {@link #resolveGlyphId(int)} for the given character code, or {@code null}
     * when no embedded program is present or the glyph cannot be resolved.
     * Drawing this outline avoids {@code java.awt.Font}, which renders the
     * default ".notdef" box for subset programs whose cmap is missing or partial
     * even when {@code canDisplay} reports the character as available (corpus
     * 46679: the dotted-leader colon of an embedded TimesNewRoman subset).
     *
     * @param code the 1-byte character code
     * @return the glyph outline, or {@code null}
     */
    public java.awt.geom.GeneralPath glyphOutlineForCode(int code) {
        if (ttReader == null) return null;
        int gid = resolveGlyphId(code);
        return gid > 0 ? ttReader.getGlyphPath(gid) : null;
    }

    /**
     * Returns true if the font dictionary carries an explicit /Encoding
     * (a base-encoding name or an encoding dictionary). Symbolic fonts
     * without one map character codes through the embedded font program's
     * own cmap (ISO 32000-1:2008, §9.6.6.4).
     *
     * @return true if /Encoding was present
     */
    public boolean hasExplicitEncoding() {
        return hasExplicitEncoding;
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
        PdfBase encValue = resolve(fontDict.get("Encoding"));
        if (encValue instanceof PdfName) {
            FontEncoding named = FontEncoding.getInstance(((PdfName) encValue).getName());
            if (named != null) {
                this.encoding = named;
                this.hasExplicitEncoding = true;
            }
        } else if (encValue instanceof PdfDictionary) {
            this.encoding = FontEncoding.fromDictionary((PdfDictionary) encValue);
            this.hasExplicitEncoding = true;
        }
        if (this.encoding == null) {
            this.encoding = FontEncoding.WIN_ANSI;
        }
    }

    private void initWidths() {
        PdfBase widthsVal = resolve(fontDict.get("Widths"));
        if (widthsVal instanceof PdfArray) {
            this.firstChar = fontDict.getInt("FirstChar", 0);
            this.lastChar = fontDict.getInt("LastChar", 255);
            PdfArray arr = (PdfArray) widthsVal;
            this.customWidths = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                customWidths[i] = getNumber(arr.get(i));
            }
        }
    }

    private void initTrueTypeReader() {
        if (fontDescriptor == null) return;
        PdfStream fontFile = fontDescriptor.getFontFile2();
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
