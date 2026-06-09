package org.aspose.pdf.engine.pdfa.fixes;

import org.aspose.pdf.ConvertErrorAction;
import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectKey;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.aspose.pdf.engine.pdfobjects.PdfString;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;

/**
 * File-structure fixes for PDF/A and PDF/X compliance.
 * <p>
 * Addresses requirements related to encryption removal, LZW-to-Flate filter
 * replacement, external stream reference removal, embedded-file removal (PDF/A-1),
 * optional-content removal (PDF/A-1), and trailer /ID generation.
 * </p>
 */
public final class FileStructureFixes {

    private static final Logger LOG = Logger.getLogger(FileStructureFixes.class.getName());

    /**
     * Creates a new FileStructureFixes instance.
     */
    public FileStructureFixes() {
        // default
    }

    /**
     * Removes the {@code /Encrypt} dictionary from the trailer.
     * <p>
     * PDF/A forbids any form of encryption (ISO 19005-1:2005, 6.1.3).
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void removeEncryption(PDFParser parser, PdfFormat format,
                                 ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        PdfDictionary trailer = parser.getTrailer();
        PdfBase encrypt = trailer.get("Encrypt");
        if (encrypt != null) {
            LOG.info("Removing /Encrypt from trailer for PDF/A compliance");
            trailer.set("Encrypt", null);
            result.addWarning("struct.1", "Removed /Encrypt from trailer",
                    "trailer/Encrypt", "ISO 19005-1:2005, 6.1.3");
        }
    }

    /**
     * Replaces all LZWDecode filters with FlateDecode across every stream object.
     * <p>
     * PDF/A forbids LZW compression (ISO 19005-1:2005, 6.1.10).
     * Each affected stream is decoded, then re-encoded with Flate.
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void replaceLzwWithFlate(PDFParser parser, PdfFormat format,
                                    ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        for (PdfObjectKey key : parser.getAllObjectKeys()) {
            PdfBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                LOG.fine(() -> "Could not load object " + key + ": " + e.getMessage());
                continue;
            }
            if (!(obj instanceof PdfStream)) {
                continue;
            }
            PdfStream stream = (PdfStream) obj;
            boolean hasLzw = false;
            for (PdfName filter : stream.getFilters()) {
                if ("LZWDecode".equals(filter.getName())) {
                    hasLzw = true;
                    break;
                }
            }
            if (!hasLzw) {
                continue;
            }

            LOG.fine(() -> "Replacing LZWDecode with FlateDecode in object " + key);

            try {
                byte[] decoded = stream.getDecodedData();
                // Re-encode with Flate
                byte[] compressed = flateCompress(decoded);
                stream.setDecodedData(decoded);
                stream.setFilter(PdfName.FLATE_DECODE);
                // Remove old DecodeParms that were LZW-specific
                stream.set("DecodeParms", null);
                result.addWarning("struct.2", "Replaced LZWDecode with FlateDecode",
                        "obj " + key.getObjectNumber(), "ISO 19005-1:2005, 6.1.10");
            } catch (IOException e) {
                result.addError("struct.2", "Failed to replace LZW in object " + key + ": " + e.getMessage(),
                        "obj " + key.getObjectNumber(), "ISO 19005-1:2005, 6.1.10");
            }
        }
    }

    /**
     * Removes external stream references ({@code /F}, {@code /FFilter}, {@code /FDecodeParms})
     * from all stream dictionaries.
     * <p>
     * PDF/A requires all data to be embedded (ISO 19005-1:2005, 6.1.7).
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void removeExternalStreamRefs(PDFParser parser, PdfFormat format,
                                         ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        for (PdfObjectKey key : parser.getAllObjectKeys()) {
            PdfBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                continue;
            }
            if (!(obj instanceof PdfStream)) {
                continue;
            }
            PdfStream stream = (PdfStream) obj;
            boolean changed = false;
            if (stream.get("F") != null && !"Font".equals(stream.getNameAsString("Type"))) {
                // /F in a stream context means external file — but check it's not /Font
                // Actually /F can be a file spec; remove it
                stream.set("F", null);
                changed = true;
            }
            if (stream.get("FFilter") != null) {
                stream.set("FFilter", null);
                changed = true;
            }
            if (stream.get("FDecodeParms") != null) {
                stream.set("FDecodeParms", null);
                changed = true;
            }
            if (changed) {
                result.addWarning("struct.3", "Removed external stream references",
                        "obj " + key.getObjectNumber(), "ISO 19005-1:2005, 6.1.7");
            }
        }
    }

    /**
     * Removes {@code /EmbeddedFiles} from the catalog's {@code /Names} dictionary.
     * <p>
     * PDF/A-1 forbids embedded files (ISO 19005-1:2005, 6.2.5). PDF/A-2 and later
     * allow them under certain conditions, so this method should only be called for
     * PDF/A-1 targets.
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void removeEmbeddedFiles(PDFParser parser, PdfFormat format,
                                    ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        PdfDictionary catalog = parser.getCatalog();
        PdfBase namesRef = catalog.get("Names");
        if (namesRef == null) {
            return;
        }
        PdfBase namesObj = parser.resolveReference(namesRef);
        if (!(namesObj instanceof PdfDictionary)) {
            return;
        }
        PdfDictionary names = (PdfDictionary) namesObj;
        if (names.get("EmbeddedFiles") != null) {
            LOG.info("Removing /EmbeddedFiles from /Names for PDF/A-1 compliance");
            names.set("EmbeddedFiles", null);
            result.addWarning("struct.4", "Removed /EmbeddedFiles from Names dictionary",
                    "catalog/Names/EmbeddedFiles", "ISO 19005-1:2005, 6.2.5");
        }
    }

    /**
     * Removes {@code /OCProperties} from the catalog.
     * <p>
     * PDF/A-1 forbids optional content (layers) (ISO 19005-1:2005, 6.1.13).
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void removeOCProperties(PDFParser parser, PdfFormat format,
                                   ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        PdfDictionary catalog = parser.getCatalog();
        if (catalog.get("OCProperties") != null) {
            LOG.info("Removing /OCProperties from catalog for PDF/A-1 compliance");
            catalog.set("OCProperties", null);
            result.addWarning("struct.5", "Removed /OCProperties (optional content) from catalog",
                    "catalog/OCProperties", "ISO 19005-1:2005, 6.1.13");
        }
    }

    /**
     * Ensures the trailer contains an {@code /ID} array with two 16-byte random
     * hex strings.
     * <p>
     * PDF/A requires a file identifier (ISO 19005-1:2005, 6.1.4).
     * </p>
     *
     * @param parser      the parsed PDF
     * @param format      the target format
     * @param errorAction the error action strategy
     * @param result      the validation result
     * @throws IOException if an I/O error occurs
     */
    public void ensureTrailerId(PDFParser parser, PdfFormat format,
                                ConvertErrorAction errorAction, PdfAValidationResult result) throws IOException {
        PdfDictionary trailer = parser.getTrailer();
        PdfBase id = trailer.get("ID");
        if (id instanceof PdfArray && ((PdfArray) id).size() >= 2) {
            LOG.fine("Trailer already has /ID array");
            return;
        }

        LOG.info("Generating /ID array for trailer");
        SecureRandom rng = new SecureRandom();
        byte[] id1 = new byte[16];
        byte[] id2 = new byte[16];
        rng.nextBytes(id1);
        rng.nextBytes(id2);

        PdfArray idArray = new PdfArray(2);
        idArray.add(new PdfString(id1));
        idArray.add(new PdfString(id2));
        trailer.set("ID", idArray);

        result.addWarning("struct.6", "Added /ID array to trailer",
                "trailer/ID", "ISO 19005-1:2005, 6.1.4");
    }

    /**
     * Compresses data using the Flate algorithm.
     *
     * @param data the uncompressed data
     * @return the compressed data
     * @throws IOException if compression fails
     */
    static byte[] flateCompress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
        try (DeflaterOutputStream dos = new DeflaterOutputStream(baos)) {
            dos.write(data);
        }
        return baos.toByteArray();
    }
}
