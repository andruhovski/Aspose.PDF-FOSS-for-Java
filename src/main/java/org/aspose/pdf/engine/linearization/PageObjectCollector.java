package org.aspose.pdf.engine.linearization;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSNull;
import org.aspose.pdf.engine.cos.COSObjectKey;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Walks the object graph from each page to classify objects for linearization.
 * ISO 32000-1:2008 Annex F §F.3.
 *
 * <p>Objects are classified as:</p>
 * <ul>
 *   <li><b>Page-private:</b> referenced by exactly one page</li>
 *   <li><b>Shared:</b> referenced by multiple pages</li>
 *   <li><b>Document-level:</b> not referenced by any page (catalog, page tree, info, etc.)</li>
 * </ul>
 */
public final class PageObjectCollector {

    private static final Logger LOG = Logger.getLogger(PageObjectCollector.class.getName());

    private PageObjectCollector() {}

    /**
     * Collects and classifies all objects in the document for linearization.
     *
     * @param parser the PDF parser with all objects accessible
     * @return a {@link LinearizationPlan} grouping objects by linearization part
     * @throws IOException if objects cannot be loaded
     */
    public static LinearizationPlan collect(PDFParser parser) throws IOException {
        COSDictionary catalog = parser.getCatalog();
        Set<COSObjectKey> allKeys = parser.getAllObjectKeys();

        // 1. Find all page object keys via page tree traversal
        List<COSObjectKey> pageKeys = collectPageKeys(catalog, parser);
        int numPages = pageKeys.size();

        // Collect page tree node keys (these are document-level, not page-private)
        Set<COSObjectKey> pageTreeKeys = collectPageTreeKeys(catalog, parser);

        // 2. For each page, collect all transitively referenced objects
        Map<COSObjectKey, Set<Integer>> objectToPages = new LinkedHashMap<>();
        for (int pageIdx = 0; pageIdx < numPages; pageIdx++) {
            Set<COSObjectKey> pageObjects = collectPageObjectGraph(
                    parser, pageKeys.get(pageIdx), pageTreeKeys);
            for (COSObjectKey objKey : pageObjects) {
                objectToPages.computeIfAbsent(objKey, k -> new HashSet<>()).add(pageIdx);
            }
        }

        // 3. Classify objects
        int firstPage = 0;

        List<COSObjectKey> firstPagePrivate = new ArrayList<>();
        List<COSObjectKey> firstPageShared = new ArrayList<>();
        Map<Integer, List<COSObjectKey>> otherPagePrivate = new LinkedHashMap<>();
        List<COSObjectKey> sharedObjects = new ArrayList<>();
        List<COSObjectKey> documentLevel = new ArrayList<>();

        for (COSObjectKey key : allKeys) {
            Set<Integer> pages = objectToPages.get(key);
            if (pages == null || pages.isEmpty()) {
                // Not referenced by any page → document-level
                documentLevel.add(key);
            } else if (pages.size() == 1 && pages.contains(firstPage)) {
                firstPagePrivate.add(key);
            } else if (pages.size() == 1) {
                int pageNum = pages.iterator().next();
                otherPagePrivate.computeIfAbsent(pageNum, k -> new ArrayList<>()).add(key);
            } else if (pages.contains(firstPage)) {
                // Shared and needed by first page
                firstPageShared.add(key);
            } else {
                // Shared between non-first pages only
                sharedObjects.add(key);
            }
        }

        LOG.fine(() -> "Linearization plan: " + numPages + " pages, "
                + firstPagePrivate.size() + " first-page-private, "
                + firstPageShared.size() + " first-page-shared, "
                + sharedObjects.size() + " shared, "
                + documentLevel.size() + " document-level");

        return new LinearizationPlan(
                pageKeys, firstPage, firstPagePrivate, firstPageShared,
                otherPagePrivate, sharedObjects, documentLevel, numPages);
    }

    /**
     * Collects the object keys of all page objects by traversing the page tree.
     * Returns them in document order (the order they appear in the tree).
     */
    static List<COSObjectKey> collectPageKeys(
            COSDictionary catalog, PDFParser parser) throws IOException {
        List<COSObjectKey> pageKeys = new ArrayList<>();
        COSBase pagesRef = catalog.get(COSName.PAGES);
        COSBase pagesObj = parser.resolveReference(pagesRef);
        if (pagesObj instanceof COSDictionary) {
            flattenPageTree((COSDictionary) pagesObj, parser, pageKeys);
        }
        return pageKeys;
    }

    /**
     * Recursively flattens the page tree to collect page object keys.
     */
    private static void flattenPageTree(COSDictionary node, PDFParser parser,
                                         List<COSObjectKey> result) throws IOException {
        String type = node.getType();
        if ("Pages".equals(type)) {
            COSBase kidsRef = node.get(COSName.KIDS);
            COSBase kidsObj = parser.resolveReference(kidsRef);
            if (kidsObj instanceof COSArray) {
                COSArray kids = (COSArray) kidsObj;
                for (int i = 0; i < kids.size(); i++) {
                    COSBase child = kids.get(i);
                    COSObjectKey childKey = null;
                    if (child instanceof COSObjectReference) {
                        childKey = ((COSObjectReference) child).getKey();
                        child = parser.resolveReference(child);
                    }
                    if (child instanceof COSDictionary) {
                        String childType = ((COSDictionary) child).getType();
                        if ("Page".equals(childType)) {
                            if (childKey != null) {
                                result.add(childKey);
                            } else if (child.getObjectKey() != null) {
                                result.add(child.getObjectKey());
                            }
                        } else {
                            flattenPageTree((COSDictionary) child, parser, result);
                        }
                    }
                }
            }
        } else if ("Page".equals(type)) {
            if (node.getObjectKey() != null) {
                result.add(node.getObjectKey());
            }
        }
    }

    /**
     * Collects the keys of all page tree intermediate nodes (/Type /Pages).
     * These are document-level objects and should not be assigned to any page.
     */
    private static Set<COSObjectKey> collectPageTreeKeys(
            COSDictionary catalog, PDFParser parser) throws IOException {
        Set<COSObjectKey> treeKeys = new HashSet<>();
        COSBase pagesRef = catalog.get(COSName.PAGES);
        if (pagesRef instanceof COSObjectReference) {
            treeKeys.add(((COSObjectReference) pagesRef).getKey());
        }
        COSBase pagesObj = parser.resolveReference(pagesRef);
        if (pagesObj instanceof COSDictionary) {
            collectPageTreeKeysRecursive((COSDictionary) pagesObj, parser, treeKeys);
        }
        return treeKeys;
    }

    private static void collectPageTreeKeysRecursive(COSDictionary node, PDFParser parser,
                                                      Set<COSObjectKey> keys) throws IOException {
        if (!"Pages".equals(node.getType())) return;
        COSBase kidsRef = node.get(COSName.KIDS);
        COSBase kidsObj = parser.resolveReference(kidsRef);
        if (kidsObj instanceof COSArray) {
            COSArray kids = (COSArray) kidsObj;
            for (int i = 0; i < kids.size(); i++) {
                COSBase child = kids.get(i);
                if (child instanceof COSObjectReference) {
                    COSObjectKey childKey = ((COSObjectReference) child).getKey();
                    COSBase resolved = parser.resolveReference(child);
                    if (resolved instanceof COSDictionary
                            && "Pages".equals(((COSDictionary) resolved).getType())) {
                        keys.add(childKey);
                        collectPageTreeKeysRecursive((COSDictionary) resolved, parser, keys);
                    }
                }
            }
        }
    }

    /**
     * BFS from a page object — collects all transitively referenced indirect objects.
     * Does not cross into page tree nodes (those are document-level).
     * Does not follow /Parent references (would go up the tree).
     */
    private static Set<COSObjectKey> collectPageObjectGraph(
            PDFParser parser, COSObjectKey pageKey,
            Set<COSObjectKey> pageTreeKeys) throws IOException {
        Set<COSObjectKey> visited = new LinkedHashSet<>();
        Queue<COSObjectKey> queue = new LinkedList<>();
        queue.add(pageKey);

        while (!queue.isEmpty()) {
            COSObjectKey current = queue.poll();
            if (visited.contains(current) || pageTreeKeys.contains(current)) continue;
            visited.add(current);

            COSBase obj = parser.getObject(current);
            if (obj == null || obj instanceof COSNull) continue;

            collectReferences(obj, queue, visited, pageTreeKeys);
        }
        return visited;
    }

    /**
     * Extracts indirect references from a COS object, adding new keys to the queue.
     * Does not follow /Parent references.
     */
    private static void collectReferences(COSBase obj, Queue<COSObjectKey> queue,
                                           Set<COSObjectKey> visited,
                                           Set<COSObjectKey> excluded) {
        if (obj instanceof COSObjectReference) {
            COSObjectKey key = ((COSObjectReference) obj).getKey();
            if (!visited.contains(key) && !excluded.contains(key)) {
                queue.add(key);
            }
        } else if (obj instanceof COSDictionary) {
            COSDictionary dict = (COSDictionary) obj;
            for (java.util.Map.Entry<COSName, COSBase> entry : dict) {
                // Skip /Parent to avoid walking up the page tree
                if ("Parent".equals(entry.getKey().getName())) continue;
                collectReferences(entry.getValue(), queue, visited, excluded);
            }
        } else if (obj instanceof COSArray) {
            COSArray arr = (COSArray) obj;
            for (int i = 0; i < arr.size(); i++) {
                collectReferences(arr.get(i), queue, visited, excluded);
            }
        }
    }
}
