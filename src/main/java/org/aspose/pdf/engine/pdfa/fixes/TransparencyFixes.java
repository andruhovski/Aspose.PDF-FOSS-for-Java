package org.aspose.pdf.engine.pdfa.fixes;

import org.aspose.pdf.ConvertErrorAction;
import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfFloat;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectKey;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Transparency-related fixes for PDF/A-1 compliance.
 * <p>
 * PDF/A-1 (ISO 19005-1:2005, 6.4) forbids all transparency features.  These
 * fixes neutralise transparency by resetting ExtGState parameters and removing
 * transparency groups from form XObjects.  They should only be applied when
 * the target format is PDF/A-1; PDF/A-2 and later allow transparency.
 * </p>
 */
public final class TransparencyFixes {

    private static final Logger LOG = Logger.getLogger(TransparencyFixes.class.getName());

    /**
     * Creates a new TransparencyFixes instance.
     */
    public TransparencyFixes() {
        // default
    }

    /**
     * Sets {@code /SMask} to {@code /None} in all ExtGState dictionaries.
     * <p>
     * A soft mask different from {@code /None} implies transparency, which is
     * forbidden in PDF/A-1 (ISO 19005-1:2005, 6.4).
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void fixSmask(PDFParser parser, PdfFormat format,
                         ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        for (PdfObjectKey key : parser.getAllObjectKeys()) {
            PdfBase obj = safeGetObject(parser, key);
            if (!(obj instanceof PdfDictionary)) {
                continue;
            }
            PdfDictionary dict = (PdfDictionary) obj;
            if (!"ExtGState".equals(dict.getNameAsString("Type"))) {
                continue;
            }
            PdfBase smask = dict.get("SMask");
            if (smask != null && !(smask instanceof PdfName && "None".equals(((PdfName) smask).getName()))) {
                dict.set("SMask", PdfName.of("None"));
                result.addWarning("trans.1", "Set /SMask to /None in ExtGState",
                        "obj " + key.getObjectNumber(), "ISO 19005-1:2005, 6.4");
            }
        }
    }

    /**
     * Sets {@code /BM} (blend mode) to {@code /Normal} in all ExtGState dictionaries.
     * <p>
     * Any blend mode other than {@code /Normal} or {@code /Compatible} implies
     * transparency (ISO 19005-1:2005, 6.4).
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void fixBlendMode(PDFParser parser, PdfFormat format,
                             ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        for (PdfObjectKey key : parser.getAllObjectKeys()) {
            PdfBase obj = safeGetObject(parser, key);
            if (!(obj instanceof PdfDictionary)) {
                continue;
            }
            PdfDictionary dict = (PdfDictionary) obj;
            if (!"ExtGState".equals(dict.getNameAsString("Type"))) {
                continue;
            }
            PdfBase bm = dict.get("BM");
            if (bm instanceof PdfName) {
                String bmName = ((PdfName) bm).getName();
                if (!"Normal".equals(bmName) && !"Compatible".equals(bmName)) {
                    dict.set("BM", PdfName.of("Normal"));
                    result.addWarning("trans.2", "Set /BM to /Normal in ExtGState (was " + bmName + ")",
                            "obj " + key.getObjectNumber(), "ISO 19005-1:2005, 6.4");
                }
            }
        }
    }

    /**
     * Sets the stroking alpha ({@code /CA}) to {@code 1.0} in all ExtGState
     * dictionaries where it is not already 1.0.
     * <p>
     * A stroking alpha less than 1.0 implies transparency (ISO 19005-1:2005, 6.4).
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void fixStrokingAlpha(PDFParser parser, PdfFormat format,
                                 ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        for (PdfObjectKey key : parser.getAllObjectKeys()) {
            PdfBase obj = safeGetObject(parser, key);
            if (!(obj instanceof PdfDictionary)) {
                continue;
            }
            PdfDictionary dict = (PdfDictionary) obj;
            if (!"ExtGState".equals(dict.getNameAsString("Type"))) {
                continue;
            }
            float ca = dict.getFloat("CA", 1.0f);
            if (ca < 1.0f) {
                dict.set("CA", new PdfFloat(1.0));
                result.addWarning("trans.3", "Set stroking alpha /CA to 1.0 (was " + ca + ")",
                        "obj " + key.getObjectNumber(), "ISO 19005-1:2005, 6.4");
            }
        }
    }

    /**
     * Sets the non-stroking alpha ({@code /ca}) to {@code 1.0} in all ExtGState
     * dictionaries where it is not already 1.0.
     * <p>
     * A non-stroking alpha less than 1.0 implies transparency (ISO 19005-1:2005, 6.4).
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void fixNonStrokingAlpha(PDFParser parser, PdfFormat format,
                                    ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        for (PdfObjectKey key : parser.getAllObjectKeys()) {
            PdfBase obj = safeGetObject(parser, key);
            if (!(obj instanceof PdfDictionary)) {
                continue;
            }
            PdfDictionary dict = (PdfDictionary) obj;
            if (!"ExtGState".equals(dict.getNameAsString("Type"))) {
                continue;
            }
            float ca = dict.getFloat("ca", 1.0f);
            if (ca < 1.0f) {
                dict.set("ca", new PdfFloat(1.0));
                result.addWarning("trans.4", "Set non-stroking alpha /ca to 1.0 (was " + ca + ")",
                        "obj " + key.getObjectNumber(), "ISO 19005-1:2005, 6.4");
            }
        }
    }

    /**
     * Removes {@code /Group} entries with {@code /S = /Transparency} from form
     * XObjects.
     * <p>
     * A transparency group on a form XObject enables transparency compositing,
     * which is forbidden in PDF/A-1 (ISO 19005-1:2005, 6.4).
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void removeTransparencyGroups(PDFParser parser, PdfFormat format,
                                         ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        for (PdfObjectKey key : parser.getAllObjectKeys()) {
            PdfBase obj = safeGetObject(parser, key);
            if (!(obj instanceof PdfStream)) {
                continue;
            }
            PdfStream stream = (PdfStream) obj;
            if (!"Form".equals(stream.getNameAsString("Subtype"))) {
                continue;
            }
            PdfBase groupRef = stream.get("Group");
            if (groupRef == null) {
                continue;
            }
            PdfBase groupObj = parser.resolveReference(groupRef);
            if (groupObj instanceof PdfDictionary) {
                PdfDictionary group = (PdfDictionary) groupObj;
                if ("Transparency".equals(group.getNameAsString("S"))) {
                    stream.set("Group", null);
                    result.addWarning("trans.5", "Removed transparency group from form XObject",
                            "obj " + key.getObjectNumber(), "ISO 19005-1:2005, 6.4");
                }
            }
        }
    }

    /**
     * Safely loads an object, returning null on failure.
     */
    private static PdfBase safeGetObject(PDFParser parser, PdfObjectKey key) throws IOException {
        try {
            return parser.getObject(key);
        } catch (IOException e) {
            LOG.fine(() -> "Could not load object " + key + ": " + e.getMessage());
            return null;
        }
    }
}
