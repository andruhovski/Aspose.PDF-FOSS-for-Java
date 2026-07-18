package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfFloat;
import org.aspose.pdf.engine.pdfobjects.PdfName;

/// Package-private helpers used by [BackgroundArtifact] and
/// [WatermarkArtifact] when synthesising raw content-stream bytes:
/// register Standard-14 fonts and opacity `/ExtGState` entries on the
/// page's `/Resources`, escape PDF literal strings, and extract RGB
/// components from a [Color].
final class ArtifactSupport {

    private ArtifactSupport() { }

    /// Ensures a Type1 Standard-14 font entry exists on the page's
    /// `/Resources/Font` and returns its resource name. Reuses an
    /// existing entry if `/BaseFont` matches; otherwise registers a
    /// new `F<sanitised-name>` entry.
    static String ensureStandardFont(Page page, String baseFont) {
        Resources res = page.ensureResources();
        PdfDictionary fonts = res.getFonts();
        if (fonts == null) {
            fonts = new PdfDictionary();
            res.getPdfDictionary().set(PdfName.of("Font"), fonts);
        }
        // Re-use any existing /BaseFont match.
        for (PdfName key : fonts.keySet()) {
            org.aspose.pdf.engine.pdfobjects.PdfBase val = fonts.get(key);
            if (val instanceof PdfDictionary) {
                String existingBase = ((PdfDictionary) val).getNameAsString("BaseFont");
                if (baseFont.equals(existingBase)) return key.getName();
            }
        }
        String name = "F" + baseFont.replace("-", "");
        if (fonts.get(name) != null) {
            int n = 2;
            while (fonts.get(name + n) != null) n++;
            name = name + n;
        }
        PdfDictionary f = new PdfDictionary();
        f.set(PdfName.of("Type"), PdfName.of("Font"));
        f.set(PdfName.of("Subtype"), PdfName.of("Type1"));
        f.set(PdfName.of("BaseFont"), PdfName.of(baseFont));
        if (!"ZapfDingbats".equals(baseFont) && !"Symbol".equals(baseFont)) {
            f.set(PdfName.of("Encoding"), PdfName.of("WinAnsiEncoding"));
        }
        fonts.set(PdfName.of(name), f);
        return name;
    }

    /// Ensures an `/ExtGState` entry providing the given non-stroking
    /// and stroking alpha exists on the page's `/Resources` and returns
    /// its resource name.
    static String ensureOpacityExtGState(Page page, double alpha) {
        Resources res = page.ensureResources();
        PdfDictionary extGs = res.getExtGState();
        if (extGs == null) {
            extGs = new PdfDictionary();
            res.getPdfDictionary().set(PdfName.of("ExtGState"), extGs);
        }
        // Re-use any existing entry whose /ca and /CA match.
        for (PdfName key : extGs.keySet()) {
            org.aspose.pdf.engine.pdfobjects.PdfBase val = extGs.get(key);
            if (val instanceof PdfDictionary) {
                PdfDictionary gs = (PdfDictionary) val;
                double caVal = gs.getFloat("ca", -1f);
                double upperCa = gs.getFloat("CA", -1f);
                if (Math.abs(caVal - alpha) < 1e-6 && Math.abs(upperCa - alpha) < 1e-6) {
                    return key.getName();
                }
            }
        }
        // Build a stable name like "GS18" for alpha=0.18.
        String name = "GS" + String.format(java.util.Locale.ROOT, "%02d",
                (int) Math.round(alpha * 100));
        int suffix = 2;
        while (extGs.get(name) != null) {
            name = "GS" + String.format(java.util.Locale.ROOT, "%02d",
                    (int) Math.round(alpha * 100)) + "_" + suffix++;
        }
        PdfDictionary gs = new PdfDictionary();
        gs.set(PdfName.of("Type"), PdfName.of("ExtGState"));
        gs.set(PdfName.of("ca"), new PdfFloat(alpha));
        gs.set(PdfName.of("CA"), new PdfFloat(alpha));
        extGs.set(PdfName.of(name), gs);
        return name;
    }

    /// Returns RGB components in [0,1] for any [Color], falling back to black.
    static double[] toRgb(Color color) {
        if (color == null) return new double[]{0, 0, 0};
        return new double[]{color.getR(), color.getG(), color.getB()};
    }

    /// Escapes a string for a PDF literal `(...)` payload.
    static String escapeLiteral(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length() + 4);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '(': out.append("\\("); break;
                case ')': out.append("\\)"); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default: out.append(c);
            }
        }
        return out.toString();
    }
}
