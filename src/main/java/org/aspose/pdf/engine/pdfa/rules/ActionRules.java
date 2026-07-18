package org.aspose.pdf.engine.pdfa.rules;

import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfa.PdfARule;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/// Validates action requirements for PDF/A compliance.
///
/// Checks the following ISO 19005 clauses:
///
///   - 6.6.1 — Forbidden action types in catalog, pages, and annotations
///   - 6.6.2 — No /AA (additional actions) in catalog, pages, or widget annotations
public final class ActionRules implements PdfARule {

    private static final Logger LOG = Logger.getLogger(ActionRules.class.getName());

    /// Action types forbidden in all PDF/A versions.
    private static final Set<String> FORBIDDEN_ALL = new HashSet<>(Arrays.asList(
            "Launch", "Sound", "Movie", "ResetForm", "ImportData", "JavaScript"
    ));

    /// Additional action types forbidden in PDF/A-2+.
    private static final Set<String> FORBIDDEN_PDFA2_EXTRA = new HashSet<>(Arrays.asList(
            "Hide", "SetOCGState", "Rendition", "Trans", "GoTo3DView"
    ));

    /// Creates a new action rules checker.
    public ActionRules() {
        // default constructor
    }

    @Override
    public void validate(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        if (!format.isPdfA()) {
            return;
        }

        checkCatalog(parser, format, result);
        checkPages(parser, format, result);
    }

    /// Checks the catalog for forbidden actions and /AA.
    private void checkCatalog(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        PdfDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not load catalog: {0}", e.getMessage());
            return;
        }

        // 6.6.2: No /AA in catalog
        if (catalog.get("AA") != null) {
            result.addError("6.6.2",
                    "Catalog must not contain /AA (additional actions)",
                    "catalog/AA", "6.6.2");
        }

        // Check /OpenAction if it's an action dictionary
        checkActionDict(catalog.get("OpenAction"), "catalog/OpenAction", format, result);
    }

    /// Iterates pages and checks for forbidden actions and /AA.
    private void checkPages(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        PdfDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            return;
        }

        PdfDictionary pages = resolveDict(catalog.get("Pages"));
        if (pages == null) {
            return;
        }
        PdfArray kids = pages.getArray("Kids");
        if (kids == null) {
            return;
        }

        for (int i = 0; i < kids.size(); i++) {
            PdfDictionary page = resolveDict(kids.get(i));
            if (page == null) {
                continue;
            }
            String pagePath = "page[" + i + "]";

            // 6.6.2: No /AA in pages
            if (page.get("AA") != null) {
                result.addError("6.6.2",
                        "Page must not contain /AA (additional actions)",
                        pagePath + "/AA", "6.6.2");
            }

            // Check page /A action
            checkActionDict(page.get("A"), pagePath + "/A", format, result);

            // Check annotations
            checkAnnotationActions(page, pagePath, format, result);
        }
    }

    /// Checks annotation actions and /AA on widget annotations.
    private void checkAnnotationActions(PdfDictionary page, String pagePath,
                                         PdfFormat format, PdfAValidationResult result) {
        PdfArray annots = resolveArray(page.get("Annots"));
        if (annots == null) {
            return;
        }

        for (int i = 0; i < annots.size(); i++) {
            PdfDictionary annot = resolveDict(annots.get(i));
            if (annot == null) {
                continue;
            }
            String annotPath = pagePath + "/Annots[" + i + "]";

            // Check /A action on annotation
            checkActionDict(annot.get("A"), annotPath + "/A", format, result);

            // 6.6.2: Widget annotations must not have /AA
            String subtype = annot.getNameAsString("Subtype");
            if ("Widget".equals(subtype) && annot.get("AA") != null) {
                result.addError("6.6.2",
                        "Widget annotation must not contain /AA (additional actions)",
                        annotPath + "/AA", "6.6.2");
            }
        }
    }

    /// Checks an action dictionary for forbidden action types.
    private void checkActionDict(PdfBase actionRef, String path,
                                  PdfFormat format, PdfAValidationResult result) {
        PdfDictionary action = resolveDict(actionRef);
        if (action == null) {
            return;
        }

        String actionType = action.getNameAsString("S");
        if (actionType == null) {
            return;
        }

        // 6.6.1: Check against forbidden action types
        if (FORBIDDEN_ALL.contains(actionType)) {
            result.addError("6.6.1",
                    "Action type '" + actionType + "' is not permitted in PDF/A",
                    path, "6.6.1");
        } else if (format.isPdfA2OrLater() && FORBIDDEN_PDFA2_EXTRA.contains(actionType)) {
            result.addError("6.6.1",
                    "Action type '" + actionType + "' is not permitted in PDF/A-2+",
                    path, "6.6.1");
        }

        // Recursively check /Next action chain
        checkActionDict(action.get("Next"), path + "/Next", format, result);
    }

    /// Resolves a PdfBase to a PdfDictionary, dereferencing indirect references.
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

    /// Resolves a PdfBase to a PdfArray, dereferencing indirect references.
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
