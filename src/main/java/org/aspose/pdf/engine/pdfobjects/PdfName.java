package org.aspose.pdf.engine.pdfobjects;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * PDF name object (§7.3.5, ISO 32000-1:2008).
 * <p>
 * An atomic symbol used as keys in dictionaries. Names are interned: all PdfName instances
 * with the same value share a single object (like {@link String#intern()}).
 * Characters outside 0x21..0x7E or delimiter characters are hex-encoded as {@code #XX}.
 * </p>
 */
public final class PdfName extends PdfBase implements Comparable<PdfName> {

    private static final Logger LOG = Logger.getLogger(PdfName.class.getName());

    private final String name;
    private final byte[] serialized;

    private static final ConcurrentHashMap<String, PdfName> CACHE = new ConcurrentHashMap<>(512);

    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    // Predefined standard PDF names
    public static final PdfName TYPE = getPredefined("Type");
    public static final PdfName SUBTYPE = getPredefined("Subtype");
    public static final PdfName PAGES = getPredefined("Pages");
    public static final PdfName PAGE = getPredefined("Page");
    public static final PdfName FONT = getPredefined("Font");
    public static final PdfName LENGTH = getPredefined("Length");
    public static final PdfName FILTER = getPredefined("Filter");
    public static final PdfName FLATE_DECODE = getPredefined("FlateDecode");
    public static final PdfName LZW_DECODE = getPredefined("LZWDecode");
    public static final PdfName ASCII_HEX_DECODE = getPredefined("ASCIIHexDecode");
    public static final PdfName ASCII85_DECODE = getPredefined("ASCII85Decode");
    public static final PdfName RUN_LENGTH_DECODE = getPredefined("RunLengthDecode");
    public static final PdfName CCITTFAX_DECODE = getPredefined("CCITTFaxDecode");
    public static final PdfName DCT_DECODE = getPredefined("DCTDecode");
    public static final PdfName JPX_DECODE = getPredefined("JPXDecode");
    public static final PdfName WIDTH = getPredefined("Width");
    public static final PdfName HEIGHT = getPredefined("Height");
    public static final PdfName RESOURCES = getPredefined("Resources");
    public static final PdfName CONTENTS = getPredefined("Contents");
    public static final PdfName MEDIABOX = getPredefined("MediaBox");
    public static final PdfName CROPBOX = getPredefined("CropBox");
    public static final PdfName TRIMBOX = getPredefined("TrimBox");
    public static final PdfName BLEEDBOX = getPredefined("BleedBox");
    public static final PdfName ARTBOX = getPredefined("ArtBox");
    public static final PdfName COUNT = getPredefined("Count");
    public static final PdfName KIDS = getPredefined("Kids");
    public static final PdfName PARENT = getPredefined("Parent");
    public static final PdfName CATALOG = getPredefined("Catalog");
    public static final PdfName ROOT = getPredefined("Root");
    public static final PdfName SIZE = getPredefined("Size");
    public static final PdfName PREV = getPredefined("Prev");
    public static final PdfName INFO = getPredefined("Info");
    public static final PdfName ID = getPredefined("ID");
    public static final PdfName ENCRYPT = getPredefined("Encrypt");
    public static final PdfName DECODE_PARMS = getPredefined("DecodeParms");
    public static final PdfName ENCODING = getPredefined("Encoding");
    public static final PdfName BASE_FONT = getPredefined("BaseFont");
    public static final PdfName FIRST_CHAR = getPredefined("FirstChar");
    public static final PdfName LAST_CHAR = getPredefined("LastChar");
    public static final PdfName WIDTHS = getPredefined("Widths");
    public static final PdfName TO_UNICODE = getPredefined("ToUnicode");
    public static final PdfName FONT_DESCRIPTOR = getPredefined("FontDescriptor");
    public static final PdfName XOBJECT = getPredefined("XObject");
    public static final PdfName IMAGE = getPredefined("Image");
    public static final PdfName FORM = getPredefined("Form");
    public static final PdfName BBOX = getPredefined("BBox");
    public static final PdfName MATRIX = getPredefined("Matrix");
    public static final PdfName ANNOTS = getPredefined("Annots");
    public static final PdfName RECT = getPredefined("Rect");
    public static final PdfName ROTATE = getPredefined("Rotate");
    public static final PdfName S = getPredefined("S");
    public static final PdfName N = getPredefined("N");
    public static final PdfName PREDICTOR = getPredefined("Predictor");
    public static final PdfName COLUMNS = getPredefined("Columns");
    public static final PdfName COLORS = getPredefined("Colors");
    public static final PdfName BITS_PER_COMPONENT = getPredefined("BitsPerComponent");

    private PdfName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        this.name = name;
        this.serialized = encode(name);
    }

    private static PdfName getPredefined(String name) {
        return CACHE.computeIfAbsent(name, PdfName::new);
    }

    /**
     * Returns a PdfName for the given decoded name. Instances are interned (cached).
     *
     * @param name the decoded name (without leading '/')
     * @return the interned PdfName
     * @throws IllegalArgumentException if name is null
     */
    public static PdfName of(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        return CACHE.computeIfAbsent(name, PdfName::new);
    }

    /**
     * Ignored: PdfName instances are interned singletons shared across every
     * document (see {@link #CACHE}). The base class stores the write-time
     * object key in mutable state, but a shared singleton must never carry a
     * per-document indirect identity — doing so leaks one document's object
     * number into every later write of the same name. Concretely, a writer
     * that assigned (say) {@code 14 0} to {@code /WinAnsiEncoding} while
     * serialising document A would make document B emit {@code /Encoding 14 0 R}
     * pointing at an object that does not exist in B, corrupting the font.
     * <p>
     * Names are valid as direct objects everywhere they appear (ISO 32000-1
     * §7.3.5), so keeping a name non-indirect simply serialises it inline,
     * which is always correct. This override makes that invariant explicit.
     *
     * @param key ignored
     */
    @Override
    public void setObjectKey(PdfObjectKey key) {
        // no-op: see Javadoc — interned names are always written inline.
    }

    /**
     * Decodes a PDF name token (with {@code #XX} hex escapes) to a PdfName.
     *
     * @param pdfToken the raw token from the PDF (without leading '/')
     * @return the decoded PdfName
     */
    public static PdfName fromPdfToken(String pdfToken) {
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
    public <T> T accept(IPdfVisitor<T> visitor) {
        return visitor.visitName(this);
    }

    @Override
    public int compareTo(PdfName other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PdfName)) return false;
        return name.equals(((PdfName) o).name);
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
