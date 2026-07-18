package org.aspose.pdf.engine.pdfa.fixes;

import org.aspose.pdf.ConvertErrorAction;
import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfBoolean;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;

import java.io.IOException;
import java.util.logging.Logger;

/// AcroForm-related fixes for PDF/A compliance.
///
/// Addresses the following requirements:
///
///   - `/NeedAppearances` must be `false` or absent
///     (ISO 19005-1:2005, 6.9)
///   - Field-level `/AA` (additional-actions) dictionaries must be removed
///     (ISO 19005-1:2005, 6.6.1)
///   - `/XFA` must be removed for PDF/A-2 and later
///     (ISO 19005-2:2011, 6.9)
public final class FormFixes {

    private static final Logger LOG = Logger.getLogger(FormFixes.class.getName());

    /// Creates a new FormFixes instance.
    public FormFixes() {
        // default
    }

    /// Sets `/NeedAppearances` to `false` or removes it from the
    /// AcroForm dictionary.
    ///
    /// PDF/A requires that form fields already have appearance streams, so the
    /// viewer must not need to generate them on the fly (ISO 19005-1:2005, 6.9).
    ///
    /// @param parser      the parsed PDF
    /// @param format      the target format
    /// @param errorAction the error action strategy
    /// @param result      the validation result
    /// @throws IOException if an I/O error occurs
    public void fixNeedAppearances(PDFParser parser, PdfFormat format,
                                   ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        PdfDictionary acroForm = getAcroForm(parser);
        if (acroForm == null) {
            return;
        }

        PdfBase na = acroForm.get("NeedAppearances");
        if (na instanceof PdfBoolean && ((PdfBoolean) na).getValue()) {
            LOG.info("Setting /NeedAppearances to false in AcroForm");
            acroForm.set("NeedAppearances", PdfBoolean.FALSE);
            result.addWarning("form.1", "Set /NeedAppearances to false in AcroForm",
                    "catalog/AcroForm/NeedAppearances", "ISO 19005-1:2005, 6.9");
        }
    }

    /// Removes `/AA` dictionaries from all field dictionaries in the
    /// AcroForm field tree.
    ///
    /// @param parser      the parsed PDF
    /// @param format      the target format
    /// @param errorAction the error action strategy
    /// @param result      the validation result
    /// @throws IOException if an I/O error occurs
    public void removeFieldAA(PDFParser parser, PdfFormat format,
                              ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        PdfDictionary acroForm = getAcroForm(parser);
        if (acroForm == null) {
            return;
        }

        PdfBase fieldsRef = acroForm.get("Fields");
        if (fieldsRef == null) {
            return;
        }
        PdfBase fieldsObj = parser.resolveReference(fieldsRef);
        if (!(fieldsObj instanceof PdfArray)) {
            return;
        }

        removeAAFromFieldTree(parser, (PdfArray) fieldsObj, result);
    }

    /// Removes the `/XFA` entry from the AcroForm dictionary.
    ///
    /// XFA forms are not allowed in PDF/A-2 and later (ISO 19005-2:2011, 6.9).
    /// This method should only be called for PDF/A-2+ targets.
    ///
    /// @param parser      the parsed PDF
    /// @param format      the target format
    /// @param errorAction the error action strategy
    /// @param result      the validation result
    /// @throws IOException if an I/O error occurs
    public void removeXFA(PDFParser parser, PdfFormat format,
                          ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        PdfDictionary acroForm = getAcroForm(parser);
        if (acroForm == null) {
            return;
        }

        if (acroForm.get("XFA") != null) {
            LOG.info("Removing /XFA from AcroForm for PDF/A-2+ compliance");
            acroForm.set("XFA", null);
            result.addWarning("form.3", "Removed /XFA from AcroForm",
                    "catalog/AcroForm/XFA", "ISO 19005-2:2011, 6.9");
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /// Retrieves the AcroForm dictionary from the catalog, resolving references.
    ///
    /// @return the AcroForm dictionary, or null if absent
    private PdfDictionary getAcroForm(PDFParser parser) throws IOException {
        PdfDictionary catalog = parser.getCatalog();
        PdfBase acroRef = catalog.get("AcroForm");
        if (acroRef == null) {
            return null;
        }
        PdfBase acroObj = parser.resolveReference(acroRef);
        if (acroObj instanceof PdfDictionary) {
            return (PdfDictionary) acroObj;
        }
        return null;
    }

    /// Recursively walks the field tree removing /AA from field dictionaries.
    private void removeAAFromFieldTree(PDFParser parser, PdfArray fields,
                                       PdfAValidationResult result) throws IOException {
        for (int i = 0; i < fields.size(); i++) {
            PdfBase fieldRef = fields.get(i);
            PdfBase fieldObj = parser.resolveReference(fieldRef);
            if (!(fieldObj instanceof PdfDictionary)) {
                continue;
            }
            PdfDictionary field = (PdfDictionary) fieldObj;

            if (field.get("AA") != null) {
                field.set("AA", null);
                result.addWarning("form.2", "Removed /AA from form field",
                        "AcroForm/Fields[" + i + "]/AA", "ISO 19005-1:2005, 6.6.1");
            }

            // Recurse into child fields (/Kids)
            PdfBase kidsRef = field.get("Kids");
            if (kidsRef != null) {
                PdfBase kidsObj = parser.resolveReference(kidsRef);
                if (kidsObj instanceof PdfArray) {
                    removeAAFromFieldTree(parser, (PdfArray) kidsObj, result);
                }
            }
        }
    }
}
