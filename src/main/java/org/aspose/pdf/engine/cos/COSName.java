package org.aspose.pdf.engine.cos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * PDF name object (§7.3.5, ISO 32000-1:2008).
 * <p>
 * An atomic symbol used as keys in dictionaries. Names are interned: all COSName instances
 * with the same value share a single object (like {@link String#intern()}).
 * Characters outside 0x21..0x7E or delimiter characters are hex-encoded as {@code #XX}.
 * </p>
 */
public final class COSName extends COSBase implements Comparable<COSName> {

    private static final Logger LOG = Logger.getLogger(COSName.class.getName());

    private final String name;
    private final byte[] serialized;

    private static final ConcurrentHashMap<String, COSName> CACHE = new ConcurrentHashMap<>(512);

    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    // Predefined standard PDF names
    public static final COSName TYPE = getPredefined("Type");
    public static final COSName SUBTYPE = getPredefined("Subtype");
    public static final COSName PAGES = getPredefined("Pages");
    public static final COSName PAGE = getPredefined("Page");
    public static final COSName FONT = getPredefined("Font");
    public static final COSName LENGTH = getPredefined("Length");
    public static final COSName FILTER = getPredefined("Filter");
    public static final COSName FLATE_DECODE = getPredefined("FlateDecode");
    public static final COSName LZW_DECODE = getPredefined("LZWDecode");
    public static final COSName ASCII_HEX_DECODE = getPredefined("ASCIIHexDecode");
    public static final COSName ASCII85_DECODE = getPredefined("ASCII85Decode");
    public static final COSName RUN_LENGTH_DECODE = getPredefined("RunLengthDecode");
    public static final COSName CCITTFAX_DECODE = getPredefined("CCITTFaxDecode");
    public static final COSName DCT_DECODE = getPredefined("DCTDecode");
    public static final COSName JPX_DECODE = getPredefined("JPXDecode");
    public static final COSName WIDTH = getPredefined("Width");
    public static final COSName HEIGHT = getPredefined("Height");
    public static final COSName RESOURCES = getPredefined("Resources");
    public static final COSName CONTENTS = getPredefined("Contents");
    public static final COSName MEDIABOX = getPredefined("MediaBox");
    public static final COSName CROPBOX = getPredefined("CropBox");
    public static final COSName TRIMBOX = getPredefined("TrimBox");
    public static final COSName BLEEDBOX = getPredefined("BleedBox");
    public static final COSName ARTBOX = getPredefined("ArtBox");
    public static final COSName COUNT = getPredefined("Count");
    public static final COSName KIDS = getPredefined("Kids");
    public static final COSName PARENT = getPredefined("Parent");
    public static final COSName CATALOG = getPredefined("Catalog");
    public static final COSName ROOT = getPredefined("Root");
    public static final COSName SIZE = getPredefined("Size");
    public static final COSName PREV = getPredefined("Prev");
    public static final COSName INFO = getPredefined("Info");
    public static final COSName ID = getPredefined("ID");
    public static final COSName ENCRYPT = getPredefined("Encrypt");
    public static final COSName DECODE_PARMS = getPredefined("DecodeParms");
    public static final COSName ENCODING = getPredefined("Encoding");
    public static final COSName BASE_FONT = getPredefined("BaseFont");
    public static final COSName FIRST_CHAR = getPredefined("FirstChar");
    public static final COSName LAST_CHAR = getPredefined("LastChar");
    public static final COSName WIDTHS = getPredefined("Widths");
    public static final COSName TO_UNICODE = getPredefined("ToUnicode");
    public static final COSName FONT_DESCRIPTOR = getPredefined("FontDescriptor");
    public static final COSName XOBJECT = getPredefined("XObject");
    public static final COSName IMAGE = getPredefined("Image");
    public static final COSName FORM = getPredefined("Form");
    public static final COSName BBOX = getPredefined("BBox");
    public static final COSName MATRIX = getPredefined("Matrix");
    public static final COSName ANNOTS = getPredefined("Annots");
    public static final COSName RECT = getPredefined("Rect");
    public static final COSName ROTATE = getPredefined("Rotate");
    public static final COSName S = getPredefined("S");
    public static final COSName N = getPredefined("N");
    public static final COSName PREDICTOR = getPredefined("Predictor");
    public static final COSName COLUMNS = getPredefined("Columns");
    public static final COSName COLORS = getPredefined("Colors");
    public static final COSName BITS_PER_COMPONENT = getPredefined("BitsPerComponent");

    private COSName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        this.name = name;
        this.serialized = encode(name);
    }

    private static COSName getPredefined(String name) {
        return CACHE.computeIfAbsent(name, COSName::new);
    }

    /**
     * Returns a COSName for the given decoded name. Instances are interned (cached).
     *
     * @param name the decoded name (without leading '/')
     * @return the interned COSName
     * @throws IllegalArgumentException if name is null
     */
    public static COSName of(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        return CACHE.computeIfAbsent(name, COSName::new);
    }

    /**
     * Decodes a PDF name token (with {@code #XX} hex escapes) to a COSName.
     *
     * @param pdfToken the raw token from the PDF (without leading '/')
     * @return the decoded COSName
     */
    public static COSName fromPdfToken(String pdfToken) {
        if (pdfToken == null || pdfToken.isEmpty()) {
            return of("");
        }
        if (pdfToken.indexOf('#') < 0) {
            return of(pdfToken);
        }
        // Decode #XX sequences
        ByteArrayOutputStream baos = new ByteArrayOutputStream(pdfToken.length());
        for (int i = 0; i < pdfToken.length(); i++) {
            char c = pdfToken.charAt(i);
            if (c == '#' && i + 2 < pdfToken.length()) {
                int hi = Character.digit(pdfToken.charAt(i + 1), 16);
                int lo = Character.digit(pdfToken.charAt(i + 2), 16);
                if (hi >= 0 && lo >= 0) {
                    baos.write((hi << 4) | lo);
                    i += 2;
                    continue;
                }
            }
            baos.write(c);
        }
        return of(new String(baos.toByteArray(), StandardCharsets.UTF_8));
    }

    /**
     * Returns the decoded name (without leading '/').
     *
     * @return the name string
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the decoded name value. Alias for {@link #getName()}.
     *
     * @return the name string
     */
    public String getValue() {
        return name;
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        os.write(serialized);
    }

    @Override
    public <T> T accept(ICOSVisitor<T> visitor) {
        return visitor.visitName(this);
    }

    @Override
    public int compareTo(COSName other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof COSName)) return false;
        return name.equals(((COSName) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "/" + name;
    }

    /**
     * Encodes a name string to PDF syntax bytes: "/" followed by hex-escaped characters.
     */
    private static byte[] encode(String name) {
        byte[] utf8 = name.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(utf8.length + 1);
        baos.write('/');
        for (byte b : utf8) {
            int unsigned = b & 0xFF;
            if (unsigned >= 0x21 && unsigned <= 0x7E && !isDelimiter(unsigned)) {
                baos.write(unsigned);
            } else {
                baos.write('#');
                baos.write(HEX_DIGITS[(unsigned >> 4) & 0x0F]);
                baos.write(HEX_DIGITS[unsigned & 0x0F]);
            }
        }
        return baos.toByteArray();
    }

    /**
     * Returns true if the byte is a PDF delimiter that must be hex-encoded in names.
     */
    private static boolean isDelimiter(int b) {
        return b == '#' || b == '(' || b == ')' || b == '<' || b == '>'
                || b == '[' || b == ']' || b == '{' || b == '}' || b == '/' || b == '%';
    }
}
