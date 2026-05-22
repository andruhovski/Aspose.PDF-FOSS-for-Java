package org.aspose.pdf.engine.pdfa.rules;

import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectKey;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSString;
import org.aspose.pdf.engine.pdfa.PdfARule;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validates PDF/X compliance requirements.
 *
 * <p>Only fires when the target format is a PDF/X variant. Checks:</p>
 * <ul>
 *   <li>Info dict must have /GTS_PDFXVersion</li>
 *   <li>PDF/X-1a: /GTS_PDFXConformance required</li>
 *   <li>/Trapped must be True or False (not Unknown)</li>
 *   <li>Each page must have /TrimBox or /ArtBox</li>
 *   <li>Catalog must have /OutputIntents with /S=/GTS_PDFX</li>
 *   <li>No /Encrypt (PDF/X-1a)</li>
 *   <li>All fonts must be embedded</li>
 * </ul>
 */
public final class PdfXRules implements PdfARule {

    private static final Logger LOG = Logger.getLogger(PdfXRules.class.getName());

    /**
     * Creates a new PDF/X rules checker.
     */
    public PdfXRules() {
        // default constructor
    }

    @Override
    public void validate(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        if (!format.isPdfX()) {
            return;
        }

        checkInfoDict(parser, format, result);
        checkOutputIntents(parser, result);
        checkPages(parser, result);
        checkEncrypt(parser, format, result);
        checkFontsEmbedded(parser, result);
    }

    /**
     * Checks /Info dictionary for required PDF/X entries.
     */
    private void checkInfoDict(PDFParser parser, PdfFormat format,
                                PdfAValidationResult result) {
        COSDictionary trailer = parser.getTrailer();
        if (trailer == null) {
            result.addError("PDFX-INFO",
                    "No trailer dictionary found",
                    "trailer", "PDF/X");
            return;
        }

        COSDictionary info = resolveDict(trailer.get("Info"));
        if (info == null) {
            result.addError("PDFX-INFO",
                    "PDF/X requires /Info dictionary in trailer",
                    "trailer", "PDF/X");
            return;
        }

        // /GTS_PDFXVersion required
        COSBase versionVal = info.get("GTS_PDFXVersion");
        if (versionVal == null) {
            result.addError("PDFX-VERSION",
                    "Info dict must have /GTS_PDFXVersion for PDF/X compliance",
                    "trailer/Info", "PDF/X");
        }

        // PDF/X-1a requires /GTS_PDFXConformance
        if (format.isPdfX1a()) {
            COSBase confVal = info.get("GTS_PDFXConformance");
            if (confVal == null) {
                result.addError("PDFX-CONFORMANCE",
                        "Info dict must have /GTS_PDFXConformance for PDF/X-1a compliance",
                        "trailer/Info", "PDF/X-1a");
            }
        }

        // /Trapped must be True or False (not Unknown)
        String trapped = info.getNameAsString("Trapped");
        if (trapped == null) {
            result.addError("PDFX-TRAPPED",
                    "Info dict /Trapped must be present and set to True or False",
                    "trailer/Info", "PDF/X");
        } else if (!"True".equals(trapped) && !"False".equals(trapped)) {
            result.addError("PDFX-TRAPPED",
                    "Info dict /Trapped must be True or False, not: " + trapped,
                    "trailer/Info", "PDF/X");
        }
    }

    /**
     * Checks that the catalog has /OutputIntents with /S=/GTS_PDFX.
     */
    private void checkOutputIntents(PDFParser parser, PdfAValidationResult result) {
        COSDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not load catalog: {0}", e.getMessage());
            return;
        }

        COSArray outputIntents = resolveArray(catalog.get("OutputIntents"));
        if (outputIntents == null || outputIntents.size() == 0) {
            result.addError("PDFX-OUTPUTINTENT",
                    "Catalog must have /OutputIntents for PDF/X compliance",
                    "catalog", "PDF/X");
            return;
        }

        boolean foundPdfX = false;
        for (int i = 0; i < outputIntents.size(); i++) {
            COSDictionary oi = resolveDict(outputIntents.get(i));
            if (oi == null) {
                continue;
            }
            String s = oi.getNameAsString("S");
            if ("GTS_PDFX".equals(s)) {
                foundPdfX = true;
                break;
            }
        }

        if (!foundPdfX) {
            result.addError("PDFX-OUTPUTINTENT",
                    "Catalog /OutputIntents must contain an entry with /S = GTS_PDFX",
                    "catalog/OutputIntents", "PDF/X");
        }
    }

    /**
     * Checks that each page has /TrimBox or /ArtBox.
     */
    private void checkPages(PDFParser parser, PdfAValidationResult result) {
        COSDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            return;
        }

        COSDictionary pages = resolveDict(catalog.get("Pages"));
        if (pages == null) {
            return;
        }
        COSArray kids = pages.getArray("Kids");
        if (kids == null) {
            return;
        }

        for (int i = 0; i < kids.size(); i++) {
            COSDictionary page = resolveDict(kids.get(i));
            if (page == null) {
                continue;
            }
            String pagePath = "page[" + i + "]";

            boolean hasTrimBox = page.get("TrimBox") != null;
            boolean hasArtBox = page.get("ArtBox") != null;
            if (!hasTrimBox && !hasArtBox) {
                result.addError("PDFX-TRIMBOX",
                        "Page must have /TrimBox or /ArtBox for PDF/X compliance",
                        pagePath, "PDF/X");
            }
        }
    }

    /**
     * Checks no /Encrypt (PDF/X-1a).
     */
    private void checkEncrypt(PDFParser parser, PdfFormat format,
                               PdfAValidationResult result) {
        if (!format.isPdfX1a()) {
            return;
        }

        COSDictionary trailer = parser.getTrailer();
        if (trailer != null && trailer.get("Encrypt") != null) {
            result.addError("PDFX-ENCRYPT",
                    "PDF/X-1a must not have /Encrypt in trailer",
                    "trailer", "PDF/X-1a");
        }
    }

    /**
     * Checks that all fonts are embedded (all font descriptors have FontFile/2/3).
     */
    private void checkFontsEmbedded(PDFParser parser, PdfAValidationResult result) {
        COSDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            return;
        }

        COSDictionary pages = resolveDict(catalog.get("Pages"));
        if (pages == null) {
            return;
        }
        COSArray kids = pages.getArray("Kids");
        if (kids == null) {
            return;
        }

        for (int i = 0; i < kids.size(); i++) {
            COSDictionary page = resolveDict(kids.get(i));
            if (page == null) {
                continue;
            }
            String pagePath = "page[" + i + "]";
            COSDictionary resources = resolveDict(page.get("Resources"));
            if (resources == null) {
                continue;
            }

            COSDictionary fonts = resolveDict(resources.get("Font"));
            if (fonts == null) {
                continue;
            }

            for (COSName key : fonts.keySet()) {
                COSDictionary font = resolveDict(fonts.get(key.getName()));
                if (font == null) {
                    continue;
                }
                checkFontEmbedded(font, pagePath + "/Resources/Font/" + key.getName(), result);
            }
        }
    }

    /**
     * Checks a single font for embedding.
     */
    private void checkFontEmbedded(COSDictionary font, String fontPath,
                                    PdfAValidationResult result) {
        String subtype = font.getNameAsString("Subtype");

        // Type0 (composite) fonts - check descendant
        if ("Type0".equals(subtype)) {
            COSArray descendants = resolveArray(font.get("DescendantFonts"));
            if (descendants != null && descendants.size() > 0) {
                COSDictionary cidFont = resolveDict(descendants.get(0));
                if (cidFont != null) {
                    checkFontDescriptorEmbedded(cidFont, fontPath, result);
                }
            }
            return;
        }

        // Type3 fonts don't require embedding via FontDescriptor
        if ("Type3".equals(subtype)) {
            return;
        }

        checkFontDescriptorEmbedded(font, fontPath, result);
    }

    /**
     * Checks FontDescriptor for FontFile/2/3.
     */
    private void checkFontDescriptorEmbedded(COSDictionary font, String fontPath,
                                              PdfAValidationResult result) {
        COSDictionary fd = resolveDict(font.get("FontDescriptor"));
        if (fd == null) {
            String baseFont = font.getNameAsString("BaseFont");
            result.addError("PDFX-FONT-EMBED",
                    "Font missing FontDescriptor (not embedded): " + baseFont,
                    fontPath, "PDF/X");
            return;
        }

        boolean hasFile = fd.get("FontFile") != null
                || fd.get("FontFile2") != null
                || fd.get("FontFile3") != null;
        if (!hasFile) {
            String baseFont = font.getNameAsString("BaseFont");
            result.addError("PDFX-FONT-EMBED",
                    "Font must be embedded (no FontFile/FontFile2/FontFile3): " + baseFont,
                    fontPath, "PDF/X");
        }
    }

    /**
     * Resolves a COSBase to a COSDictionary, dereferencing indirect references.
     */
    private static COSDictionary resolveDict(COSBase val) {
        if (val instanceof COSObjectReference) {
            try {
                val = ((COSObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return (val instanceof COSDictionary) ? (COSDictionary) val : null;
    }

    /**
     * Resolves a COSBase to a COSArray, dereferencing indirect references.
     */
    private static COSArray resolveArray(COSBase val) {
        if (val instanceof COSObjectReference) {
            try {
                val = ((COSObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return (val instanceof COSArray) ? (COSArray) val : null;
    }
}
