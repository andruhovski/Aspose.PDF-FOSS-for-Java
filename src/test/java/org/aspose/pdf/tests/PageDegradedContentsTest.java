package org.aspose.pdf.tests;

import org.aspose.pdf.Page;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Sprint 63 B.6 — guarded degradation in [Page#getContents()].
///
/// A page whose `/Contents` stream cannot be decoded must (a) parse to an
/// empty operator collection instead of throwing, and (b) NEVER serialise that
/// degraded cache back over the original raw bytes on save.
///
public class PageDegradedContentsTest {

    /// Builds a /Contents stream whose FlateDecode payload is undecodable.
    private PdfStream undecodableContentStream() {
        PdfStream stream = new PdfStream();
        // Too short to salvage and an invalid zlib/raw header -> decode throws.
        stream.setEncodedData(new byte[] {0x00, 0x01, 0x02, 0x03, 0x04});
        stream.setFilter(PdfName.FLATE_DECODE);
        return stream;
    }

    @Test
    public void undecodableContents_degradeToEmpty_doNotThrow() throws Exception {
        PdfStream content = undecodableContentStream();
        PdfDictionary pageDict = new PdfDictionary();
        pageDict.set(PdfName.TYPE, PdfName.of("Page"));
        pageDict.set(PdfName.CONTENTS, content);

        Page page = new Page(pageDict, null);
        // Must not throw — degrades to an empty operator collection.
        assertEquals(0, page.getContents().size(),
                "undecodable content stream should degrade to empty operators");
    }

    @Test
    public void degradedCache_isNotSerialisedOverOriginalBytes() throws Exception {
        PdfStream content = undecodableContentStream();
        byte[] originalEncoded = content.getEncodedData().clone();

        PdfDictionary pageDict = new PdfDictionary();
        pageDict.set(PdfName.TYPE, PdfName.of("Page"));
        pageDict.set(PdfName.CONTENTS, content);

        Page page = new Page(pageDict, null);
        page.getContents();          // triggers degradation
        page.markContentsDirty();    // pretend something edited the (empty) cache
        page.flushContentsIfDirty(); // guard must refuse to overwrite

        // /Contents is still the same PdfStream and its raw bytes are untouched.
        assertTrue(pageDict.get(PdfName.CONTENTS) == content,
                "the original /Contents stream object must be preserved");
        assertArrayEquals(originalEncoded, content.getEncodedData(),
                "degraded cache must never overwrite the original /Contents bytes");
    }
}
