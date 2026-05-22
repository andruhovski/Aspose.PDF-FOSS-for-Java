package org.aspose.pdf.engine.pdfa.rules;

import org.aspose.pdf.PdfFormat;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.pdfa.PdfARule;
import org.aspose.pdf.engine.pdfa.PdfAValidationResult;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validates metadata requirements for PDF/A compliance.
 *
 * <p>Checks the following ISO 19005 clauses:</p>
 * <ul>
 *   <li>6.7.2 &mdash; Catalog must have /Metadata pointing to a stream</li>
 *   <li>6.7.2 &mdash; Metadata stream must not have /Filter</li>
 *   <li>6.7.11 &mdash; XMP must contain pdfaid:part and pdfaid:conformance matching target</li>
 * </ul>
 */
public final class MetadataRules implements PdfARule {

    private static final Logger LOG = Logger.getLogger(MetadataRules.class.getName());

    /**
     * Creates a new metadata rules checker.
     */
    public MetadataRules() {
        // default constructor
    }

    @Override
    public void validate(PDFParser parser, PdfFormat format, PdfAValidationResult result) {
        if (!format.isPdfA()) {
            return;
        }

        COSDictionary catalog;
        try {
            catalog = parser.getCatalog();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not load catalog: {0}", e.getMessage());
            result.addError("6.7.2", "Cannot load catalog to check metadata",
                    "catalog", "6.7.2");
            return;
        }

        // 6.7.2: Catalog must have /Metadata
        COSBase metadataRef = catalog.get("Metadata");
        if (metadataRef == null) {
            result.addError("6.7.2",
                    "Catalog must contain /Metadata stream for PDF/A compliance",
                    "catalog", "6.7.2");
            return;
        }

        COSBase metadata = resolve(metadataRef);
        if (!(metadata instanceof COSStream)) {
            result.addError("6.7.2",
                    "Catalog /Metadata must be a stream object",
                    "catalog/Metadata", "6.7.2");
            return;
        }

        COSStream metaStream = (COSStream) metadata;

        // 6.7.2: Metadata stream must not have /Filter
        if (metaStream.get("Filter") != null) {
            result.addError("6.7.2",
                    "Metadata stream must not have /Filter (must be uncompressed)",
                    "catalog/Metadata", "6.7.2");
        }

        // 6.7.11: Check XMP pdfaid:part and pdfaid:conformance
        checkXmpIdentification(metaStream, format, result);
    }

    /**
     * Checks that the XMP metadata contains correct pdfaid:part and pdfaid:conformance.
     *
     * <p>This is a string-based check on the raw XMP data. If an XmpMetadataHandler
     * is available in the future, it should be used for proper XML parsing.</p>
     */
    private void checkXmpIdentification(COSStream metaStream, PdfFormat format,
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

    /**
     * Attempts to extract a simple XMP element value from raw text.
     * Handles both {@code <tag>value</tag>} and {@code tag="value"} forms.
     *
     * @param xmp the XMP text
     * @param tag the tag name to search for (e.g. "pdfaid:part")
     * @return the extracted value, or null if not found
     */
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

    /**
     * Resolves a COSBase value, dereferencing indirect references.
     */
    private static COSBase resolve(COSBase val) {
        if (val instanceof COSObjectReference) {
            try {
                val = ((COSObjectReference) val).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return val;
    }
}
