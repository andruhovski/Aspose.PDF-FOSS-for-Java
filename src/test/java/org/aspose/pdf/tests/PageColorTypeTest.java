package org.aspose.pdf.tests;

import org.aspose.pdf.ColorType;
import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.operators.GSave;
import org.aspose.pdf.operators.GRestore;
import org.aspose.pdf.operators.Re;
import org.aspose.pdf.operators.Fill;
import org.aspose.pdf.operators.SetRGBColor;
import org.aspose.pdf.operators.SetGray;
import org.aspose.pdf.operators.SetCMYKColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/// Smoke tests for [Page#getColorType()] — exercises the classifier on
/// hand-crafted content streams whose colour content is unambiguous, so the
/// tests don't depend on any particular renderer or input PDF corpus file.
public class PageColorTypeTest {

    @Test
    public void emptyPageIsUndefined() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            assertEquals(ColorType.Undefined, page.getColorType());
        }
    }

    @Test
    public void rgbFillMakesRgb() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            page.getContents().add(new SetRGBColor(0.8, 0.1, 0.2));
            page.getContents().add(new Re(10, 10, 50, 50));
            page.getContents().add(new Fill());
            assertEquals(ColorType.Rgb, page.getColorType());
        }
    }

    @Test
    public void grayMidtoneMakesGrayscale() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            page.getContents().add(new SetGray(0.5));
            page.getContents().add(new Re(10, 10, 50, 50));
            page.getContents().add(new Fill());
            assertEquals(ColorType.Grayscale, page.getColorType());
        }
    }

    @Test
    public void pureBlackAndWhiteIsBw() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            page.getContents().add(new SetGray(0.0));
            page.getContents().add(new Re(10, 10, 50, 50));
            page.getContents().add(new Fill());
            page.getContents().add(new SetGray(1.0));
            page.getContents().add(new Re(60, 60, 50, 50));
            page.getContents().add(new Fill());
            assertEquals(ColorType.BlackAndWhite, page.getColorType());
        }
    }

    @Test
    public void rgbWithEqualComponentsIsTreatedAsGray() throws Exception {
        // r==g==b is a gray expressed via /rg — classifier collapses it.
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            page.getContents().add(new SetRGBColor(0.5, 0.5, 0.5));
            page.getContents().add(new Re(0, 0, 10, 10));
            page.getContents().add(new Fill());
            assertEquals(ColorType.Grayscale, page.getColorType());
        }
    }

    @Test
    public void cmykBlackIsBw() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            // C=M=Y=0, K=1 → solid black (gray=0)
            page.getContents().add(new SetCMYKColor(0, 0, 0, 1));
            page.getContents().add(new Re(0, 0, 10, 10));
            page.getContents().add(new Fill());
            assertEquals(ColorType.BlackAndWhite, page.getColorType());
        }
    }

    @Test
    public void rgbAfterGrayStillRgb() throws Exception {
        try (Document doc = new Document()) {
            Page page = doc.getPages().add();
            page.getContents().add(new SetGray(0.5));
            page.getContents().add(new Re(0, 0, 10, 10));
            page.getContents().add(new Fill());
            page.getContents().add(new SetRGBColor(1, 0, 0));
            page.getContents().add(new Re(20, 20, 10, 10));
            page.getContents().add(new Fill());
            assertEquals(ColorType.Rgb, page.getColorType());
        }
    }
}
