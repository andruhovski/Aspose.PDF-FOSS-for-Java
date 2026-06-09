package org.aspose.pdf.engine.pdfobjects;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link NumberTree}: parallel to {@link NameTreeTest} but with
 * integer keys and {@code /Nums} leaf arrays.
 */
public class NumberTreeTest {

    // ────────────────────────────────────────────────────────────────────
    //  Read paths
    // ────────────────────────────────────────────────────────────────────

    @Test
    public void getReturnsValueFromSingleLeafRoot() {
        PdfDictionary root = leaf(1, "one", 5, "five", 9, "nine");
        NumberTree tree = new NumberTree(root);

        assertEquals("one", string(tree.get(1)));
        assertEquals("five", string(tree.get(5)));
        assertEquals("nine", string(tree.get(9)));
        assertNull(tree.get(42));
    }

    @Test
    public void getDescendsKidsAndUsesLimitsPruning() {
        PdfDictionary low = leafWithLimits(0, 9, 0, "zero", 5, "five", 9, "nine");
        PdfDictionary high = leafWithLimits(100, 200, 100, "hundred", 200, "two-hundred");
        PdfDictionary root = intermediate(low, high);
        NumberTree tree = new NumberTree(root);

        assertEquals("hundred", string(tree.get(100)));
        assertEquals("nine", string(tree.get(9)));
        assertNull(tree.get(50));
    }

    @Test
    public void entriesReturnsAllEntries() {
        PdfDictionary low = leafWithLimits(0, 5, 0, "zero", 5, "five");
        PdfDictionary high = leafWithLimits(10, 20, 10, "ten", 20, "twenty");
        PdfDictionary root = intermediate(low, high);

        List<Map.Entry<Integer, PdfBase>> entries = new NumberTree(root).entries();
        assertEquals(4, entries.size());
        assertEquals(0, entries.get(0).getKey().intValue());
        assertEquals(20, entries.get(3).getKey().intValue());
    }

    @Test
    public void emptyTreeBehavesGracefully() {
        NumberTree empty = new NumberTree(null);
        assertTrue(empty.isEmpty());
        assertNull(empty.get(1));
        assertTrue(empty.entries().isEmpty());
    }

    // ────────────────────────────────────────────────────────────────────
    //  Write paths
    // ────────────────────────────────────────────────────────────────────

    @Test
    public void putKeepsLeafNumsSorted() {
        PdfDictionary root = new PdfDictionary();
        NumberTree tree = new NumberTree(root);

        tree.put(5, new PdfString("five"));
        tree.put(1, new PdfString("one"));
        tree.put(3, new PdfString("three"));

        List<Map.Entry<Integer, PdfBase>> entries = tree.entries();
        assertEquals(3, entries.size());
        assertEquals(1, entries.get(0).getKey().intValue());
        assertEquals(3, entries.get(1).getKey().intValue());
        assertEquals(5, entries.get(2).getKey().intValue());
    }

    @Test
    public void putReplacesExistingValue() {
        PdfDictionary root = new PdfDictionary();
        NumberTree tree = new NumberTree(root);

        assertNull(tree.put(1, new PdfString("v1")));
        PdfBase prev = tree.put(1, new PdfString("v2"));
        assertEquals("v1", string(prev));
        assertEquals("v2", string(tree.get(1)));
    }

    @Test
    public void putOnRootLeafKeepsNoLimits() {
        PdfDictionary root = new PdfDictionary();
        new NumberTree(root).put(1, new PdfString("x"));
        assertNull(root.get("Limits"));
    }

    @Test
    public void putExtendsLeafAndUpdatesLimits() {
        PdfDictionary low = leafWithLimits(0, 5, 0, "zero", 5, "five");
        PdfDictionary high = leafWithLimits(100, 200, 100, "hundred", 200, "two-hundred");
        PdfDictionary root = intermediate(low, high);
        NumberTree tree = new NumberTree(root);

        // 7 falls between the two leaves — closest leaf below is `low`.
        tree.put(7, new PdfString("seven"));

        PdfArray limits = (PdfArray) low.get("Limits");
        assertEquals(0, ((PdfInteger) limits.get(0)).intValue());
        assertEquals(7, ((PdfInteger) limits.get(1)).intValue());

        PdfArray nums = (PdfArray) low.get("Nums");
        assertEquals(0, ((PdfInteger) nums.get(0)).intValue());
        assertEquals(5, ((PdfInteger) nums.get(2)).intValue());
        assertEquals(7, ((PdfInteger) nums.get(4)).intValue());
    }

    @Test
    public void removeShrinksLimits() {
        PdfDictionary leaf = leafWithLimits(1, 3, 1, "a", 2, "b", 3, "c");
        PdfDictionary root = intermediate(leaf);
        NumberTree tree = new NumberTree(root);

        tree.remove(3);

        PdfArray limits = (PdfArray) leaf.get("Limits");
        assertEquals(1, ((PdfInteger) limits.get(0)).intValue());
        assertEquals(2, ((PdfInteger) limits.get(1)).intValue());
    }

    @Test
    public void clearAndReinsertWorks() {
        PdfDictionary root = intermediate(leafWithLimits(1, 2, 1, "a", 2, "b"));
        NumberTree tree = new NumberTree(root);
        tree.clear();
        assertTrue(tree.isEmpty());

        tree.put(42, new PdfString("answer"));
        assertEquals("answer", string(tree.get(42)));
    }

    // ────────────────────────────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────────────────────────────

    private static PdfDictionary leaf(Object... kv) {
        PdfDictionary dict = new PdfDictionary();
        PdfArray nums = new PdfArray();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            nums.add(PdfInteger.valueOf((Integer) kv[i]));
            nums.add(new PdfString((String) kv[i + 1]));
        }
        dict.set("Nums", nums);
        return dict;
    }

    private static PdfDictionary leafWithLimits(int min, int max, Object... kv) {
        PdfDictionary dict = leaf(kv);
        PdfArray limits = new PdfArray();
        limits.add(PdfInteger.valueOf(min));
        limits.add(PdfInteger.valueOf(max));
        dict.set("Limits", limits);
        return dict;
    }

    private static PdfDictionary intermediate(PdfDictionary... kids) {
        PdfDictionary dict = new PdfDictionary();
        PdfArray arr = new PdfArray();
        for (PdfDictionary k : kids) arr.add(k);
        dict.set("Kids", arr);
        return dict;
    }

    private static String string(PdfBase value) {
        return value == null ? null : ((PdfString) value).getString();
    }
}
