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
 * Read/write view over a PDF name tree (ISO 32000-1:2008, §7.9.6).
 *
 * <p>A name tree is a balanced tree whose leaves hold sorted {@code /Names}
 * pair arrays {@code [key0 value0 key1 value1 …]}, and whose intermediate
 * nodes carry {@code /Kids} arrays plus a {@code /Limits} pair
 * {@code [minKey maxKey]} for range pruning. This class hides the traversal
 * and provides the usual {@link #get}, {@link #put}, {@link #remove},
 * {@link #entries} primitives.</p>
 *
 * <p>The implementation tolerates malformed inputs: missing {@code /Limits},
 * unsorted {@code /Names}, mixed {@code /Names}+{@code /Kids} on a single
 * node — read paths fall back to linear scans and write paths repair
 * {@code /Limits} from observed data after every mutation. Per spec writes
 * keep {@code /Names} sorted and {@code /Limits} synced bottom-up.</p>
 *
 * <p>This class does not rebalance the tree (no node splitting/merging).
 * Inserts go into the leaf whose range already covers the key, or whose
 * range is closest if the key is out of bounds; that leaf grows until the
 * caller decides to rebalance externally.</p>
 */
public final class NameTree {

    private static final Logger LOG = Logger.getLogger(NameTree.class.getName());

    private static final COSName KIDS = COSName.of("Kids");
    private static final COSName NAMES = COSName.of("Names");
    private static final COSName LIMITS = COSName.of("Limits");

    private final COSDictionary root;

    /**
     * Wraps the given root dictionary. The dictionary is the name-tree root
     * itself (e.g. the {@code /Dests} sub-dictionary under
     * {@code /Catalog/Names}) — <em>not</em> the catalog.
     *
     * @param root the name-tree root, or {@code null} for an empty view
     */
    public NameTree(COSDictionary root) {
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
     * @return {@code true} when no name maps to any value
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
     * Looks up a key. Uses {@code /Limits} to prune sub-trees when present.
     *
     * @param key the key to look up
     * @return the associated value, or {@code null} if not found
     */
    public COSBase get(String key) {
        if (root == null || key == null) return null;
        return findInNode(root, key);
    }

    /**
     * Returns whether the tree contains the given key.
     *
     * @param key the key
     * @return {@code true} if a value is associated with {@code key}
     */
    public boolean containsKey(String key) {
        return get(key) != null;
    }

    /**
     * Returns all entries in tree order (which, for a conformant tree, is
     * key-sorted order). The returned list is a snapshot — subsequent
     * mutations do not affect it.
     *
     * @return the list of {@code (key, value)} entries
     */
    public List<Map.Entry<String, COSBase>> entries() {
        List<Map.Entry<String, COSBase>> out = new ArrayList<>();
        if (root != null) collectEntries(root, out);
        return out;
    }

    /**
     * Returns all keys in tree order. Convenience wrapper around
     * {@link #entries()} for callers that only need the names.
     *
     * @return the list of keys
     */
    public List<String> keys() {
        List<Map.Entry<String, COSBase>> es = entries();
        List<String> out = new ArrayList<>(es.size());
        for (Map.Entry<String, COSBase> e : es) out.add(e.getKey());
        return out;
    }

    /**
     * Inserts or replaces a value. The host {@code /Names} array stays
     * sorted by key and {@code /Limits} are re-synced bottom-up along the
     * insertion path.
     *
     * @param key   the key to insert (must not be {@code null})
     * @param value the value to associate
     * @return the previous value bound to {@code key}, or {@code null}
     * @throws IllegalStateException if this view wraps a {@code null} root
     */
    public COSBase put(String key, COSBase value) {
        requireRoot();
        if (key == null) throw new IllegalArgumentException("key must not be null");
        if (value == null) return remove(key);
        List<COSDictionary> path = new ArrayList<>();
        COSDictionary leaf = locateLeafForInsert(root, key, path);
        COSBase previous = insertSorted(leaf, key, value);
        // The leaf itself is the last entry on the path — recompute limits
        // starting there and walking up to (but not including) the root.
        refreshLimitsAlongPath(path);
        return previous;
    }

    /**
     * Removes a key. Empty leaves keep their (now empty) {@code /Names}
     * array — the writer is content-preserving; pruning leaves is a job
     * for an external rebalancing pass.
     *
     * @param key the key to remove
     * @return the removed value, or {@code null} if the key was absent
     */
    public COSBase remove(String key) {
        if (root == null || key == null) return null;
        List<COSDictionary> path = new ArrayList<>();
        COSDictionary leaf = locateLeafForLookup(root, key, path);
        if (leaf == null) return null;
        COSBase removed = removeFromLeaf(leaf, key);
        if (removed != null) refreshLimitsAlongPath(path);
        return removed;
    }

    /**
     * Empties the tree: drops all {@code /Kids}, {@code /Limits} and any
     * {@code /Names} array contents. Leaves an empty {@code /Names} on the
     * root so subsequent inserts have a leaf to fall into.
     */
    public void clear() {
        requireRoot();
        root.remove(KIDS);
        root.remove(LIMITS);
        root.set(NAMES, new COSArray());
    }

    // ────────────────────────────────────────────────────────────────────
    //  Reading
    // ────────────────────────────────────────────────────────────────────

    private COSBase findInNode(COSDictionary node, String key) {
        if (!keyInLimits(node, key)) return null;
        COSBase names = resolve(node.get(NAMES));
        if (names instanceof COSArray) {
            COSBase found = findInLeaf((COSArray) names, key);
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

    private static COSBase findInLeaf(COSArray pairs, String key) {
        for (int i = 0; i + 1 < pairs.size(); i += 2) {
            String k = asString(resolve(pairs.get(i)));
            if (key.equals(k)) return resolve(pairs.get(i + 1));
        }
        return null;
    }

    private static boolean keyInLimits(COSDictionary node, String key) {
        COSBase limits = resolve(node.get(LIMITS));
        if (!(limits instanceof COSArray) || ((COSArray) limits).size() < 2) return true;
        String min = asString(resolve(((COSArray) limits).get(0)));
        String max = asString(resolve(((COSArray) limits).get(1)));
        if (min != null && key.compareTo(min) < 0) return false;
        if (max != null && key.compareTo(max) > 0) return false;
        return true;
    }

    private static void collectEntries(COSDictionary node, List<Map.Entry<String, COSBase>> out) {
        COSBase names = resolve(node.get(NAMES));
        if (names instanceof COSArray) {
            COSArray arr = (COSArray) names;
            for (int i = 0; i + 1 < arr.size(); i += 2) {
                String k = asString(resolve(arr.get(i)));
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
        COSBase names = resolve(node.get(NAMES));
        if (names instanceof COSArray) n += ((COSArray) names).size() / 2;
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

    private COSDictionary locateLeafForInsert(COSDictionary node, String key, List<COSDictionary> path) {
        path.add(node);
        COSBase kids = resolve(node.get(KIDS));
        if (!(kids instanceof COSArray) || ((COSArray) kids).size() == 0) {
            // Treat as a leaf: ensure /Names exists so the insert has somewhere to land.
            if (!(resolve(node.get(NAMES)) instanceof COSArray)) {
                node.set(NAMES, new COSArray());
            }
            return node;
        }
        COSDictionary chosen = pickKidForKey((COSArray) kids, key);
        return locateLeafForInsert(chosen, key, path);
    }

    private COSDictionary locateLeafForLookup(COSDictionary node, String key, List<COSDictionary> path) {
        path.add(node);
        if (!keyInLimits(node, key)) {
            path.remove(path.size() - 1);
            return null;
        }
        COSBase names = resolve(node.get(NAMES));
        if (names instanceof COSArray && containsKeyInPairs((COSArray) names, key)) {
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

    private static boolean containsKeyInPairs(COSArray pairs, String key) {
        for (int i = 0; i + 1 < pairs.size(); i += 2) {
            if (key.equals(asString(resolve(pairs.get(i))))) return true;
        }
        return false;
    }

    /**
     * Picks the kid whose /Limits range contains the key, or — if no range
     * does — the best-fit kid (first kid for keys below all mins, last
     * kid for keys above all maxes, nearest by min-distance otherwise).
     * Kids without /Limits are treated as universal matches and chosen
     * preferentially over fallback kids.
     */
    private COSDictionary pickKidForKey(COSArray kids, String key) {
        COSDictionary universalFallback = null;
        COSDictionary nearestBelow = null;  // largest /Limits[1] still < key
        COSDictionary nearestAbove = null;  // smallest /Limits[0] still > key
        String nearestBelowMax = null;
        String nearestAboveMin = null;
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
            String min = asString(resolve(((COSArray) limits).get(0)));
            String max = asString(resolve(((COSArray) limits).get(1)));
            if (min != null && max != null && key.compareTo(min) >= 0 && key.compareTo(max) <= 0) {
                return kid;
            }
            if (max != null && key.compareTo(max) > 0
                    && (nearestBelowMax == null || max.compareTo(nearestBelowMax) > 0)) {
                nearestBelow = kid;
                nearestBelowMax = max;
            }
            if (min != null && key.compareTo(min) < 0
                    && (nearestAboveMin == null || min.compareTo(nearestAboveMin) < 0)) {
                nearestAbove = kid;
                nearestAboveMin = min;
            }
        }
        if (universalFallback != null) return universalFallback;
        if (nearestBelow != null) return nearestBelow;   // extends last in-range leaf upward
        if (nearestAbove != null) return nearestAbove;   // extends first in-range leaf downward
        if (lastKid != null) return lastKid;
        return firstKid;                                  // empty /Kids — unreachable in practice
    }

    /**
     * Inserts (or overwrites) the pair in a sorted leaf {@code /Names}
     * array. Returns the previous value if the key was already present.
     */
    private COSBase insertSorted(COSDictionary leaf, String key, COSBase value) {
        COSBase namesObj = resolve(leaf.get(NAMES));
        COSArray names;
        if (namesObj instanceof COSArray) {
            names = (COSArray) namesObj;
        } else {
            names = new COSArray();
            leaf.set(NAMES, names);
        }
        int insertAt = names.size();
        for (int i = 0; i + 1 < names.size(); i += 2) {
            String k = asString(resolve(names.get(i)));
            if (k == null) continue;
            int cmp = key.compareTo(k);
            if (cmp == 0) {
                COSBase old = resolve(names.get(i + 1));
                names.set(i + 1, value);
                return old;
            }
            if (cmp < 0) {
                insertAt = i;
                break;
            }
        }
        names.add(insertAt, makeKey(key));
        names.add(insertAt + 1, value);
        return null;
    }

    private COSBase removeFromLeaf(COSDictionary leaf, String key) {
        COSBase namesObj = resolve(leaf.get(NAMES));
        if (!(namesObj instanceof COSArray)) return null;
        COSArray names = (COSArray) namesObj;
        for (int i = 0; i + 1 < names.size(); i += 2) {
            String k = asString(resolve(names.get(i)));
            if (key.equals(k)) {
                COSBase removed = resolve(names.get(i + 1));
                names.remove(i + 1);
                names.remove(i);
                return removed;
            }
        }
        return null;
    }

    /**
     * Rebuilds {@code /Limits} on every node along the path except the
     * root (the root in a name tree carries no {@code /Limits} per spec).
     * Walks from leaf up so each ancestor sees a consistent child state.
     */
    private void refreshLimitsAlongPath(List<COSDictionary> path) {
        for (int i = path.size() - 1; i >= 0; i--) {
            COSDictionary node = path.get(i);
            if (node == root) {
                // §7.9.6 Table 36: root must NOT carry /Limits.
                node.remove(LIMITS);
                continue;
            }
            refreshLimits(node);
        }
    }

    private void refreshLimits(COSDictionary node) {
        String min = null;
        String max = null;
        COSBase names = resolve(node.get(NAMES));
        if (names instanceof COSArray) {
            COSArray arr = (COSArray) names;
            for (int i = 0; i + 1 < arr.size(); i += 2) {
                String k = asString(resolve(arr.get(i)));
                if (k == null) continue;
                if (min == null || k.compareTo(min) < 0) min = k;
                if (max == null || k.compareTo(max) > 0) max = k;
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
                String kMin = asString(resolve(((COSArray) kidLimits).get(0)));
                String kMax = asString(resolve(((COSArray) kidLimits).get(1)));
                if (kMin != null && (min == null || kMin.compareTo(min) < 0)) min = kMin;
                if (kMax != null && (max == null || kMax.compareTo(max) > 0)) max = kMax;
            }
        }
        if (min == null || max == null) {
            node.remove(LIMITS);
            return;
        }
        COSArray limits = new COSArray();
        limits.add(makeKey(min));
        limits.add(makeKey(max));
        node.set(LIMITS, limits);
    }

    // ────────────────────────────────────────────────────────────────────
    //  Utilities
    // ────────────────────────────────────────────────────────────────────

    private void requireRoot() {
        if (root == null) throw new IllegalStateException("NameTree has no backing root dictionary");
    }

    private static COSBase resolve(COSBase value) {
        if (value instanceof COSObjectReference) {
            try {
                return ((COSObjectReference) value).dereference();
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Failed to dereference inside NameTree", e);
                return null;
            }
        }
        return value;
    }

    private static String asString(COSBase value) {
        if (value instanceof COSString) return ((COSString) value).getString();
        if (value instanceof COSName) return ((COSName) value).getName();
        return null;
    }

    /**
     * Builds the COSString key. COSString(String) chooses PDFDocEncoding when
     * possible and falls back to UTF-16BE with BOM for non-encodable code
     * points — matching how strings are stored in conformant PDF writers.
     */
    private static COSBase makeKey(String key) {
        return new COSString(key);
    }

    /**
     * Returns an unmodifiable view of {@link #entries()} for callers that
     * want a {@link List} interface but no mutation.
     *
     * @return unmodifiable entry list
     */
    public List<Map.Entry<String, COSBase>> entriesUnmodifiable() {
        return Collections.unmodifiableList(entries());
    }
}
