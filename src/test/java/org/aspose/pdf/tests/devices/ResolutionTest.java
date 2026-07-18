package org.aspose.pdf.tests.devices;

import org.aspose.pdf.devices.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [Resolution].
public class ResolutionTest {

    @Test
    public void singleDpiSetsEqual() {
        Resolution r = new Resolution(300);
        assertEquals(300, r.getX());
        assertEquals(300, r.getY());
    }

    @Test
    public void separateDpi() {
        Resolution r = new Resolution(150, 300);
        assertEquals(150, r.getX());
        assertEquals(300, r.getY());
    }

    @Test
    public void zeroDpiThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Resolution(0));
    }

    @Test
    public void negativeDpiThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Resolution(-72));
    }

    @Test
    public void toStringSymmetric() {
        assertEquals("72 DPI", new Resolution(72).toString());
    }

    @Test
    public void toStringAsymmetric() {
        assertEquals("150x300 DPI", new Resolution(150, 300).toString());
    }
}
