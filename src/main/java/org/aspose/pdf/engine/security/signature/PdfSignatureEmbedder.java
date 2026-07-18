package org.aspose.pdf.engine.security.signature;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/// Handles byte range computation and PKCS#7 signature embedding in PDF files
/// (ISO 32000-1:2008, §12.8.1).
///
/// The /ByteRange array specifies which bytes of the PDF are covered by the
/// signature. It excludes the /Contents hex value so that the signature can
/// be embedded after the digest is computed.
///
final class PdfSignatureEmbedder {

    private static final Logger LOG = Logger.getLogger(PdfSignatureEmbedder.class.getName());

    private PdfSignatureEmbedder() {}

    /// Finds the start offset of the /Contents hex string value in the PDF bytes.
    /// Searches for the pattern `/Contents <` and returns the offset of the
    /// first hex digit after the opening angle bracket.
    ///
    /// @param pdfBytes the serialized PDF
    /// @return the offset, or -1 if not found
    static int findContentsHexStart(byte[] pdfBytes) {
        byte[] pattern = "/Contents <".getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < pdfBytes.length - pattern.length; i++) {
            boolean match = true;
            for (int j = 0; j < pattern.length; j++) {
                if (pdfBytes[i + j] != pattern[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return i + pattern.length;
            }
        }
        return -1;
    }

    /// Finds the end of the /Contents hex string (the closing angle bracket).
    ///
    /// @param pdfBytes the serialized PDF
    /// @param hexStart the start offset of hex digits
    /// @return the offset of '>', or -1 if not found
    static int findContentsHexEnd(byte[] pdfBytes, int hexStart) {
        for (int i = hexStart; i < pdfBytes.length; i++) {
            if (pdfBytes[i] == '>') return i;
        }
        return -1;
    }

    /// Computes the byte range from the saved PDF.
    ///
    /// The byte range consists of two intervals: [0, before\_contents) and
    /// (after\_contents, EOF]. The /Contents hex value (including angle brackets)
    /// is excluded from the signed data.
    ///
    /// @param pdfBytes        the serialized PDF
    /// @param contentsHexStart offset of first hex digit in /Contents
    /// @param contentsHexEnd   offset of closing '>' in /Contents
    /// @return int[4]: {offset1, length1, offset2, length2}
    static int[] computeByteRange(byte[] pdfBytes, int contentsHexStart, int contentsHexEnd) {
        // The '<' is at contentsHexStart - 1, so the signed region before is [0, contentsHexStart - 1)
        int beforeEnd = contentsHexStart - 1; // offset of '<'
        int afterStart = contentsHexEnd + 1;  // byte after '>'
        int afterLen = pdfBytes.length - afterStart;
        if (afterLen < 0) afterLen = 0;

        return new int[]{0, beforeEnd, afterStart, afterLen};
    }

    /// Extracts the signed bytes from a PDF based on the byte range.
    /// Concatenates the two signed regions.
    ///
    /// @param pdfBytes  the serialized PDF
    /// @param byteRange the 4-element byte range array
    /// @return the concatenated signed bytes
    static byte[] extractSignedBytes(byte[] pdfBytes, int[] byteRange) {
        int totalLen = byteRange[1] + byteRange[3];
        byte[] result = new byte[totalLen];
        System.arraycopy(pdfBytes, byteRange[0], result, 0, byteRange[1]);
        if (byteRange[3] > 0) {
            System.arraycopy(pdfBytes, byteRange[2], result, byteRange[1], byteRange[3]);
        }
        return result;
    }

    /// Embeds a PKCS#7 signature into the /Contents hex placeholder.
    /// The signature bytes are hex-encoded and zero-padded to fill the placeholder.
    ///
    /// @param pdfBytes         the mutable PDF byte array
    /// @param contentsHexStart offset of first hex digit
    /// @param hexSize          total hex characters available
    /// @param pkcs7Bytes       the DER-encoded PKCS#7 signature
    /// @throws IOException if the signature is too large for the placeholder
    static void embedSignature(byte[] pdfBytes, int contentsHexStart, int hexSize,
                               byte[] pkcs7Bytes) throws IOException {
        if (pkcs7Bytes.length * 2 > hexSize) {
            throw new IOException("PKCS#7 signature too large: " + pkcs7Bytes.length
                    + " bytes > " + (hexSize / 2) + " available");
        }
        StringBuilder hex = new StringBuilder(hexSize);
        for (byte b : pkcs7Bytes) {
            hex.append(String.format("%02X", b & 0xFF));
        }
        while (hex.length() < hexSize) {
            hex.append('0');
        }
        byte[] hexBytes = hex.toString().getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(hexBytes, 0, pdfBytes, contentsHexStart, hexSize);
    }

    /// Updates the /ByteRange array in the PDF bytes with actual values.
    /// Searches for the placeholder `/ByteRange [0 0 0 0]` pattern
    /// and replaces it with the actual values.
    ///
    /// @param pdfBytes  the mutable PDF byte array
    /// @param byteRange the 4-element byte range array
    static void updateByteRange(byte[] pdfBytes, int[] byteRange) {
        // Find the /ByteRange array in the PDF
        byte[] brPattern = "/ByteRange [".getBytes(StandardCharsets.US_ASCII);
        int pos = findPattern(pdfBytes, brPattern);
        if (pos < 0) {
            LOG.fine("ByteRange not found — skipping update");
            return;
        }

        // Find the closing ']'
        int endBracket = -1;
        for (int i = pos + brPattern.length; i < pdfBytes.length; i++) {
            if (pdfBytes[i] == ']') {
                endBracket = i;
                break;
            }
        }
        if (endBracket < 0) return;

        int placeholderLen = endBracket - pos + 1; // includes /ByteRange [....]

        // Build the new ByteRange string, padded to same length
        String newBR = String.format("/ByteRange [%d %d %d %d]",
                byteRange[0], byteRange[1], byteRange[2], byteRange[3]);
        byte[] newBRBytes = newBR.getBytes(StandardCharsets.US_ASCII);

        // Pad with spaces to maintain file offsets
        byte[] padded = new byte[placeholderLen];
        java.util.Arrays.fill(padded, (byte) ' ');
        System.arraycopy(newBRBytes, 0, padded, 0, Math.min(newBRBytes.length, placeholderLen));
        System.arraycopy(padded, 0, pdfBytes, pos, placeholderLen);
    }

    private static int findPattern(byte[] data, byte[] pattern) {
        for (int i = 0; i <= data.length - pattern.length; i++) {
            boolean match = true;
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }
}
