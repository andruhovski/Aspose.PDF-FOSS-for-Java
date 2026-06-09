package org.aspose.pdf.engine.pdfobjects;

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
        PdfDictionary root = leaf("apple", "1", "banana", "2", "cherry", "3");
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
        PdfDictionary leafA = leafWithLimits("a", "b", "a", "1", "b", "2");
        PdfDictionary leafY = leafWithLimits("y", "z", "y", "9", "z", "10");
        PdfDictionary root = intermediate(leafA, leafY);
        NameTree tree = new NameTree(root);

        assertEquals("9", string(tree.get("y")));
        assertEquals("10", string(tree.get("z")));
        assertEquals("1", string(tree.get("a")));
        assertNull(tree.get("middle"));   // outside every leaf's /Limits
    }

    @Test
    public void getToleratesMissingLimitsWithLinearFallback() {
        PdfDictionary leafA = leaf("a", "1", "b", "2");           // no /Limits
        PdfDictionary leafY = leaf("y", "9", "z", "10");          // no /Limits
        PdfDictionary root = intermediate(leafA, leafY);
        NameTree tree = new NameTree(root);

        assertEquals("9", string(tree.get("y")));
        assertEquals("2", string(tree.get("b")));
        assertNull(tree.get("x"));
    }

    @Test
    public void entriesReturnsAllEntriesInTreeOrder() {
        PdfDictionary leafA = leafWithLimits("a", "b", "a", "1", "b", "2");
        PdfDictionary leafY = leafWithLimits("y", "z", "y", "9", "z", "10");
        PdfDictionary root = intermediate(leafA, leafY);

        List<Map.Entry<String, PdfBase>> entries = new NameTree(root).entries();
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

        NameTree empty = new NameTree(new PdfDictionary());
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());
        assertNull(empty.get("anything"));
    }

    // ────────────────────────────────────────────────────────────────────
    //  Write paths
    // ────────────────────────────────────────────────────────────────────

    @Test
    public void putKeepsLeafNamesSorted() {
        PdfDictionary root = new PdfDictionary();
        NameTree tree = new NameTree(root);

        tree.put("cherry", new PdfString("3"));
        tree.put("apple", new PdfString("1"));
        tree.put("banana", new PdfString("2"));

        List<Map.Entry<String, PdfBase>> entries = tree.entries();
        assertEquals(3, entries.size());
        assertEquals("apple", entries.get(0).getKey());
        assertEquals("banana", entries.get(1).getKey());
        assertEquals("cherry", entries.get(2).getKey());
    }

    @Test
    public void putReplacesExistingValueAndReturnsPrevious() {
        PdfDictionary root = new PdfDictionary();
        NameTree tree = new NameTree(root);

        assertNull(tree.put("k", new PdfString("v1")));
        PdfBase prev = tree.put("k", new PdfString("v2"));
        assertEquals("v1", string(prev));
        assertEquals("v2", string(tree.get("k")));
        assertEquals(1, tree.size());
    }

    @Test
    public void putOnRootLeafDoesNotWriteLimits() {
        // §7.9.6 Table 36: the root node of a name tree must NOT carry /Limits.
        PdfDictionary root = new PdfDictionary();
        NameTree tree = new NameTree(root);
        tree.put("k", new PdfString("v"));
        assertNull(root.get("Limits"));
    }

    @Test
    public void putIntoMultiLevelTreeUpdatesIntermediateLimits() {
        PdfDictionary leafA = leafWithLimits("a", "b", "a", "1", "b", "2");
        PdfDictionary leafY = leafWithLimits("y", "z", "y", "9", "z", "10");
        PdfDictionary root = intermediate(leafA, leafY);
        NameTree tree = new NameTree(root);

        // Insert "c" — extends the first leaf (closest by /Limits below "y").
        tree.put("c", new PdfString("3"));

        // First leaf now [a, b, c] and its /Limits should be [a, c].
        PdfArray firstLimits = (PdfArray) leafA.get("Limits");
        assertNotNull(firstLimits);
        assertEquals("a", ((PdfString) firstLimits.get(0)).getString());
        assertEquals("c", ((PdfString) firstLimits.get(1)).getString());

        // /Names array must remain sorted within the leaf.
        PdfArray firstNames = (PdfArray) leafA.get("Names");
        assertEquals("a", ((PdfString) firstNames.get(0)).getString());
        assertEquals("b", ((PdfString) firstNames.get(2)).getString());
        assertEquals("c", ((PdfString) firstNames.get(4)).getString());

        // Root keeps no /Limits.
        assertNull(root.get("Limits"));
    }

    @Test
    public void removeDropsKeyAndUpdatesLimits() {
        PdfDictionary leafA = leafWithLimits("a", "c", "a", "1", "b", "2", "c", "3");
        PdfDictionary root = intermediate(leafA);
        NameTree tree = new NameTree(root);

        PdfBase removed = tree.remove("b");
        assertEquals("2", string(removed));
        assertNull(tree.get("b"));
        assertEquals(2, tree.size());

        // /Limits unchanged because min/max keys are still "a"/"c".
        PdfArray limits = (PdfArray) leafA.get("Limits");
        assertEquals("a", ((PdfString) limits.get(0)).getString());
        assertEquals("c", ((PdfString) limits.get(1)).getString());
    }

    @Test
    public void removeShrinksLimitsAtBoundary() {
        PdfDictionary leafA = leafWithLimits("a", "c", "a", "1", "b", "2", "c", "3");
        PdfDictionary root = intermediate(leafA);
        NameTree tree = new NameTree(root);

        tree.remove("c");

        PdfArray limits = (PdfArray) leafA.get("Limits");
        assertEquals("a", ((PdfString) limits.get(0)).getString());
        assertEquals("b", ((PdfString) limits.get(1)).getString());
    }

    @Test
    public void removeReturnsNullForMissingKey() {
        PdfDictionary leaf = leaf("a", "1");
        assertNull(new NameTree(leaf).remove("missing"));
    }

    @Test
    public void clearEmptiesTreeAndRetainsNamesArray() {
        PdfDictionary root = intermediate(leafWithLimits("a", "b", "a", "1", "b", "2"));
        NameTree tree = new NameTree(root);
        tree.clear();

        assertTrue(tree.isEmpty());
        assertNotNull(root.get("Names"));   // empty /Names left for future inserts
        assertNull(root.get("Kids"));
        assertNull(root.get("Limits"));
    }

    @Test
    public void putAfterClearWorks() {
        PdfDictionary root = intermediate(leafWithLimits("a", "b", "a", "1", "b", "2"));
        NameTree tree = new NameTree(root);
        tree.clear();

        tree.put("x", new PdfString("X"));
        assertEquals("X", string(tree.get("x")));
        assertEquals(1, tree.size());
    }

    // ────────────────────────────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────────────────────────────

    private static PdfDictionary leaf(String... kv) {
        PdfDictionary dict = new PdfDictionary();
        PdfArray names = new PdfArray();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            names.add(new PdfString(kv[i]));
            names.add(new PdfString(kv[i + 1]));
        }
        dict.set("Names", names);
        return dict;
    }

    private static PdfDictionary leafWithLimits(String min, String max, String... kv) {
        PdfDictionary dict = leaf(kv);
        PdfArray limits = new PdfArray();
        limits.add(new PdfString(min));
        limits.add(new PdfString(max));
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
