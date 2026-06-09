package org.aspose.pdf.engine.pdfa.rules;

import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfa.PdfARule;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validates interactive form requirements for PDF/A compliance.
 *
 * <p>Checks the following ISO 19005 clauses:</p>
 * <ul>
 *   <li>6.9.1 &mdash; AcroForm /NeedAppearances must be absent or false</li>
 *   <li>6.9.2 &mdash; Widget/Field dicts must not have /AA</li>
 *   <li>6.9.4 &mdash; PDF/A-2+: AcroForm must not have /XFA; catalog must not have /NeedsRendering</li>
 * </ul>
 */
public final class InteractiveFormRules implements PdfARule {

    private static final Logger LOG = Logger.getLogger(InteractiveFormRules.class.getName());

    /**
     * Creates a new interactive form rules checker.
     */
    public InteractiveFormRules() {
        // default constructor
    }

    @Override
    public void validate(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        if (!format.isPdfA()) {
            return;
        }

        PdfDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not load catalog: {0}", e.getMessage());
            return;
        }

        PdfDictionary acroForm = resolveDict(catalog.get("AcroForm"));
        if (acroForm == null) {
            // No AcroForm means no form-related violations
            // But still check NeedsRendering
            if (format.isPdfA2OrLater()) {
                checkNeedsRendering(catalog, result);
            }
            return;
        }

        // 6.9.1: /NeedAppearances must be absent or false
        checkNeedAppearances(acroForm, result);

        // 6.9.2: Check fields for /AA
        checkFieldActions(acroForm, result);

        // 6.9.4: PDF/A-2+ specific checks
        if (format.isPdfA2OrLater()) {
            checkXfa(acroForm, result);
            checkNeedsRendering(catalog, result);
        }
    }

    /**
     * 6.9.1: AcroForm /NeedAppearances must be absent or false.
     */
    private void checkNeedAppearances(PdfDictionary acroForm, PdfAValidationResult result) {
        boolean needAppearances = acroForm.getBoolean("NeedAppearances", false);
        if (needAppearances) {
            result.addError("6.9.1",
                    "AcroForm /NeedAppearances must be false or absent for PDF/A compliance",
                    "catalog/AcroForm", "6.9.1");
        }
    }

    /**
     * 6.9.2: Widget/Field dicts must not have /AA.
     */
    private void checkFieldActions(PdfDictionary acroForm, PdfAValidationResult result) {
        PdfArray fields = resolveArray(acroForm.get("Fields"));
        if (fields == null) {
            return;
        }

        for (int i = 0; i < fields.size(); i++) {
            checkFieldRecursive(fields.get(i), "catalog/AcroForm/Fields[" + i + "]", result);
        }
    }

    /**
     * Recursively checks a field and its children for /AA.
     */
    private void checkFieldRecursive(PdfBase fieldRef, String path,
                                      PdfAValidationResult result) {
        PdfDictionary field = resolveDict(fieldRef);
        if (field == null) {
            return;
        }

        // 6.9.2: Field must not have /AA
        if (field.get("AA") != null) {
            result.addError("6.9.2",
                    "Form field/widget must not contain /AA (additional actions)",
                    path + "/AA", "6.9.2");
        }

        // Recurse into /Kids
        PdfArray kids = resolveArray(field.get("Kids"));
        if (kids != null) {
            for (int i = 0; i < kids.size(); i++) {
                checkFieldRecursive(kids.get(i), path + "/Kids[" + i + "]", result);
            }
        }
    }

    /**
     * 6.9.4: PDF/A-2+: AcroForm must not have /XFA.
     */
    private void checkXfa(PdfDictionary acroForm, PdfAValidationResult result) {
        if (acroForm.get("XFA") != null) {
            result.addError("6.9.4",
                    "AcroForm must not contain /XFA for PDF/A-2+ compliance",
                    "catalog/AcroForm/XFA", "6.9.4");
        }
    }

    /**
     * 6.9.4: PDF/A-2+: Catalog must not have /NeedsRendering.
     */
    private void checkNeedsRendering(PdfDictionary catalog, PdfAValidationResult result) {
        boolean needsRendering = catalog.getBoolean("NeedsRendering", false);
        if (needsRendering) {
            result.addError("6.9.4",
                    "Catalog /NeedsRendering must not be true for PDF/A-2+ compliance",
                    "catalog/NeedsRendering", "6.9.4");
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

    /**
     * Resolves a PdfBase to a PdfArray, dereferencing indirect references.
     */
    private static PdfArray resolveArray(PdfBase val) {
        if (val instanceof PdfObjectReference) {
            try {
                val = ((PdfObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return (val instanceof PdfArray) ? (PdfArray) val : null;
    }
}
