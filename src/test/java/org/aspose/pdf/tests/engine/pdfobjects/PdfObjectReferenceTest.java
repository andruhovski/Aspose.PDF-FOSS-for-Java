package org.aspose.pdf.tests.engine.pdfobjects;
import org.aspose.pdf.engine.pdfobjects.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [PdfObjectReference].
public class PdfObjectReferenceTest {

    @Test
    public void writeTo() throws IOException {
        PdfObjectReference ref = new PdfObjectReference(12, 0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ref.writeTo(baos);
        assertEquals("12 0 R", baos.toString("US-ASCII"));
    }

    @Test
    public void writeToWithGeneration() throws IOException {
        PdfObjectReference ref = new PdfObjectReference(1, 2);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ref.writeTo(baos);
        assertEquals("1 2 R", baos.toString("US-ASCII"));
    }

    @Test
    public void dereferenceWithResolver() throws IOException {
        PdfObjectReference ref = new PdfObjectReference(10, 0);
        ref.setResolver(key -> PdfInteger.valueOf(42));
        PdfBase resolved = ref.dereference();
        assertEquals(PdfInteger.valueOf(42), resolved);
    }

    @Test
    public void dereferenceCaches() throws IOException {
        int[] callCount = {0};
        PdfObjectReference ref = new PdfObjectReference(10, 0);
        ref.setResolver(key -> {
            callCount[0]++;
            return PdfInteger.valueOf(42);
        });
        ref.dereference();
        ref.dereference();
        ref.dereference();
        assertEquals(1, callCount[0], "Resolver should be called only once");
    }

    @Test
    public void dereferenceWithoutResolverThrows() {
        PdfObjectReference ref = new PdfObjectReference(10, 0);
        assertThrows(IllegalStateException.class, ref::dereference);
    }

    @Test
    public void dereferenceNullReturnsNull() throws IOException {
        PdfObjectReference ref = new PdfObjectReference(10, 0);
        ref.setResolver(key -> null);
        PdfBase resolved = ref.dereference();
        assertSame(PdfNull.INSTANCE, resolved);
    }

    @Test
    public void getKey() {
        PdfObjectReference ref = new PdfObjectReference(new PdfObjectKey(5, 3));
        assertEquals(5, ref.getKey().getObjectNumber());
        assertEquals(3, ref.getKey().getGenerationNumber());
    }

    @Test
    public void equalsAndHashCode() {
        PdfObjectReference ref1 = new PdfObjectReference(10, 0);
        PdfObjectReference ref2 = new PdfObjectReference(10, 0);
        assertEquals(ref1, ref2);
        assertEquals(ref1.hashCode(), ref2.hashCode());
    }

    @Test
    public void constructorWithResolver() throws IOException {
        PdfObjectReference ref = new PdfObjectReference(
                new PdfObjectKey(1, 0),
                key -> PdfInteger.valueOf(99));
        assertEquals(PdfInteger.valueOf(99), ref.dereference());
    }
}
