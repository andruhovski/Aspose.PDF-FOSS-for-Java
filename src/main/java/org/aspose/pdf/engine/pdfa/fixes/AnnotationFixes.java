package org.aspose.pdf.engine.pdfa.fixes;

import org.aspose.pdf.ConvertErrorAction;
import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/// Annotation-related fixes for PDF/A compliance.
///
/// Removes forbidden annotation types (FileAttachment, Sound, Movie) and corrects
/// the `/F` (flags) field on remaining annotations to satisfy the requirements
/// of ISO 19005-1:2005, 6.5.3.
///
public final class AnnotationFixes {

    private static final Logger LOG = Logger.getLogger(AnnotationFixes.class.getName());

    /// Annotation flag bit positions (§12.5.3, Table 165).
    private static final int FLAG_INVISIBLE = 0x0001;
    private static final int FLAG_HIDDEN    = 0x0002;
    private static final int FLAG_PRINT     = 0x0004;
    private static final int FLAG_NO_ZOOM   = 0x0008;
    private static final int FLAG_NO_ROTATE = 0x0010;
    private static final int FLAG_NO_VIEW   = 0x0020;

    /// Annotation subtypes that are forbidden in PDF/A.
    private static final Set<String> FORBIDDEN_SUBTYPES = new HashSet<>();
    static {
        FORBIDDEN_SUBTYPES.add("FileAttachment");
        FORBIDDEN_SUBTYPES.add("Sound");
        FORBIDDEN_SUBTYPES.add("Movie");
    }

    /// Creates a new AnnotationFixes instance.
    public AnnotationFixes() {
        // default
    }

    /// Removes forbidden annotation types (FileAttachment, Sound, Movie) from every
    /// page's `/Annots` array.
    ///
    /// When `errorAction` is [ConvertErrorAction#Delete], the annotations
    /// are silently removed.  Otherwise a warning is logged but no removal occurs.
    ///
    /// @param parser      the parsed PDF
    /// @param format      the target format
    /// @param errorAction the error action strategy
    /// @param result      the validation result
    /// @throws IOException if an I/O error occurs
    public void removeForbiddenAnnotations(PDFParser parser, PdfFormat format,
                                           ConvertErrorAction errorAction,
                                           PdfAValidationResult result) throws IOException {
        PdfDictionary catalog = parser.getCatalog();
        PdfBase pagesRef = catalog.get("Pages");
        if (pagesRef == null) {
            return;
        }
        PdfBase pagesObj = parser.resolveReference(pagesRef);
        if (!(pagesObj instanceof PdfDictionary)) {
            return;
        }
        processPageTreeForForbiddenAnnotations(parser, (PdfDictionary) pagesObj, errorAction, result);
    }

    /// Fixes annotation flag bits on all annotations across all pages.
    ///
    /// PDF/A requires: Print flag ON, Hidden/Invisible/NoView flags OFF.
    /// For Text annotations, NoZoom and NoRotate should also be set.
    /// (ISO 19005-1:2005, 6.5.3)
    ///
    /// @param parser      the parsed PDF
    /// @param format      the target format
    /// @param errorAction the error action strategy
    /// @param result      the validation result
    /// @throws IOException if an I/O error occurs
    public void fixAnnotationFlags(PDFParser parser, PdfFormat format,
                                   ConvertErrorAction errorAction,
                                   PdfAValidationResult result) throws IOException {
        PdfDictionary catalog = parser.getCatalog();
        PdfBase pagesRef = catalog.get("Pages");
        if (pagesRef == null) {
            return;
        }
        PdfBase pagesObj = parser.resolveReference(pagesRef);
        if (!(pagesObj instanceof PdfDictionary)) {
            return;
        }
        processPageTreeForFlags(parser, (PdfDictionary) pagesObj, result);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /// Recursively walks the page tree removing forbidden annotations.
    private void processPageTreeForForbiddenAnnotations(PDFParser parser, PdfDictionary node,
                                                        ConvertErrorAction errorAction,
                                                        PdfAValidationResult result) throws IOException {
        String type = node.getNameAsString("Type");
        if ("Pages".equals(type)) {
            PdfBase kidsRef = node.get("Kids");
            if (kidsRef == null) {
                return;
            }
            PdfBase kidsObj = parser.resolveReference(kidsRef);
            if (!(kidsObj instanceof PdfArray)) {
                return;
            }
            PdfArray kids = (PdfArray) kidsObj;
            for (int i = 0; i < kids.size(); i++) {
                PdfBase childRef = kids.get(i);
                PdfBase childObj = parser.resolveReference(childRef);
                if (childObj instanceof PdfDictionary) {
                    processPageTreeForForbiddenAnnotations(parser, (PdfDictionary) childObj, errorAction, result);
                }
            }
        } else if ("Page".equals(type) || type == null) {
            // It's a page (type may be absent per spec)
            PdfBase annotsRef = node.get("Annots");
            if (annotsRef == null) {
                return;
            }
            PdfBase annotsObj = parser.resolveReference(annotsRef);
            if (!(annotsObj instanceof PdfArray)) {
                return;
            }
            PdfArray annots = (PdfArray) annotsObj;
            for (int i = annots.size() - 1; i >= 0; i--) {
                PdfBase annotRef = annots.get(i);
                PdfBase annotObj = parser.resolveReference(annotRef);
                if (!(annotObj instanceof PdfDictionary)) {
                    continue;
                }
                PdfDictionary annotDict = (PdfDictionary) annotObj;
                String subtype = annotDict.getNameAsString("Subtype");
                if (subtype != null && FORBIDDEN_SUBTYPES.contains(subtype)) {
                    if (errorAction != null && errorAction.isDelete()) {
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

    /// Recursively walks the page tree fixing annotation flags.
    private void processPageTreeForFlags(PDFParser parser, PdfDictionary node,
                                         PdfAValidationResult result) throws IOException {
        String type = node.getNameAsString("Type");
        if ("Pages".equals(type)) {
            PdfBase kidsRef = node.get("Kids");
            if (kidsRef == null) {
                return;
            }
            PdfBase kidsObj = parser.resolveReference(kidsRef);
            if (!(kidsObj instanceof PdfArray)) {
                return;
            }
            PdfArray kids = (PdfArray) kidsObj;
            for (int i = 0; i < kids.size(); i++) {
                PdfBase childRef = kids.get(i);
                PdfBase childObj = parser.resolveReference(childRef);
                if (childObj instanceof PdfDictionary) {
                    processPageTreeForFlags(parser, (PdfDictionary) childObj, result);
                }
            }
        } else if ("Page".equals(type) || type == null) {
            PdfBase annotsRef = node.get("Annots");
            if (annotsRef == null) {
                return;
            }
            PdfBase annotsObj = parser.resolveReference(annotsRef);
            if (!(annotsObj instanceof PdfArray)) {
                return;
            }
            PdfArray annots = (PdfArray) annotsObj;
            for (int i = 0; i < annots.size(); i++) {
                PdfBase annotRef = annots.get(i);
                PdfBase annotObj = parser.resolveReference(annotRef);
                if (!(annotObj instanceof PdfDictionary)) {
                    continue;
                }
                PdfDictionary annotDict = (PdfDictionary) annotObj;
                fixSingleAnnotationFlags(annotDict, result, i);
            }
        }
    }

    /// Fixes flag bits on a single annotation dictionary.
    private void fixSingleAnnotationFlags(PdfDictionary annotDict, PdfAValidationResult result, int idx) {
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
            annotDict.set("F", PdfInteger.valueOf(flags));
            result.addWarning("annot.2", "Fixed annotation flags (was 0x"
                    + Integer.toHexString(original) + ", now 0x" + Integer.toHexString(flags) + ")",
                    "page/Annots[" + idx + "]", "ISO 19005-1:2005, 6.5.3");
        }
    }
}
