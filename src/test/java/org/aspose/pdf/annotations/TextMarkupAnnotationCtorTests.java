package org.aspose.pdf.annotations;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for auto-derivation of {@code /QuadPoints} from the bounding
 * rectangle in the {@code (Page, Rectangle)} constructors of text-markup
 * annotations (matches C# Aspose behavior — Bug #8).
 */
public class TextMarkupAnnotationCtorTests {

    @Test
    public void highlightAnnotation_rectCtor_derivesQuadPoints() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            Rectangle rect = new Rectangle(100, 200, 300, 250);
            HighlightAnnotation h = new HighlightAnnotation(page, rect);
            double[] qp = h.getQuadPoints();
            assertNotNull(qp, "QuadPoints should be auto-derived");
            assertEquals(8, qp.length);
            // top-left
            assertEquals(100.0, qp[0], 0.001);
            assertEquals(250.0, qp[1], 0.001);
            // top-right
            assertEquals(300.0, qp[2], 0.001);
            assertEquals(250.0, qp[3], 0.001);
            // bottom-left
            assertEquals(100.0, qp[4], 0.001);
            assertEquals(200.0, qp[5], 0.001);
            // bottom-right
            assertEquals(300.0, qp[6], 0.001);
            assertEquals(200.0, qp[7], 0.001);
        }
    }

    @Test
    public void underlineAnnotation_rectCtor_derivesQuadPoints() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            UnderlineAnnotation u = new UnderlineAnnotation(page, new Rectangle(50, 100, 200, 120));
            double[] qp = u.getQuadPoints();
            assertNotNull(qp);
            assertEquals(8, qp.length);
        }
    }

    @Test
    public void strikeOutAnnotation_rectCtor_derivesQuadPoints() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            StrikeOutAnnotation s = new StrikeOutAnnotation(page, new Rectangle(50, 100, 200, 120));
            double[] qp = s.getQuadPoints();
            assertNotNull(qp);
            assertEquals(8, qp.length);
        }
    }

    @Test
    public void squigglyAnnotation_rectCtor_derivesQuadPoints() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            SquigglyAnnotation sq = new SquigglyAnnotation(page, new Rectangle(50, 100, 200, 120));
            double[] qp = sq.getQuadPoints();
            assertNotNull(qp);
            assertEquals(8, qp.length);
        }
    }

    @Test
    public void cosCtor_doesNotOverrideExistingQuadPoints() throws Exception {
        // Reload path: when constructed from an existing dictionary, QuadPoints
        // already on the dict must NOT be clobbered.
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            HighlightAnnotation orig = new HighlightAnnotation(page, new Rectangle(0, 0, 100, 50));
            double[] custom = {1, 2, 3, 4, 5, 6, 7, 8};
            orig.setQuadPoints(custom);
            HighlightAnnotation reloaded = new HighlightAnnotation(orig.getCOSDictionary(), page);
            assertArrayEquals(custom, reloaded.getQuadPoints(), 0.001);
        }
    }

    @Test
    public void allFourSubtypesAreSet() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            Rectangle r = new Rectangle(10, 20, 30, 40);
            assertEquals("Highlight",
                    new HighlightAnnotation(page, r).getSubtype());
            assertEquals("Underline",
                    new UnderlineAnnotation(page, r).getSubtype());
            assertEquals("StrikeOut",
                    new StrikeOutAnnotation(page, r).getSubtype());
            assertEquals("Squiggly",
                    new SquigglyAnnotation(page, r).getSubtype());
        }
    }
}
