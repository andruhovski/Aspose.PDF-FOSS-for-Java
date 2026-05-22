package org.aspose.pdf.engine.pdfa.fixes;

import org.aspose.pdf.ConvertErrorAction;
import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectKey;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Annotation-related fixes for PDF/A compliance.
 * <p>
 * Removes forbidden annotation types (FileAttachment, Sound, Movie) and corrects
 * the {@code /F} (flags) field on remaining annotations to satisfy the requirements
 * of ISO 19005-1:2005, 6.5.3.
 * </p>
 */
public final class AnnotationFixes {

    private static final Logger LOG = Logger.getLogger(AnnotationFixes.class.getName());

    /** Annotation flag bit positions (§12.5.3, Table 165). */
    private static final int FLAG_INVISIBLE = 0x0001;
    private static final int FLAG_HIDDEN    = 0x0002;
    private static final int FLAG_PRINT     = 0x0004;
    private static final int FLAG_NO_ZOOM   = 0x0008;
    private static final int FLAG_NO_ROTATE = 0x0010;
    private static final int FLAG_NO_VIEW   = 0x0020;

    /** Annotation subtypes that are forbidden in PDF/A. */
    private static final Set<String> FORBIDDEN_SUBTYPES = new HashSet<>();
    static {
        FORBIDDEN_SUBTYPES.add("FileAttachment");
        FORBIDDEN_SUBTYPES.add("Sound");
        FORBIDDEN_SUBTYPES.add("Movie");
    }

    /**
     * Creates a new AnnotationFixes instance.
     */
    public AnnotationFixes() {
        // default
    }

    /**
     * Removes forbidden annotation types (FileAttachment, Sound, Movie) from every
     * page's {@code /Annots} array.
     * <p>
     * When {@code errorAction} is {@link ConvertErrorAction#Delete}, the annotations
     * are silently removed.  Otherwise a warning is logged but no removal occurs.
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void removeForbiddenAnnotations(PDFParser parser, PdfFormat format,
                                           ConvertErrorAction errorAction,
                                           PdfAValidationResult result) throws IOException {
        COSDictionary catalog = parser.getCatalog();
        COSBase pagesRef = catalog.get("Pages");
        if (pagesRef == null) {
            return;
        }
        COSBase pagesObj = parser.resolveReference(pagesRef);
        if (!(pagesObj instanceof COSDictionary)) {
            return;
        }
        processPageTreeForForbiddenAnnotations(parser, (COSDictionary) pagesObj, errorAction, result);
    }

    /**
     * Fixes annotation flag bits on all annotations across all pages.
     * <p>
     * PDF/A requires: Print flag ON, Hidden/Invisible/NoView flags OFF.
     * For Text annotations, NoZoom and NoRotate should also be set.
     * (ISO 19005-1:2005, 6.5.3)
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void fixAnnotationFlags(PDFParser parser, PdfFormat format,
                                   ConvertErrorAction errorAction,
                                   PdfAValidationResult result) throws IOException {
        COSDictionary catalog = parser.getCatalog();
        COSBase pagesRef = catalog.get("Pages");
        if (pagesRef == null) {
            return;
        }
        COSBase pagesObj = parser.resolveReference(pagesRef);
        if (!(pagesObj instanceof COSDictionary)) {
            return;
        }
        processPageTreeForFlags(parser, (COSDictionary) pagesObj, result);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Recursively walks the page tree removing forbidden annotations.
     */
    private void processPageTreeForForbiddenAnnotations(PDFParser parser, COSDictionary node,
                                                        ConvertErrorAction errorAction,
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
                    processPageTreeForForbiddenAnnotations(parser, (COSDictionary) childObj, errorAction, result);
                }
            }
        } else if ("Page".equals(type) || type == null) {
            // It's a page (type may be absent per spec)
            COSBase annotsRef = node.get("Annots");
            if (annotsRef == null) {
                return;
            }
            COSBase annotsObj = parser.resolveReference(annotsRef);
            if (!(annotsObj instanceof COSArray)) {
                return;
            }
            COSArray annots = (COSArray) annotsObj;
            for (int i = annots.size() - 1; i >= 0; i--) {
                COSBase annotRef = annots.get(i);
                COSBase annotObj = parser.resolveReference(annotRef);
                if (!(annotObj instanceof COSDictionary)) {
                    continue;
                }
                COSDictionary annotDict = (COSDictionary) annotObj;
                String subtype = annotDict.getNameAsString("Subtype");
                if (subtype != null && FORBIDDEN_SUBTYPES.contains(subtype)) {
                    if (errorAction == ConvertErrorAction.Delete) {
                        annots.remove(i);
                        result.addWarning("annot.1",
                                "Removed forbidden " + subtype + " annotation",
                                "page/Annots[" + i + "]", "ISO 19005-1:2005, 6.5.3");
                    } else {
                        result.addError("annot.1",
                                "Forbidden " + subtype + " annotation found (not removed, errorAction=None)",
                                "page/Annots[" + i + "]", "ISO 19005-1:2005, 6.5.3");
                    }
                }
            }
        }
    }

    /**
     * Recursively walks the page tree fixing annotation flags.
     */
    private void processPageTreeForFlags(PDFParser parser, COSDictionary node,
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
                    processPageTreeForFlags(parser, (COSDictionary) childObj, result);
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
                if (!(annotObj instanceof COSDictionary)) {
                    continue;
                }
                COSDictionary annotDict = (COSDictionary) annotObj;
                fixSingleAnnotationFlags(annotDict, result, i);
            }
        }
    }

    /**
     * Fixes flag bits on a single annotation dictionary.
     */
    private void fixSingleAnnotationFlags(COSDictionary annotDict, PdfAValidationResult result, int idx) {
        int flags = annotDict.getInt("F", 0);
        int original = flags;

        // Print must be on
        flags |= FLAG_PRINT;
        // Invisible, Hidden, NoView must be off
        flags &= ~FLAG_INVISIBLE;
        flags &= ~FLAG_HIDDEN;
        flags &= ~FLAG_NO_VIEW;

        // For Text annotations, also set NoZoom and NoRotate
        String subtype = annotDict.getNameAsString("Subtype");
        if ("Text".equals(subtype)) {
            flags |= FLAG_NO_ZOOM;
            flags |= FLAG_NO_ROTATE;
        }

        if (flags != original) {
            annotDict.set("F", COSInteger.valueOf(flags));
            result.addWarning("annot.2", "Fixed annotation flags (was 0x"
                    + Integer.toHexString(original) + ", now 0x" + Integer.toHexString(flags) + ")",
                    "page/Annots[" + idx + "]", "ISO 19005-1:2005, 6.5.3");
        }
    }
}
