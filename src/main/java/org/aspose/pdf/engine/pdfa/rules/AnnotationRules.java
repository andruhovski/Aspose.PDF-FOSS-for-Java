package org.aspose.pdf.engine.pdfa.rules;

import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.pdfa.PdfARule;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validates annotation requirements for PDF/A compliance.
 *
 * <p>Checks the following ISO 19005 clauses:</p>
 * <ul>
 *   <li>6.5.2 &mdash; Forbidden annotation subtypes</li>
 *   <li>6.5.3 &mdash; /F flags: Print=1, Hidden=0, Invisible=0, NoView=0</li>
 * </ul>
 */
public final class AnnotationRules implements PdfARule {

    private static final Logger LOG = Logger.getLogger(AnnotationRules.class.getName());

    /** Annotation subtypes forbidden in PDF/A-1. */
    private static final Set<String> FORBIDDEN_PDFA1 = new HashSet<>(Arrays.asList(
            "FileAttachment", "Sound", "Movie"
    ));

    /** Annotation subtypes forbidden in PDF/A-2+. */
    private static final Set<String> FORBIDDEN_PDFA2 = new HashSet<>(Arrays.asList(
            "3D", "Sound", "Screen", "Movie"
    ));

    /** Bit position for Print flag (bit 3, zero-indexed from bit 1). */
    private static final int FLAG_PRINT = 1 << 2;
    /** Bit position for Hidden flag (bit 2). */
    private static final int FLAG_HIDDEN = 1 << 1;
    /** Bit position for Invisible flag (bit 1). */
    private static final int FLAG_INVISIBLE = 1;
    /** Bit position for NoView flag (bit 6). */
    private static final int FLAG_NOVIEW = 1 << 5;

    /**
     * Creates a new annotation rules checker.
     */
    public AnnotationRules() {
        // default constructor
    }

    @Override
    public void validate(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        if (!format.isPdfA()) {
            return;
        }
        checkPages(parser, format, result);
    }

    /**
     * Iterates all pages and checks their annotations.
     */
    private void checkPages(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        COSDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not load catalog: {0}", e.getMessage());
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
            checkAnnotations(page, pagePath, format, result);
        }
    }

    /**
     * Checks all annotations on a page.
     */
    private void checkAnnotations(COSDictionary page, String pagePath,
                                   PdfFormat format, PdfAValidationResult result) {
        COSBase annotsRef = page.get("Annots");
        COSArray annots = resolveArray(annotsRef);
        if (annots == null) {
            return;
        }

        Set<String> forbidden = format.isPdfA1() ? FORBIDDEN_PDFA1 : FORBIDDEN_PDFA2;

        for (int i = 0; i < annots.size(); i++) {
            COSDictionary annot = resolveDict(annots.get(i));
            if (annot == null) {
                continue;
            }
            String annotPath = pagePath + "/Annots[" + i + "]";
            String subtype = annot.getNameAsString("Subtype");

            // 6.5.2: Forbidden subtypes
            if (subtype != null && forbidden.contains(subtype)) {
                result.addError("6.5.2",
                        "Annotation subtype '" + subtype + "' is not permitted in "
                                + (format.isPdfA1() ? "PDF/A-1" : "PDF/A-2+"),
                        annotPath, "6.5.2");
            }

            // 6.5.3: /F flags check
            int flags = annot.getInt("F", 0);
            if ((flags & FLAG_PRINT) == 0) {
                result.addError("6.5.3",
                        "Annotation /F flag must have Print bit set",
                        annotPath, "6.5.3");
            }
            if ((flags & FLAG_HIDDEN) != 0) {
                result.addError("6.5.3",
                        "Annotation /F flag must not have Hidden bit set",
                        annotPath, "6.5.3");
            }
            if ((flags & FLAG_INVISIBLE) != 0) {
                result.addError("6.5.3",
                        "Annotation /F flag must not have Invisible bit set",
                        annotPath, "6.5.3");
            }
            if ((flags & FLAG_NOVIEW) != 0) {
                result.addError("6.5.3",
                        "Annotation /F flag must not have NoView bit set",
                        annotPath, "6.5.3");
            }
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
