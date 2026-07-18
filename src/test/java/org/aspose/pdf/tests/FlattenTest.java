package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.annotations.*;
import org.aspose.pdf.engine.pdfobjects.*;
import org.aspose.pdf.forms.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for annotation and form flattening.
public class FlattenTest {

    /// Creates a minimal page dictionary with a MediaBox.
    private Page createPage() {
        PdfDictionary pageDict = new PdfDictionary();
        pageDict.set(PdfName.TYPE, PdfName.PAGE);
        pageDict.set(PdfName.MEDIABOX, new Rectangle(0, 0, 595, 842).toPdfArray());
        return new Page(pageDict, null);
    }

    /// Creates an annotation dictionary with a normal appearance stream (/AP /N).
    ///
    /// @param rect       the annotation rectangle
    /// @param apContent  the appearance stream content bytes
    /// @param bbox       the appearance BBox
    /// @return the annotation PDF dictionary
    private PdfDictionary createAnnotWithAppearance(Rectangle rect, String apContent, Rectangle bbox) {
        PdfDictionary annotDict = new PdfDictionary();
        annotDict.set(PdfName.of("Type"), PdfName.of("Annot"));
        annotDict.set(PdfName.of("Subtype"), PdfName.of("Stamp"));
        annotDict.set(PdfName.of("Rect"), rect.toPdfArray());

        PdfStream apStream = new PdfStream();
        apStream.setDecodedData(apContent.getBytes(StandardCharsets.US_ASCII));
        apStream.set(PdfName.BBOX, bbox.toPdfArray());

        PdfDictionary apDict = new PdfDictionary();
        apDict.set(PdfName.of("N"), apStream);
        annotDict.set(PdfName.of("AP"), apDict);

        return annotDict;
    }

    @Test
    public void testFlattenAnnotationAppendsCTM() throws IOException {
        Page page = createPage();
        PdfDictionary pageDict = page.getPdfDictionary();

        // Create annotation at (100,200)-(200,300) with a 100x100 BBox appearance
        Rectangle annotRect = new Rectangle(100, 200, 200, 300);
        Rectangle bbox = new Rectangle(0, 0, 100, 100);
        String apContent = "1 0 0 1 0 0 cm";

        PdfDictionary annotDict = createAnnotWithAppearance(annotRect, apContent, bbox);

        // Add annotation to page /Annots array
        PdfArray annots = new PdfArray();
        annots.add(annotDict);
        pageDict.set(PdfName.ANNOTS, annots);

        // Flatten
        page.flattenAnnotations();

        // Verify /Annots is removed
        assertNull(pageDict.get(PdfName.ANNOTS),
                "Annots should be removed after flattening");

        // Verify content stream was created with CTM operators
        PdfBase contents = pageDict.get(PdfName.CONTENTS);
        assertNotNull(contents, "Content stream should exist after flattening");

        // Read the flattened content
        String contentStr = getContentString(contents);
        assertTrue(contentStr.contains("q"), "Flattened content should contain 'q' (save state)");
        assertTrue(contentStr.contains("cm"), "Flattened content should contain 'cm' (CTM)");
        assertTrue(contentStr.contains("Q"), "Flattened content should contain 'Q' (restore state)");
        // BUG-F4 fix (Sprint 21): the appearance is placed as a Form XObject
        // invocation (`/FmFlat... Do`) rather than inlined, so its bytes live in
        // the XObject stream — not the page content stream. This keeps the
        // appearance's own internal operators out of the page content (ISO
        // 32000-1:2008 §12.5.5).
        assertTrue(contentStr.contains("Do"),
                "Flattened content should invoke the appearance Form XObject via 'Do'");
        PdfDictionary xobjs = page.getResources().getXObjects();
        assertNotNull(xobjs, "Flattening should register the appearance as an XObject");
        boolean apFound = false;
        for (PdfName key : xobjs.keySet()) {
            PdfBase v = xobjs.get(key);
            if (v instanceof PdfStream) {
                String xc = new String(((PdfStream) v).getDecodedData(), StandardCharsets.US_ASCII);
                if (xc.contains(apContent)) { apFound = true; break; }
            }
        }
        assertTrue(apFound, "Original appearance content should live in the placed XObject stream");
    }

    @Test
    public void testFlattenSkipsHiddenAnnotation() throws IOException {
        Page page = createPage();
        PdfDictionary pageDict = page.getPdfDictionary();

        // Create hidden annotation (flag bit 2 = 0x02)
        Rectangle annotRect = new Rectangle(10, 10, 50, 50);
        Rectangle bbox = new Rectangle(0, 0, 40, 40);
        PdfDictionary annotDict = createAnnotWithAppearance(annotRect, "0 0 m", bbox);
        annotDict.set(PdfName.of("F"), PdfInteger.valueOf(0x02)); // Hidden

        PdfArray annots = new PdfArray();
        annots.add(annotDict);
        pageDict.set(PdfName.ANNOTS, annots);

        page.flattenAnnotations();

        // /Annots should still be removed (array is cleared)
        assertNull(pageDict.get(PdfName.ANNOTS));

        // No content should have been appended (hidden annotation was skipped)
        PdfBase contents = pageDict.get(PdfName.CONTENTS);
        assertNull(contents, "No content should be added for hidden annotations");
    }

    @Test
    public void testFlattenAnnotationWithNoAppearance() throws IOException {
        Page page = createPage();
        PdfDictionary pageDict = page.getPdfDictionary();

        // Create annotation without /AP
        PdfDictionary annotDict = new PdfDictionary();
        annotDict.set(PdfName.of("Type"), PdfName.of("Annot"));
        annotDict.set(PdfName.of("Subtype"), PdfName.of("Text"));
        annotDict.set(PdfName.of("Rect"), new Rectangle(0, 0, 50, 50).toPdfArray());

        PdfArray annots = new PdfArray();
        annots.add(annotDict);
        pageDict.set(PdfName.ANNOTS, annots);

        page.flattenAnnotations();

        assertNull(pageDict.get(PdfName.ANNOTS));
        assertNull(pageDict.get(PdfName.CONTENTS),
                "No content should be added when annotation has no appearance");
    }

    @Test
    public void testFlattenFormField() throws IOException {
        // Build a minimal document structure
        Document doc = new Document();
        PageCollection pages = doc.getPages();
        Page page = pages.add();
        PdfDictionary pageDict = page.getPdfDictionary();

        // Create a text field with appearance
        PdfDictionary fieldDict = new PdfDictionary();
        fieldDict.set(PdfName.of("Type"), PdfName.of("Annot"));
        fieldDict.set(PdfName.of("Subtype"), PdfName.of("Widget"));
        fieldDict.set(PdfName.of("FT"), PdfName.of("Tx"));
        fieldDict.set(PdfName.of("T"), new PdfString("field1".getBytes(StandardCharsets.UTF_8)));
        Rectangle fieldRect = new Rectangle(50, 700, 200, 720);
        fieldDict.set(PdfName.of("Rect"), fieldRect.toPdfArray());

        // Create appearance stream for the field
        PdfStream apStream = new PdfStream();
        apStream.setDecodedData("BT /F1 12 Tf (Hello) Tj ET".getBytes(StandardCharsets.US_ASCII));
        apStream.set(PdfName.BBOX, new Rectangle(0, 0, 150, 20).toPdfArray());

        PdfDictionary apDict = new PdfDictionary();
        apDict.set(PdfName.of("N"), apStream);
        fieldDict.set(PdfName.of("AP"), apDict);

        // Add widget annotation to the page's /Annots
        PdfArray annots = new PdfArray();
        annots.add(fieldDict);
        pageDict.set(PdfName.ANNOTS, annots);

        // Flatten annotations on the page directly
        page.flattenAnnotations();

        // Verify annotations are removed
        assertNull(pageDict.get(PdfName.ANNOTS),
                "Annots should be removed after field flattening");

        // Verify content was appended
        PdfBase contents = pageDict.get(PdfName.CONTENTS);
        assertNotNull(contents, "Content stream should exist after field flattening");
    }

    @Test
    public void testDocumentFlatten() throws IOException {
        // Build a document with an annotation on a page
        Document doc = new Document();
        PageCollection pages = doc.getPages();
        Page page = pages.add();
        PdfDictionary pageDict = page.getPdfDictionary();

        // Add a stamp annotation with appearance
        Rectangle annotRect = new Rectangle(100, 100, 200, 200);
        Rectangle bbox = new Rectangle(0, 0, 100, 100);
        PdfDictionary annotDict = createAnnotWithAppearance(annotRect, "0 1 0 rg 0 0 100 100 re f", bbox);

        PdfArray annots = new PdfArray();
        annots.add(annotDict);
        pageDict.set(PdfName.ANNOTS, annots);

        // Flatten the whole document
        doc.flatten();

        // Verify annotations removed
        assertNull(pageDict.get(PdfName.ANNOTS),
                "Annots should be removed after document.flatten()");

        // Verify content was generated
        PdfBase contents = pageDict.get(PdfName.CONTENTS);
        assertNotNull(contents, "Content stream should exist after document.flatten()");
    }

    @Test
    public void testAppendToContentStreamSingleStream() {
        Page page = createPage();
        PdfDictionary pageDict = page.getPdfDictionary();

        // Set initial content
        PdfStream initial = new PdfStream();
        initial.setDecodedData("BT (Hello) Tj ET".getBytes(StandardCharsets.US_ASCII));
        pageDict.set(PdfName.CONTENTS, initial);

        // Append new content
        page.appendToContentStream("q 1 0 0 1 0 0 cm Q".getBytes(StandardCharsets.US_ASCII));

        // Should now be a PdfArray with 2 entries
        PdfBase contents = pageDict.get(PdfName.CONTENTS);
        assertTrue(contents instanceof PdfArray, "Contents should be an array after append");
        assertEquals(2, ((PdfArray) contents).size());
    }

    @Test
    public void testAppendToContentStreamExistingArray() {
        Page page = createPage();
        PdfDictionary pageDict = page.getPdfDictionary();

        // Set initial content as array
        PdfArray arr = new PdfArray();
        PdfStream s1 = new PdfStream();
        s1.setDecodedData("BT ET".getBytes(StandardCharsets.US_ASCII));
        arr.add(s1);
        pageDict.set(PdfName.CONTENTS, arr);

        // Append
        page.appendToContentStream("q Q".getBytes(StandardCharsets.US_ASCII));

        assertEquals(2, arr.size(), "Array should grow by one after append");
    }

    @Test
    public void testGetNormalAppearance() {
        PdfDictionary annotDict = new PdfDictionary();
        annotDict.set(PdfName.of("Subtype"), PdfName.of("Stamp"));

        // No AP -> null
        Annotation annot = Annotation.fromDictionary(annotDict, null);
        assertNull(annot.getNormalAppearanceStream());

        // Add AP/N stream
        PdfStream apStream = new PdfStream();
        apStream.setDecodedData("test".getBytes(StandardCharsets.US_ASCII));
        PdfDictionary apDict = new PdfDictionary();
        apDict.set(PdfName.of("N"), apStream);
        annotDict.set(PdfName.of("AP"), apDict);

        annot = Annotation.fromDictionary(annotDict, null);
        assertNotNull(annot.getNormalAppearanceStream());
        assertSame(apStream, annot.getNormalAppearanceStream());
    }

    /// Helper: extracts text from a content stream PDF object (PdfStream or PdfArray).
    private String getContentString(PdfBase contents) throws IOException {
        if (contents instanceof PdfStream) {
            return new String(((PdfStream) contents).getDecodedData(), StandardCharsets.US_ASCII);
        }
        if (contents instanceof PdfArray) {
            StringBuilder sb = new StringBuilder();
            PdfArray arr = (PdfArray) contents;
            for (int i = 0; i < arr.size(); i++) {
                PdfBase item = arr.get(i);
                if (item instanceof PdfStream) {
                    sb.append(new String(((PdfStream) item).getDecodedData(), StandardCharsets.US_ASCII));
                }
            }
            return sb.toString();
        }
        return "";
    }
}
