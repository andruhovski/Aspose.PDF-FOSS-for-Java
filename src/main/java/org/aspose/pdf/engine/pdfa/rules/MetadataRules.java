package org.aspose.pdf.engine.pdfa.rules;

import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfa.PdfARule;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.aspose.pdf.engine.pdfobjects.PdfStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/// Validates metadata requirements for PDF/A compliance.
///
/// Checks the following ISO 19005 clauses:
///
///   - 6.7.2 — Catalog must have /Metadata pointing to a stream
///   - 6.7.2 — Metadata stream must not have /Filter
///   - 6.7.11 — XMP must contain pdfaid:part and pdfaid:conformance matching target
public final class MetadataRules implements PdfARule {

    private static final Logger LOG = Logger.getLogger(MetadataRules.class.getName());

    /// Creates a new metadata rules checker.
    public MetadataRules() {
        // default constructor
    }

    @Override
    public void validate(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        if (!format.isPdfA()) {
            return;
        }

        PdfDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not load catalog: {0}", e.getMessage());
            result.addError("6.7.2", "Cannot load catalog to check metadata",
                    "catalog", "6.7.2");
            return;
        }

        // 6.7.2: Catalog must have /Metadata
        PdfBase metadataRef = catalog.get("Metadata");
        if (metadataRef == null) {
            result.addError("6.7.2",
                    "Catalog must contain /Metadata stream for PDF/A compliance",
                    "catalog", "6.7.2");
            return;
        }

        PdfBase metadata = resolve(metadataRef);
        if (!(metadata instanceof PdfStream)) {
            result.addError("6.7.2",
                    "Catalog /Metadata must be a stream object",
                    "catalog/Metadata", "6.7.2");
            return;
        }

        PdfStream metaStream = (PdfStream) metadata;

        // 6.7.2: Metadata stream must not have /Filter
        if (metaStream.get("Filter") != null) {
            result.addError("6.7.2",
                    "Metadata stream must not have /Filter (must be uncompressed)",
                    "catalog/Metadata", "6.7.2");
        }

        // 6.7.11: Check XMP pdfaid:part and pdfaid:conformance
        checkXmpIdentification(metaStream, format, result);
    }

    /// Checks that the XMP metadata contains correct pdfaid:part and pdfaid:conformance.
    ///
    /// This is a string-based check on the raw XMP data. If an XmpMetadataHandler
    /// is available in the future, it should be used for proper XML parsing.
    private void checkXmpIdentification(PdfStream metaStream, PdfFormat format,
                                         PdfAValidationResult result) {
        byte[] data;
        try {
            data = metaStream.getDecodedData();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not decode metadata stream: {0}", e.getMessage());
            result.addWarning("6.7.11",
                    "Could not decode metadata stream to verify pdfaid identification",
                    "catalog/Metadata", "6.7.11");
            return;
        }

        if (data == null || data.length == 0) {
            result.addError("6.7.11",
                    "Metadata stream is empty",
                    "catalog/Metadata", "6.7.11");
            return;
        }

        String xmpText = new String(data, StandardCharsets.UTF_8);

        // Check for pdfaid:part
        int expectedPart = format.getPart();
        String partTag = "pdfaid:part";
        if (!xmpText.contains(partTag)) {
            result.addError("6.7.11",
                    "XMP metadata must contain pdfaid:part element",
                    "catalog/Metadata", "6.7.11");
        } else {
            // Try to extract the value and check it matches
            String partValue = extractXmpValue(xmpText, partTag);
            if (partValue != null) {
                try {
                    int actualPart = Integer.parseInt(partValue.trim());
                    if (actualPart != expectedPart) {
                        result.addError("6.7.11",
                                "XMP pdfaid:part is " + actualPart + " but expected "
                                        + expectedPart + " for " + format.name(),
                                "catalog/Metadata", "6.7.11");
                    }
                } catch (NumberFormatException e) {
                    result.addWarning("6.7.11",
                            "Could not parse pdfaid:part value: " + partValue,
                            "catalog/Metadata", "6.7.11");
                }
            }
        }

        // Check for pdfaid:conformance
        String confTag = "pdfaid:conformance";
        String expectedConf = format.getConformance();
        if (expectedConf != null) {
            if (!xmpText.contains(confTag)) {
                result.addError("6.7.11",
                        "XMP metadata must contain pdfaid:conformance element",
                        "catalog/Metadata", "6.7.11");
            } else {
                String confValue = extractXmpValue(xmpText, confTag);
                if (confValue != null && !expectedConf.equalsIgnoreCase(confValue.trim())) {
                    result.addError("6.7.11",
                            "XMP pdfaid:conformance is '" + confValue.trim()
                                    + "' but expected '" + expectedConf + "' for " + format.name(),
                            "catalog/Metadata", "6.7.11");
                }
            }
        }
    }

    /// Attempts to extract a simple XMP element value from raw text.
    /// Handles both `<tag>value</tag>` and `tag="value"` forms.
    ///
    /// @param xmp the XMP text
    /// @param tag the tag name to search for (e.g. "pdfaid:part")
    /// @return the extracted value, or null if not found
    private static String extractXmpValue(String xmp, String tag) {
        // Try <tag>value</tag> form
        String openTag = "<" + tag + ">";
        String closeTag = "</" + tag + ">";
        int start = xmp.indexOf(openTag);
        if (start >= 0) {
            int valStart = start + openTag.length();
            int end = xmp.indexOf(closeTag, valStart);
            if (end > valStart) {
                return xmp.substring(valStart, end);
            }
        }

        // Try tag="value" form (attribute)
        String attrPrefix = tag + "=\"";
        int attrStart = xmp.indexOf(attrPrefix);
        if (attrStart >= 0) {
            int valStart = attrStart + attrPrefix.length();
            int end = xmp.indexOf('"', valStart);
            if (end > valStart) {
                return xmp.substring(valStart, end);
            }
        }

        return null;
    }

    /// Resolves a PdfBase value, dereferencing indirect references.
    private static PdfBase resolve(PdfBase val) {
        if (val instanceof PdfObjectReference) {
            try {
                val = ((PdfObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return val;
    }
}
