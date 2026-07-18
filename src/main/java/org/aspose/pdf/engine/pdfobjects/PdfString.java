package org.aspose.pdf.engine.pdfobjects;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// PDF string object (§7.3.4, ISO 32000-1:2008).
///
/// A sequence of bytes representable in literal `(Hello)` or hexadecimal
/// `<48656C6C6F>` form. Supports Unicode via BOM (0xFE 0xFF) + UTF-16BE encoding.
///
public final class PdfString extends PdfBase {

    private static final Logger LOG = Logger.getLogger(PdfString.class.getName());

    private final byte[] bytes;
    private boolean forceHex;

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "D:(\\d{4})(\\d{2})?(\\d{2})?(\\d{2})?(\\d{2})?(\\d{2})?([+\\-Z])?(\\d{2})?'?(\\d{2})?'?"
    );

    // PDFDocEncoding table for bytes 0x80..0xFF (ISO 32000 Table D.2)
    // Characters that differ from Latin-1
    private static final char[] PDF_DOC_ENCODING = new char[256];

    static {
        // 0x00..0x7F: ASCII (with some control char overrides)
        for (int i = 0; i < 128; i++) {
            PDF_DOC_ENCODING[i] = (char) i;
        }
        // Special mappings for control characters used in PDFDocEncoding
        PDF_DOC_ENCODING[0x7F] = '\uFFFD'; // undefined
        PDF_DOC_ENCODING[0x80] = '\u2022'; // BULLET
        PDF_DOC_ENCODING[0x81] = '\u2020'; // DAGGER
        PDF_DOC_ENCODING[0x82] = '\u2021'; // DOUBLE DAGGER
        PDF_DOC_ENCODING[0x83] = '\u2026'; // HORIZONTAL ELLIPSIS
        PDF_DOC_ENCODING[0x84] = '\u2014'; // EM DASH
        PDF_DOC_ENCODING[0x85] = '\u2013'; // EN DASH
        PDF_DOC_ENCODING[0x86] = '\u0192'; // LATIN SMALL LETTER F WITH HOOK
        PDF_DOC_ENCODING[0x87] = '\u2044'; // FRACTION SLASH
        PDF_DOC_ENCODING[0x88] = '\u2039'; // SINGLE LEFT-POINTING ANGLE QUOTATION MARK
        PDF_DOC_ENCODING[0x89] = '\u203A'; // SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
        PDF_DOC_ENCODING[0x8A] = '\u2212'; // MINUS SIGN
        PDF_DOC_ENCODING[0x8B] = '\u2030'; // PER MILLE SIGN
        PDF_DOC_ENCODING[0x8C] = '\u201E'; // DOUBLE LOW-9 QUOTATION MARK
        PDF_DOC_ENCODING[0x8D] = '\u201C'; // LEFT DOUBLE QUOTATION MARK
        PDF_DOC_ENCODING[0x8E] = '\u201D'; // RIGHT DOUBLE QUOTATION MARK
        PDF_DOC_ENCODING[0x8F] = '\u2018'; // LEFT SINGLE QUOTATION MARK
        PDF_DOC_ENCODING[0x90] = '\u2019'; // RIGHT SINGLE QUOTATION MARK
        PDF_DOC_ENCODING[0x91] = '\u201A'; // SINGLE LOW-9 QUOTATION MARK
        PDF_DOC_ENCODING[0x92] = '\u2122'; // TRADE MARK SIGN
        PDF_DOC_ENCODING[0x93] = '\uFB01'; // LATIN SMALL LIGATURE FI
        PDF_DOC_ENCODING[0x94] = '\uFB02'; // LATIN SMALL LIGATURE FL
        PDF_DOC_ENCODING[0x95] = '\u0141'; // LATIN CAPITAL LETTER L WITH STROKE
        PDF_DOC_ENCODING[0x96] = '\u0152'; // LATIN CAPITAL LIGATURE OE
        PDF_DOC_ENCODING[0x97] = '\u0160'; // LATIN CAPITAL LETTER S WITH CARON
        PDF_DOC_ENCODING[0x98] = '\u0178'; // LATIN CAPITAL LETTER Y WITH DIAERESIS
        PDF_DOC_ENCODING[0x99] = '\u017D'; // LATIN CAPITAL LETTER Z WITH CARON
        PDF_DOC_ENCODING[0x9A] = '\u0131'; // LATIN SMALL LETTER DOTLESS I
        PDF_DOC_ENCODING[0x9B] = '\u0142'; // LATIN SMALL LETTER L WITH STROKE
        PDF_DOC_ENCODING[0x9C] = '\u0153'; // LATIN SMALL LIGATURE OE
        PDF_DOC_ENCODING[0x9D] = '\u0161'; // LATIN SMALL LETTER S WITH CARON
        PDF_DOC_ENCODING[0x9E] = '\u017E'; // LATIN SMALL LETTER Z WITH CARON
        PDF_DOC_ENCODING[0x9F] = '\uFFFD'; // undefined
        PDF_DOC_ENCODING[0xA0] = '\u20AC'; // EURO SIGN
        // 0xA1..0xFF: same as Latin-1
        for (int i = 0xA1; i <= 0xFF; i++) {
            PDF_DOC_ENCODING[i] = (char) i;
        }
        // Fix: some undefined control chars at low range
        PDF_DOC_ENCODING[0x18] = '\u02D8'; // BREVE
        PDF_DOC_ENCODING[0x19] = '\u02C7'; // CARON
        PDF_DOC_ENCODING[0x1A] = '\u02C6'; // MODIFIER LETTER CIRCUMFLEX ACCENT
        PDF_DOC_ENCODING[0x1B] = '\u02D9'; // DOT ABOVE
        PDF_DOC_ENCODING[0x1C] = '\u02DD'; // DOUBLE ACUTE ACCENT
        PDF_DOC_ENCODING[0x1D] = '\u02DB'; // OGONEK
        PDF_DOC_ENCODING[0x1E] = '\u02DA'; // RING ABOVE
        PDF_DOC_ENCODING[0x1F] = '\u02DC'; // SMALL TILDE
    }

    /// Creates a PdfString from raw bytes.
    ///
    /// @param bytes the raw PDF string bytes
    /// @throws IllegalArgumentException if bytes is null
    public PdfString(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Bytes must not be null");
        }
        this.bytes = bytes.clone();
    }

    /// Creates a PdfString from a Java string.
    /// Uses PDFDocEncoding if possible, otherwise UTF-16BE with BOM.
    ///
    /// @param text the string value
    /// @throws IllegalArgumentException if text is null
    public PdfString(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text must not be null");
        }
        if (text.isEmpty()) {
            this.bytes = new byte[0];
            return;
        }
        // Try PDFDocEncoding first
        byte[] pdfDoc = tryEncodePdfDoc(text);
        if (pdfDoc != null) {
            this.bytes = pdfDoc;
        } else {
            // Fall back to UTF-16BE with BOM
            byte[] utf16 = text.getBytes(StandardCharsets.UTF_16BE);
            this.bytes = new byte[utf16.length + 2];
            this.bytes[0] = (byte) 0xFE;
            this.bytes[1] = (byte) 0xFF;
            System.arraycopy(utf16, 0, this.bytes, 2, utf16.length);
        }
    }

    /// Creates a PdfString from a hex string (without angle brackets).
    ///
    /// @param hex the hex string (e.g. "48656C6C6F")
    /// @return the PdfString
    /// @throws IllegalArgumentException if hex is null
    public static PdfString fromHex(String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("Hex string must not be null");
        }
        // Remove whitespace
        String cleaned = hex.replaceAll("\\s", "");
        // Pad odd-length with trailing 0
        if (cleaned.length() % 2 != 0) {
            cleaned = cleaned + "0";
        }
        byte[] result = new byte[cleaned.length() / 2];
        for (int i = 0; i < result.length; i++) {
            int hi = Character.digit(cleaned.charAt(i * 2), 16);
            int lo = Character.digit(cleaned.charAt(i * 2 + 1), 16);
            if (hi < 0 || lo < 0) {
                throw new IllegalArgumentException("Invalid hex character at position " + (i * 2));
            }
            result[i] = (byte) ((hi << 4) | lo);
        }
        PdfString s = new PdfString(result);
        s.forceHex = true;
        return s;
    }

    /// Returns the raw bytes of this string.
    ///
    /// @return a copy of the raw bytes
    public byte[] getBytes() {
        return bytes.clone();
    }

    /// Decodes the bytes as text: if BOM present, UTF-16BE; otherwise PDFDocEncoding.
    ///
    /// @return the decoded string
    public String getString() {
        if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFE && (bytes[1] & 0xFF) == 0xFF) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
        }
        // PDFDocEncoding
        StringBuilder sb = new StringBuilder(bytes.length);
        for (byte b : bytes) {
            sb.append(PDF_DOC_ENCODING[b & 0xFF]);
        }
        return sb.toString();
    }

    /// Sets whether this string should always be serialized in hex form.
    ///
    /// @param forceHex true to force hex serialization
    public void setForceHex(boolean forceHex) {
        this.forceHex = forceHex;
    }

    /// Returns whether this string forces hex serialization.
    ///
    /// @return true if hex is forced
    public boolean isForceHex() {
        return forceHex;
    }

    /// Attempts to parse the string as a PDF date.
    /// Format: `D:YYYYMMDDHHmmSS+HH'mm'`.
    ///
    /// @return the parsed date, or null if not a valid PDF date
    public LocalDateTime getAsDate() {
        String text = getString();
        Matcher m = DATE_PATTERN.matcher(text);
        if (!m.matches()) {
            return null;
        }
        try {
            int year = Integer.parseInt(m.group(1));
            int month = m.group(2) != null ? Integer.parseInt(m.group(2)) : 1;
            int day = m.group(3) != null ? Integer.parseInt(m.group(3)) : 1;
            int hour = m.group(4) != null ? Integer.parseInt(m.group(4)) : 0;
            int minute = m.group(5) != null ? Integer.parseInt(m.group(5)) : 0;
            int second = m.group(6) != null ? Integer.parseInt(m.group(6)) : 0;
            return LocalDateTime.of(year, month, day, hour, minute, second);
        } catch (Exception e) {
            LOG.fine(() -> "Failed to parse PDF date: " + text + " - " + e.getMessage());
            return null;
        }
    }

    /// Returns the string in hex representation: <AABBCC...>
    /// Used for binary strings (IDs, encryption keys).
    ///
    /// @return hex representation with angle brackets
    public String getHexString() {
        StringBuilder sb = new StringBuilder(bytes.length * 2 + 2);
        sb.append('<');
        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        sb.append('>');
        return sb.toString();
    }

    /// Returns true if this string contains non-printable bytes
    /// (likely binary data like file ID or encryption key).
    ///
    /// @return true if any byte is outside printable ASCII range (0x20..0x7E)
    public boolean isBinary() {
        for (byte b : bytes) {
            int v = b & 0xFF;
            if (v < 0x20 || v > 0x7E) return true;
        }
        return false;
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        if (forceHex || hasManyNonPrintable()) {
            writeHex(os);
        } else {
            writeLiteral(os);
        }
    }

    @Override
    public <T> T accept(IPdfVisitor<T> visitor) {
        return visitor.visitString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PdfString)) return false;
        return Arrays.equals(bytes, ((PdfString) o).bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return "PdfString{" + getString() + "}";
    }

    private void writeLiteral(OutputStream os) throws IOException {
        os.write('(');
        for (byte b : bytes) {
            int unsigned = b & 0xFF;
            switch (unsigned) {
                case '\n':
                    os.write('\\');
                    os.write('n');
                    break;
                case '\r':
                    os.write('\\');
                    os.write('r');
                    break;
                case '\t':
                    os.write('\\');
                    os.write('t');
                    break;
                case '\b':
                    os.write('\\');
                    os.write('b');
                    break;
                case '\f':
                    os.write('\\');
                    os.write('f');
                    break;
                case '\\':
                    os.write('\\');
                    os.write('\\');
                    break;
                case '(':
                    os.write('\\');
                    os.write('(');
                    break;
                case ')':
                    os.write('\\');
                    os.write(')');
                    break;
                default:
                    if (unsigned < 0x20 && unsigned != '\n' && unsigned != '\r'
                            && unsigned != '\t' && unsigned != '\b' && unsigned != '\f') {
                        // Octal escape for control characters
                        os.write('\\');
                        os.write('0' + ((unsigned >> 6) & 7));
                        os.write('0' + ((unsigned >> 3) & 7));
                        os.write('0' + (unsigned & 7));
                    } else {
                        os.write(unsigned);
                    }
                    break;
            }
        }
        os.write(')');
    }

    private void writeHex(OutputStream os) throws IOException {
        os.write('<');
        for (byte b : bytes) {
            int unsigned = b & 0xFF;
            os.write(hexChar((unsigned >> 4) & 0x0F));
            os.write(hexChar(unsigned & 0x0F));
        }
        os.write('>');
    }

    private static int hexChar(int nibble) {
        return nibble < 10 ? '0' + nibble : 'A' + nibble - 10;
    }

    private boolean hasManyNonPrintable() {
        if (bytes.length == 0) return false;
        int nonPrintable = 0;
        for (byte b : bytes) {
            int u = b & 0xFF;
            // Exclude common whitespace that has named escapes
            if (u < 0x20 && u != '\n' && u != '\r' && u != '\t' && u != '\b' && u != '\f') {
                nonPrintable++;
            } else if (u > 0x7E) {
                nonPrintable++;
            }
        }
        // Switch to hex only if more than half the bytes are non-printable
        return nonPrintable > bytes.length / 2;
    }

    /// Tries to encode the string in PDFDocEncoding. Returns null if not possible.
    private static byte[] tryEncodePdfDoc(String text) {
        byte[] result = new byte[text.length()];
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int encoded = findPdfDocByte(c);
            if (encoded < 0) {
                return null; // Character not in PDFDocEncoding
            }
            result[i] = (byte) encoded;
        }
        return result;
    }

    /// Finds the PDFDocEncoding byte for a Unicode character, or -1 if not encodable.
    private static int findPdfDocByte(char c) {
        // Fast path for ASCII
        if (c < 0x80) {
            return c;
        }
        // Search the mapping table
        for (int i = 0x80; i < 256; i++) {
            if (PDF_DOC_ENCODING[i] == c) {
                return i;
            }
        }
        return -1;
    }
}
