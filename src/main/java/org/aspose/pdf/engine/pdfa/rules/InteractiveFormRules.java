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

        COSDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not load catalog: {0}", e.getMessage());
            return;
        }

        COSDictionary acroForm = resolveDict(catalog.get("AcroForm"));
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
    private void checkNeedAppearances(COSDictionary acroForm, PdfAValidationResult result) {
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
    private void checkFieldActions(COSDictionary acroForm, PdfAValidationResult result) {
        COSArray fields = resolveArray(acroForm.get("Fields"));
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
    private void checkFieldRecursive(COSBase fieldRef, String path,
                                      PdfAValidationResult result) {
        COSDictionary field = resolveDict(fieldRef);
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
        COSArray kids = resolveArray(field.get("Kids"));
        if (kids != null) {
            for (int i = 0; i < kids.size(); i++) {
                checkFieldRecursive(kids.get(i), path + "/Kids[" + i + "]", result);
            }
        }
    }

    /**
     * 6.9.4: PDF/A-2+: AcroForm must not have /XFA.
     */
    private void checkXfa(COSDictionary acroForm, PdfAValidationResult result) {
        if (acroForm.get("XFA") != null) {
            result.addError("6.9.4",
                    "AcroForm must not contain /XFA for PDF/A-2+ compliance",
                    "catalog/AcroForm/XFA", "6.9.4");
        }
    }

    /**
     * 6.9.4: PDF/A-2+: Catalog must not have /NeedsRendering.
     */
    private void checkNeedsRendering(COSDictionary catalog, PdfAValidationResult result) {
        boolean needsRendering = catalog.getBoolean("NeedsRendering", false);
        if (needsRendering) {
            result.addError("6.9.4",
                    "Catalog /NeedsRendering must not be true for PDF/A-2+ compliance",
                    "catalog/NeedsRendering", "6.9.4");
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
