package org.aspose.pdf.engine.linearization;

import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfNull;
import org.aspose.pdf.engine.pdfobjects.PdfObjectKey;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;
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
        PdfDictionary catalog = parser.getCatalog();
        Set<PdfObjectKey> allKeys = parser.getAllObjectKeys();

        // 1. Find all page object keys via page tree traversal
        List<PdfObjectKey> pageKeys = collectPageKeys(catalog, parser);
        int numPages = pageKeys.size();

        // Collect page tree node keys (these are document-level, not page-private)
        Set<PdfObjectKey> pageTreeKeys = collectPageTreeKeys(catalog, parser);

        // 2. For each page, collect all transitively referenced objects
        Map<PdfObjectKey, Set<Integer>> objectToPages = new LinkedHashMap<>();
        for (int pageIdx = 0; pageIdx < numPages; pageIdx++) {
            Set<PdfObjectKey> pageObjects = collectPageObjectGraph(
                    parser, pageKeys.get(pageIdx), pageTreeKeys);
            for (PdfObjectKey objKey : pageObjects) {
                objectToPages.computeIfAbsent(objKey, k -> new HashSet<>()).add(pageIdx);
            }
        }

        // 3. Classify objects
        int firstPage = 0;

        List<PdfObjectKey> firstPagePrivate = new ArrayList<>();
        List<PdfObjectKey> firstPageShared = new ArrayList<>();
        Map<Integer, List<PdfObjectKey>> otherPagePrivate = new LinkedHashMap<>();
        List<PdfObjectKey> sharedObjects = new ArrayList<>();
        List<PdfObjectKey> documentLevel = new ArrayList<>();

        for (PdfObjectKey key : allKeys) {
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
    static List<PdfObjectKey> collectPageKeys(
            PdfDictionary catalog, PDFParser parser) throws IOException {
        List<PdfObjectKey> pageKeys = new ArrayList<>();
        PdfBase pagesRef = catalog.get(PdfName.PAGES);
        PdfBase pagesObj = parser.resolveReference(pagesRef);
        if (pagesObj instanceof PdfDictionary) {
            flattenPageTree((PdfDictionary) pagesObj, parser, pageKeys);
        }
        return pageKeys;
    }

    /**
     * Recursively flattens the page tree to collect page object keys.
     */
    private static void flattenPageTree(PdfDictionary node, PDFParser parser,
                                         List<PdfObjectKey> result) throws IOException {
        String type = node.getType();
        if ("Pages".equals(type)) {
            PdfBase kidsRef = node.get(PdfName.KIDS);
            PdfBase kidsObj = parser.resolveReference(kidsRef);
            if (kidsObj instanceof PdfArray) {
                PdfArray kids = (PdfArray) kidsObj;
                for (int i = 0; i < kids.size(); i++) {
                    PdfBase child = kids.get(i);
                    PdfObjectKey childKey = null;
                    if (child instanceof PdfObjectReference) {
                        childKey = ((PdfObjectReference) child).getKey();
                        child = parser.resolveReference(child);
                    }
                    if (child instanceof PdfDictionary) {
                        String childType = ((PdfDictionary) child).getType();
                        if ("Page".equals(childType)) {
                            if (childKey != null) {
                                result.add(childKey);
                            } else if (child.getObjectKey() != null) {
                                result.add(child.getObjectKey());
                            }
                        } else {
                            flattenPageTree((PdfDictionary) child, parser, result);
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
    private static Set<PdfObjectKey> collectPageTreeKeys(
            PdfDictionary catalog, PDFParser parser) throws IOException {
        Set<PdfObjectKey> treeKeys = new HashSet<>();
        PdfBase pagesRef = catalog.get(PdfName.PAGES);
        if (pagesRef instanceof PdfObjectReference) {
            treeKeys.add(((PdfObjectReference) pagesRef).getKey());
        }
        PdfBase pagesObj = parser.resolveReference(pagesRef);
        if (pagesObj instanceof PdfDictionary) {
            collectPageTreeKeysRecursive((PdfDictionary) pagesObj, parser, treeKeys);
        }
        return treeKeys;
    }

    private static void collectPageTreeKeysRecursive(PdfDictionary node, PDFParser parser,
                                                      Set<PdfObjectKey> keys) throws IOException {
        if (!"Pages".equals(node.getType())) return;
        PdfBase kidsRef = node.get(PdfName.KIDS);
        PdfBase kidsObj = parser.resolveReference(kidsRef);
        if (kidsObj instanceof PdfArray) {
            PdfArray kids = (PdfArray) kidsObj;
            for (int i = 0; i < kids.size(); i++) {
                PdfBase child = kids.get(i);
                if (child instanceof PdfObjectReference) {
                    PdfObjectKey childKey = ((PdfObjectReference) child).getKey();
                    PdfBase resolved = parser.resolveReference(child);
                    if (resolved instanceof PdfDictionary
                            && "Pages".equals(((PdfDictionary) resolved).getType())) {
                        keys.add(childKey);
                        collectPageTreeKeysRecursive((PdfDictionary) resolved, parser, keys);
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
    private static Set<PdfObjectKey> collectPageObjectGraph(
            PDFParser parser, PdfObjectKey pageKey,
            Set<PdfObjectKey> pageTreeKeys) throws IOException {
        Set<PdfObjectKey> visited = new LinkedHashSet<>();
        Queue<PdfObjectKey> queue = new LinkedList<>();
        queue.add(pageKey);

        while (!queue.isEmpty()) {
            PdfObjectKey current = queue.poll();
            if (visited.contains(current) || pageTreeKeys.contains(current)) continue;
            visited.add(current);

            PdfBase obj = parser.getObject(current);
            if (obj == null || obj instanceof PdfNull) continue;

            collectReferences(obj, queue, visited, pageTreeKeys);
        }
        return visited;
    }

    /**
     * Extracts indirect references from a PDF object, adding new keys to the queue.
     * Does not follow /Parent references.
     */
    private static void collectReferences(PdfBase obj, Queue<PdfObjectKey> queue,
                                           Set<PdfObjectKey> visited,
                                           Set<PdfObjectKey> excluded) {
        if (obj instanceof PdfObjectReference) {
            PdfObjectKey key = ((PdfObjectReference) obj).getKey();
            if (!visited.contains(key) && !excluded.contains(key)) {
                queue.add(key);
            }
        } else if (obj instanceof PdfDictionary) {
            PdfDictionary dict = (PdfDictionary) obj;
            for (java.util.Map.Entry<PdfName, PdfBase> entry : dict) {
                // Skip /Parent to avoid walking up the page tree
                if ("Parent".equals(entry.getKey().getName())) continue;
                collectReferences(entry.getValue(), queue, visited, excluded);
            }
        } else if (obj instanceof PdfArray) {
            PdfArray arr = (PdfArray) obj;
            for (int i = 0; i < arr.size(); i++) {
                collectReferences(arr.get(i), queue, visited, excluded);
            }
        }
    }
}
