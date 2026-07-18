package org.aspose.pdf.engine.filter;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;

import java.io.IOException;

/// Interface for PDF stream filters (§7.4, ISO 32000-1:2008).
///
/// Each filter provides decode (decompress) and encode (compress) operations.
/// The `params` dictionary carries filter-specific parameters such as
/// `/Predictor`, `/Columns`, etc.
///
public interface PdfFilter {

    /// Decodes (decompresses) the given data.
    ///
    /// @param encoded the encoded bytes
    /// @param params  the decode parameters dictionary, or `null` if none
    /// @return the decoded bytes
    /// @throws IOException if decoding fails
    byte[] decode(byte[] encoded, PdfDictionary params) throws IOException;

    /// Encodes (compresses) the given data.
    ///
    /// @param decoded the raw bytes
    /// @param params  the encode parameters dictionary, or `null` if none
    /// @return the encoded bytes
    /// @throws IOException if encoding fails
    byte[] encode(byte[] decoded, PdfDictionary params) throws IOException;

    /// Returns the canonical PDF name for this filter (e.g., `/FlateDecode`).
    ///
    /// @return the filter name as a [PdfName]
    PdfName getName();
}
