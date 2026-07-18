package org.aspose.pdf.tests;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.pdfobjects.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for EmbeddedFileCollection, FileSpecification, FileParams.
public class EmbeddedFileTest {

    @Test
    public void testEmptyDocument() throws Exception {
        Document doc = new Document();
        EmbeddedFileCollection ef = doc.getEmbeddedFiles();
        assertNotNull(ef);
        assertEquals(0, ef.getCount());
        doc.close();
    }

    @Test
    public void testFileSpecFromStream() throws Exception {
        byte[] data = "Hello, embedded file!".getBytes();
        FileSpecification fs = new FileSpecification(new ByteArrayInputStream(data), "test.txt");
        assertEquals("test.txt", fs.getName());
        assertEquals("test.txt", fs.getUnicodeFileName());
        assertNotNull(fs.getEmbeddedStream());
        byte[] readBack = fs.getData();
        assertArrayEquals(data, readBack);
    }

    @Test
    public void testFileSpecDescription() throws Exception {
        byte[] data = "data".getBytes();
        FileSpecification fs = new FileSpecification(new ByteArrayInputStream(data), "file.bin");
        fs.setDescription("A test file");
        assertEquals("A test file", fs.getDescription());
    }

    @Test
    public void testFileSpecMIMEType() throws Exception {
        byte[] data = "data".getBytes();
        FileSpecification fs = new FileSpecification(new ByteArrayInputStream(data), "file.txt");
        fs.setMIMEType("text/plain");
        assertEquals("text/plain", fs.getMIMEType());
    }

    @Test
    public void testFileParams() throws Exception {
        byte[] data = "test data".getBytes();
        FileSpecification fs = new FileSpecification(new ByteArrayInputStream(data), "test.txt");
        FileParams params = fs.getParams();
        assertNotNull(params);
        assertEquals(data.length, params.getSize());
    }

    @Test
    public void testFileParamsEmpty() {
        FileParams params = new FileParams();
        assertEquals(0, params.getSize());
        assertNull(params.getCreationDate());
        assertNull(params.getModDate());
        assertNull(params.getCheckSum());
    }

    @Test
    public void testCollectionAdd() throws Exception {
        Document doc = new Document();
        EmbeddedFileCollection ef = doc.getEmbeddedFiles();
        byte[] data = "attachment content".getBytes();
        FileSpecification fs = new FileSpecification(new ByteArrayInputStream(data), "attach.txt");
        ef.add(fs);
        assertEquals(1, ef.getCount());
        assertEquals("attach.txt", ef.get(1).getName());
        doc.close();
    }

    @Test
    public void testCollectionIteration() throws Exception {
        Document doc = new Document();
        EmbeddedFileCollection ef = doc.getEmbeddedFiles();
        ef.add(new FileSpecification(new ByteArrayInputStream("a".getBytes()), "a.txt"));
        ef.add(new FileSpecification(new ByteArrayInputStream("b".getBytes()), "b.txt"));

        int count = 0;
        for (FileSpecification fs : ef) {
            assertNotNull(fs.getName());
            count++;
        }
        assertEquals(2, count);
        doc.close();
    }

    @Test
    public void testCollectionOneBasedIndex() throws Exception {
        Document doc = new Document();
        EmbeddedFileCollection ef = doc.getEmbeddedFiles();
        ef.add(new FileSpecification(new ByteArrayInputStream("x".getBytes()), "x.txt"));
        assertThrows(IndexOutOfBoundsException.class, () -> ef.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> ef.get(2));
        doc.close();
    }

    @Test
    public void testCollectionDelete() throws Exception {
        Document doc = new Document();
        EmbeddedFileCollection ef = doc.getEmbeddedFiles();
        ef.add(new FileSpecification(new ByteArrayInputStream("a".getBytes()), "a.txt"));
        ef.add(new FileSpecification(new ByteArrayInputStream("b".getBytes()), "b.txt"));
        assertEquals(2, ef.getCount());
        ef.delete(1);
        assertEquals(1, ef.getCount());
        doc.close();
    }

    @Test
    public void testFileSpecRelationship() throws Exception {
        byte[] data = "data".getBytes();
        FileSpecification fs = new FileSpecification(new ByteArrayInputStream(data), "file.xml");
        fs.setRelationship("Source");
        assertEquals("Source", fs.getRelationship());
    }

    @Test
    public void testWrapExistingDict() {
        PdfDictionary dict = new PdfDictionary();
        dict.set(PdfName.of("F"), new PdfString("existing.pdf".getBytes()));
        dict.set(PdfName.of("Desc"), new PdfString("An existing file".getBytes()));
        FileSpecification fs = new FileSpecification(dict);
        assertEquals("existing.pdf", fs.getName());
        assertEquals("An existing file", fs.getDescription());
    }
}
