package org.aspose.pdf.tests;

import org.aspose.pdf.Resources;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSObjectKey;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Resources}.
 */
public class ResourcesTest {

    @Test
    public void constructorRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Resources(null));
    }

    @Test
    public void getFontsReturnsFontDictionary() {
        COSDictionary fontDict = new COSDictionary();
        fontDict.set("F1", COSName.of("Helvetica"));

        COSDictionary resourceDict = new COSDictionary();
        resourceDict.set("Font", fontDict);

        Resources resources = new Resources(resourceDict);
        COSDictionary result = resources.getFonts();
        assertNotNull(result);
        assertSame(fontDict, result);
    }

    @Test
    public void getFontsReturnsNullWhenAbsent() {
        Resources resources = new Resources(new COSDictionary());
        assertNull(resources.getFonts());
    }

    @Test
    public void getXObjectsReturnsXObjectDictionary() {
        COSDictionary xobjDict = new COSDictionary();
        xobjDict.set("Im1", COSName.of("Image"));

        COSDictionary resourceDict = new COSDictionary();
        resourceDict.set("XObject", xobjDict);

        Resources resources = new Resources(resourceDict);
        assertSame(xobjDict, resources.getXObjects());
    }

    @Test
    public void getExtGStateReturnsDictionary() {
        COSDictionary gsDict = new COSDictionary();
        COSDictionary resourceDict = new COSDictionary();
        resourceDict.set("ExtGState", gsDict);

        Resources resources = new Resources(resourceDict);
        assertSame(gsDict, resources.getExtGState());
    }

    @Test
    public void getColorSpacesReturnsDictionary() {
        COSDictionary csDict = new COSDictionary();
        COSDictionary resourceDict = new COSDictionary();
        resourceDict.set("ColorSpace", csDict);

        Resources resources = new Resources(resourceDict);
        assertSame(csDict, resources.getColorSpaces());
    }

    @Test
    public void getPatternsReturnsDictionary() {
        COSDictionary patDict = new COSDictionary();
        COSDictionary resourceDict = new COSDictionary();
        resourceDict.set("Pattern", patDict);

        Resources resources = new Resources(resourceDict);
        assertSame(patDict, resources.getPatterns());
    }

    @Test
    public void getShadingsReturnsDictionary() {
        COSDictionary shadDict = new COSDictionary();
        COSDictionary resourceDict = new COSDictionary();
        resourceDict.set("Shading", shadDict);

        Resources resources = new Resources(resourceDict);
        assertSame(shadDict, resources.getShadings());
    }

    @Test
    public void getPropertiesReturnsDictionary() {
        COSDictionary propDict = new COSDictionary();
        COSDictionary resourceDict = new COSDictionary();
        resourceDict.set("Properties", propDict);

        Resources resources = new Resources(resourceDict);
        assertSame(propDict, resources.getProperties());
    }

    @Test
    public void getCOSDictionaryReturnsUnderlying() {
        COSDictionary resourceDict = new COSDictionary();
        Resources resources = new Resources(resourceDict);
        assertSame(resourceDict, resources.getCOSDictionary());
    }

    @Test
    public void dereferencesIndirectReference() {
        COSDictionary fontDict = new COSDictionary();
        fontDict.set("F1", COSName.of("Times"));

        COSObjectKey key = new COSObjectKey(5, 0);
        COSObjectReference ref = new COSObjectReference(key, k -> fontDict);

        COSDictionary resourceDict = new COSDictionary();
        resourceDict.set(COSName.of("Font"), ref);

        Resources resources = new Resources(resourceDict);
        COSDictionary result = resources.getFonts();
        assertNotNull(result);
        assertSame(fontDict, result);
    }

    @Test
    public void returnsNullForNonDictionaryValue() {
        COSDictionary resourceDict = new COSDictionary();
        resourceDict.set("Font", COSName.of("NotADict"));

        Resources resources = new Resources(resourceDict);
        assertNull(resources.getFonts());
    }

    @Test
    public void allGettersReturnNullForEmptyDict() {
        Resources resources = new Resources(new COSDictionary());
        assertNull(resources.getFonts());
        assertNull(resources.getXObjects());
        assertNull(resources.getExtGState());
        assertNull(resources.getColorSpaces());
        assertNull(resources.getPatterns());
        assertNull(resources.getShadings());
        assertNull(resources.getProperties());
    }
}
