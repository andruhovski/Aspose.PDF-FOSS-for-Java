package org.aspose.pdf.engine.pdfobjects;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/// Read/write view over a PDF number tree (ISO 32000-1:2008, §7.9.7).
///
/// A number tree mirrors a name tree (see [NameTree]) but uses
/// integer keys and stores leaf pairs in a `/Nums` array instead of
/// `/Names`. Examples in the spec: `/PageLabels` on the
/// catalog (§12.4.2) and `/ParentTree` on a structure tree root
/// (§14.7.4.4).
///
/// Read paths tolerate missing `/Limits` and unsorted
/// `/Nums` by falling back to a linear scan. Write paths keep
/// `/Nums` sorted by key and resync `/Limits` bottom-up after
/// every mutation. No node splitting/merging — inserts grow the leaf
/// whose range covers the key (or the closest leaf if the key is out of
/// range).
public final class NumberTree {

    private static final Logger LOG = Logger.getLogger(NumberTree.class.getName());

    private static final PdfName KIDS = PdfName.of("Kids");
    private static final PdfName NUMS = PdfName.of("Nums");
    private static final PdfName LIMITS = PdfName.of("Limits");

    private final PdfDictionary root;

    /// Wraps the given root dictionary. The dictionary is the number-tree
    /// root itself (e.g. the `/PageLabels` sub-dictionary in the
    /// catalog) — _not_ the catalog.
    ///
    /// @param root the number-tree root, or `null` for an empty view
    public NumberTree(PdfDictionary root) {
        this.root = root;
    }

    /// Returns the underlying root dictionary, or `null` if this view
    /// was constructed over a `null` root.
    ///
    /// @return the root dictionary
    public PdfDictionary getRoot() {
        return root;
    }

    /// Returns whether the tree has no entries.
    ///
    /// @return `true` when no key maps to any value
    public boolean isEmpty() {
        return root == null || countEntries(root) == 0;
    }

    /// Returns the number of (key, value) pairs in the tree.
    ///
    /// @return the entry count
    public int size() {
        return root == null ? 0 : countEntries(root);
    }

    /// Looks up a key. Uses `/Limits` pruning when present.
    ///
    /// @param key the integer key
    /// @return the associated value, or `null` if not found
    public PdfBase get(int key) {
        if (root == null) return null;
        return findInNode(root, key);
    }

    /// Returns whether the tree contains the given key.
    ///
    /// @param key the integer key
    /// @return `true` if a value is associated with `key`
    public boolean containsKey(int key) {
        return get(key) != null;
    }

    /// Returns all entries in tree order (which, for a conformant tree, is
    /// key-sorted order).
    ///
    /// @return the list of `(key, value)` entries
    public List<Map.Entry<Integer, PdfBase>> entries() {
        List<Map.Entry<Integer, PdfBase>> out = new ArrayList<>();
        if (root != null) collectEntries(root, out);
        return out;
    }

    /// Returns all keys in tree order.
    ///
    /// @return the list of integer keys
    public List<Integer> keys() {
        List<Map.Entry<Integer, PdfBase>> es = entries();
        List<Integer> out = new ArrayList<>(es.size());
        for (Map.Entry<Integer, PdfBase> e : es) out.add(e.getKey());
        return out;
    }

    /// Inserts or replaces a value. The host `/Nums` array stays
    /// sorted and `/Limits` are re-synced along the insertion path.
    ///
    /// @param key   the integer key
    /// @param value the value to associate
    /// @return the previous value bound to `key`, or `null`
    /// @throws IllegalStateException if this view wraps a `null` root
    public PdfBase put(int key, PdfBase value) {
        requireRoot();
        if (value == null) return remove(key);
        List<PdfDictionary> path = new ArrayList<>();
        PdfDictionary leaf = locateLeafForInsert(root, key, path);
        PdfBase previous = insertSorted(leaf, key, value);
        refreshLimitsAlongPath(path);
        return previous;
    }

    /// Removes a key. Empty leaves keep their (now empty) `/Nums`
    /// array — rebalancing is left to an external pass.
    ///
    /// @param key the integer key to remove
    /// @return the removed value, or `null` if the key was absent
    public PdfBase remove(int key) {
        if (root == null) return null;
        List<PdfDictionary> path = new ArrayList<>();
        PdfDictionary leaf = locateLeafForLookup(root, key, path);
        if (leaf == null) return null;
        PdfBase removed = removeFromLeaf(leaf, key);
        if (removed != null) refreshLimitsAlongPath(path);
        return removed;
    }

    /// Empties the tree.
    public void clear() {
        requireRoot();
        root.remove(KIDS);
        root.remove(LIMITS);
        root.set(NUMS, new PdfArray());
    }

    /// Returns an unmodifiable view of [#entries()].
    ///
    /// @return unmodifiable entry list
    public List<Map.Entry<Integer, PdfBase>> entriesUnmodifiable() {
        return Collections.unmodifiableList(entries());
    }

    // ────────────────────────────────────────────────────────────────────
    //  Reading
    // ────────────────────────────────────────────────────────────────────

    private PdfBase findInNode(PdfDictionary node, int key) {
        if (!keyInLimits(node, key)) return null;
        PdfBase nums = resolve(node.get(NUMS));
        if (nums instanceof PdfArray) {
            PdfBase found = findInLeaf((PdfArray) nums, key);
            if (found != null) return found;
        }
        PdfBase kids = resolve(node.get(KIDS));
        if (kids instanceof PdfArray) {
            PdfArray arr = (PdfArray) kids;
            for (int i = 0; i < arr.size(); i++) {
                PdfBase kid = resolve(arr.get(i));
                if (kid instanceof PdfDictionary) {
                    PdfBase r = findInNode((PdfDictionary) kid, key);
                    if (r != null) return r;
                }
            }
        }
        return null;
    }

    private static PdfBase findInLeaf(PdfArray pairs, int key) {
        for (int i = 0; i + 1 < pairs.size(); i += 2) {
            Integer k = asInt(resolve(pairs.get(i)));
            if (k != null && k == key) return resolve(pairs.get(i + 1));
        }
        return null;
    }

    private static boolean keyInLimits(PdfDictionary node, int key) {
        PdfBase limits = resolve(node.get(LIMITS));
        if (!(limits instanceof PdfArray) || ((PdfArray) limits).size() < 2) return true;
        Integer min = asInt(resolve(((PdfArray) limits).get(0)));
        Integer max = asInt(resolve(((PdfArray) limits).get(1)));
        if (min != null && key < min) return false;
        if (max != null && key > max) return false;
        return true;
    }

    private static void collectEntries(PdfDictionary node, List<Map.Entry<Integer, PdfBase>> out) {
        PdfBase nums = resolve(node.get(NUMS));
        if (nums instanceof PdfArray) {
            PdfArray arr = (PdfArray) nums;
            for (int i = 0; i + 1 < arr.size(); i += 2) {
                Integer k = asInt(resolve(arr.get(i)));
                if (k != null) out.add(new AbstractMap.SimpleEntry<>(k, resolve(arr.get(i + 1))));
            }
        }
        PdfBase kids = resolve(node.get(KIDS));
        if (kids instanceof PdfArray) {
            PdfArray arr = (PdfArray) kids;
            for (int i = 0; i < arr.size(); i++) {
                PdfBase kid = resolve(arr.get(i));
                if (kid instanceof PdfDictionary) collectEntries((PdfDictionary) kid, out);
            }
        }
    }

    private static int countEntries(PdfDictionary node) {
        int n = 0;
        PdfBase nums = resolve(node.get(NUMS));
        if (nums instanceof PdfArray) n += ((PdfArray) nums).size() / 2;
        PdfBase kids = resolve(node.get(KIDS));
        if (kids instanceof PdfArray) {
            PdfArray arr = (PdfArray) kids;
            for (int i = 0; i < arr.size(); i++) {
                PdfBase kid = resolve(arr.get(i));
                if (kid instanceof PdfDictionary) n += countEntries((PdfDictionary) kid);
            }
        }
        return n;
    }

    // ────────────────────────────────────────────────────────────────────
    //  Writing
    // ────────────────────────────────────────────────────────────────────

    private PdfDictionary locateLeafForInsert(PdfDictionary node, int key, List<PdfDictionary> path) {
        path.add(node);
        PdfBase kids = resolve(node.get(KIDS));
        if (!(kids instanceof PdfArray) || ((PdfArray) kids).size() == 0) {
            if (!(resolve(node.get(NUMS)) instanceof PdfArray)) {
                node.set(NUMS, new PdfArray());
            }
            return node;
        }
        PdfDictionary chosen = pickKidForKey((PdfArray) kids, key);
        return locateLeafForInsert(chosen, key, path);
    }

    private PdfDictionary locateLeafForLookup(PdfDictionary node, int key, List<PdfDictionary> path) {
        path.add(node);
        if (!keyInLimits(node, key)) {
            path.remove(path.size() - 1);
            return null;
        }
        PdfBase nums = resolve(node.get(NUMS));
        if (nums instanceof PdfArray && containsKeyInPairs((PdfArray) nums, key)) {
            return node;
        }
        PdfBase kids = resolve(node.get(KIDS));
        if (kids instanceof PdfArray) {
            PdfArray arr = (PdfArray) kids;
            for (int i = 0; i < arr.size(); i++) {
                PdfBase kid = resolve(arr.get(i));
                if (kid instanceof PdfDictionary) {
                    PdfDictionary found = locateLeafForLookup((PdfDictionary) kid, key, path);
                    if (found != null) return found;
                }
            }
        }
        path.remove(path.size() - 1);
        return null;
    }

    private static boolean containsKeyInPairs(PdfArray pairs, int key) {
        for (int i = 0; i + 1 < pairs.size(); i += 2) {
            Integer k = asInt(resolve(pairs.get(i)));
            if (k != null && k == key) return true;
        }
        return false;
    }

    private PdfDictionary pickKidForKey(PdfArray kids, int key) {
        PdfDictionary universalFallback = null;
        PdfDictionary nearestBelow = null;
        PdfDictionary nearestAbove = null;
        Integer nearestBelowMax = null;
        Integer nearestAboveMin = null;
        PdfDictionary firstKid = null;
        PdfDictionary lastKid = null;
        for (int i = 0; i < kids.size(); i++) {
            PdfBase kidObj = resolve(kids.get(i));
            if (!(kidObj instanceof PdfDictionary)) continue;
            PdfDictionary kid = (PdfDictionary) kidObj;
            if (firstKid == null) firstKid = kid;
            lastKid = kid;
            PdfBase limits = resolve(kid.get(LIMITS));
            if (!(limits instanceof PdfArray) || ((PdfArray) limits).size() < 2) {
                if (universalFallback == null) universalFallback = kid;
                continue;
            }
            Integer min = asInt(resolve(((PdfArray) limits).get(0)));
            Integer max = asInt(resolve(((PdfArray) limits).get(1)));
            if (min != null && max != null && key >= min && key <= max) {
                return kid;
            }
            if (max != null && key > max && (nearestBelowMax == null || max > nearestBelowMax)) {
                nearestBelow = kid;
                nearestBelowMax = max;
            }
            if (min != null && key < min && (nearestAboveMin == null || min < nearestAboveMin)) {
                nearestAbove = kid;
                nearestAboveMin = min;
            }
        }
        if (universalFallback != null) return universalFallback;
        if (nearestBelow != null) return nearestBelow;
        if (nearestAbove != null) return nearestAbove;
        if (lastKid != null) return lastKid;
        return firstKid;
    }

    private PdfBase insertSorted(PdfDictionary leaf, int key, PdfBase value) {
        PdfBase numsObj = resolve(leaf.get(NUMS));
        PdfArray nums;
        if (numsObj instanceof PdfArray) {
            nums = (PdfArray) numsObj;
        } else {
            nums = new PdfArray();
            leaf.set(NUMS, nums);
        }
        int insertAt = nums.size();
        for (int i = 0; i + 1 < nums.size(); i += 2) {
            Integer k = asInt(resolve(nums.get(i)));
            if (k == null) continue;
            if (k == key) {
                PdfBase old = resolve(nums.get(i + 1));
                nums.set(i + 1, value);
                return old;
            }
            if (key < k) {
                insertAt = i;
                break;
            }
        }
        nums.add(insertAt, PdfInteger.valueOf(key));
        nums.add(insertAt + 1, value);
        return null;
    }

    private PdfBase removeFromLeaf(PdfDictionary leaf, int key) {
        PdfBase numsObj = resolve(leaf.get(NUMS));
        if (!(numsObj instanceof PdfArray)) return null;
        PdfArray nums = (PdfArray) numsObj;
        for (int i = 0; i + 1 < nums.size(); i += 2) {
            Integer k = asInt(resolve(nums.get(i)));
            if (k != null && k == key) {
                PdfBase removed = resolve(nums.get(i + 1));
                nums.remove(i + 1);
                nums.remove(i);
                return removed;
            }
        }
        return null;
    }

    private void refreshLimitsAlongPath(List<PdfDictionary> path) {
        for (int i = path.size() - 1; i >= 0; i--) {
            PdfDictionary node = path.get(i);
            if (node == root) {
                node.remove(LIMITS);
                continue;
            }
            refreshLimits(node);
        }
    }

    private void refreshLimits(PdfDictionary node) {
        Integer min = null;
        Integer max = null;
        PdfBase nums = resolve(node.get(NUMS));
        if (nums instanceof PdfArray) {
            PdfArray arr = (PdfArray) nums;
            for (int i = 0; i + 1 < arr.size(); i += 2) {
                Integer k = asInt(resolve(arr.get(i)));
                if (k == null) continue;
                if (min == null || k < min) min = k;
                if (max == null || k > max) max = k;
            }
        }
        PdfBase kids = resolve(node.get(KIDS));
        if (kids instanceof PdfArray) {
            PdfArray arr = (PdfArray) kids;
            for (int i = 0; i < arr.size(); i++) {
                PdfBase kid = resolve(arr.get(i));
                if (!(kid instanceof PdfDictionary)) continue;
                PdfBase kidLimits = resolve(((PdfDictionary) kid).get(LIMITS));
                if (!(kidLimits instanceof PdfArray) || ((PdfArray) kidLimits).size() < 2) continue;
                Integer kMin = asInt(resolve(((PdfArray) kidLimits).get(0)));
                Integer kMax = asInt(resolve(((PdfArray) kidLimits).get(1)));
                if (kMin != null && (min == null || kMin < min)) min = kMin;
                if (kMax != null && (max == null || kMax > max)) max = kMax;
            }
        }
        if (min == null || max == null) {
            node.remove(LIMITS);
            return;
        }
        PdfArray limits = new PdfArray();
        limits.add(PdfInteger.valueOf(min));
        limits.add(PdfInteger.valueOf(max));
        node.set(LIMITS, limits);
    }

    // ────────────────────────────────────────────────────────────────────
    //  Utilities
    // ────────────────────────────────────────────────────────────────────

    private void requireRoot() {
        if (root == null) throw new IllegalStateException("NumberTree has no backing root dictionary");
    }

    private static PdfBase resolve(PdfBase value) {
        if (value instanceof PdfObjectReference) {
            try {
                return ((PdfObjectReference) value).dereference();
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Failed to dereference inside NumberTree", e);
                return null;
            }
        }
        return value;
    }

    private static Integer asInt(PdfBase value) {
        if (value instanceof PdfInteger) return ((PdfInteger) value).intValue();
        if (value instanceof PdfFloat) return (int) ((PdfFloat) value).doubleValue();
        return null;
    }
}
