package org.aspose.pdf.tests.engine.cos;
import org.aspose.pdf.engine.cos.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link COSObjectReference}.
 */
public class COSObjectReferenceTest {

    @Test
    public void writeTo() throws IOException {
        COSObjectReference ref = new COSObjectReference(12, 0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ref.writeTo(baos);
        assertEquals("12 0 R", baos.toString("US-ASCII"));
    }

    @Test
    public void writeToWithGeneration() throws IOException {
        COSObjectReference ref = new COSObjectReference(1, 2);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ref.writeTo(baos);
        assertEquals("1 2 R", baos.toString("US-ASCII"));
    }

    @Test
    public void dereferenceWithResolver() throws IOException {
        COSObjectReference ref = new COSObjectReference(10, 0);
        ref.setResolver(key -> COSInteger.valueOf(42));
        COSBase resolved = ref.dereference();
        assertEquals(COSInteger.valueOf(42), resolved);
    }

    @Test
    public void dereferenceCaches() throws IOException {
        int[] callCount = {0};
        COSObjectReference ref = new COSObjectReference(10, 0);
        ref.setResolver(key -> {
            callCount[0]++;
            return COSInteger.valueOf(42);
        });
        ref.dereference();
        ref.dereference();
        ref.dereference();
        assertEquals(1, callCount[0], "Resolver should be called only once");
    }

    @Test
    public void dereferenceWithoutResolverThrows() {
        COSObjectReference ref = new COSObjectReference(10, 0);
        assertThrows(IllegalStateException.class, ref::dereference);
    }

    @Test
    public void dereferenceNullReturnsNull() throws IOException {
        COSObjectReference ref = new COSObjectReference(10, 0);
        ref.setResolver(key -> null);
        COSBase resolved = ref.dereference();
        assertSame(COSNull.INSTANCE, resolved);
    }

    @Test
    public void getKey() {
        COSObjectReference ref = new COSObjectReference(new COSObjectKey(5, 3));
        assertEquals(5, ref.getKey().getObjectNumber());
        assertEquals(3, ref.getKey().getGenerationNumber());
    }

    @Test
    public void equalsAndHashCode() {
        COSObjectReference ref1 = new COSObjectReference(10, 0);
        COSObjectReference ref2 = new COSObjectReference(10, 0);
        assertEquals(ref1, ref2);
        assertEquals(ref1.hashCode(), ref2.hashCode());
    }

    @Test
    public void constructorWithResolver() throws IOException {
        COSObjectReference ref = new COSObjectReference(
                new COSObjectKey(1, 0),
                key -> COSInteger.valueOf(99));
        assertEquals(COSInteger.valueOf(99), ref.dereference());
    }
}
