package org.aspose.pdf.engine.pdfa.rules;

import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfa.PdfARule;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validates logical structure requirements for PDF/A Level A compliance.
 *
 * <p>Checks the following ISO 19005 clauses (Level A only):</p>
 * <ul>
 *   <li>6.8.2 &mdash; Catalog /MarkInfo dict with /Marked=true</li>
 *   <li>6.8.3 &mdash; Catalog must have /StructTreeRoot</li>
 *   <li>6.8.4 &mdash; Catalog must have /Lang</li>
 * </ul>
 */
public final class LogicalStructureRules implements PdfARule {

    private static final Logger LOG = Logger.getLogger(LogicalStructureRules.class.getName());

    /**
     * Creates a new logical structure rules checker.
     */
    public LogicalStructureRules() {
        // default constructor
    }

    @Override
    public void validate(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        // Level A only
        if (!format.isLevelA()) {
            return;
        }

        PdfDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not load catalog: {0}", e.getMessage());
            result.addError("6.8.2",
                    "Cannot load catalog to check logical structure",
                    "catalog", "6.8.2");
            return;
        }

        checkMarkInfo(catalog, result);
        checkStructTreeRoot(catalog, result);
        checkLang(catalog, result);
    }

    /**
     * 6.8.2: Catalog must have /MarkInfo dict with /Marked = true.
     */
    private void checkMarkInfo(PdfDictionary catalog, PdfAValidationResult result) {
        PdfBase markInfoRef = catalog.get("MarkInfo");
        PdfDictionary markInfo = resolveDict(markInfoRef);

        if (markInfo == null) {
            result.addError("6.8.2",
                    "Catalog must have /MarkInfo dictionary for Level A compliance",
                    "catalog", "6.8.2");
            return;
        }

        boolean marked = markInfo.getBoolean("Marked", false);
        if (!marked) {
            result.addError("6.8.2",
                    "Catalog /MarkInfo/Marked must be true for Level A compliance",
                    "catalog/MarkInfo", "6.8.2");
        }
    }

    /**
     * 6.8.3: Catalog must have /StructTreeRoot.
     */
    private void checkStructTreeRoot(PdfDictionary catalog, PdfAValidationResult result) {
        if (catalog.get("StructTreeRoot") == null) {
            result.addError("6.8.3",
                    "Catalog must have /StructTreeRoot for Level A compliance",
                    "catalog", "6.8.3");
        }
    }

    /**
     * 6.8.4: Catalog must have /Lang.
     */
    private void checkLang(PdfDictionary catalog, PdfAValidationResult result) {
        if (catalog.get("Lang") == null) {
            result.addError("6.8.4",
                    "Catalog must have /Lang for Level A compliance",
                    "catalog", "6.8.4");
        }
    }

    /**
     * Resolves a PdfBase to a PdfDictionary, dereferencing indirect references.
     */
    private static PdfDictionary resolveDict(PdfBase val) {
        if (val instanceof PdfObjectReference) {
            try {
                val = ((PdfObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return (val instanceof PdfDictionary) ? (PdfDictionary) val : null;
    }
}
