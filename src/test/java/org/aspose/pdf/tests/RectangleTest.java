package org.aspose.pdf.tests;

import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSFloat;
import org.aspose.pdf.engine.cos.COSInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Rectangle}.
 */
public class RectangleTest {

    @Test
    public void constructorAndGetters() {
        Rectangle r = new Rectangle(10, 20, 110, 220);
        assertEquals(10, r.getLLX());
        assertEquals(20, r.getLLY());
        assertEquals(110, r.getURX());
        assertEquals(220, r.getURY());
    }

    @Test
    public void widthAndHeight() {
        Rectangle r = new Rectangle(10, 20, 110, 220);
        assertEquals(100, r.getWidth(), 1e-10);
        assertEquals(200, r.getHeight(), 1e-10);
    }

    @Test
    public void fromCOSArrayWithFloats() {
        COSArray arr = new COSArray(4);
        arr.add(new COSFloat(0));
        arr.add(new COSFloat(0));
        arr.add(new COSFloat(595.276));
        arr.add(new COSFloat(841.89));
        Rectangle r = Rectangle.fromCOSArray(arr);
        assertEquals(0, r.getLLX(), 1e-3);
        assertEquals(0, r.getLLY(), 1e-3);
        assertEquals(595.276, r.getURX(), 1e-3);
        assertEquals(841.89, r.getURY(), 1e-3);
    }

    @Test
    public void fromCOSArrayWithIntegers() {
        COSArray arr = new COSArray(4);
        arr.add(COSInteger.valueOf(0));
        arr.add(COSInteger.valueOf(0));
        arr.add(COSInteger.valueOf(612));
        arr.add(COSInteger.valueOf(792));
        Rectangle r = Rectangle.fromCOSArray(arr);
        assertEquals(0, r.getLLX());
        assertEquals(0, r.getLLY());
        assertEquals(612, r.getURX());
        assertEquals(792, r.getURY());
    }

    @Test
    public void fromCOSArrayNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> Rectangle.fromCOSArray(null));
    }

    @Test
    public void fromCOSArrayWrongSizeThrows() {
        COSArray arr = new COSArray();
        arr.add(new COSFloat(1));
        assertThrows(IllegalArgumentException.class, () -> Rectangle.fromCOSArray(arr));
    }

    @Test
    public void toCOSArray() {
        Rectangle r = new Rectangle(10, 20, 110, 220);
        COSArray arr = r.toCOSArray();
        assertEquals(4, arr.size());
        assertEquals(10f, arr.getFloat(0, 0f), 1e-10);
        assertEquals(20f, arr.getFloat(1, 0f), 1e-10);
        assertEquals(110f, arr.getFloat(2, 0f), 1e-10);
        assertEquals(220f, arr.getFloat(3, 0f), 1e-10);
    }

    @Test
    public void roundTrip() {
        Rectangle original = new Rectangle(1.5, 2.5, 100.25, 200.75);
        COSArray arr = original.toCOSArray();
        Rectangle restored = Rectangle.fromCOSArray(arr);
        assertEquals(original, restored);
    }

    @Test
    public void equalsAndHashCode() {
        Rectangle r1 = new Rectangle(0, 0, 100, 200);
        Rectangle r2 = new Rectangle(0, 0, 100, 200);
        Rectangle r3 = new Rectangle(0, 0, 100, 201);
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
    }

    @Test
    public void toStringContainsCoordinates() {
        Rectangle r = new Rectangle(10, 20, 30, 40);
        String s = r.toString();
        assertTrue(s.contains("10"));
        assertTrue(s.contains("40"));
    }
}
