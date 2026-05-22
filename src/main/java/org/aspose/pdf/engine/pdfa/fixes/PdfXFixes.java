package org.aspose.pdf.engine.pdfa.fixes;

import org.aspose.pdf.ConvertErrorAction;
import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectKey;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.cos.COSString;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.parser.PDFParser;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;

/**
 * PDF/X-specific fixes (ISO 15930).
 * <p>
 * These fixes are only applied when the target format is a PDF/X variant.
 * They ensure that the document carries the mandatory PDF/X version and
 * conformance markers, trap information, page geometry (TrimBox), and an
 * output intent with the correct subtype.
 * </p>
 */
public final class PdfXFixes {

    private static final Logger LOG = Logger.getLogger(PdfXFixes.class.getName());

    /**
     * Creates a new PdfXFixes instance.
     */
    public PdfXFixes() {
        // default
    }

    /**
     * Sets the {@code /GTS_PDFXVersion} key in the Info dictionary.
     * <p>
     * For example, for PDF/X-1a:2001 the value is {@code "PDF/X-1a:2001"}.
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void addPdfXVersion(PDFParser parser, PdfFormat format,
                               ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        COSDictionary info = getOrCreateInfo(parser);
        String version = determinePdfXVersionString(format);
        info.set("GTS_PDFXVersion", new COSString(version));
        result.addWarning("pdfx.1", "Set /GTS_PDFXVersion to " + version,
                "Info/GTS_PDFXVersion", null);
    }

    /**
     * Sets the {@code /GTS_PDFXConformance} key in the Info dictionary for
     * PDF/X-1a documents.
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void addPdfXConformance(PDFParser parser, PdfFormat format,
                                   ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        if (!format.isPdfX1a()) {
            return;
        }
        COSDictionary info = getOrCreateInfo(parser);
        String conformance = "PDF/X-1a:2001";
        info.set("GTS_PDFXConformance", new COSString(conformance));
        result.addWarning("pdfx.2", "Set /GTS_PDFXConformance to " + conformance,
                "Info/GTS_PDFXConformance", null);
    }

    /**
     * Sets {@code /Trapped} to {@code /False} in the Info dictionary if it is
     * missing or has an invalid value.
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void addTrapped(PDFParser parser, PdfFormat format,
                           ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        COSDictionary info = getOrCreateInfo(parser);
        COSBase trapped = info.get("Trapped");
        if (trapped == null) {
            info.set("Trapped", COSName.of("False"));
            result.addWarning("pdfx.3", "Set /Trapped to /False",
                    "Info/Trapped", null);
        }
    }

    /**
     * Copies {@code /MediaBox} to {@code /TrimBox} on pages that lack a TrimBox.
     * <p>
     * PDF/X requires a TrimBox on every page (ISO 15930-1, 6.3).
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void addTrimBox(PDFParser parser, PdfFormat format,
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
        addTrimBoxToPageTree(parser, (COSDictionary) pagesObj, result);
    }

    /**
     * Creates an OutputIntent with {@code /S = /GTS_PDFX} if no PDF/X output
     * intent is present yet.
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void addOutputIntentPdfX(PDFParser parser, PdfFormat format,
                                    ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        COSDictionary catalog = parser.getCatalog();
        COSBase existingRef = catalog.get("OutputIntents");
        if (existingRef != null) {
            COSBase existingObj = parser.resolveReference(existingRef);
            if (existingObj instanceof COSArray) {
                COSArray existing = (COSArray) existingObj;
                // Check if a PDF/X output intent already exists
                for (int i = 0; i < existing.size(); i++) {
                    COSBase intentRef = existing.get(i);
                    COSBase intentObj = parser.resolveReference(intentRef);
                    if (intentObj instanceof COSDictionary) {
                        String s = ((COSDictionary) intentObj).getNameAsString("S");
                        if ("GTS_PDFX".equals(s)) {
                            LOG.fine("PDF/X output intent already exists");
                            return;
                        }
                    }
                }
            }
        }

        LOG.info("Adding PDF/X output intent");

        // Build sRGB ICC profile stream
        ICC_Profile srgb = ICC_Profile.getInstance(ColorSpace.CS_sRGB);
        byte[] iccData = srgb.getData();

        COSStream iccStream = new COSStream();
        iccStream.setDecodedData(iccData);
        iccStream.setFilter(COSName.FLATE_DECODE);
        iccStream.set("N", COSInteger.valueOf(3));

        int maxObj = findMaxObjectNumber(parser);
        COSObjectKey iccKey = new COSObjectKey(maxObj + 1, 0);
        COSObjectReference iccRef = new COSObjectReference(iccKey, k -> iccStream);

        COSDictionary intent = new COSDictionary();
        intent.set("Type", COSName.of("OutputIntent"));
        intent.set("S", COSName.of("GTS_PDFX"));
        intent.set("OutputConditionIdentifier", new COSString("sRGB IEC61966-2.1"));
        intent.set("RegistryName", new COSString("http://www.color.org"));
        intent.set("Info", new COSString("sRGB IEC61966-2.1"));
        intent.set("DestOutputProfile", iccRef);

        COSObjectKey intentKey = new COSObjectKey(maxObj + 2, 0);
        COSObjectReference intentRef = new COSObjectReference(intentKey, k -> intent);

        // Add to existing OutputIntents or create new array
        COSBase oiRef = catalog.get("OutputIntents");
        COSArray intents;
        if (oiRef != null) {
            COSBase oiObj = parser.resolveReference(oiRef);
            if (oiObj instanceof COSArray) {
                intents = (COSArray) oiObj;
            } else {
                intents = new COSArray(1);
            }
        } else {
            intents = new COSArray(1);
        }
        intents.add(intentRef);
        catalog.set("OutputIntents", intents);

        result.addWarning("pdfx.5", "Added PDF/X output intent with sRGB profile",
                "catalog/OutputIntents", null);
    }

    /**
     * Removes {@code /Encrypt} from the trailer for PDF/X-1a compliance.
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void removeEncryption(PDFParser parser, PdfFormat format,
                                 ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        COSDictionary trailer = parser.getTrailer();
        if (trailer.get("Encrypt") != null) {
            LOG.info("Removing /Encrypt from trailer for PDF/X-1a compliance");
            trailer.set("Encrypt", null);
            result.addWarning("pdfx.6", "Removed /Encrypt from trailer for PDF/X-1a",
                    "trailer/Encrypt", null);
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Retrieves the Info dictionary, creating one if absent.
     */
    private COSDictionary getOrCreateInfo(PDFParser parser) throws IOException {
        COSDictionary trailer = parser.getTrailer();
        COSBase infoRef = trailer.get("Info");
        if (infoRef != null) {
            COSBase infoObj = parser.resolveReference(infoRef);
            if (infoObj instanceof COSDictionary) {
                return (COSDictionary) infoObj;
            }
        }

        // Create a new Info dictionary
        COSDictionary info = new COSDictionary();
        int maxObj = findMaxObjectNumber(parser);
        COSObjectKey infoKey = new COSObjectKey(maxObj + 1, 0);
        COSObjectReference ref = new COSObjectReference(infoKey, k -> info);
        trailer.set("Info", ref);
        return info;
    }

    /**
     * Recursively walks the page tree, copying MediaBox to TrimBox where needed.
     */
    private void addTrimBoxToPageTree(PDFParser parser, COSDictionary node,
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
                    addTrimBoxToPageTree(parser, (COSDictionary) childObj, result);
                }
            }
        } else if ("Page".equals(type) || type == null) {
            if (node.get("TrimBox") == null) {
                COSBase mediaBox = node.get("MediaBox");
                if (mediaBox != null) {
                    node.set("TrimBox", mediaBox);
                    result.addWarning("pdfx.4", "Copied /MediaBox to /TrimBox on page",
                            "page/TrimBox", "ISO 15930-1, 6.3");
                }
            }
        }
    }

    /**
     * Determines the PDF/X version string for the given format.
     */
    private static String determinePdfXVersionString(PdfFormat format) {
        switch (format) {
            case PDF_X_1A:
            case PDF_X_1A_2001:
                return "PDF/X-1a:2001";
            case PDF_X_3:
                return "PDF/X-3:2003";
            default:
                return "PDF/X-1a:2001";
        }
    }

    /**
     * Finds the maximum object number currently in the parser.
     */
    private static int findMaxObjectNumber(PDFParser parser) {
        int maxObj = 0;
        for (COSObjectKey k : parser.getAllObjectKeys()) {
            maxObj = Math.max(maxObj, k.getObjectNumber());
        }
        return maxObj;
    }
}
