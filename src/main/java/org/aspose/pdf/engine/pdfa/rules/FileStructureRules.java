package org.aspose.pdf.engine.pdfa.rules;

import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectKey;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.pdfa.PdfARule;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validates PDF file structure requirements for PDF/A compliance.
 *
 * <p>Checks the following ISO 19005 clauses:</p>
 * <ul>
 *   <li>6.1.3 &mdash; Trailer must have /ID and must NOT have /Encrypt</li>
 *   <li>6.1.7 &mdash; No stream dictionary may have /F, /FFilter, /FDecodeParms</li>
 *   <li>6.1.10 &mdash; No /LZWDecode filter in any stream</li>
 *   <li>6.1.11 &mdash; Embedded files restrictions</li>
 *   <li>6.1.13 &mdash; No /OCProperties in catalog (PDF/A-1 only)</li>
 * </ul>
 */
public final class FileStructureRules implements PdfARule {

    private static final Logger LOG = Logger.getLogger(FileStructureRules.class.getName());

    /**
     * Creates a new file structure rules checker.
     */
    public FileStructureRules() {
        // default constructor
    }

    @Override
    public void validate(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        if (!format.isPdfA() && !format.isPdfX()) {
            return;
        }

        checkTrailer(parser, format, result);
        checkStreams(parser, format, result);
        checkEmbeddedFiles(parser, format, result);
        checkOptionalContent(parser, format, result);
    }

    /**
     * 6.1.3: Trailer must have /ID and must NOT have /Encrypt.
     */
    private void checkTrailer(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        COSDictionary trailer = parser.getTrailer();
        if (trailer == null) {
            result.addError("6.1.3", "No trailer dictionary found", "trailer", "6.1.3");
            return;
        }

        // /ID is required
        COSBase id = trailer.get("ID");
        if (id == null) {
            result.addError("6.1.3", "Trailer must contain /ID array", "trailer", "6.1.3");
        }

        // /Encrypt is forbidden
        COSBase encrypt = trailer.get("Encrypt");
        if (encrypt != null) {
            result.addError("6.1.3", "Trailer must not contain /Encrypt for PDF/A compliance",
                    "trailer", "6.1.3");
        }
    }

    /**
     * 6.1.7: No stream dict may have /F, /FFilter, /FDecodeParms.
     * 6.1.10: No /LZWDecode filter in any stream.
     */
    private void checkStreams(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        Set<COSObjectKey> keys = parser.getAllObjectKeys();
        if (keys == null) {
            return;
        }

        for (COSObjectKey key : keys) {
            COSBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                LOG.log(Level.FINE, "Could not load object {0}: {1}",
                        new Object[]{key, e.getMessage()});
                continue;
            }

            if (!(obj instanceof COSStream)) {
                continue;
            }

            COSStream stream = (COSStream) obj;
            String objPath = "obj(" + key.getObjectNumber() + " " + key.getGenerationNumber() + ")";

            // 6.1.7: External file reference keys forbidden
            if (stream.get("F") != null) {
                result.addError("6.1.7",
                        "Stream dictionary must not contain /F key",
                        objPath, "6.1.7");
            }
            if (stream.get("FFilter") != null) {
                result.addError("6.1.7",
                        "Stream dictionary must not contain /FFilter key",
                        objPath, "6.1.7");
            }
            if (stream.get("FDecodeParms") != null) {
                result.addError("6.1.7",
                        "Stream dictionary must not contain /FDecodeParms key",
                        objPath, "6.1.7");
            }

            // 6.1.10: LZWDecode is forbidden in PDF/A-1 and PDF/A-2+
            checkLzwFilter(stream, objPath, result);
        }
    }

    /**
     * Checks whether a stream uses LZWDecode filter (forbidden by 6.1.10).
     */
    private void checkLzwFilter(COSStream stream, String objPath, PdfAValidationResult result) {
        COSBase filterVal = stream.get("Filter");
        if (filterVal == null) {
            return;
        }
        if (filterVal instanceof COSObjectReference) {
            try {
                filterVal = ((COSObjectReference) filterVal).dereference();
            } catch (IOException e) {
                LOG.log(Level.FINE, "Could not dereference filter: {0}", e.getMessage());
                return;
            }
        }

        if (filterVal instanceof COSName) {
            if ("LZWDecode".equals(((COSName) filterVal).getName())) {
                result.addError("6.1.10",
                        "LZWDecode filter is not permitted in PDF/A",
                        objPath, "6.1.10");
            }
        } else if (filterVal instanceof COSArray) {
            COSArray arr = (COSArray) filterVal;
            for (int i = 0; i < arr.size(); i++) {
                COSBase elem = arr.get(i);
                if (elem instanceof COSObjectReference) {
                    try {
                        elem = ((COSObjectReference) elem).dereference();
                    } catch (IOException e) {
                        continue;
                    }
                }
                if (elem instanceof COSName && "LZWDecode".equals(((COSName) elem).getName())) {
                    result.addError("6.1.10",
                            "LZWDecode filter is not permitted in PDF/A",
                            objPath + "/Filter[" + i + "]", "6.1.10");
                }
            }
        }
    }

    /**
     * 6.1.11: Embedded files restrictions.
     * PDF/A-1: no /EF in file specs, no /EmbeddedFiles in names dict.
     * PDF/A-2: allowed if compliant.
     * PDF/A-3: any embedded files allowed.
     */
    private void checkEmbeddedFiles(PDFParser parser, PdfFormat format,
                                     PdfAValidationResult result) {
        // Only PDF/A-1 forbids embedded files entirely
        if (!format.isPdfA1()) {
            return;
        }

        COSDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not load catalog: {0}", e.getMessage());
            return;
        }

        // Check /Names -> /EmbeddedFiles
        COSBase namesRef = catalog.get("Names");
        if (namesRef != null) {
            COSDictionary names = resolveDict(namesRef);
            if (names != null && names.get("EmbeddedFiles") != null) {
                result.addError("6.1.11",
                        "PDF/A-1 must not have /EmbeddedFiles in the names dictionary",
                        "catalog/Names/EmbeddedFiles", "6.1.11");
            }
        }

        // Check all file specification dicts for /EF
        Set<COSObjectKey> keys = parser.getAllObjectKeys();
        if (keys == null) {
            return;
        }
        for (COSObjectKey key : keys) {
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
            String type = dict.getNameAsString("Type");
            if ("Filespec".equals(type) && dict.get("EF") != null) {
                result.addError("6.1.11",
                        "PDF/A-1 file specification must not contain /EF key",
                        "obj(" + key.getObjectNumber() + " " + key.getGenerationNumber() + ")",
                        "6.1.11");
            }
        }
    }

    /**
     * 6.1.13: No /OCProperties in catalog (PDF/A-1 only).
     */
    private void checkOptionalContent(PDFParser parser, PdfFormat format,
                                       PdfAValidationResult result) {
        if (!format.isPdfA1()) {
            return;
        }

        COSDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not load catalog: {0}", e.getMessage());
            return;
        }

        if (catalog.get("OCProperties") != null) {
            result.addError("6.1.13",
                    "PDF/A-1 must not contain /OCProperties (optional content) in the catalog",
                    "catalog/OCProperties", "6.1.13");
        }
    }

    /**
     * Resolves a COSBase value to a COSDictionary, dereferencing if needed.
     *
     * @param val the value (may be an indirect reference)
     * @return the dictionary, or null
     */
    private static COSDictionary resolveDict(COSBase val) {
        if (val instanceof COSObjectReference) {
            try {
                val = ((COSObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return (val instanceof COSDictionary) ? (COSDictionary) val : null;
    }
}
