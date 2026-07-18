package org.aspose.pdf.engine.filter;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/// ASCIIHexDecode filter (§7.4.2, ISO 32000-1:2008).
///
/// Decodes hexadecimal-encoded data. Each pair of hex digits represents one byte.
/// Whitespace is ignored. The end-of-data marker is `>`. If the final digit
/// is odd, it is treated as if followed by `0`.
///
public final class ASCIIHexFilter implements PdfFilter {

    private static final Logger LOG = Logger.getLogger(ASCIIHexFilter.class.getName());

    private static final char[] HEX_UPPER = "0123456789ABCDEF".toCharArray();

    /// Creates an ASCIIHexFilter instance.
    public ASCIIHexFilter() {
        // Stateless
    }

    /// {@inheritDoc}
    @Override
    public byte[] decode(byte[] encoded, PdfDictionary params) throws IOException {
        if (encoded == null || encoded.length == 0) {
            return new byte[0];
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(encoded.length / 2);
        int highNibble = -1;

        for (byte b : encoded) {
            char ch = (char) (b & 0xFF);

            // EOD marker
            if (ch == '>') {
                break;
            }

            // Skip whitespace (0x00, 0x09, 0x0A, 0x0C, 0x0D, 0x20)
            if (ch == 0x00 || ch == 0x09 || ch == 0x0A || ch == 0x0C || ch == 0x0D || ch == 0x20) {
                continue;
            }

            int digit = hexDigit(ch);
            if (digit == -1) {
                throw new IOException("Invalid hex character in ASCIIHexDecode: '" + ch + "' (0x" +
                        Integer.toHexString(ch) + ")");
            }

            if (highNibble == -1) {
                highNibble = digit;
            } else {
                out.write((highNibble << 4) | digit);
                highNibble = -1;
            }
        }

        // Odd final digit: pad with 0 in low nibble
        if (highNibble != -1) {
            out.write(highNibble << 4);
        }

        LOG.fine(() -> "ASCIIHexDecode: " + encoded.length + " bytes → " + out.size() + " bytes");
        return out.toByteArray();
    }

    /// {@inheritDoc}
    @Override
    public byte[] encode(byte[] decoded, PdfDictionary params) throws IOException {
        if (decoded == null || decoded.length == 0) {
            return new byte[]{'>'}; // EOD marker only
        }

        // 2 hex chars per byte + EOD marker
        byte[] result = new byte[decoded.length * 2 + 1];
        int pos = 0;
        for (byte b : decoded) {
            int unsigned = b & 0xFF;
            result[pos++] = (byte) HEX_UPPER[unsigned >> 4];
            result[pos++] = (byte) HEX_UPPER[unsigned & 0x0F];
        }
        result[pos] = '>';

        LOG.fine(() -> "ASCIIHexEncode: " + decoded.length + " bytes → " + result.length + " bytes");
        return result;
    }

    /// {@inheritDoc}
    @Override
    public PdfName getName() {
        return PdfName.ASCII_HEX_DECODE;
    }

    private static int hexDigit(char ch) {
        if (ch >= '0' && ch <= '9') return ch - '0';
        if (ch >= 'A' && ch <= 'F') return ch - 'A' + 10;
        if (ch >= 'a' && ch <= 'f') return ch - 'a' + 10;
        return -1;
    }
}
