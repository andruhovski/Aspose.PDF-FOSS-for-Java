package org.aspose.pdf.engine.xfa.flatten.paint;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.engine.font.ttf.FontDiskLookup;
import org.aspose.pdf.engine.font.ttf.TrueTypeReader;
import org.aspose.pdf.engine.font.ttf.Type0FontBuilder;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfobjects.PdfStream;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Resolves the real font a piece of XFA text should be painted in, in strict priority order, and
 * builds an embeddable {@code /Type0} font from it (XFA-FONTEMBED sprint). Lifts the font-AA floor:
 * text is drawn in the actual family (Arial/…) instead of the standard-14 Helvetica substitute, so
 * glyph shapes match the reference render.
 *
 * <p>Priority (deterministic, licence-clean):</p>
 * <ol>
 *   <li><b>Embedded-in-source-PDF</b> — a font program already embedded in the source document
 *       ({@code /FontDescriptor/FontFile2}) whose family matches; reused verbatim (the user's own
 *       document — licence-safe, best fidelity).</li>
 *   <li><b>Host system font</b> — discovered on disk via {@link FontDiskLookup#loadStyled} over the
 *       configured dirs ({@code -Dxfa.fontDir}, default OS roots), style-aware.</li>
 *   <li><b>Substitution</b> — none found ⇒ {@code resolve} returns {@code null} and the painter keeps
 *       its standard-14 Helvetica path.</li>
 * </ol>
 *
 * <p><b>No proprietary font is ever bundled</b>: source fonts come from the user's PDF, host fonts
 * from the machine at runtime. The whole feature is gated by {@code -Dxfa.embedFonts} (default on);
 * a {@link #disabled()} resolver reproduces the pre-sprint substitution behaviour exactly (used by
 * the WinAnsi unit tests).</p>
 */
public final class XfaFontResolver {

    private static final Logger LOG = Logger.getLogger(XfaFontResolver.class.getName());
    private static final Pattern SUBSET_PREFIX = Pattern.compile("^[A-Z]{6}\\+");

    /** How a family was resolved (for reporting). */
    public enum Source { SOURCE_PDF, HOST, FALLBACK }

    /** A resolved, embeddable font: a unique resource key, the Type0 dict, and the reader for GIDs. */
    public static final class Embedded {
        /** Unique {@code registerFont} key (one per family+style). */
        public final String fontKey;
        /** The assembled {@code /Type0} font dictionary to attach under {@code /Resources/Font}. */
        public final PdfDictionary type0Dict;
        /** The reader providing Unicode→GID for {@code markFontAsType0}/{@code showText}. */
        public final TrueTypeReader reader;

        Embedded(String fontKey, PdfDictionary type0Dict, TrueTypeReader reader) {
            this.fontKey = fontKey;
            this.type0Dict = type0Dict;
            this.reader = reader;
        }
    }

    private final Map<String, byte[]> sourceFonts; // normalized family → TTF bytes
    private final String[] extraDirs;
    private final boolean enabled;
    private final Map<String, Embedded> cache = new HashMap<>();      // resolve key → embedded (null = fell back)
    private final Map<String, Source> resolvedVia = new LinkedHashMap<>();
    private final Set<String> fellBack = new TreeSet<>();

    private XfaFontResolver(Map<String, byte[]> sourceFonts, String[] extraDirs, boolean enabled) {
        this.sourceFonts = sourceFonts;
        this.extraDirs = extraDirs;
        this.enabled = enabled;
    }

    /**
     * Builds a resolver configured from system properties ({@code xfa.embedFonts}, {@code xfa.fontDir})
     * and the source document's embedded fonts (priority 1).
     *
     * @param source the source PDF whose embedded fonts may be reused, or {@code null}
     * @return a resolver (possibly {@link #disabled()} when {@code xfa.embedFonts=false})
     */
    public static XfaFontResolver create(Document source) {
        boolean enabled = !"false".equalsIgnoreCase(System.getProperty("xfa.embedFonts", "true"));
        if (!enabled) {
            return disabled();
        }
        String dirProp = System.getProperty("xfa.fontDir", "");
        String[] dirs = dirProp.isEmpty() ? new String[0] : dirProp.split(Pattern.quote(File.pathSeparator));
        Map<String, byte[]> src = source != null ? extractEmbeddedFonts(source) : Collections.emptyMap();
        return new XfaFontResolver(src, dirs, true);
    }

    /** A resolver that never embeds (the painter keeps its Helvetica substitution) — exact legacy behaviour. */
    public static XfaFontResolver disabled() {
        return new XfaFontResolver(Collections.emptyMap(), new String[0], false);
    }

    /** @return {@code true} if embedding is active. */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Resolves the font for a requested family/style to an embeddable Type0 font, or {@code null} if
     * the painter should fall back to its standard-14 substitute. Results (including misses) are cached
     * so a family is resolved and embedded once.
     *
     * @param typeface the XFA {@code <font typeface>} family, e.g. {@code "Arial"}
     * @param bold     bold requested
     * @param italic   italic requested
     * @return the embedded font, or {@code null} to fall back
     */
    public Embedded resolve(String typeface, boolean bold, boolean italic) {
        if (!enabled) {
            return null;
        }
        String family = (typeface == null || typeface.trim().isEmpty()) ? "Helvetica" : typeface.trim();
        String key = normalize(family) + (bold ? "-b" : "") + (italic ? "-i" : "");
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        Embedded e = build(family, bold, italic, key);
        cache.put(key, e);
        if (e == null) {
            fellBack.add(key);
        }
        return e;
    }

    /** @return the embedded font previously registered under {@code fontKey} (for the merge step), or {@code null}. */
    public Embedded embeddedFor(String fontKey) {
        for (Embedded e : cache.values()) {
            if (e != null && e.fontKey.equals(fontKey)) {
                return e;
            }
        }
        return null;
    }

    /** @return families resolved, mapped to how (source/host) — for the findings report. */
    public Map<String, Source> resolvedVia() {
        return Collections.unmodifiableMap(resolvedVia);
    }

    /** @return resolve keys that fell back to substitution. */
    public Set<String> fellBack() {
        return Collections.unmodifiableSet(fellBack);
    }

    /* -------------------------------- build -------------------------------- */

    private Embedded build(String family, boolean bold, boolean italic, String key) {
        byte[] ttf = sourceFonts.get(normalize(family));
        Source via = Source.SOURCE_PDF;
        if (ttf == null) {
            ttf = FontDiskLookup.loadStyled(family, bold, italic, extraDirs);
            via = Source.HOST;
        }
        if (ttf == null) {
            // Last resort: a guaranteed Unicode-capable system face matched to the family's category,
            // so non-WinAnsi text (Czech/Polish/Turkish/…) is embedded with real glyphs instead of
            // collapsing to '?' through the standard-14 WinAnsi substitution path.
            ttf = FontDiskLookup.loadFallback(isSerif(family), isMono(family), bold, italic, extraDirs);
            via = Source.FALLBACK;
        }
        if (ttf == null) {
            return null; // fall back to substitution
        }
        try {
            if (FontDiskLookup.isTTC(ttf)) {
                byte[] unpacked = FontDiskLookup.extractFirstFaceFromTTC(ttf);
                if (unpacked != null) {
                    ttf = unpacked;
                }
            }
            String pdfName = pdfSafe(family) + (bold ? "-Bold" : "") + (italic ? "-Italic" : "");
            Type0FontBuilder.Result built = Type0FontBuilder.buildLatin(pdfName, ttf);
            resolvedVia.put(key, via);
            LOG.fine(() -> "Resolved XFA font " + key + " via " + resolvedVia.get(key));
            return new Embedded(key, built.type0Font, built.reader);
        } catch (Exception ex) {
            LOG.warning(() -> "Embedding failed for " + family + " (" + ex.getMessage() + "); substituting");
            return null;
        }
    }

    /* ------------------------- source-PDF extraction ------------------------ */

    /**
     * Scans a source document's page (and Form-XObject) font resources for embedded TrueType programs
     * ({@code /FontFile2}) and indexes them by normalized family name. Best-effort and exception-safe.
     */
    static Map<String, byte[]> extractEmbeddedFonts(Document doc) {
        Map<String, byte[]> out = new HashMap<>();
        try {
            int n = doc.getPages().getCount();
            for (int i = 1; i <= n; i++) {
                Page page = doc.getPages().get(i);
                PdfDictionary res = page.getResources() != null ? page.getResources().getPdfDictionary() : null;
                collectFonts(res, out, new HashSet<>(), 0);
            }
        } catch (Exception e) {
            LOG.fine("Source font extraction skipped: " + e.getMessage());
        }
        return out;
    }

    private static void collectFonts(PdfDictionary resources, Map<String, byte[]> out,
                                     Set<PdfDictionary> visited, int depth) {
        if (resources == null || depth > 8 || !visited.add(resources)) {
            return;
        }
        PdfDictionary fonts = resolveDict(resources.get("Font"));
        if (fonts != null) {
            for (PdfName key : fonts.keySet()) {
                extractOne(resolveDict(fonts.get(key)), out);
            }
        }
        PdfDictionary xobjects = resolveDict(resources.get("XObject"));
        if (xobjects != null) {
            for (PdfName key : xobjects.keySet()) {
                PdfBase xb = xobjects.get(key);
                if (xb instanceof PdfObjectReference) {
                    try {
                        xb = ((PdfObjectReference) xb).dereference();
                    } catch (Exception e) {
                        continue;
                    }
                }
                if (xb instanceof PdfStream) {
                    collectFonts(resolveDict(((PdfStream) xb).get("Resources")), out, visited, depth + 1);
                }
            }
        }
    }

    private static void extractOne(PdfDictionary fontDict, Map<String, byte[]> out) {
        if (fontDict == null) {
            return;
        }
        String baseFont = fontDict.getNameAsString("BaseFont");
        PdfDictionary descriptor = resolveDict(fontDict.get("FontDescriptor"));
        if (descriptor == null) {
            // composite Type0 → descriptor on the descendant CIDFont
            PdfBase desc = fontDict.get("DescendantFonts");
            if (desc instanceof PdfObjectReference) {
                try {
                    desc = ((PdfObjectReference) desc).dereference();
                } catch (Exception e) {
                    desc = null;
                }
            }
            if (desc instanceof org.aspose.pdf.engine.pdfobjects.PdfArray
                    && ((org.aspose.pdf.engine.pdfobjects.PdfArray) desc).size() > 0) {
                PdfDictionary cid = resolveDict(((org.aspose.pdf.engine.pdfobjects.PdfArray) desc).get(0));
                if (cid != null) {
                    descriptor = resolveDict(cid.get("FontDescriptor"));
                    if (baseFont == null) {
                        baseFont = cid.getNameAsString("BaseFont");
                    }
                }
            }
        }
        if (descriptor == null || baseFont == null) {
            return;
        }
        PdfBase ff2 = descriptor.get("FontFile2"); // only TrueType reuse for now (FontFile/3 = gap)
        if (ff2 instanceof PdfObjectReference) {
            try {
                ff2 = ((PdfObjectReference) ff2).dereference();
            } catch (Exception e) {
                return;
            }
        }
        if (!(ff2 instanceof PdfStream)) {
            return;
        }
        try {
            byte[] bytes = ((PdfStream) ff2).getDecodedData();
            if (bytes != null && bytes.length > 0) {
                out.putIfAbsent(normalize(baseFont), bytes);
            }
        } catch (Exception e) {
            LOG.fine("Could not decode FontFile2 for " + baseFont + ": " + e.getMessage());
        }
    }

    /* -------------------------------- helpers ------------------------------- */

    private static PdfDictionary resolveDict(PdfBase val) {
        if (val instanceof PdfObjectReference) {
            try {
                val = ((PdfObjectReference) val).dereference();
            } catch (Exception e) {
                return null;
            }
        }
        return val instanceof PdfDictionary ? (PdfDictionary) val : null;
    }

    /** Normalizes a family/BaseFont to a match key: strip subset prefix, lowercase, drop style/suffix words. */
    static String normalize(String name) {
        if (name == null) {
            return "";
        }
        String s = SUBSET_PREFIX.matcher(name).replaceFirst("");
        s = s.toLowerCase(Locale.ROOT);
        // drop the common style/format tokens so "ArialMT"/"Arial-BoldMT" all key to "arial"
        for (String tok : new String[]{"boldoblique", "bolditalic", "bold", "italic", "oblique",
                "regular", "mt", "ps", "std"}) {
            s = s.replace(tok, "");
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                b.append(c);
            }
        }
        return b.toString();
    }

    /** @return true when the family reads as a serif face (so the fallback picks Times/serif). */
    private static boolean isSerif(String family) {
        String s = family.toLowerCase(Locale.ROOT);
        return s.contains("times") || s.contains("serif") || s.contains("roman") || s.contains("georgia")
                || s.contains("garamond") || s.contains("minion") || s.contains("book antiqua")
                || s.contains("palatino") || s.contains("cambria");
    }

    /** @return true when the family reads as a monospace face (so the fallback picks Courier/mono). */
    private static boolean isMono(String family) {
        String s = family.toLowerCase(Locale.ROOT);
        return s.contains("courier") || s.contains("mono") || s.contains("consol")
                || s.contains("typewriter");
    }

    private static String pdfSafe(String name) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c > 0x20 && c < 0x7F && c != '/' && c != '(' && c != ')' && c != '<' && c != '>'
                    && c != '[' && c != ']' && c != '{' && c != '}' && c != '%' && c != '#') {
                b.append(c);
            }
        }
        return b.length() == 0 ? "EmbeddedFont" : b.toString();
    }
}
