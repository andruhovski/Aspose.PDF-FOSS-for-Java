package org.aspose.pdf.engine.parser;

import org.aspose.pdf.Document;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Regression tests for the permissive cross-reference / header recovery added
/// in Sprint 62: a malformed xref table must fall through to a full object-scan
/// rebuild, and a file whose `%PDF-` header is missing must still open when
/// it otherwise contains PDF structure. Both cases mirror Acrobat / pdf.js
/// tolerance.
public class XRefRecoveryTest {

    /// Builds a valid `pages`-page PDF using the library writer (classic xref table).
    private static byte[] makePdf(int pages) throws IOException {
        try (Document doc = new Document()) {
            for (int i = 0; i < pages; i++) {
                doc.getPages().add();
            }
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            doc.save(bo);
            return bo.toByteArray();
        }
    }

    private static int indexOf(byte[] hay, String needle) {
        return new String(hay, StandardCharsets.ISO_8859_1).indexOf(needle);
    }

    @Test
    public void corruptXrefTableFallsBackToObjectScan() throws Exception {
        byte[] pdf = makePdf(2);
        // The free head entry "0000000000 65535 f" is always the first xref entry.
        // Corrupt its type char 'f' so parseTableFormat() throws "Expected 'n' or 'f'".
        int p = indexOf(pdf, "65535 f");
        assertTrue(p >= 0, "expected a classic xref table with a free head entry");
        pdf[p + 6] = 'q';

        try (Document doc = new Document(new ByteArrayInputStream(pdf))) {
            assertEquals(2, doc.getPages().getCount(),
                    "object-scan rebuild should recover both pages despite the broken xref table");
        }
    }

    @Test
    public void headerlessFileWithStructureStillOpens() throws Exception {
        byte[] pdf = makePdf(2);
        // Overwrite the "%PDF-" marker so no %PDF-/%FDF- header exists anywhere.
        int h = indexOf(pdf, "%PDF-");
        assertTrue(h >= 0);
        pdf[h + 1] = 'Z';
        pdf[h + 2] = 'Z';
        pdf[h + 3] = 'Z';
        assertEquals(-1, indexOf(pdf, "%PDF-"), "header marker should be gone");

        try (Document doc = new Document(new ByteArrayInputStream(pdf))) {
            assertEquals(2, doc.getPages().getCount(),
                    "a headerless file with intact object body + startxref should still open");
        }
    }
}
