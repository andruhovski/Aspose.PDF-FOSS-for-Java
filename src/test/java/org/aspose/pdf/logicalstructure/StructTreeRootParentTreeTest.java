package org.aspose.pdf.logicalstructure;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.NumberTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies {@link StructTreeRoot#getParentTree()} and the convenience
 * helpers built on top of it. Mirrors a minimal tagged-PDF /ParentTree
 * shape (§14.7.4.4):
 * <pre>
 *   /ParentTree &lt;&lt;
 *     /Nums [
 *       0  [ &lt;structElemForMcid0&gt; &lt;structElemForMcid1&gt; ]
 *       1  &lt;structElemForAnnotation&gt;
 *     ]
 *   &gt;&gt;
 * </pre>
 */
public class StructTreeRootParentTreeTest {

    @Test
    public void getParentTreeReturnsNullWhenAbsent() {
        StructTreeRoot root = new StructTreeRoot(new COSDictionary(), null);
        assertNull(root.getParentTree());
    }

    @Test
    public void getParentTreeReadsExistingNumberTree() {
        COSDictionary structElem = makeStructElem("P");

        COSArray pageEntry = new COSArray();
        pageEntry.add(structElem);
        pageEntry.add(structElem);

        COSDictionary parentTreeDict = new COSDictionary();
        COSArray nums = new COSArray();
        nums.add(COSInteger.valueOf(0));
        nums.add(pageEntry);
        nums.add(COSInteger.valueOf(1));
        nums.add(structElem);
        parentTreeDict.set(COSName.of("Nums"), nums);

        COSDictionary stRoot = new COSDictionary();
        stRoot.set(COSName.of("ParentTree"), parentTreeDict);

        StructTreeRoot root = new StructTreeRoot(stRoot, null);
        NumberTree tree = root.getParentTree();
        assertNotNull(tree);
        assertEquals(2, tree.size());

        COSBase entry0 = tree.get(0);
        assertTrue(entry0 instanceof COSArray);
        assertEquals(2, ((COSArray) entry0).size());

        COSBase entry1 = tree.get(1);
        assertTrue(entry1 instanceof COSDictionary);
    }

    @Test
    public void findElementByMcidResolvesArrayEntry() {
        COSDictionary firstElem = makeStructElem("H1");
        COSDictionary secondElem = makeStructElem("P");

        COSArray pageEntry = new COSArray();
        pageEntry.add(firstElem);
        pageEntry.add(secondElem);

        COSDictionary parentTreeDict = new COSDictionary();
        COSArray nums = new COSArray();
        nums.add(COSInteger.valueOf(0));
        nums.add(pageEntry);
        parentTreeDict.set(COSName.of("Nums"), nums);

        COSDictionary stRoot = new COSDictionary();
        stRoot.set(COSName.of("ParentTree"), parentTreeDict);

        StructTreeRoot root = new StructTreeRoot(stRoot, null);

        StructureElement zero = root.findElementByMcid(0, 0);
        StructureElement one = root.findElementByMcid(0, 1);
        assertNotNull(zero);
        assertNotNull(one);
        assertEquals("H1", zero.getCOSDictionary().get("S").toString().replace("/", ""));
        assertEquals("P", one.getCOSDictionary().get("S").toString().replace("/", ""));

        assertNull(root.findElementByMcid(0, 99));   // out of range
        assertNull(root.findElementByMcid(99, 0));   // unknown StructParents key
    }

    private static COSDictionary makeStructElem(String type) {
        COSDictionary d = new COSDictionary();
        d.set(COSName.of("Type"), COSName.of("StructElem"));
        d.set(COSName.of("S"), COSName.of(type));
        return d;
    }
}
