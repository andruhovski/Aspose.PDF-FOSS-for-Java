package org.aspose.pdf.tests;

import org.aspose.pdf.XmpValue;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for XmpValue typed wrapper.
public class XmpValueTest {

    @Test
    public void testStringValue() {
        XmpValue v = new XmpValue("hello");
        assertTrue(v.isString());
        assertFalse(v.isInteger());
        assertFalse(v.isDouble());
        assertFalse(v.isDateTime());
        assertFalse(v.isArray());
        assertEquals("hello", v.toString());
        assertEquals("hello", v.toStringValue());
    }

    @Test
    public void testIntegerValue() {
        XmpValue v = new XmpValue(42);
        assertTrue(v.isInteger());
        assertFalse(v.isDouble());
        assertFalse(v.isArray());
        assertEquals(42, v.toInteger());
        assertEquals("42", v.toString());
    }

    @Test
    public void testDoubleValue() {
        XmpValue v = new XmpValue(3.14);
        assertTrue(v.isDouble());
        assertFalse(v.isInteger());
        assertEquals(3.14, v.toDouble(), 0.001);
    }

    @Test
    public void testDateValue() {
        Date now = new Date();
        XmpValue v = new XmpValue(now);
        assertTrue(v.isDateTime());
        assertNotNull(v.toDateTime());
        // Should be ISO 8601 format
        assertTrue(v.toString().contains("T"));
    }

    @Test
    public void testArrayValue() {
        Object[] arr = {"a", "b", "c"};
        XmpValue v = new XmpValue(arr);
        assertTrue(v.isArray());
        assertEquals(3, v.toArray().length);
        assertEquals("a", v.toString()); // first item
    }

    @Test
    public void testNullStringValue() {
        XmpValue v = new XmpValue((String) null);
        assertEquals("", v.toString());
    }

    @Test
    public void testNullDateValue() {
        XmpValue v = new XmpValue((Date) null);
        assertEquals("", v.toString());
    }

    @Test
    public void testIsDateTimeFormat() {
        XmpValue v = new XmpValue("2024-01-15T10:30:00Z");
        assertTrue(v.isDateTime());
        assertNotNull(v.toDateTime());
    }

    @Test
    public void testNotDateTime() {
        XmpValue v = new XmpValue("not a date");
        assertFalse(v.isDateTime());
    }

    @Test
    public void testIntegerParsing() {
        XmpValue v = new XmpValue("111");
        assertTrue(v.isInteger());
        assertEquals(111, v.toInteger());
    }

    @Test
    public void testDoubleParsing() {
        XmpValue v = new XmpValue("111.11");
        assertTrue(v.isDouble());
        assertEquals(111.11, v.toDouble(), 0.001);
    }
}
