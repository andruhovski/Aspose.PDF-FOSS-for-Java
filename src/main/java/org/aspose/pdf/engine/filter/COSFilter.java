package org.aspose.pdf.engine.filter;

import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;

import java.io.IOException;

/**
 * Interface for PDF stream filters (§7.4, ISO 32000-1:2008).
 * <p>
 * Each filter provides decode (decompress) and encode (compress) operations.
 * The {@code params} dictionary carries filter-specific parameters such as
 * {@code /Predictor}, {@code /Columns}, etc.
 * </p>
 */
public interface COSFilter {

    /**
     * Decodes (decompresses) the given data.
     *
     * @param encoded the encoded bytes
     * @param params  the decode parameters dictionary, or {@code null} if none
     * @return the decoded bytes
     * @throws IOException if decoding fails
     */
    byte[] decode(byte[] encoded, COSDictionary params) throws IOException;

    /**
     * Encodes (compresses) the given data.
     *
     * @param decoded the raw bytes
     * @param params  the encode parameters dictionary, or {@code null} if none
     * @return the encoded bytes
     * @throws IOException if encoding fails
     */
    byte[] encode(byte[] decoded, COSDictionary params) throws IOException;

    /**
     * Returns the canonical PDF name for this filter (e.g., {@code /FlateDecode}).
     *
     * @return the filter name as a {@link COSName}
     */
    COSName getName();
}
