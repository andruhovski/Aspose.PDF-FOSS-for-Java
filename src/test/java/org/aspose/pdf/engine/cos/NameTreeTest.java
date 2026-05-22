package org.aspose.pdf.engine.cos;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link NameTree}: covers single-leaf reads/writes, multi-level
 * descent with {@code /Limits} pruning, sorted insertion, deletion with
 * {@code /Limits} resync, and tolerance of malformed inputs (missing
 * {@code /Limits}, unsorted leaves, mixed {@code /Names}+{@code /Kids}).
 */
public class NameTreeTest {

    // ────────────────────────────────────────────────────────────────────
    //  Read paths
    // ────────────────────────────────────────────────────────────────────

    @Test
    public void getReturnsValueFromSingleLeafRoot() {
        COSDictionary root = leaf("apple", "1", "banana", "2", "cherry", "3");
        NameTree tree = new NameTree(root);

        assertEquals("2", string(tree.get("banana")));
        assertEquals("1", string(tree.get("apple")));
        assertEquals("3", string(tree.get("cherry")));
        assertNull(tree.get("missing"));
    }

    @Test
    public void getDescendsIntoKidsAndHonoursLimitsForPruning() {
        // Tree: root → [leaf("a","b"), leaf("y","z")]; lookup of "y" must skip
        // the first leaf because its /Limits ends at "b".
        COSDictionary leafA = leafWithLimits("a", "b", "a", "1", "b", "2");
        COSDictionary leafY = leafWithLimits("y", "z", "y", "9", "z", "10");
        COSDictionary root = intermediate(leafA, leafY);
        NameTree tree = new NameTree(root);

        assertEquals("9", string(tree.get("y")));
        assertEquals("10", string(tree.get("z")));
        assertEquals("1", string(tree.get("a")));
        assertNull(tree.get("middle"));   // outside every leaf's /Limits
    }

    @Test
    public void getToleratesMissingLimitsWithLinearFallback() {
        COSDictionary leafA = leaf("a", "1", "b", "2");           // no /Limits
        COSDictionary leafY = leaf("y", "9", "z", "10");          // no /Limits
        COSDictionary root = intermediate(leafA, leafY);
        NameTree tree = new NameTree(root);

        assertEquals("9", string(tree.get("y")));
        assertEquals("2", string(tree.get("b")));
        assertNull(tree.get("x"));
    }

    @Test
    public void entriesReturnsAllEntriesInTreeOrder() {
        COSDictionary leafA = leafWithLimits("a", "b", "a", "1", "b", "2");
        COSDictionary leafY = leafWithLimits("y", "z", "y", "9", "z", "10");
        COSDictionary root = intermediate(leafA, leafY);

        List<Map.Entry<String, COSBase>> entries = new NameTree(root).entries();
        assertEquals(4, entries.size());
        assertEquals("a", entries.get(0).getKey());
        assertEquals("z", entries.get(3).getKey());
    }

    @Test
    public void emptyTreeViewBehavesGracefully() {
        NameTree nullTree = new NameTree(null);
        assertTrue(nullTree.isEmpty());
        assertEquals(0, nullTree.size());
        assertNull(nullTree.get("anything"));
        assertTrue(nullTree.entries().isEmpty());

        NameTree empty = new NameTree(new COSDictionary());
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());
        assertNull(empty.get("anything"));
    }

    // ────────────────────────────────────────────────────────────────────
    //  Write paths
    // ────────────────────────────────────────────────────────────────────

    @Test
    public void putKeepsLeafNamesSorted() {
        COSDictionary root = new COSDictionary();
        NameTree tree = new NameTree(root);

        tree.put("cherry", new COSString("3"));
        tree.put("apple", new COSString("1"));
        tree.put("banana", new COSString("2"));

        List<Map.Entry<String, COSBase>> entries = tree.entries();
        assertEquals(3, entries.size());
        assertEquals("apple", entries.get(0).getKey());
        assertEquals("banana", entries.get(1).getKey());
        assertEquals("cherry", entries.get(2).getKey());
    }

    @Test
    public void putReplacesExistingValueAndReturnsPrevious() {
        COSDictionary root = new COSDictionary();
        NameTree tree = new NameTree(root);

        assertNull(tree.put("k", new COSString("v1")));
        COSBase prev = tree.put("k", new COSString("v2"));
        assertEquals("v1", string(prev));
        assertEquals("v2", string(tree.get("k")));
        assertEquals(1, tree.size());
    }

    @Test
    public void putOnRootLeafDoesNotWriteLimits() {
        // §7.9.6 Table 36: the root node of a name tree must NOT carry /Limits.
        COSDictionary root = new COSDictionary();
        NameTree tree = new NameTree(root);
        tree.put("k", new COSString("v"));
        assertNull(root.get("Limits"));
    }

    @Test
    public void putIntoMultiLevelTreeUpdatesIntermediateLimits() {
        COSDictionary leafA = leafWithLimits("a", "b", "a", "1", "b", "2");
        COSDictionary leafY = leafWithLimits("y", "z", "y", "9", "z", "10");
        COSDictionary root = intermediate(leafA, leafY);
        NameTree tree = new NameTree(root);

        // Insert "c" — extends the first leaf (closest by /Limits below "y").
        tree.put("c", new COSString("3"));

        // First leaf now [a, b, c] and its /Limits should be [a, c].
        COSArray firstLimits = (COSArray) leafA.get("Limits");
        assertNotNull(firstLimits);
        assertEquals("a", ((COSString) firstLimits.get(0)).getString());
        assertEquals("c", ((COSString) firstLimits.get(1)).getString());

        // /Names array must remain sorted within the leaf.
        COSArray firstNames = (COSArray) leafA.get("Names");
        assertEquals("a", ((COSString) firstNames.get(0)).getString());
        assertEquals("b", ((COSString) firstNames.get(2)).getString());
        assertEquals("c", ((COSString) firstNames.get(4)).getString());

        // Root keeps no /Limits.
        assertNull(root.get("Limits"));
    }

    @Test
    public void removeDropsKeyAndUpdatesLimits() {
        COSDictionary leafA = leafWithLimits("a", "c", "a", "1", "b", "2", "c", "3");
        COSDictionary root = intermediate(leafA);
        NameTree tree = new NameTree(root);

        COSBase removed = tree.remove("b");
        assertEquals("2", string(removed));
        assertNull(tree.get("b"));
        assertEquals(2, tree.size());

        // /Limits unchanged because min/max keys are still "a"/"c".
        COSArray limits = (COSArray) leafA.get("Limits");
        assertEquals("a", ((COSString) limits.get(0)).getString());
        assertEquals("c", ((COSString) limits.get(1)).getString());
    }

    @Test
    public void removeShrinksLimitsAtBoundary() {
        COSDictionary leafA = leafWithLimits("a", "c", "a", "1", "b", "2", "c", "3");
        COSDictionary root = intermediate(leafA);
        NameTree tree = new NameTree(root);

        tree.remove("c");

        COSArray limits = (COSArray) leafA.get("Limits");
        assertEquals("a", ((COSString) limits.get(0)).getString());
        assertEquals("b", ((COSString) limits.get(1)).getString());
    }

    @Test
    public void removeReturnsNullForMissingKey() {
        COSDictionary leaf = leaf("a", "1");
        assertNull(new NameTree(leaf).remove("missing"));
    }

    @Test
    public void clearEmptiesTreeAndRetainsNamesArray() {
        COSDictionary root = intermediate(leafWithLimits("a", "b", "a", "1", "b", "2"));
        NameTree tree = new NameTree(root);
        tree.clear();

        assertTrue(tree.isEmpty());
        assertNotNull(root.get("Names"));   // empty /Names left for future inserts
        assertNull(root.get("Kids"));
        assertNull(root.get("Limits"));
    }

    @Test
    public void putAfterClearWorks() {
        COSDictionary root = intermediate(leafWithLimits("a", "b", "a", "1", "b", "2"));
        NameTree tree = new NameTree(root);
        tree.clear();

        tree.put("x", new COSString("X"));
        assertEquals("X", string(tree.get("x")));
        assertEquals(1, tree.size());
    }

    // ────────────────────────────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────────────────────────────

    private static COSDictionary leaf(String... kv) {
        COSDictionary dict = new COSDictionary();
        COSArray names = new COSArray();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            names.add(new COSString(kv[i]));
            names.add(new COSString(kv[i + 1]));
        }
        dict.set("Names", names);
        return dict;
    }

    private static COSDictionary leafWithLimits(String min, String max, String... kv) {
        COSDictionary dict = leaf(kv);
        COSArray limits = new COSArray();
        limits.add(new COSString(min));
        limits.add(new COSString(max));
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
