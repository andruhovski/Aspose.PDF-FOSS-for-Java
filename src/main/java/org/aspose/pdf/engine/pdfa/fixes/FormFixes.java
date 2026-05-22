package org.aspose.pdf.engine.pdfa.fixes;

import org.aspose.pdf.ConvertErrorAction;
import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSBoolean;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * AcroForm-related fixes for PDF/A compliance.
 * <p>
 * Addresses the following requirements:
 * </p>
 * <ul>
 *   <li>{@code /NeedAppearances} must be {@code false} or absent
 *       (ISO 19005-1:2005, 6.9)</li>
 *   <li>Field-level {@code /AA} (additional-actions) dictionaries must be removed
 *       (ISO 19005-1:2005, 6.6.1)</li>
 *   <li>{@code /XFA} must be removed for PDF/A-2 and later
 *       (ISO 19005-2:2011, 6.9)</li>
 * </ul>
 */
public final class FormFixes {

    private static final Logger LOG = Logger.getLogger(FormFixes.class.getName());

    /**
     * Creates a new FormFixes instance.
     */
    public FormFixes() {
        // default
    }

    /**
     * Sets {@code /NeedAppearances} to {@code false} or removes it from the
     * AcroForm dictionary.
     * <p>
     * PDF/A requires that form fields already have appearance streams, so the
     * viewer must not need to generate them on the fly (ISO 19005-1:2005, 6.9).
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void fixNeedAppearances(PDFParser parser, PdfFormat format,
                                   ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        COSDictionary acroForm = getAcroForm(parser);
        if (acroForm == null) {
            return;
        }

        COSBase na = acroForm.get("NeedAppearances");
        if (na instanceof COSBoolean && ((COSBoolean) na).getValue()) {
            LOG.info("Setting /NeedAppearances to false in AcroForm");
            acroForm.set("NeedAppearances", COSBoolean.FALSE);
            result.addWarning("form.1", "Set /NeedAppearances to false in AcroForm",
                    "catalog/AcroForm/NeedAppearances", "ISO 19005-1:2005, 6.9");
        }
    }

    /**
     * Removes {@code /AA} dictionaries from all field dictionaries in the
     * AcroForm field tree.
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void removeFieldAA(PDFParser parser, PdfFormat format,
                              ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        COSDictionary acroForm = getAcroForm(parser);
        if (acroForm == null) {
            return;
        }

        COSBase fieldsRef = acroForm.get("Fields");
        if (fieldsRef == null) {
            return;
        }
        COSBase fieldsObj = parser.resolveReference(fieldsRef);
        if (!(fieldsObj instanceof COSArray)) {
            return;
        }

        removeAAFromFieldTree(parser, (COSArray) fieldsObj, result);
    }

    /**
     * Removes the {@code /XFA} entry from the AcroForm dictionary.
     * <p>
     * XFA forms are not allowed in PDF/A-2 and later (ISO 19005-2:2011, 6.9).
     * This method should only be called for PDF/A-2+ targets.
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void removeXFA(PDFParser parser, PdfFormat format,
                          ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        COSDictionary acroForm = getAcroForm(parser);
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

    /**
     * Retrieves the AcroForm dictionary from the catalog, resolving references.
     *
     * @return the AcroForm dictionary, or null if absent
     */
    private COSDictionary getAcroForm(PDFParser parser) throws IOException {
        COSDictionary catalog = parser.getCatalog();
        COSBase acroRef = catalog.get("AcroForm");
        if (acroRef == null) {
            return null;
        }
        COSBase acroObj = parser.resolveReference(acroRef);
        if (acroObj instanceof COSDictionary) {
            return (COSDictionary) acroObj;
        }
        return null;
    }

    /**
     * Recursively walks the field tree removing /AA from field dictionaries.
     */
    private void removeAAFromFieldTree(PDFParser parser, COSArray fields,
                                       PdfAValidationResult result) throws IOException {
        for (int i = 0; i < fields.size(); i++) {
            COSBase fieldRef = fields.get(i);
            COSBase fieldObj = parser.resolveReference(fieldRef);
            if (!(fieldObj instanceof COSDictionary)) {
                continue;
            }
            COSDictionary field = (COSDictionary) fieldObj;

            if (field.get("AA") != null) {
                field.set("AA", null);
                result.addWarning("form.2", "Removed /AA from form field",
                        "AcroForm/Fields[" + i + "]/AA", "ISO 19005-1:2005, 6.6.1");
            }

            // Recurse into child fields (/Kids)
            COSBase kidsRef = field.get("Kids");
            if (kidsRef != null) {
                COSBase kidsObj = parser.resolveReference(kidsRef);
                if (kidsObj instanceof COSArray) {
                    removeAAFromFieldTree(parser, (COSArray) kidsObj, result);
                }
            }
        }
    }
}
