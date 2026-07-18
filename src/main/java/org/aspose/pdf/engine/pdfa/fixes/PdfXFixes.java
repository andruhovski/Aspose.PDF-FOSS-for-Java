package org.aspose.pdf.engine.pdfa.fixes;

import org.aspose.pdf.ConvertErrorAction;
import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.pdfobjects.*;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.util.logging.Logger;

/// PDF/X-specific fixes (ISO 15930).
///
/// These fixes are only applied when the target format is a PDF/X variant.
/// They ensure that the document carries the mandatory PDF/X version and
/// conformance markers, trap information, page geometry (TrimBox), and an
/// output intent with the correct subtype.
///
public final class PdfXFixes {

    private static final Logger LOG = Logger.getLogger(PdfXFixes.class.getName());

    /// Creates a new PdfXFixes instance.
    public PdfXFixes() {
        // default
    }

    /// Sets the `/GTS_PDFXVersion` key in the Info dictionary.
    ///
    /// For example, for PDF/X-1a:2001 the value is `"PDF/X-1a:2001"`.
    ///
    /// @param parser      the parsed PDF
    /// @param format      the target format
    /// @param errorAction the error action strategy
    /// @param result      the validation result
    /// @throws IOException if an I/O error occurs
    public void addPdfXVersion(PDFParser parser, PdfFormat format,
                               ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        PdfDictionary info = getOrCreateInfo(parser);
        String version = determinePdfXVersionString(format);
        info.set("GTS_PDFXVersion", new PdfString(version));
        result.addWarning("pdfx.1", "Set /GTS_PDFXVersion to " + version,
                "Info/GTS_PDFXVersion", null);
    }

    /// Sets the `/GTS_PDFXConformance` key in the Info dictionary for
    /// PDF/X-1a documents.
    ///
    /// @param parser      the parsed PDF
    /// @param format      the target format
    /// @param errorAction the error action strategy
    /// @param result      the validation result
    /// @throws IOException if an I/O error occurs
    public void addPdfXConformance(PDFParser parser, PdfFormat format,
                                   ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        if (!format.isPdfX1a()) {
            return;
        }
        PdfDictionary info = getOrCreateInfo(parser);
        String conformance = "PDF/X-1a:2001";
        info.set("GTS_PDFXConformance", new PdfString(conformance));
        result.addWarning("pdfx.2", "Set /GTS_PDFXConformance to " + conformance,
                "Info/GTS_PDFXConformance", null);
    }

    /// Sets `/Trapped` to `/False` in the Info dictionary if it is
    /// missing or has an invalid value.
    ///
    /// @param parser      the parsed PDF
    /// @param format      the target format
    /// @param errorAction the error action strategy
    /// @param result      the validation result
    /// @throws IOException if an I/O error occurs
    public void addTrapped(PDFParser parser, PdfFormat format,
                           ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        PdfDictionary info = getOrCreateInfo(parser);
        PdfBase trapped = info.get("Trapped");
        if (trapped == null) {
            info.set("Trapped", PdfName.of("False"));
            result.addWarning("pdfx.3", "Set /Trapped to /False",
                    "Info/Trapped", null);
        }
    }

    /// Copies `/MediaBox` to `/TrimBox` on pages that lack a TrimBox.
    ///
    /// PDF/X requires a TrimBox on every page (ISO 15930-1, 6.3).
    ///
    /// @param parser      the parsed PDF
    /// @param format      the target format
    /// @param errorAction the error action strategy
    /// @param result      the validation result
    /// @throws IOException if an I/O error occurs
    public void addTrimBox(PDFParser parser, PdfFormat format,
                           ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        PdfDictionary catalog = parser.getCatalog();
        PdfBase pagesRef = catalog.get("Pages");
        if (pagesRef == null) {
            return;
        }
        PdfBase pagesObj = parser.resolveReference(pagesRef);
        if (!(pagesObj instanceof PdfDictionary)) {
            return;
        }
        addTrimBoxToPageTree(parser, (PdfDictionary) pagesObj, result);
    }

    /// Creates an OutputIntent with `/S = /GTS_PDFX` if no PDF/X output
    /// intent is present yet.
    ///
    /// @param parser      the parsed PDF
    /// @param format      the target format
    /// @param errorAction the error action strategy
    /// @param result      the validation result
    /// @throws IOException if an I/O error occurs
    public void addOutputIntentPdfX(PDFParser parser, PdfFormat format,
                                    ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        PdfDictionary catalog = parser.getCatalog();
        PdfBase existingRef = catalog.get("OutputIntents");
        if (existingRef != null) {
            PdfBase existingObj = parser.resolveReference(existingRef);
            if (existingObj instanceof PdfArray) {
                PdfArray existing = (PdfArray) existingObj;
                // Check if a PDF/X output intent already exists
                for (int i = 0; i < existing.size(); i++) {
                    PdfBase intentRef = existing.get(i);
                    PdfBase intentObj = parser.resolveReference(intentRef);
                    if (intentObj instanceof PdfDictionary) {
                        String s = ((PdfDictionary) intentObj).getNameAsString("S");
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

        PdfStream iccStream = new PdfStream();
        iccStream.setDecodedData(iccData);
        iccStream.setFilter(PdfName.FLATE_DECODE);
        iccStream.set("N", PdfInteger.valueOf(3));

        int maxObj = findMaxObjectNumber(parser);
        PdfObjectKey iccKey = new PdfObjectKey(maxObj + 1, 0);
        PdfObjectReference iccRef = new PdfObjectReference(iccKey, k -> iccStream);

        PdfDictionary intent = new PdfDictionary();
        intent.set("Type", PdfName.of("OutputIntent"));
        intent.set("S", PdfName.of("GTS_PDFX"));
        intent.set("OutputConditionIdentifier", new PdfString("sRGB IEC61966-2.1"));
        intent.set("RegistryName", new PdfString("http://www.color.org"));
        intent.set("Info", new PdfString("sRGB IEC61966-2.1"));
        intent.set("DestOutputProfile", iccRef);

        PdfObjectKey intentKey = new PdfObjectKey(maxObj + 2, 0);
        PdfObjectReference intentRef = new PdfObjectReference(intentKey, k -> intent);

        // Add to existing OutputIntents or create new array
        PdfBase oiRef = catalog.get("OutputIntents");
        PdfArray intents;
        if (oiRef != null) {
            PdfBase oiObj = parser.resolveReference(oiRef);
            if (oiObj instanceof PdfArray) {
                intents = (PdfArray) oiObj;
            } else {
                intents = new PdfArray(1);
            }
        } else {
            intents = new PdfArray(1);
        }
        intents.add(intentRef);
        catalog.set("OutputIntents", intents);

        result.addWarning("pdfx.5", "Added PDF/X output intent with sRGB profile",
                "catalog/OutputIntents", null);
    }

    /// Removes `/Encrypt` from the trailer for PDF/X-1a compliance.
    ///
    /// @param parser      the parsed PDF
    /// @param format      the target format
    /// @param errorAction the error action strategy
    /// @param result      the validation result
    /// @throws IOException if an I/O error occurs
    public void removeEncryption(PDFParser parser, PdfFormat format,
                                 ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        PdfDictionary trailer = parser.getTrailer();
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

    /// Retrieves the Info dictionary, creating one if absent.
    private PdfDictionary getOrCreateInfo(PDFParser parser) throws IOException {
        PdfDictionary trailer = parser.getTrailer();
        PdfBase infoRef = trailer.get("Info");
        if (infoRef != null) {
            PdfBase infoObj = parser.resolveReference(infoRef);
            if (infoObj instanceof PdfDictionary) {
                return (PdfDictionary) infoObj;
            }
        }

        // Create a new Info dictionary
        PdfDictionary info = new PdfDictionary();
        int maxObj = findMaxObjectNumber(parser);
        PdfObjectKey infoKey = new PdfObjectKey(maxObj + 1, 0);
        PdfObjectReference ref = new PdfObjectReference(infoKey, k -> info);
        trailer.set("Info", ref);
        return info;
    }

    /// Recursively walks the page tree, copying MediaBox to TrimBox where needed.
    private void addTrimBoxToPageTree(PDFParser parser, PdfDictionary node,
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
                    addTrimBoxToPageTree(parser, (PdfDictionary) childObj, result);
                }
            }
        } else if ("Page".equals(type) || type == null) {
            if (node.get("TrimBox") == null) {
                PdfBase mediaBox = node.get("MediaBox");
                if (mediaBox != null) {
                    node.set("TrimBox", mediaBox);
                    result.addWarning("pdfx.4", "Copied /MediaBox to /TrimBox on page",
                            "page/TrimBox", "ISO 15930-1, 6.3");
                }
            }
        }
    }

    /// Determines the PDF/X version string for the given format.
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

    /// Finds the maximum object number currently in the parser.
    private static int findMaxObjectNumber(PDFParser parser) {
        int maxObj = 0;
        for (PdfObjectKey k : parser.getAllObjectKeys()) {
            maxObj = Math.max(maxObj, k.getObjectNumber());
        }
        return maxObj;
    }
}
