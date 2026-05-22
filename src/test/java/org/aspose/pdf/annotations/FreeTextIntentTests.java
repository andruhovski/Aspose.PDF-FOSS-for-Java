package org.aspose.pdf.annotations;

import org.aspose.pdf.Color;
import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FreeTextIntent} enum and the {@code intent / rotate /
 * callout} accessors on {@link FreeTextAnnotation} (ISO 32000-1:2008 §12.5.6.6).
 */
public class FreeTextIntentTests {

    @Test
    public void enum_toFromPdfName_roundtrip() {
        assertEquals("FreeText", FreeTextIntent.FreeText.toPdfName());
        assertEquals("FreeTextCallout", FreeTextIntent.FreeTextCallout.toPdfName());
        assertEquals("FreeTextTypeWriter", FreeTextIntent.FreeTextTypeWriter.toPdfName());
        assertNull(FreeTextIntent.Undefined.toPdfName());

        assertEquals(FreeTextIntent.FreeText, FreeTextIntent.fromPdfName("FreeText"));
        assertEquals(FreeTextIntent.FreeTextCallout, FreeTextIntent.fromPdfName("FreeTextCallout"));
        assertEquals(FreeTextIntent.FreeTextTypeWriter, FreeTextIntent.fromPdfName("FreeTextTypeWriter"));
        assertEquals(FreeTextIntent.Undefined, FreeTextIntent.fromPdfName(null));
        assertEquals(FreeTextIntent.Undefined, FreeTextIntent.fromPdfName("Bogus"));
    }

    @Test
    public void intent_defaultIsUndefined() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            DefaultAppearance da = new DefaultAppearance("Helv", 10, Color.BLACK);
            FreeTextAnnotation fta = new FreeTextAnnotation(page,
                    new Rectangle(50, 50, 200, 100), da);
            assertEquals(FreeTextIntent.Undefined, fta.getIntent());
        }
    }

    @Test
    public void intent_setAndRead() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            DefaultAppearance da = new DefaultAppearance("Helv", 10, Color.BLACK);
            FreeTextAnnotation fta = new FreeTextAnnotation(page,
                    new Rectangle(50, 50, 200, 100), da);

            fta.setIntent(FreeTextIntent.FreeTextCallout);
            assertEquals(FreeTextIntent.FreeTextCallout, fta.getIntent());

            fta.setIntent(FreeTextIntent.FreeTextTypeWriter);
            assertEquals(FreeTextIntent.FreeTextTypeWriter, fta.getIntent());

            // Setting Undefined removes the entry
            fta.setIntent(FreeTextIntent.Undefined);
            assertEquals(FreeTextIntent.Undefined, fta.getIntent());

            // Setting null removes the entry
            fta.setIntent(FreeTextIntent.FreeText);
            assertEquals(FreeTextIntent.FreeText, fta.getIntent());
            fta.setIntent(null);
            assertEquals(FreeTextIntent.Undefined, fta.getIntent());
        }
    }

    @Test
    public void rotate_defaultIsZero() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            DefaultAppearance da = new DefaultAppearance();
            FreeTextAnnotation fta = new FreeTextAnnotation(page,
                    new Rectangle(0, 0, 100, 50), da);
            assertEquals(0, fta.getRotate());
        }
    }

    @Test
    public void rotate_setAndRead() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            DefaultAppearance da = new DefaultAppearance();
            FreeTextAnnotation fta = new FreeTextAnnotation(page,
                    new Rectangle(0, 0, 100, 50), da);
            fta.setRotate(180);
            assertEquals(180, fta.getRotate());
            fta.setRotate(90);
            assertEquals(90, fta.getRotate());
        }
    }

    @Test
    public void callout_defaultIsNull() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            DefaultAppearance da = new DefaultAppearance();
            FreeTextAnnotation fta = new FreeTextAnnotation(page,
                    new Rectangle(0, 0, 100, 50), da);
            assertNull(fta.getCallout());
        }
    }

    @Test
    public void callout_threePoints_setAndRead() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            DefaultAppearance da = new DefaultAppearance();
            FreeTextAnnotation fta = new FreeTextAnnotation(page,
                    new Rectangle(0, 0, 100, 50), da);
            fta.setCallout(new double[][] {{10, 10}, {50, 50}, {100, 100}});
            double[][] cl = fta.getCallout();
            assertNotNull(cl);
            assertEquals(3, cl.length);
            assertEquals(10.0, cl[0][0], 0.001);
            assertEquals(10.0, cl[0][1], 0.001);
            assertEquals(50.0, cl[1][0], 0.001);
            assertEquals(100.0, cl[2][0], 0.001);
            assertEquals(100.0, cl[2][1], 0.001);
        }
    }

    @Test
    public void callout_twoPoints_setAndRead() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            DefaultAppearance da = new DefaultAppearance();
            FreeTextAnnotation fta = new FreeTextAnnotation(page,
                    new Rectangle(0, 0, 100, 50), da);
            fta.setCallout(new double[][] {{0, 0}, {200, 100}});
            double[][] cl = fta.getCallout();
            assertNotNull(cl);
            assertEquals(2, cl.length);
            assertEquals(200.0, cl[1][0], 0.001);
        }
    }

    @Test
    public void callout_setNull_removes() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            DefaultAppearance da = new DefaultAppearance();
            FreeTextAnnotation fta = new FreeTextAnnotation(page,
                    new Rectangle(0, 0, 100, 50), da);
            fta.setCallout(new double[][] {{0, 0}, {1, 1}});
            assertNotNull(fta.getCallout());
            fta.setCallout(null);
            assertNull(fta.getCallout());
        }
    }

    @Test
    public void callout_invalidLength_isNoOp() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            DefaultAppearance da = new DefaultAppearance();
            FreeTextAnnotation fta = new FreeTextAnnotation(page,
                    new Rectangle(0, 0, 100, 50), da);
            fta.setCallout(new double[][] {{0, 0}, {1, 1}});
            // 4 points is not allowed — the set should be a no-op
            fta.setCallout(new double[][] {{0, 0}, {1, 1}, {2, 2}, {3, 3}});
            double[][] cl = fta.getCallout();
            assertNotNull(cl);
            assertEquals(2, cl.length, "Original 2-point callout should be preserved on invalid set");
        }
    }
}
