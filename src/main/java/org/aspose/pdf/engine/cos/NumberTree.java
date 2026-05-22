package org.aspose.pdf.engine.cos;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Read/write view over a PDF number tree (ISO 32000-1:2008, §7.9.7).
 *
 * <p>A number tree mirrors a name tree (see {@link NameTree}) but uses
 * integer keys and stores leaf pairs in a {@code /Nums} array instead of
 * {@code /Names}. Examples in the spec: {@code /PageLabels} on the
 * catalog (§12.4.2) and {@code /ParentTree} on a structure tree root
 * (§14.7.4.4).</p>
 *
 * <p>Read paths tolerate missing {@code /Limits} and unsorted
 * {@code /Nums} by falling back to a linear scan. Write paths keep
 * {@code /Nums} sorted by key and resync {@code /Limits} bottom-up after
 * every mutation. No node splitting/merging — inserts grow the leaf
 * whose range covers the key (or the closest leaf if the key is out of
 * range).</p>
 */
public final class NumberTree {

    private static final Logger LOG = Logger.getLogger(NumberTree.class.getName());

    private static final COSName KIDS = COSName.of("Kids");
    private static final COSName NUMS = COSName.of("Nums");
    private static final COSName LIMITS = COSName.of("Limits");

    private final COSDictionary root;

    /**
     * Wraps the given root dictionary. The dictionary is the number-tree
     * root itself (e.g. the {@code /PageLabels} sub-dictionary in the
     * catalog) — <em>not</em> the catalog.
     *
     * @param root the number-tree root, or {@code null} for an empty view
     */
    public NumberTree(COSDictionary root) {
        this.root = root;
    }

    /**
     * Returns the underlying root dictionary, or {@code null} if this view
     * was constructed over a {@code null} root.
     *
     * @return the root dictionary
     */
    public COSDictionary getRoot() {
        return root;
    }

    /**
     * Returns whether the tree has no entries.
     *
     * @return {@code true} when no key maps to any value
     */
    public boolean isEmpty() {
        return root == null || countEntries(root) == 0;
    }

    /**
     * Returns the number of (key, value) pairs in the tree.
     *
     * @return the entry count
     */
    public int size() {
        return root == null ? 0 : countEntries(root);
    }

    /**
     * Looks up a key. Uses {@code /Limits} pruning when present.
     *
     * @param key the integer key
     * @return the associated value, or {@code null} if not found
     */
    public COSBase get(int key) {
        if (root == null) return null;
        return findInNode(root, key);
    }

    /**
     * Returns whether the tree contains the given key.
     *
     * @param key the integer key
     * @return {@code true} if a value is associated with {@code key}
     */
    public boolean containsKey(int key) {
        return get(key) != null;
    }

    /**
     * Returns all entries in tree order (which, for a conformant tree, is
     * key-sorted order).
     *
     * @return the list of {@code (key, value)} entries
     */
    public List<Map.Entry<Integer, COSBase>> entries() {
        List<Map.Entry<Integer, COSBase>> out = new ArrayList<>();
        if (root != null) collectEntries(root, out);
        return out;
    }

    /**
     * Returns all keys in tree order.
     *
     * @return the list of integer keys
     */
    public List<Integer> keys() {
        List<Map.Entry<Integer, COSBase>> es = entries();
        List<Integer> out = new ArrayList<>(es.size());
        for (Map.Entry<Integer, COSBase> e : es) out.add(e.getKey());
        return out;
    }

    /**
     * Inserts or replaces a value. The host {@code /Nums} array stays
     * sorted and {@code /Limits} are re-synced along the insertion path.
     *
     * @param key   the integer key
     * @param value the value to associate
     * @return the previous value bound to {@code key}, or {@code null}
     * @throws IllegalStateException if this view wraps a {@code null} root
     */
    public COSBase put(int key, COSBase value) {
        requireRoot();
        if (value == null) return remove(key);
        List<COSDictionary> path = new ArrayList<>();
        COSDictionary leaf = locateLeafForInsert(root, key, path);
        COSBase previous = insertSorted(leaf, key, value);
        refreshLimitsAlongPath(path);
        return previous;
    }

    /**
     * Removes a key. Empty leaves keep their (now empty) {@code /Nums}
     * array — rebalancing is left to an external pass.
     *
     * @param key the integer key to remove
     * @return the removed value, or {@code null} if the key was absent
     */
    public COSBase remove(int key) {
        if (root == null) return null;
        List<COSDictionary> path = new ArrayList<>();
        COSDictionary leaf = locateLeafForLookup(root, key, path);
        if (leaf == null) return null;
        COSBase removed = removeFromLeaf(leaf, key);
        if (removed != null) refreshLimitsAlongPath(path);
        return removed;
    }

    /**
     * Empties the tree.
     */
    public void clear() {
        requireRoot();
        root.remove(KIDS);
        root.remove(LIMITS);
        root.set(NUMS, new COSArray());
    }

    /**
     * Returns an unmodifiable view of {@link #entries()}.
     *
     * @return unmodifiable entry list
     */
    public List<Map.Entry<Integer, COSBase>> entriesUnmodifiable() {
        return Collections.unmodifiableList(entries());
    }

    // ────────────────────────────────────────────────────────────────────
    //  Reading
    // ────────────────────────────────────────────────────────────────────

    private COSBase findInNode(COSDictionary node, int key) {
        if (!keyInLimits(node, key)) return null;
        COSBase nums = resolve(node.get(NUMS));
        if (nums instanceof COSArray) {
            COSBase found = findInLeaf((COSArray) nums, key);
            if (found != null) return found;
        }
        COSBase kids = resolve(node.get(KIDS));
        if (kids instanceof COSArray) {
            COSArray arr = (COSArray) kids;
            for (int i = 0; i < arr.size(); i++) {
                COSBase kid = resolve(arr.get(i));
                if (kid instanceof COSDictionary) {
                    COSBase r = findInNode((COSDictionary) kid, key);
                    if (r != null) return r;
                }
            }
        }
        return null;
    }

    private static COSBase findInLeaf(COSArray pairs, int key) {
        for (int i = 0; i + 1 < pairs.size(); i += 2) {
            Integer k = asInt(resolve(pairs.get(i)));
            if (k != null && k == key) return resolve(pairs.get(i + 1));
        }
        return null;
    }

    private static boolean keyInLimits(COSDictionary node, int key) {
        COSBase limits = resolve(node.get(LIMITS));
        if (!(limits instanceof COSArray) || ((COSArray) limits).size() < 2) return true;
        Integer min = asInt(resolve(((COSArray) limits).get(0)));
        Integer max = asInt(resolve(((COSArray) limits).get(1)));
        if (min != null && key < min) return false;
        if (max != null && key > max) return false;
        return true;
    }

    private static void collectEntries(COSDictionary node, List<Map.Entry<Integer, COSBase>> out) {
        COSBase nums = resolve(node.get(NUMS));
        if (nums instanceof COSArray) {
            COSArray arr = (COSArray) nums;
            for (int i = 0; i + 1 < arr.size(); i += 2) {
                Integer k = asInt(resolve(arr.get(i)));
                if (k != null) out.add(new AbstractMap.SimpleEntry<>(k, resolve(arr.get(i + 1))));
            }
        }
        COSBase kids = resolve(node.get(KIDS));
        if (kids instanceof COSArray) {
            COSArray arr = (COSArray) kids;
            for (int i = 0; i < arr.size(); i++) {
                COSBase kid = resolve(arr.get(i));
                if (kid instanceof COSDictionary) collectEntries((COSDictionary) kid, out);
            }
        }
    }

    private static int countEntries(COSDictionary node) {
        int n = 0;
        COSBase nums = resolve(node.get(NUMS));
        if (nums instanceof COSArray) n += ((COSArray) nums).size() / 2;
        COSBase kids = resolve(node.get(KIDS));
        if (kids instanceof COSArray) {
            COSArray arr = (COSArray) kids;
            for (int i = 0; i < arr.size(); i++) {
                COSBase kid = resolve(arr.get(i));
                if (kid instanceof COSDictionary) n += countEntries((COSDictionary) kid);
            }
        }
        return n;
    }

    // ────────────────────────────────────────────────────────────────────
    //  Writing
    // ────────────────────────────────────────────────────────────────────

    private COSDictionary locateLeafForInsert(COSDictionary node, int key, List<COSDictionary> path) {
        path.add(node);
        COSBase kids = resolve(node.get(KIDS));
        if (!(kids instanceof COSArray) || ((COSArray) kids).size() == 0) {
            if (!(resolve(node.get(NUMS)) instanceof COSArray)) {
                node.set(NUMS, new COSArray());
            }
            return node;
        }
        COSDictionary chosen = pickKidForKey((COSArray) kids, key);
        return locateLeafForInsert(chosen, key, path);
    }

    private COSDictionary locateLeafForLookup(COSDictionary node, int key, List<COSDictionary> path) {
        path.add(node);
        if (!keyInLimits(node, key)) {
            path.remove(path.size() - 1);
            return null;
        }
        COSBase nums = resolve(node.get(NUMS));
        if (nums instanceof COSArray && containsKeyInPairs((COSArray) nums, key)) {
            return node;
        }
        COSBase kids = resolve(node.get(KIDS));
        if (kids instanceof COSArray) {
            COSArray arr = (COSArray) kids;
            for (int i = 0; i < arr.size(); i++) {
                COSBase kid = resolve(arr.get(i));
                if (kid instanceof COSDictionary) {
                    COSDictionary found = locateLeafForLookup((COSDictionary) kid, key, path);
                    if (found != null) return found;
                }
            }
        }
        path.remove(path.size() - 1);
        return null;
    }

    private static boolean containsKeyInPairs(COSArray pairs, int key) {
        for (int i = 0; i + 1 < pairs.size(); i += 2) {
            Integer k = asInt(resolve(pairs.get(i)));
            if (k != null && k == key) return true;
        }
        return false;
    }

    private COSDictionary pickKidForKey(COSArray kids, int key) {
        COSDictionary universalFallback = null;
        COSDictionary nearestBelow = null;
        COSDictionary nearestAbove = null;
        Integer nearestBelowMax = null;
        Integer nearestAboveMin = null;
        COSDictionary firstKid = null;
        COSDictionary lastKid = null;
        for (int i = 0; i < kids.size(); i++) {
            COSBase kidObj = resolve(kids.get(i));
            if (!(kidObj instanceof COSDictionary)) continue;
            COSDictionary kid = (COSDictionary) kidObj;
            if (firstKid == null) firstKid = kid;
            lastKid = kid;
            COSBase limits = resolve(kid.get(LIMITS));
            if (!(limits instanceof COSArray) || ((COSArray) limits).size() < 2) {
                if (universalFallback == null) universalFallback = kid;
                continue;
            }
            Integer min = asInt(resolve(((COSArray) limits).get(0)));
            Integer max = asInt(resolve(((COSArray) limits).get(1)));
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

    private COSBase insertSorted(COSDictionary leaf, int key, COSBase value) {
        COSBase numsObj = resolve(leaf.get(NUMS));
        COSArray nums;
        if (numsObj instanceof COSArray) {
            nums = (COSArray) numsObj;
        } else {
            nums = new COSArray();
            leaf.set(NUMS, nums);
        }
        int insertAt = nums.size();
        for (int i = 0; i + 1 < nums.size(); i += 2) {
            Integer k = asInt(resolve(nums.get(i)));
            if (k == null) continue;
            if (k == key) {
                COSBase old = resolve(nums.get(i + 1));
                nums.set(i + 1, value);
                return old;
            }
            if (key < k) {
                insertAt = i;
                break;
            }
        }
        nums.add(insertAt, COSInteger.valueOf(key));
        nums.add(insertAt + 1, value);
        return null;
    }

    private COSBase removeFromLeaf(COSDictionary leaf, int key) {
        COSBase numsObj = resolve(leaf.get(NUMS));
        if (!(numsObj instanceof COSArray)) return null;
        COSArray nums = (COSArray) numsObj;
        for (int i = 0; i + 1 < nums.size(); i += 2) {
            Integer k = asInt(resolve(nums.get(i)));
            if (k != null && k == key) {
                COSBase removed = resolve(nums.get(i + 1));
                nums.remove(i + 1);
                nums.remove(i);
                return removed;
            }
        }
        return null;
    }

    private void refreshLimitsAlongPath(List<COSDictionary> path) {
        for (int i = path.size() - 1; i >= 0; i--) {
            COSDictionary node = path.get(i);
            if (node == root) {
                node.remove(LIMITS);
                continue;
            }
            refreshLimits(node);
        }
    }

    private void refreshLimits(COSDictionary node) {
        Integer min = null;
        Integer max = null;
        COSBase nums = resolve(node.get(NUMS));
        if (nums instanceof COSArray) {
            COSArray arr = (COSArray) nums;
            for (int i = 0; i + 1 < arr.size(); i += 2) {
                Integer k = asInt(resolve(arr.get(i)));
                if (k == null) continue;
                if (min == null || k < min) min = k;
                if (max == null || k > max) max = k;
            }
        }
        COSBase kids = resolve(node.get(KIDS));
        if (kids instanceof COSArray) {
            COSArray arr = (COSArray) kids;
            for (int i = 0; i < arr.size(); i++) {
                COSBase kid = resolve(arr.get(i));
                if (!(kid instanceof COSDictionary)) continue;
                COSBase kidLimits = resolve(((COSDictionary) kid).get(LIMITS));
                if (!(kidLimits instanceof COSArray) || ((COSArray) kidLimits).size() < 2) continue;
                Integer kMin = asInt(resolve(((COSArray) kidLimits).get(0)));
                Integer kMax = asInt(resolve(((COSArray) kidLimits).get(1)));
                if (kMin != null && (min == null || kMin < min)) min = kMin;
                if (kMax != null && (max == null || kMax > max)) max = kMax;
            }
        }
        if (min == null || max == null) {
            node.remove(LIMITS);
            return;
        }
        COSArray limits = new COSArray();
        limits.add(COSInteger.valueOf(min));
        limits.add(COSInteger.valueOf(max));
        node.set(LIMITS, limits);
    }

    // ────────────────────────────────────────────────────────────────────
    //  Utilities
    // ────────────────────────────────────────────────────────────────────

    private void requireRoot() {
        if (root == null) throw new IllegalStateException("NumberTree has no backing root dictionary");
    }

    private static COSBase resolve(COSBase value) {
        if (value instanceof COSObjectReference) {
            try {
                return ((COSObjectReference) value).dereference();
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Failed to dereference inside NumberTree", e);
                return null;
            }
        }
        return value;
    }

    private static Integer asInt(COSBase value) {
        if (value instanceof COSInteger) return ((COSInteger) value).intValue();
        if (value instanceof COSFloat) return (int) ((COSFloat) value).doubleValue();
        return null;
    }
}
