package org.aspose.pdf.engine.cos;

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
        COSDictionary root = leaf(1, "one", 5, "five", 9, "nine");
        NumberTree tree = new NumberTree(root);

        assertEquals("one", string(tree.get(1)));
        assertEquals("five", string(tree.get(5)));
        assertEquals("nine", string(tree.get(9)));
        assertNull(tree.get(42));
    }

    @Test
    public void getDescendsKidsAndUsesLimitsPruning() {
        COSDictionary low = leafWithLimits(0, 9, 0, "zero", 5, "five", 9, "nine");
        COSDictionary high = leafWithLimits(100, 200, 100, "hundred", 200, "two-hundred");
        COSDictionary root = intermediate(low, high);
        NumberTree tree = new NumberTree(root);

        assertEquals("hundred", string(tree.get(100)));
        assertEquals("nine", string(tree.get(9)));
        assertNull(tree.get(50));
    }

    @Test
    public void entriesReturnsAllEntries() {
        COSDictionary low = leafWithLimits(0, 5, 0, "zero", 5, "five");
        COSDictionary high = leafWithLimits(10, 20, 10, "ten", 20, "twenty");
        COSDictionary root = intermediate(low, high);

        List<Map.Entry<Integer, COSBase>> entries = new NumberTree(root).entries();
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
        COSDictionary root = new COSDictionary();
        NumberTree tree = new NumberTree(root);

        tree.put(5, new COSString("five"));
        tree.put(1, new COSString("one"));
        tree.put(3, new COSString("three"));

        List<Map.Entry<Integer, COSBase>> entries = tree.entries();
        assertEquals(3, entries.size());
        assertEquals(1, entries.get(0).getKey().intValue());
        assertEquals(3, entries.get(1).getKey().intValue());
        assertEquals(5, entries.get(2).getKey().intValue());
    }

    @Test
    public void putReplacesExistingValue() {
        COSDictionary root = new COSDictionary();
        NumberTree tree = new NumberTree(root);

        assertNull(tree.put(1, new COSString("v1")));
        COSBase prev = tree.put(1, new COSString("v2"));
        assertEquals("v1", string(prev));
        assertEquals("v2", string(tree.get(1)));
    }

    @Test
    public void putOnRootLeafKeepsNoLimits() {
        COSDictionary root = new COSDictionary();
        new NumberTree(root).put(1, new COSString("x"));
        assertNull(root.get("Limits"));
    }

    @Test
    public void putExtendsLeafAndUpdatesLimits() {
        COSDictionary low = leafWithLimits(0, 5, 0, "zero", 5, "five");
        COSDictionary high = leafWithLimits(100, 200, 100, "hundred", 200, "two-hundred");
        COSDictionary root = intermediate(low, high);
        NumberTree tree = new NumberTree(root);

        // 7 falls between the two leaves — closest leaf below is `low`.
        tree.put(7, new COSString("seven"));

        COSArray limits = (COSArray) low.get("Limits");
        assertEquals(0, ((COSInteger) limits.get(0)).intValue());
        assertEquals(7, ((COSInteger) limits.get(1)).intValue());

        COSArray nums = (COSArray) low.get("Nums");
        assertEquals(0, ((COSInteger) nums.get(0)).intValue());
        assertEquals(5, ((COSInteger) nums.get(2)).intValue());
        assertEquals(7, ((COSInteger) nums.get(4)).intValue());
    }

    @Test
    public void removeShrinksLimits() {
        COSDictionary leaf = leafWithLimits(1, 3, 1, "a", 2, "b", 3, "c");
        COSDictionary root = intermediate(leaf);
        NumberTree tree = new NumberTree(root);

        tree.remove(3);

        COSArray limits = (COSArray) leaf.get("Limits");
        assertEquals(1, ((COSInteger) limits.get(0)).intValue());
        assertEquals(2, ((COSInteger) limits.get(1)).intValue());
    }

    @Test
    public void clearAndReinsertWorks() {
        COSDictionary root = intermediate(leafWithLimits(1, 2, 1, "a", 2, "b"));
        NumberTree tree = new NumberTree(root);
        tree.clear();
        assertTrue(tree.isEmpty());

        tree.put(42, new COSString("answer"));
        assertEquals("answer", string(tree.get(42)));
    }

    // ────────────────────────────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────────────────────────────

    private static COSDictionary leaf(Object... kv) {
        COSDictionary dict = new COSDictionary();
        COSArray nums = new COSArray();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            nums.add(COSInteger.valueOf((Integer) kv[i]));
            nums.add(new COSString((String) kv[i + 1]));
        }
        dict.set("Nums", nums);
        return dict;
    }

    private static COSDictionary leafWithLimits(int min, int max, Object... kv) {
        COSDictionary dict = leaf(kv);
        COSArray limits = new COSArray();
        limits.add(COSInteger.valueOf(min));
        limits.add(COSInteger.valueOf(max));
        dict.set("Limits", limits);
        return dict;
    }

    private static COSDictionary intermediate(COSDictionary... kids) {
        COSDictionary dict = new COSDictionary();
        COSArray arr = new COSArray();
        for (COSDictionary k : kids) arr.add(k);
        dict.set("Kids", arr);
        return dict;
    }

    private static String string(COSBase value) {
        return value == null ? null : ((COSString) value).getString();
    }
}
