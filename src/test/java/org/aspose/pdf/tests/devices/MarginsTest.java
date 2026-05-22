package org.aspose.pdf.tests.devices;

import org.aspose.pdf.devices.Margins;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests for {@link Margins}. Confirms the 4-int constructor parameter
 * order ({@code left, right, top, bottom}) matches Aspose.Pdf.Devices.Margins
 * so ports can write {@code new Margins(0, 0, 0, 0)} verbatim from C#.
 */
public class MarginsTest {

    @Test
    public void defaultZeroEverywhere() {
        Margins m = new Margins();
        assertEquals(0, m.getLeft());
        assertEquals(0, m.getRight());
        assertEquals(0, m.getTop());
        assertEquals(0, m.getBottom());
    }

    @Test
    public void fourArgConstructor() {
        Margins m = new Margins(1, 2, 3, 4);
        assertEquals(1, m.getLeft());
        assertEquals(2, m.getRight());
        assertEquals(3, m.getTop());
        assertEquals(4, m.getBottom());
    }

    @Test
    public void mutationsRoundTrip() {
        Margins m = new Margins();
        m.setLeft(10);
        m.setRight(20);
        m.setTop(30);
        m.setBottom(40);
        assertEquals(10, m.getLeft());
        assertEquals(20, m.getRight());
        assertEquals(30, m.getTop());
        assertEquals(40, m.getBottom());
    }

    @Test
    public void equalsAndHashCode() {
        assertEquals(new Margins(1, 2, 3, 4), new Margins(1, 2, 3, 4));
        assertNotEquals(new Margins(1, 2, 3, 4), new Margins(4, 3, 2, 1));
        assertEquals(new Margins(0, 0, 0, 0).hashCode(), new Margins().hashCode());
    }

    @Test
    public void toStringContainsAllSides() {
        String s = new Margins(1, 2, 3, 4).toString();
        assertTrue(s.contains("L=1"));
        assertTrue(s.contains("R=2"));
        assertTrue(s.contains("T=3"));
        assertTrue(s.contains("B=4"));
    }
}
