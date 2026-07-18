package org.aspose.pdf.annotations;

import org.aspose.pdf.Color;
import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.forms.AppearanceCharacteristics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/// Unit tests for [AppearanceCharacteristics] when accessed through
/// [WidgetAnnotation#getCharacteristics()] — wraps the `/MK`
/// appearance-characteristics dictionary (ISO 32000-1:2008 §12.5.6.19).
public class AnnotationCharacteristicsTests {

    @Test
    public void getCharacteristics_neverNull() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            WidgetAnnotation w = new WidgetAnnotation(page, new Rectangle(0, 0, 100, 50));
            assertNotNull(w.getCharacteristics());
        }
    }

    @Test
    public void getCharacteristics_isCached() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            WidgetAnnotation w = new WidgetAnnotation(page, new Rectangle(0, 0, 100, 50));
            AppearanceCharacteristics first = w.getCharacteristics();
            AppearanceCharacteristics second = w.getCharacteristics();
            assertSame(first, second, "getCharacteristics() should return the same instance");
        }
    }

    @Test
    public void border_setAndRead_rgb() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            WidgetAnnotation w = new WidgetAnnotation(page, new Rectangle(0, 0, 100, 50));
            w.getCharacteristics().setBorder(Color.fromRgb(1, 0, 0));
            Color got = w.getCharacteristics().getBorder();
            assertNotNull(got);
            assertEquals(1.0, got.getR(), 0.01);
            assertEquals(0.0, got.getG(), 0.01);
            assertEquals(0.0, got.getB(), 0.01);
        }
    }

    @Test
    public void border_clear_removesEntry() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            WidgetAnnotation w = new WidgetAnnotation(page, new Rectangle(0, 0, 100, 50));
            w.getCharacteristics().setBorder(Color.fromRgb(0, 1, 0));
            assertNotNull(w.getCharacteristics().getBorder());
            w.getCharacteristics().setBorder(null);
            assertNull(w.getCharacteristics().getBorder());
        }
    }

    @Test
    public void background_setAndRead() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            WidgetAnnotation w = new WidgetAnnotation(page, new Rectangle(0, 0, 100, 50));
            w.getCharacteristics().setBackground(Color.fromRgb(0, 0.5, 0.5));
            Color got = w.getCharacteristics().getBackground();
            assertNotNull(got);
            assertEquals(0.5, got.getG(), 0.01);
        }
    }

    @Test
    public void rotate_default_isZero() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            WidgetAnnotation w = new WidgetAnnotation(page, new Rectangle(0, 0, 100, 50));
            assertEquals(0, w.getCharacteristics().getRotate());
        }
    }

    @Test
    public void rotate_setAndRead() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            WidgetAnnotation w = new WidgetAnnotation(page, new Rectangle(0, 0, 100, 50));
            w.getCharacteristics().setRotate(90);
            assertEquals(90, w.getCharacteristics().getRotate());
            w.getCharacteristics().setRotate(270);
            assertEquals(270, w.getCharacteristics().getRotate());
        }
    }

    @Test
    public void caption_setAndRead() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            WidgetAnnotation w = new WidgetAnnotation(page, new Rectangle(0, 0, 100, 50));
            assertNull(w.getCharacteristics().getCaption());
            w.getCharacteristics().setCaption("Submit");
            assertEquals("Submit", w.getCharacteristics().getCaption());
            w.getCharacteristics().setCaption(null);
            assertNull(w.getCharacteristics().getCaption());
        }
    }
}
