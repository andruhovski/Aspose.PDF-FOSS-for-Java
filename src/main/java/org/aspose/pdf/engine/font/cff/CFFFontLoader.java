package org.aspose.pdf.engine.font.cff;

import org.aspose.pdf.engine.font.FontDescriptor;
import org.aspose.pdf.engine.font.FontEncoding;
import org.aspose.pdf.engine.font.PdfFont;
import org.aspose.pdf.engine.cos.COSStream;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Loads an AWT {@link java.awt.Font} from a PDF font that has an embedded
 * Type1C / CIDFontType0C / OpenType-CFF font program in its
 * {@code /FontDescriptor /FontFile3} stream.
 *
 * <p>Workflow:</p>
 * <ol>
 *   <li>Parse the CFF via {@link CFFParser} to learn glyph count and the
 *       glyph-id → glyph-name table</li>
 *   <li>Walk the PDF's {@code /Encoding} (charcode → glyph name) and map
 *       each charcode's Unicode codepoint to the CFF glyph id, producing the
 *       cmap input for {@link OpenTypeBuilder}</li>
 *   <li>Wrap the CFF in a synthetic OTF and hand the bytes to
 *       {@code Font.createFont(TRUETYPE_FONT, …)}</li>
 * </ol>
 */
public final class CFFFontLoader {

    private static final Logger LOG = Logger.getLogger(CFFFontLoader.class.getName());

    /** Cache: PdfFont identity (font dict reference) → loaded AWT Font (or null on failure). */
    private static final Map<Object, Font> CACHE = new HashMap<>();

    private CFFFontLoader() {}

    /**
     * Returns a Java {@code Font} backed by the embedded CFF, or {@code null}
     * if the font has no FontFile3 stream or parsing fails. The returned font
     * has size 1 pt; callers should derive a sized variant via
     * {@link Font#deriveFont}.
     */
    public static Font load(PdfFont pdfFont) {
        if (pdfFont == null) return null;
        Object key = pdfFont.getFontDictionary();
        if (key == null) key = pdfFont;
        if (CACHE.containsKey(key)) return CACHE.get(key);

        Font result = doLoad(pdfFont);
        CACHE.put(key, result);
        if (result != null) {
            LOG.fine(() -> "CFFFontLoader: loaded " + pdfFont.getBaseFont() + " → " + result.getFontName());
        }
        return result;
    }

    private static Font doLoad(PdfFont pdfFont) {
        FontDescriptor fd = pdfFont.getFontDescriptor();
        if (fd == null) return null;

        // Prefer FontFile2 (full TTF/OpenType program) when present — Java's
        // Font.createFont accepts it as-is, no synthetic wrapping needed.
        // PDF spec §9.9 says /FontFile2 carries the entire sfnt-housed font.
        COSStream fontFile2 = fd.getFontFile2();
        if (fontFile2 != null) {
            try {
                byte[] ttf = fontFile2.getDecodedData();
                if (ttf != null && ttf.length > 4) {
                    return Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(ttf));
                }
            } catch (FontFormatException | IOException e) {
                LOG.fine(() -> "FontFile2 load failed for " + pdfFont.getBaseFont() + ": " + e);
            }
        }

        COSStream fontFile3 = fd.getFontFile3();
        if (fontFile3 == null) return null;

        byte[] cffBytes;
        try {
            cffBytes = fontFile3.getDecodedData();
        } catch (IOException e) {
            LOG.fine(() -> "CFFFontLoader: cannot read FontFile3 for " + pdfFont.getBaseFont() + ": " + e);
            return null;
        }
        if (cffBytes == null || cffBytes.length < 4) return null;

        CFFParser cff;
        try {
            cff = new CFFParser(cffBytes);
        } catch (IOException e) {
            LOG.fine(() -> "CFFFontLoader: parse failed for " + pdfFont.getBaseFont() + ": " + e);
            return null;
        }

        // Build name → gid reverse lookup
        Map<String, Integer> nameToGid = new HashMap<>(cff.numGlyphs * 2);
        for (int gid = 0; gid < cff.numGlyphs; gid++) {
            String n = cff.glyphNames[gid];
            if (n != null && !nameToGid.containsKey(n)) nameToGid.put(n, gid);
        }

        // Build Unicode → gid via PDF /Encoding
        Map<Integer, Integer> unicodeToGid = new HashMap<>();
        FontEncoding enc = pdfFont.getEncoding();
        for (int code = 0; code <= 255; code++) {
            String name;
            int unicode;
            if (enc != null) {
                name = enc.getGlyphName(code);
                unicode = enc.getUnicode(code);
            } else {
                name = null;
                unicode = code;
            }
            if (name == null || name.isEmpty() || ".notdef".equals(name)) continue;
            Integer gid = nameToGid.get(name);
            if (gid == null) {
                // Fall back to the Adobe Glyph List for names not in this font's charset
                continue;
            }
            if (unicode <= 0) {
                // Some encodings return 0 for unmapped codes. Use the charcode
                // itself as a placeholder Unicode value so drawString can route
                // through the cmap.
                unicode = code;
            }
            if (unicode >= 0xE000 && unicode <= 0xF8FF) {
                // private-use — fine
            }
            if (unicode > 0xFFFD) continue;
            unicodeToGid.putIfAbsent(unicode, gid);
        }

        // Always map space if available
        Integer spaceGid = nameToGid.get("space");
        if (spaceGid != null) unicodeToGid.putIfAbsent(0x20, spaceGid);

        // Build advance-width array (units 1/1000 em)
        int[] widths = new int[cff.numGlyphs];
        for (int code = 0; code <= 255; code++) {
            String name = (enc != null) ? enc.getGlyphName(code) : null;
            if (name == null) continue;
            Integer gid = nameToGid.get(name);
            if (gid == null) continue;
            double w = pdfFont.getWidth(code);
            if (w > 0) widths[gid] = (int) Math.round(w);
        }
        // Fill in a sensible default for glyphs that the PDF /Widths array doesn't cover
        int avg = 500;
        int filled = 0;
        long sum = 0;
        for (int w : widths) if (w > 0) { sum += w; filled++; }
        if (filled > 0) avg = (int) (sum / filled);
        for (int i = 0; i < widths.length; i++) if (widths[i] == 0) widths[i] = avg;

        OpenTypeBuilder.Inputs in = new OpenTypeBuilder.Inputs();
        in.cff = cff;
        in.nameToGid = nameToGid;
        in.unicodeToGid = unicodeToGid;
        in.advanceWidths = widths;
        in.displayName = stripSubsetPrefix(pdfFont.getBaseFont() != null ? pdfFont.getBaseFont() : cff.fontName);
        in.psName = in.displayName;

        byte[] otf;
        try {
            otf = OpenTypeBuilder.build(in);
        } catch (IOException e) {
            LOG.fine(() -> "CFFFontLoader: OTF build failed: " + e);
            return null;
        }
        try {
            return Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(otf));
        } catch (FontFormatException | IOException e) {
            LOG.fine(() -> "CFFFontLoader: createFont rejected synthetic OTF for "
                    + pdfFont.getBaseFont() + ": " + e);
            return null;
        }
    }

    /** Removes the "XXXXXX+" subset prefix from a base font name, if present. */
    private static String stripSubsetPrefix(String name) {
        if (name == null) return null;
        if (name.length() > 7 && name.charAt(6) == '+') return name.substring(7);
        return name;
    }
}
