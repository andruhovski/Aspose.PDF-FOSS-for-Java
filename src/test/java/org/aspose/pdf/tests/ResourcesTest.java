package org.aspose.pdf.tests;

import org.aspose.pdf.Resources;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectKey;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for [Resources].
public class ResourcesTest {

    @Test
    public void constructorRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Resources(null));
    }

    @Test
    public void getFontsReturnsFontDictionary() {
        PdfDictionary fontDict = new PdfDictionary();
        fontDict.set("F1", PdfName.of("Helvetica"));

        PdfDictionary resourceDict = new PdfDictionary();
        resourceDict.set("Font", fontDict);

        Resources resources = new Resources(resourceDict);
        PdfDictionary result = resources.getFonts();
        assertNotNull(result);
        assertSame(fontDict, result);
    }

    @Test
    public void getFontsReturnsNullWhenAbsent() {
        Resources resources = new Resources(new PdfDictionary());
        assertNull(resources.getFonts());
    }

    @Test
    public void getXObjectsReturnsXObjectDictionary() {
        PdfDictionary xobjDict = new PdfDictionary();
        xobjDict.set("Im1", PdfName.of("Image"));

        PdfDictionary resourceDict = new PdfDictionary();
        resourceDict.set("XObject", xobjDict);

        Resources resources = new Resources(resourceDict);
        assertSame(xobjDict, resources.getXObjects());
    }

    @Test
    public void getExtGStateReturnsDictionary() {
        PdfDictionary gsDict = new PdfDictionary();
        PdfDictionary resourceDict = new PdfDictionary();
        resourceDict.set("ExtGState", gsDict);

        Resources resources = new Resources(resourceDict);
        assertSame(gsDict, resources.getExtGState());
    }

    @Test
    public void getColorSpacesReturnsDictionary() {
        PdfDictionary csDict = new PdfDictionary();
        PdfDictionary resourceDict = new PdfDictionary();
        resourceDict.set("ColorSpace", csDict);

        Resources resources = new Resources(resourceDict);
        assertSame(csDict, resources.getColorSpaces());
    }

    @Test
    public void getPatternsReturnsDictionary() {
        PdfDictionary patDict = new PdfDictionary();
        PdfDictionary resourceDict = new PdfDictionary();
        resourceDict.set("Pattern", patDict);

        Resources resources = new Resources(resourceDict);
        assertSame(patDict, resources.getPatterns());
    }

    @Test
    public void getShadingsReturnsDictionary() {
        PdfDictionary shadDict = new PdfDictionary();
        PdfDictionary resourceDict = new PdfDictionary();
        resourceDict.set("Shading", shadDict);

        Resources resources = new Resources(resourceDict);
        assertSame(shadDict, resources.getShadings());
    }

    @Test
    public void getPropertiesReturnsDictionary() {
        PdfDictionary propDict = new PdfDictionary();
        PdfDictionary resourceDict = new PdfDictionary();
        resourceDict.set("Properties", propDict);

        Resources resources = new Resources(resourceDict);
        assertSame(propDict, resources.getProperties());
    }

    @Test
    public void getPdfDictionaryReturnsUnderlying() {
        PdfDictionary resourceDict = new PdfDictionary();
        Resources resources = new Resources(resourceDict);
        assertSame(resourceDict, resources.getPdfDictionary());
    }

    @Test
    public void dereferencesIndirectReference() {
        PdfDictionary fontDict = new PdfDictionary();
        fontDict.set("F1", PdfName.of("Times"));

        PdfObjectKey key = new PdfObjectKey(5, 0);
        PdfObjectReference ref = new PdfObjectReference(key, k -> fontDict);

        PdfDictionary resourceDict = new PdfDictionary();
        resourceDict.set(PdfName.of("Font"), ref);

        Resources resources = new Resources(resourceDict);
        PdfDictionary result = resources.getFonts();
        assertNotNull(result);
        assertSame(fontDict, result);
    }

    @Test
    public void returnsNullForNonDictionaryValue() {
        PdfDictionary resourceDict = new PdfDictionary();
        resourceDict.set("Font", PdfName.of("NotADict"));

        Resources resources = new Resources(resourceDict);
        assertNull(resources.getFonts());
    }

    @Test
    public void allGettersReturnNullForEmptyDict() {
        Resources resources = new Resources(new PdfDictionary());
        assertNull(resources.getFonts());
        assertNull(resources.getXObjects());
        assertNull(resources.getExtGState());
        assertNull(resources.getColorSpaces());
        assertNull(resources.getPatterns());
        assertNull(resources.getShadings());
        assertNull(resources.getProperties());
    }
}
