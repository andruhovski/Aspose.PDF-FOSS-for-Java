package org.aspose.pdf.logicalstructure;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfInteger;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.NumberTree;
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
        StructTreeRoot root = new StructTreeRoot(new PdfDictionary(), null);
        assertNull(root.getParentTree());
    }

    @Test
    public void getParentTreeReadsExistingNumberTree() {
        PdfDictionary structElem = makeStructElem("P");

        PdfArray pageEntry = new PdfArray();
        pageEntry.add(structElem);
        pageEntry.add(structElem);

        PdfDictionary parentTreeDict = new PdfDictionary();
        PdfArray nums = new PdfArray();
        nums.add(PdfInteger.valueOf(0));
        nums.add(pageEntry);
        nums.add(PdfInteger.valueOf(1));
        nums.add(structElem);
        parentTreeDict.set(PdfName.of("Nums"), nums);

        PdfDictionary stRoot = new PdfDictionary();
        stRoot.set(PdfName.of("ParentTree"), parentTreeDict);

        StructTreeRoot root = new StructTreeRoot(stRoot, null);
        NumberTree tree = root.getParentTree();
        assertNotNull(tree);
        assertEquals(2, tree.size());

        PdfBase entry0 = tree.get(0);
        assertTrue(entry0 instanceof PdfArray);
        assertEquals(2, ((PdfArray) entry0).size());

        PdfBase entry1 = tree.get(1);
        assertTrue(entry1 instanceof PdfDictionary);
    }

    @Test
    public void findElementByMcidResolvesArrayEntry() {
        PdfDictionary firstElem = makeStructElem("H1");
        PdfDictionary secondElem = makeStructElem("P");

        PdfArray pageEntry = new PdfArray();
        pageEntry.add(firstElem);
        pageEntry.add(secondElem);

        PdfDictionary parentTreeDict = new PdfDictionary();
        PdfArray nums = new PdfArray();
        nums.add(PdfInteger.valueOf(0));
        nums.add(pageEntry);
        parentTreeDict.set(PdfName.of("Nums"), nums);

        PdfDictionary stRoot = new PdfDictionary();
        stRoot.set(PdfName.of("ParentTree"), parentTreeDict);

        StructTreeRoot root = new StructTreeRoot(stRoot, null);

        StructureElement zero = root.findElementByMcid(0, 0);
        StructureElement one = root.findElementByMcid(0, 1);
        assertNotNull(zero);
        assertNotNull(one);
        assertEquals("H1", zero.getPdfDictionary().get("S").toString().replace("/", ""));
        assertEquals("P", one.getPdfDictionary().get("S").toString().replace("/", ""));

        assertNull(root.findElementByMcid(0, 99));   // out of range
        assertNull(root.findElementByMcid(99, 0));   // unknown StructParents key
    }

    private static PdfDictionary makeStructElem(String type) {
        PdfDictionary d = new PdfDictionary();
        d.set(PdfName.of("Type"), PdfName.of("StructElem"));
        d.set(PdfName.of("S"), PdfName.of(type));
        return d;
    }
}
