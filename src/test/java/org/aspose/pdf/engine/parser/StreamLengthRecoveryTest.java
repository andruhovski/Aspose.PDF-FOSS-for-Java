package org.aspose.pdf.engine.parser;

import org.aspose.pdf.engine.io.RandomAccessReader;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Sprint 65 — stream-extraction tolerance.
 *
 * <p>When a stream's {@code /Length} entry disagrees with the file's actual
 * {@code endstream} position, {@link PDFParser} must re-derive the true length by
 * scanning for {@code endstream} and hand the decode filter exactly the stream
 * bytes — never trailing junk (which surfaces downstream as Flate "incorrect
 * header check" / ASCII85 "invalid character") nor a truncated prefix. This
 * mirrors Acrobat / pdf.js tolerance and is the upstream root cause that
 * the Sprint 63 filter-symptom analysis deferred.</p>
 */
public class StreamLengthRecoveryTest {

    private static final byte[] CONTENT = "ABCDE".getBytes(StandardCharsets.ISO_8859_1);

    /**
     * Assembles a minimal single-object PDF with one uncompressed stream whose
     * declared {@code /Length} is {@code declaredLength} while the true content is
     * always {@link #CONTENT}. The xref offsets are measured from the assembled
     * bytes, so they stay valid regardless of the (wrong) declared length.
     */
    private static byte[] buildPdf(int declaredLength) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        write(bo, "%PDF-1.7\n");

        int objOffset = bo.size();
        write(bo, "1 0 obj\n");
        write(bo, "<< /Length " + declaredLength + " >>\n");
        write(bo, "stream\n");
        bo.write(CONTENT, 0, CONTENT.length);
        write(bo, "\nendstream\nendobj\n");

        int xrefOffset = bo.size();
        write(bo, "xref\n");
        write(bo, "0 2\n");
        write(bo, "0000000000 65535 f \n");
        write(bo, pad10(objOffset) + " 00000 n \n");
        write(bo, "trailer\n");
        write(bo, "<< /Size 2 /Root 1 0 R >>\n");
        write(bo, "startxref\n");
        write(bo, xrefOffset + "\n");
        write(bo, "%%EOF");
        return bo.toByteArray();
    }

    private static void write(ByteArrayOutputStream bo, String s) {
        byte[] b = s.getBytes(StandardCharsets.ISO_8859_1);
        bo.write(b, 0, b.length);
    }

    private static String pad10(int n) {
        String s = Integer.toString(n);
        StringBuilder sb = new StringBuilder();
        for (int i = s.length(); i < 10; i++) {
            sb.append('0');
        }
        return sb.append(s).toString();
    }

    private static byte[] extractStream(byte[] pdf) throws Exception {
        try (PDFParser parser = new PDFParser(RandomAccessReader.fromBytes(pdf))) {
            parser.parse();
            PdfBase obj = parser.getObject(1);
            PdfStream stream = assertInstanceOf(PdfStream.class, obj,
                    "object 1 should parse as a stream");
            return stream.getDecodedData();
        }
    }

    @Test
    public void lengthTooLargeDoesNotCaptureTrailingJunk() throws Exception {
        // Declares 50 bytes but only 5 exist: a naive reader would swallow
        // "ABCDE\nendstream\nendobj\nxref..." and feed the junk to the filter.
        byte[] data = extractStream(buildPdf(50));
        assertArrayEquals(CONTENT, data,
                "overshooting /Length must be corrected back to the real stream bytes");
    }

    @Test
    public void lengthTooSmallRecoversFullStream() throws Exception {
        // Declares 2 bytes but 5 exist: a naive reader would truncate to "AB".
        byte[] data = extractStream(buildPdf(2));
        assertArrayEquals(CONTENT, data,
                "an undersized /Length must be corrected back to the full stream bytes");
    }

    @Test
    public void correctLengthIsUntouched() throws Exception {
        byte[] data = extractStream(buildPdf(CONTENT.length));
        assertArrayEquals(CONTENT, data,
                "a correct /Length must be read verbatim with no recovery");
    }

    @Test
    public void gigabyteLengthIsClampedWithoutAllocating() throws Exception {
        // A corrupt /Length of 2 000 000 000 in a tiny file must not allocate
        // a 2 GB buffer (mass-corpus runs died in OOMs on exactly this); the
        // parser clamps to the bytes physically remaining and the endstream
        // scan then recovers the true 5-byte content.
        byte[] data = extractStream(buildPdf(2_000_000_000));
        assertArrayEquals(CONTENT, data,
                "an absurd /Length must be clamped and recovered by the endstream scan");
    }
}
