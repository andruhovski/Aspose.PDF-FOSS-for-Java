package org.aspose.pdf.engine.pdfa.rules;

import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfa.PdfARule;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.pdfobjects.*;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/// Validates PDF file structure requirements for PDF/A compliance.
///
/// Checks the following ISO 19005 clauses:
///
///   - 6.1.3 — Trailer must have /ID and must NOT have /Encrypt
///   - 6.1.7 — No stream dictionary may have /F, /FFilter, /FDecodeParms
///   - 6.1.10 — No /LZWDecode filter in any stream
///   - 6.1.11 — Embedded files restrictions
///   - 6.1.13 — No /OCProperties in catalog (PDF/A-1 only)
public final class FileStructureRules implements PdfARule {

    private static final Logger LOG = Logger.getLogger(FileStructureRules.class.getName());

    /// Creates a new file structure rules checker.
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

    /// 6.1.3: Trailer must have /ID and must NOT have /Encrypt.
    private void checkTrailer(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        PdfDictionary trailer = parser.getTrailer();
        if (trailer == null) {
            result.addError("6.1.3", "No trailer dictionary found", "trailer", "6.1.3");
            return;
        }

        // /ID is required
        PdfBase id = trailer.get("ID");
        if (id == null) {
            result.addError("6.1.3", "Trailer must contain /ID array", "trailer", "6.1.3");
        }

        // /Encrypt is forbidden
        PdfBase encrypt = trailer.get("Encrypt");
        if (encrypt != null) {
            result.addError("6.1.3", "Trailer must not contain /Encrypt for PDF/A compliance",
                    "trailer", "6.1.3");
        }
    }

    /// 6.1.7: No stream dict may have /F, /FFilter, /FDecodeParms.
    /// 6.1.10: No /LZWDecode filter in any stream.
    private void checkStreams(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        Set<PdfObjectKey> keys = parser.getAllObjectKeys();
        if (keys == null) {
            return;
        }

        for (PdfObjectKey key : keys) {
            PdfBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                LOG.log(Level.FINE, "Could not load object {0}: {1}",
                        new Object[]{key, e.getMessage()});
                continue;
            }

            if (!(obj instanceof PdfStream)) {
                continue;
            }

            PdfStream stream = (PdfStream) obj;
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

    /// Checks whether a stream uses LZWDecode filter (forbidden by 6.1.10).
    private void checkLzwFilter(PdfStream stream, String objPath, PdfAValidationResult result) {
        PdfBase filterVal = stream.get("Filter");
        if (filterVal == null) {
            return;
        }
        if (filterVal instanceof PdfObjectReference) {
            try {
                filterVal = ((PdfObjectReference) filterVal).dereference();
            } catch (IOException e) {
                LOG.log(Level.FINE, "Could not dereference filter: {0}", e.getMessage());
                return;
            }
        }

        if (filterVal instanceof PdfName) {
            if ("LZWDecode".equals(((PdfName) filterVal).getName())) {
                result.addError("6.1.10",
                        "LZWDecode filter is not permitted in PDF/A",
                        objPath, "6.1.10");
            }
        } else if (filterVal instanceof PdfArray) {
            PdfArray arr = (PdfArray) filterVal;
            for (int i = 0; i < arr.size(); i++) {
                PdfBase elem = arr.get(i);
                if (elem instanceof PdfObjectReference) {
                    try {
                        elem = ((PdfObjectReference) elem).dereference();
                    } catch (IOException e) {
                        continue;
                    }
                }
                if (elem instanceof PdfName && "LZWDecode".equals(((PdfName) elem).getName())) {
                    result.addError("6.1.10",
                            "LZWDecode filter is not permitted in PDF/A",
                            objPath + "/Filter[" + i + "]", "6.1.10");
                }
            }
        }
    }

    /// 6.1.11: Embedded files restrictions.
    /// PDF/A-1: no /EF in file specs, no /EmbeddedFiles in names dict.
    /// PDF/A-2: allowed if compliant.
    /// PDF/A-3: any embedded files allowed.
    private void checkEmbeddedFiles(PDFParser parser, PdfFormat format,
                                     PdfAValidationResult result) {
        // Only PDF/A-1 forbids embedded files entirely
        if (!format.isPdfA1()) {
            return;
        }

        PdfDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not load catalog: {0}", e.getMessage());
            return;
        }

        // Check /Names -> /EmbeddedFiles
        PdfBase namesRef = catalog.get("Names");
        if (namesRef != null) {
            PdfDictionary names = resolveDict(namesRef);
            if (names != null && names.get("EmbeddedFiles") != null) {
                result.addError("6.1.11",
                        "PDF/A-1 must not have /EmbeddedFiles in the names dictionary",
                        "catalog/Names/EmbeddedFiles", "6.1.11");
            }
        }

        // Check all file specification dicts for /EF
        Set<PdfObjectKey> keys = parser.getAllObjectKeys();
        if (keys == null) {
            return;
        }
        for (PdfObjectKey key : keys) {
            PdfBase obj;
            try {
                obj = parser.getObject(key);
            } catch (IOException e) {
                continue;
            }
            if (!(obj instanceof PdfDictionary)) {
                continue;
            }
            PdfDictionary dict = (PdfDictionary) obj;
            String type = dict.getNameAsString("Type");
            if ("Filespec".equals(type) && dict.get("EF") != null) {
                result.addError("6.1.11",
                        "PDF/A-1 file specification must not contain /EF key",
                        "obj(" + key.getObjectNumber() + " " + key.getGenerationNumber() + ")",
                        "6.1.11");
            }
        }
    }

    /// 6.1.13: No /OCProperties in catalog (PDF/A-1 only).
    private void checkOptionalContent(PDFParser parser, PdfFormat format,
                                       PdfAValidationResult result) {
        if (!format.isPdfA1()) {
            return;
        }

        PdfDictionary catalog;
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

    /// Resolves a PdfBase value to a PdfDictionary, dereferencing if needed.
    ///
    /// @param val the value (may be an indirect reference)
    /// @return the dictionary, or null
    private static PdfDictionary resolveDict(PdfBase val) {
        if (val instanceof PdfObjectReference) {
            try {
                val = ((PdfObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return (val instanceof PdfDictionary) ? (PdfDictionary) val : null;
    }
}
