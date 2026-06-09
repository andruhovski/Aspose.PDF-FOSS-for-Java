package org.aspose.pdf.tests.engine.parser;

import org.aspose.pdf.engine.pdfobjects.PdfObjectKey;
import org.aspose.pdf.engine.io.RandomAccessReader;
import org.aspose.pdf.engine.parser.PDFLexer;
import org.aspose.pdf.engine.parser.XRefEntry;
import org.aspose.pdf.engine.parser.XRefParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Recovery-path tests for {@link XRefParser}: malformed xref entries that the
 * parser must tolerate by clamping into the legal range required by
 * {@link PdfObjectKey}, while still emitting a WARNING for traceability.
 *
 * <p>The trigger case in production is the free-list head written with
 * {@code gen=65536} (one over the spec maximum of 65535) — a quirk produced
 * by various non-conforming writers. See REG-1 in the regression notes.</p>
 */
public class XRefParserRecoveryTest {

    @Test
    public void malformedXrefGeneration_isSanitizedWithWarning() throws IOException {
        // Free head with gen=65536 (malformed: ISO 32000-1 §7.5.4 mandates 65535).
        // Real-world example: 45497.pdf in the regression corpus.
        String xref = "xref\n" +
                "0 2\n" +
                "0000000000 65536 f \r\n" +
                "0000000010 00000 n \r\n" +
                "trailer\n" +
                "<< /Size 2 /Root 1 0 R >>\n";

        Logger logger = Logger.getLogger("org.aspose.pdf.engine.parser.XRefParser");
        List<String> warns = new ArrayList<>();
        Handler handler = new Handler() {
            @Override public void publish(LogRecord r) {
                if (r.getLevel() == Level.WARNING) {
                    warns.add(java.text.MessageFormat.format(r.getMessage(),
                            r.getParameters() == null ? new Object[0] : r.getParameters()));
                }
            }
            @Override public void flush() {}
            @Override public void close() {}
        };
        Level previous = logger.getLevel();
        logger.setLevel(Level.ALL);
        logger.addHandler(handler);
        try {
            RandomAccessReader reader =
                    RandomAccessReader.fromBytes(xref.getBytes(StandardCharsets.US_ASCII));
            PDFLexer lexer = new PDFLexer(reader);
            XRefParser parser = new XRefParser(reader, lexer);

            // Must not throw IllegalArgumentException from PdfObjectKey ctor.
            assertDoesNotThrow(() -> parser.parse(0));

            // Entry for obj 0 must be present, with gen clamped to 65535.
            Map<PdfObjectKey, XRefEntry> entries = parser.getEntries();
            XRefEntry free = entries.get(new PdfObjectKey(0, 65535));
            assertNotNull(free, "Free-head entry must be registered with clamped gen=65535");
            assertEquals(XRefEntry.Type.FREE, free.getType());

            // And there must be a WARNING mentioning the raw value.
            assertTrue(warns.stream().anyMatch(m -> m.contains("65536")),
                    "Expected WARNING mentioning the raw 65536 value, got: " + warns);
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(previous);
        }
    }

    @Test
    public void wellFormedXrefGeneration_emitsNoWarning() throws IOException {
        String xref = "xref\n" +
                "0 2\n" +
                "0000000000 65535 f \r\n" +
                "0000000010 00000 n \r\n" +
                "trailer\n" +
                "<< /Size 2 /Root 1 0 R >>\n";

        Logger logger = Logger.getLogger("org.aspose.pdf.engine.parser.XRefParser");
        List<String> warns = new ArrayList<>();
        Handler handler = new Handler() {
            @Override public void publish(LogRecord r) {
                if (r.getLevel() == Level.WARNING && r.getMessage() != null
                        && r.getMessage().contains("Malformed xref generation")) {
                    warns.add(r.getMessage());
                }
            }
            @Override public void flush() {}
            @Override public void close() {}
        };
        logger.addHandler(handler);
        try {
            RandomAccessReader reader =
                    RandomAccessReader.fromBytes(xref.getBytes(StandardCharsets.US_ASCII));
            PDFLexer lexer = new PDFLexer(reader);
            XRefParser parser = new XRefParser(reader, lexer);
            parser.parse(0);
            assertTrue(warns.isEmpty(),
                    "No sanitize WARNING expected for a well-formed xref, got: " + warns);
        } finally {
            logger.removeHandler(handler);
        }
    }
}
