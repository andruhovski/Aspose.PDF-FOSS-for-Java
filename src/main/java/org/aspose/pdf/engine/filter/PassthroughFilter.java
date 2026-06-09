package org.aspose.pdf.engine.filter;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * No-op filter: returns data unchanged.
 * Placeholder for image filters (DCTDecode, JBIG2Decode, JPXDecode, CCITTFaxDecode)
 * that require specialized decoders not yet implemented (Stage 3).
 *
 * <p>Per ISO 32000-1:2008:</p>
 * <ul>
 *   <li>§7.4.8 DCTDecode — JPEG baseline/progressive</li>
 *   <li>§7.4.9 JPXDecode — JPEG2000</li>
 *   <li>§7.4.7 JBIG2Decode — monochrome images</li>
 *   <li>§7.4.6 CCITTFaxDecode — CCITT Group 3/4 fax</li>
 * </ul>
 */
public class PassthroughFilter implements PdfFilter {

    private static final Logger LOG = Logger.getLogger(PassthroughFilter.class.getName());
    private final String filterName;

    /**
     * Creates a passthrough filter with the given canonical name.
     *
     * @param filterName the PDF filter name (e.g. "DCTDecode")
     */
    public PassthroughFilter(String filterName) {
        this.filterName = filterName;
    }

    @Override
    public byte[] decode(byte[] encoded, PdfDictionary params) throws IOException {
        LOG.fine(() -> filterName + " passthrough decode: " + encoded.length + " bytes (real decoding in Stage 3)");
        return encoded;
    }

    @Override
    public byte[] encode(byte[] decoded, PdfDictionary params) throws IOException {
        LOG.fine(() -> filterName + " passthrough encode: " + decoded.length + " bytes");
        return decoded;
    }

    @Override
    public PdfName getName() {
        return PdfName.of(filterName);
    }
}
