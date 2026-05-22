package org.aspose.pdf.engine.pdfa.fixes;

import org.aspose.pdf.ConvertErrorAction;
import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSBoolean;
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
 * Graphics-related fixes for PDF/A compliance.
 * <p>
 * Handles output-intent creation (with an sRGB ICC profile), removal of image
 * alternate representations, removal of OPI dictionaries, and interpolation flag
 * correction.
 * </p>
 */
public final class GraphicsFixes {

    private static final Logger LOG = Logger.getLogger(GraphicsFixes.class.getName());

    /**
     * Creates a new GraphicsFixes instance.
     */
    public GraphicsFixes() {
        // default
    }

    /**
     * Adds an sRGB output intent to the catalog if no {@code /OutputIntents} array
     * is present yet.
     * <p>
     * An output intent with {@code /S = /GTS_PDFA1} and an embedded sRGB ICC
     * profile satisfies ISO 19005-1:2005, 6.2.2.
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void addOutputIntent(PDFParser parser, PdfFormat format,
                                ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        COSDictionary catalog = parser.getCatalog();
        COSBase existing = catalog.get("OutputIntents");
        if (existing != null) {
            COSBase resolved = parser.resolveReference(existing);
            if (resolved instanceof COSArray && ((COSArray) resolved).size() > 0) {
                LOG.fine("Catalog already has /OutputIntents");
                return;
            }
        }

        LOG.info("Adding sRGB output intent for PDF/A compliance");

        // Build ICC profile stream
        ICC_Profile srgb = ICC_Profile.getInstance(ColorSpace.CS_sRGB);
        byte[] iccData = srgb.getData();
        byte[] compressedIcc = flateCompress(iccData);

        COSStream iccStream = new COSStream();
        iccStream.setDecodedData(iccData);
        iccStream.setFilter(COSName.FLATE_DECODE);
        iccStream.set("N", COSInteger.valueOf(3)); // 3 components for RGB
        iccStream.set(COSName.LENGTH, COSInteger.valueOf(compressedIcc.length));

        // Register ICC stream as indirect object
        int maxObj = findMaxObjectNumber(parser);
        COSObjectKey iccKey = new COSObjectKey(maxObj + 1, 0);
        COSObjectReference iccRef = new COSObjectReference(iccKey, k -> iccStream);

        // Build OutputIntent dictionary
        COSDictionary intent = new COSDictionary();
        intent.set("Type", COSName.of("OutputIntent"));
        intent.set("S", COSName.of("GTS_PDFA1"));
        intent.set("OutputConditionIdentifier", new COSString("sRGB IEC61966-2.1"));
        intent.set("RegistryName", new COSString("http://www.color.org"));
        intent.set("Info", new COSString("sRGB IEC61966-2.1"));
        intent.set("DestOutputProfile", iccRef);

        // Register intent as indirect object
        COSObjectKey intentKey = new COSObjectKey(maxObj + 2, 0);
        COSObjectReference intentRef = new COSObjectReference(intentKey, k -> intent);

        COSArray intents = new COSArray(1);
        intents.add(intentRef);
        catalog.set("OutputIntents", intents);

        result.addWarning("gfx.1", "Added sRGB output intent",
                "catalog/OutputIntents", "ISO 19005-1:2005, 6.2.2");
    }

    /**
     * Removes {@code /Alternates} entries from all image XObject dictionaries.
     * <p>
     * PDF/A forbids alternate image representations (ISO 19005-1:2005, 6.2.4).
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void removeImageAlternates(PDFParser parser, PdfFormat format,
                                      ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        for (COSObjectKey key : parser.getAllObjectKeys()) {
            COSBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                continue;
            }
            if (!(obj instanceof COSStream)) {
                continue;
            }
            COSStream stream = (COSStream) obj;
            if (!"XObject".equals(stream.getNameAsString("Type"))
                    && !"Image".equals(stream.getNameAsString("Subtype"))) {
                // Check Subtype alone — many images lack /Type
                if (!"Image".equals(stream.getNameAsString("Subtype"))) {
                    continue;
                }
            }
            if (stream.get("Alternates") != null) {
                stream.set("Alternates", null);
                result.addWarning("gfx.2", "Removed /Alternates from image XObject",
                        "obj " + key.getObjectNumber(), "ISO 19005-1:2005, 6.2.4");
            }
        }
    }

    /**
     * Removes {@code /OPI} dictionaries from all image and form XObjects.
     * <p>
     * PDF/A forbids OPI (Open Pre-press Interface) references (ISO 19005-1:2005, 6.2.4).
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void removeOPI(PDFParser parser, PdfFormat format,
                          ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        for (COSObjectKey key : parser.getAllObjectKeys()) {
            COSBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                continue;
            }
            if (!(obj instanceof COSStream)) {
                continue;
            }
            COSStream stream = (COSStream) obj;
            String subtype = stream.getNameAsString("Subtype");
            if (!"Image".equals(subtype) && !"Form".equals(subtype)) {
                continue;
            }
            if (stream.get("OPI") != null) {
                stream.set("OPI", null);
                result.addWarning("gfx.3", "Removed /OPI from XObject",
                        "obj " + key.getObjectNumber(), "ISO 19005-1:2005, 6.2.4");
            }
        }
    }

    /**
     * Sets {@code /Interpolate} to {@code false} on all image XObjects.
     * <p>
     * PDF/A-1 requires /Interpolate to be false (ISO 19005-1:2005, 6.2.4).
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void fixInterpolate(PDFParser parser, PdfFormat format,
                               ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        for (COSObjectKey key : parser.getAllObjectKeys()) {
            COSBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                continue;
            }
            if (!(obj instanceof COSStream)) {
                continue;
            }
            COSStream stream = (COSStream) obj;
            if (!"Image".equals(stream.getNameAsString("Subtype"))) {
                continue;
            }
            COSBase interpVal = stream.get("Interpolate");
            if (interpVal instanceof COSBoolean && ((COSBoolean) interpVal).getValue()) {
                stream.set("Interpolate", COSBoolean.FALSE);
                result.addWarning("gfx.4", "Set /Interpolate to false on image",
                        "obj " + key.getObjectNumber(), "ISO 19005-1:2005, 6.2.4");
            }
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

    /**
     * Compresses data using Flate.
     */
    private static byte[] flateCompress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
        try (DeflaterOutputStream dos = new DeflaterOutputStream(baos)) {
            dos.write(data);
        }
        return baos.toByteArray();
    }
}
