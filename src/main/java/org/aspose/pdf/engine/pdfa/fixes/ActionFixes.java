package org.aspose.pdf.engine.pdfa.fixes;

import org.aspose.pdf.ConvertErrorAction;
import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectKey;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Action-related fixes for PDF/A compliance.
 * <p>
 * PDF/A forbids additional-actions dictionaries ({@code /AA}) on the catalog,
 * pages, and widget annotations.  It also forbids certain action types such as
 * JavaScript, Launch, Sound, Movie, ResetForm, ImportData, and others
 * (ISO 19005-1:2005, 6.6.1 / 6.6.2).
 * </p>
 */
public final class ActionFixes {

    private static final Logger LOG = Logger.getLogger(ActionFixes.class.getName());

    /** Action types forbidden in PDF/A. */
    private static final Set<String> FORBIDDEN_ACTION_TYPES = new HashSet<>();
    static {
        FORBIDDEN_ACTION_TYPES.add("JavaScript");
        FORBIDDEN_ACTION_TYPES.add("Launch");
        FORBIDDEN_ACTION_TYPES.add("Sound");
        FORBIDDEN_ACTION_TYPES.add("Movie");
        FORBIDDEN_ACTION_TYPES.add("ResetForm");
        FORBIDDEN_ACTION_TYPES.add("ImportData");
        FORBIDDEN_ACTION_TYPES.add("SetState");     // deprecated
        FORBIDDEN_ACTION_TYPES.add("NOP");           // deprecated
    }

    /**
     * Creates a new ActionFixes instance.
     */
    public ActionFixes() {
        // default
    }

    /**
     * Removes the {@code /AA} (additional-actions) dictionary from the catalog.
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void removeCatalogAA(PDFParser parser, PdfFormat format,
                                ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        COSDictionary catalog = parser.getCatalog();
        if (catalog.get("AA") != null) {
            LOG.info("Removing /AA from catalog");
            catalog.set("AA", null);
            result.addWarning("action.1", "Removed /AA from catalog",
                    "catalog/AA", "ISO 19005-1:2005, 6.6.1");
        }
    }

    /**
     * Removes the {@code /AA} dictionary from all page objects.
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void removePageAA(PDFParser parser, PdfFormat format,
                             ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        COSDictionary catalog = parser.getCatalog();
        COSBase pagesRef = catalog.get("Pages");
        if (pagesRef == null) {
            return;
        }
        COSBase pagesObj = parser.resolveReference(pagesRef);
        if (!(pagesObj instanceof COSDictionary)) {
            return;
        }
        removeAAFromPageTree(parser, (COSDictionary) pagesObj, result);
    }

    /**
     * Removes the {@code /AA} dictionary from all annotation and widget
     * dictionaries found across all pages.
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void removeWidgetAA(PDFParser parser, PdfFormat format,
                               ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        COSDictionary catalog = parser.getCatalog();
        COSBase pagesRef = catalog.get("Pages");
        if (pagesRef == null) {
            return;
        }
        COSBase pagesObj = parser.resolveReference(pagesRef);
        if (!(pagesObj instanceof COSDictionary)) {
            return;
        }
        removeAAFromAnnotations(parser, (COSDictionary) pagesObj, result);
    }

    /**
     * Removes {@code /A} entries with forbidden action types from all annotation
     * dictionaries.
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void removeForbiddenActions(PDFParser parser, PdfFormat format,
                                       ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        for (COSObjectKey key : parser.getAllObjectKeys()) {
            COSBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                continue;
            }
            if (!(obj instanceof COSDictionary)) {
                continue;
            }
            COSDictionary dict = (COSDictionary) obj;
            removeForbiddenActionEntry(parser, dict, key, errorAction, result);
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Recursively walks the page tree removing /AA from page dictionaries.
     */
    private void removeAAFromPageTree(PDFParser parser, COSDictionary node,
                                      PdfAValidationResult result) throws IOException {
        String type = node.getNameAsString("Type");
        if ("Pages".equals(type)) {
            COSBase kidsRef = node.get("Kids");
            if (kidsRef == null) {
                return;
            }
            COSBase kidsObj = parser.resolveReference(kidsRef);
            if (!(kidsObj instanceof COSArray)) {
                return;
            }
            COSArray kids = (COSArray) kidsObj;
            for (int i = 0; i < kids.size(); i++) {
                COSBase childRef = kids.get(i);
                COSBase childObj = parser.resolveReference(childRef);
                if (childObj instanceof COSDictionary) {
                    removeAAFromPageTree(parser, (COSDictionary) childObj, result);
                }
            }
        } else if ("Page".equals(type) || type == null) {
            if (node.get("AA") != null) {
                node.set("AA", null);
                result.addWarning("action.2", "Removed /AA from page",
                        "page/AA", "ISO 19005-1:2005, 6.6.1");
            }
        }
    }

    /**
     * Recursively walks the page tree removing /AA from annotations.
     */
    private void removeAAFromAnnotations(PDFParser parser, COSDictionary node,
                                         PdfAValidationResult result) throws IOException {
        String type = node.getNameAsString("Type");
        if ("Pages".equals(type)) {
            COSBase kidsRef = node.get("Kids");
            if (kidsRef == null) {
                return;
            }
            COSBase kidsObj = parser.resolveReference(kidsRef);
            if (!(kidsObj instanceof COSArray)) {
                return;
            }
            COSArray kids = (COSArray) kidsObj;
            for (int i = 0; i < kids.size(); i++) {
                COSBase childRef = kids.get(i);
                COSBase childObj = parser.resolveReference(childRef);
                if (childObj instanceof COSDictionary) {
                    removeAAFromAnnotations(parser, (COSDictionary) childObj, result);
                }
            }
        } else if ("Page".equals(type) || type == null) {
            COSBase annotsRef = node.get("Annots");
            if (annotsRef == null) {
                return;
            }
            COSBase annotsObj = parser.resolveReference(annotsRef);
            if (!(annotsObj instanceof COSArray)) {
                return;
            }
            COSArray annots = (COSArray) annotsObj;
            for (int i = 0; i < annots.size(); i++) {
                COSBase annotRef = annots.get(i);
                COSBase annotObj = parser.resolveReference(annotRef);
                if (annotObj instanceof COSDictionary) {
                    COSDictionary annotDict = (COSDictionary) annotObj;
                    if (annotDict.get("AA") != null) {
                        annotDict.set("AA", null);
                        result.addWarning("action.3", "Removed /AA from annotation",
                                "page/Annots[" + i + "]/AA", "ISO 19005-1:2005, 6.6.1");
                    }
                }
            }
        }
    }

    /**
     * Removes the /A entry from a dictionary if it references a forbidden action type.
     */
    private void removeForbiddenActionEntry(PDFParser parser, COSDictionary dict,
                                            COSObjectKey key, ConvertErrorAction errorAction,
                                            PdfAValidationResult result) throws IOException {
        COSBase actionRef = dict.get("A");
        if (actionRef == null) {
            return;
        }
        COSBase actionObj = parser.resolveReference(actionRef);
        if (!(actionObj instanceof COSDictionary)) {
            return;
        }
        COSDictionary actionDict = (COSDictionary) actionObj;
        String actionType = actionDict.getNameAsString("S");
        if (actionType != null && FORBIDDEN_ACTION_TYPES.contains(actionType)) {
            if (errorAction == ConvertErrorAction.Delete) {
                dict.set("A", null);
                result.addWarning("action.4", "Removed forbidden " + actionType + " action",
                        "obj " + key.getObjectNumber() + "/A", "ISO 19005-1:2005, 6.6.2");
            } else {
                result.addError("action.4",
                        "Forbidden " + actionType + " action found (not removed, errorAction=None)",
                        "obj " + key.getObjectNumber() + "/A", "ISO 19005-1:2005, 6.6.2");
            }
        }
    }
}
