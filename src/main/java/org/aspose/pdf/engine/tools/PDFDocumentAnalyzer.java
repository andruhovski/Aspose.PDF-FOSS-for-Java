package org.aspose.pdf.engine.tools;

import org.aspose.pdf.engine.cos.*;
import org.aspose.pdf.engine.io.RandomAccessReader;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Programmatic analyzer for PDF documents.
 * Opens a PDF file, parses it, and provides structured access to all COS objects,
 * page tree, trailer, catalog, etc.
 *
 * <p>This class contains NO console/CLI logic — it is purely a data layer.
 * For CLI inspection use {@code org.aspose.pdf.tools.PDFInspector} in the pdf-tools module.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 *   PDFDocumentAnalyzer analyzer = new PDFDocumentAnalyzer();
 *   analyzer.open("file.pdf");
 *   List&lt;ObjectInfo&gt; objects = analyzer.getObjects();
 *   ObjectInfo catalog = analyzer.findByType("Catalog");
 *   analyzer.close();
 * </pre>
 */
public class PDFDocumentAnalyzer implements Closeable {

    private PDFParser parser;
    private RandomAccessReader reader;
    private String sourceName;
    private long sourceSize;
    private long parseTimeMs;

    // ─── Open methods ────────────────────────────────────────────

    /**
     * Open a PDF from a file path.
     *
     * @param path path to the PDF file
     * @throws IOException if the file cannot be read or parsed
     */
    public void open(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + path);
        }
        open(RandomAccessReader.fromFile(file), file.getAbsolutePath(), file.length());
    }

    /**
     * Open a PDF from an InputStream. The stream is read entirely into memory.
     *
     * @param inputStream the input stream containing PDF data
     * @throws IOException if the stream cannot be read or parsed
     */
    public void open(InputStream inputStream) throws IOException {
        open(inputStream, "<InputStream>");
    }

    /**
     * Open a PDF from an InputStream with a source name for diagnostics.
     *
     * @param inputStream the input stream containing PDF data
     * @param sourceName  descriptive name of the source (for logging)
     * @throws IOException if the stream cannot be read or parsed
     */
    public void open(InputStream inputStream, String sourceName) throws IOException {
        RandomAccessReader r = RandomAccessReader.fromStream(inputStream);
        open(r, sourceName, r.getLength());
    }

    /**
     * Open a PDF from a byte array.
     *
     * @param pdfBytes the PDF content as bytes
     * @throws IOException if the data cannot be parsed
     */
    public void open(byte[] pdfBytes) throws IOException {
        open(pdfBytes, "<byte[]>");
    }

    /**
     * Open a PDF from a byte array with a source name for diagnostics.
     *
     * @param pdfBytes   the PDF content as bytes
     * @param sourceName descriptive name of the source (for logging)
     * @throws IOException if the data cannot be parsed
     */
    public void open(byte[] pdfBytes, String sourceName) throws IOException {
        open(RandomAccessReader.fromBytes(pdfBytes), sourceName, pdfBytes.length);
    }

    private void open(RandomAccessReader r, String sourceName, long size) throws IOException {
        this.reader = r;
        this.parser = new PDFParser(reader);

        long startTime = System.currentTimeMillis();
        parser.parse();
        this.parseTimeMs = System.currentTimeMillis() - startTime;

        this.sourceName = sourceName;
        this.sourceSize = size;
    }

    @Override
    public void close() throws IOException {
        if (reader != null) reader.close();
    }

    // ─── Accessors ───────────────────────────────────────────────

    /** Returns the underlying parser for direct access. */
    public PDFParser getParser() { return parser; }

    /** PDF version (e.g. 1.4, 1.5, 2.0). */
    public float getVersion() { return parser.getVersion(); }

    /** Number of objects in the xref table. */
    public int getObjectCount() { return parser.getAllObjectKeys().size(); }

    /** Trailer dictionary. */
    public COSDictionary getTrailer() { return parser.getTrailer(); }

    /** Source name (file path or descriptive label). */
    public String getSourceName() { return sourceName; }

    /** Source size in bytes. */
    public long getSourceSize() { return sourceSize; }

    /** Time spent parsing, in milliseconds. */
    public long getParseTimeMs() { return parseTimeMs; }

    // ─── Object access ──────────────────────────────────────────

    /**
     * Load a single object by number (generation = 0).
     *
     * @param objectNumber the object number
     * @return the COS object, or null if not found
     * @throws IOException if the object cannot be read
     */
    public COSBase getObject(int objectNumber) throws IOException {
        return parser.getObject(new COSObjectKey(objectNumber, 0));
    }

    /**
     * Resolve an indirect reference to its target object.
     *
     * @param obj a COS object (possibly a COSObjectReference)
     * @return the resolved object
     * @throws IOException if resolution fails
     */
    public COSBase resolve(COSBase obj) throws IOException {
        return parser.resolveReference(obj);
    }

    /** All object keys, sorted by object number. */
    public List<COSObjectKey> getSortedKeys() {
        List<COSObjectKey> keys = new ArrayList<>(parser.getAllObjectKeys());
        Collections.sort(keys);
        return keys;
    }

    /**
     * Load all objects into a map. Errors are collected, not thrown.
     *
     * @return map of object key to loaded COS object
     */
    public Map<COSObjectKey, COSBase> loadAllObjects() {
        Map<COSObjectKey, COSBase> result = new LinkedHashMap<>();
        for (COSObjectKey key : getSortedKeys()) {
            try {
                COSBase obj = parser.getObject(key);
                if (obj != null && !(obj instanceof COSNull)) {
                    result.put(key, obj);
                }
            } catch (Exception e) {
                // skip — caller can use getObjects() for error-tolerant listing
            }
        }
        return result;
    }

    // ─── Structured queries ─────────────────────────────────────

    /**
     * Get all objects with metadata as {@link ObjectInfo} records.
     *
     * @return list of ObjectInfo for every object in the document
     */
    public List<ObjectInfo> getObjects() {
        List<ObjectInfo> result = new ArrayList<>();
        for (COSObjectKey key : getSortedKeys()) {
            try {
                COSBase obj = parser.getObject(key);
                if (obj != null && !(obj instanceof COSNull)) {
                    result.add(new ObjectInfo(key, obj, getCosTypeName(obj)));
                }
            } catch (Exception e) {
                result.add(new ObjectInfo(key, COSNull.INSTANCE, "ERROR:" + e.getMessage()));
            }
        }
        return result;
    }

    /** Find the first object with the given /Type value, or null. */
    public ObjectInfo findByType(String type) {
        return getObjects().stream().filter(o -> o.isType(type)).findFirst().orElse(null);
    }

    /** Find all objects with the given /Type value. */
    public List<ObjectInfo> findAllByType(String type) {
        return getObjects().stream().filter(o -> o.isType(type)).collect(Collectors.toList());
    }

    /** Find all objects whose dictionary contains the given key. */
    public List<ObjectInfo> findByKey(String key) {
        return getObjects().stream().filter(o -> o.hasKey(key)).collect(Collectors.toList());
    }

    /** Find all stream objects. */
    public List<ObjectInfo> getStreams() {
        return getObjects().stream().filter(o -> o.isStream).collect(Collectors.toList());
    }

    /** Count objects by COS type name. */
    public Map<String, Integer> getTypeCounts() {
        Map<String, Integer> counts = new TreeMap<>();
        for (COSObjectKey key : parser.getAllObjectKeys()) {
            try {
                COSBase obj = parser.getObject(key);
                String type = getCosTypeName(obj);
                counts.merge(type, 1, Integer::sum);
            } catch (Exception e) {
                counts.merge("ERROR", 1, Integer::sum);
            }
        }
        return counts;
    }

    /**
     * Get the page tree: list of Page objects in document order.
     *
     * @return list of ObjectInfo for each page
     * @throws IOException if page tree cannot be traversed
     */
    public List<ObjectInfo> getPages() throws IOException {
        List<ObjectInfo> pages = new ArrayList<>();
        COSDictionary catalog = parser.getCatalog();
        COSBase pagesRef = catalog.get("Pages");
        COSBase pagesObj = parser.resolveReference(pagesRef);
        if (pagesObj instanceof COSDictionary) {
            collectPages((COSDictionary) pagesObj, pages);
        }
        return pages;
    }

    private void collectPages(COSDictionary node, List<ObjectInfo> pages) throws IOException {
        String type = node.getNameAsString("Type");
        if ("Page".equals(type)) {
            COSObjectKey key = node.getObjectKey();
            if (key == null) key = new COSObjectKey(0, 0);
            pages.add(new ObjectInfo(key, node, "Dictionary"));
        } else if ("Pages".equals(type)) {
            COSBase kidsObj = node.get("Kids");
            COSBase kids = parser.resolveReference(kidsObj);
            if (kids instanceof COSArray) {
                for (int i = 0; i < ((COSArray) kids).size(); i++) {
                    COSBase childRef = ((COSArray) kids).get(i);
                    COSBase child = parser.resolveReference(childRef);
                    if (child instanceof COSDictionary) {
                        collectPages((COSDictionary) child, pages);
                    }
                }
            }
        }
    }

    /**
     * Check whether a COS object tree contains a reference to a given object number.
     *
     * @param obj          the root of the subtree to search
     * @param targetObjNum the target object number
     * @return true if a reference to targetObjNum is found
     */
    public boolean containsReference(COSBase obj, int targetObjNum) {
        return containsReference(obj, targetObjNum, new HashSet<>());
    }

    private boolean containsReference(COSBase obj, int targetObjNum, Set<Integer> visited) {
        if (obj instanceof COSObjectReference) {
            return ((COSObjectReference) obj).getKey().getObjectNumber() == targetObjNum;
        }
        if (obj instanceof COSDictionary) {
            for (var entry : (COSDictionary) obj) {
                if (containsReference(entry.getValue(), targetObjNum, visited)) return true;
            }
        }
        if (obj instanceof COSArray) {
            COSArray arr = (COSArray) obj;
            for (int i = 0; i < arr.size(); i++) {
                if (containsReference(arr.get(i), targetObjNum, visited)) return true;
            }
        }
        return false;
    }

    // ─── COS type utilities ─────────────────────────────────────

    /**
     * Get a human-readable COS type name for an object.
     *
     * @param obj a COS object
     * @return type name such as "Dictionary", "Stream", "Array", "Integer", etc.
     */
    public static String getCosTypeName(COSBase obj) {
        if (obj instanceof COSStream) return "Stream";
        if (obj instanceof COSDictionary) return "Dictionary";
        if (obj instanceof COSArray) return "Array";
        if (obj instanceof COSInteger) return "Integer";
        if (obj instanceof COSFloat) return "Float";
        if (obj instanceof COSString) return "String";
        if (obj instanceof COSName) return "Name";
        if (obj instanceof COSBoolean) return "Boolean";
        if (obj instanceof COSNull) return "Null";
        if (obj instanceof COSObjectReference) return "Reference";
        return obj.getClass().getSimpleName();
    }

    /**
     * Get the /Type value from a dictionary object, or empty string.
     *
     * @param obj a COS object
     * @return the /Type value, or "" if not a dictionary or no /Type key
     */
    public static String getPdfType(COSBase obj) {
        if (obj instanceof COSDictionary) {
            String type = ((COSDictionary) obj).getNameAsString("Type");
            return type != null ? type : "";
        }
        return "";
    }

    // ─── ObjectInfo ─────────────────────────────────────────────

    /**
     * Metadata record for a single PDF object — for programmatic enumeration and inspection.
     */
    public static class ObjectInfo {
        /** Object number in the xref table. */
        public final int objectNumber;
        /** Generation number. */
        public final int generationNumber;
        /** Object key (number + generation). */
        public final COSObjectKey key;
        /** The COS object itself. */
        public final COSBase object;
        /** COS type name: "Dictionary", "Stream", "Array", "Integer", etc. */
        public final String cosType;
        /** /Type value from dictionary: "Catalog", "Pages", "Page", "Font", etc. Empty if none. */
        public final String pdfType;
        /** /Subtype (or /S) value from dictionary. Empty if none. */
        public final String pdfSubtype;
        /** True if this is a COSStream. */
        public final boolean isStream;
        /** Number of dictionary keys (-1 if not a dictionary). */
        public final int dictSize;
        /** Encoded stream length in bytes (-1 if not a stream). */
        public final long streamLength;
        /** Dictionary key names (empty list if not a dictionary). */
        public final List<String> dictKeys;

        ObjectInfo(COSObjectKey key, COSBase obj, String cosType) {
            this.key = key;
            this.objectNumber = key.getObjectNumber();
            this.generationNumber = key.getGenerationNumber();
            this.object = obj;
            this.cosType = cosType;
            this.isStream = obj instanceof COSStream;

            if (obj instanceof COSDictionary) {
                COSDictionary dict = (COSDictionary) obj;
                String t = dict.getNameAsString("Type");
                this.pdfType = t != null ? t : "";
                String st = dict.getNameAsString("Subtype");
                if (st == null) st = dict.getNameAsString("S");
                this.pdfSubtype = st != null ? st : "";
                this.dictSize = dict.size();
                List<String> keys = new ArrayList<>();
                for (var entry : dict) keys.add(entry.getKey().getName());
                this.dictKeys = Collections.unmodifiableList(keys);
            } else {
                this.pdfType = "";
                this.pdfSubtype = "";
                this.dictSize = -1;
                this.dictKeys = Collections.emptyList();
            }

            if (obj instanceof COSStream) {
                this.streamLength = ((COSStream) obj).getLength();
            } else {
                this.streamLength = -1;
            }
        }

        /** Check if this object has the given /Type. */
        public boolean isType(String type) { return type.equals(pdfType); }

        /** Check if the dictionary contains the given key. */
        public boolean hasKey(String key) { return dictKeys.contains(key); }

        /** Cast to COSDictionary, or null. */
        public COSDictionary asDict() {
            return (object instanceof COSDictionary) ? (COSDictionary) object : null;
        }

        /** Cast to COSStream, or null. */
        public COSStream asStream() {
            return (object instanceof COSStream) ? (COSStream) object : null;
        }

        /** Cast to COSArray, or null. */
        public COSArray asArray() {
            return (object instanceof COSArray) ? (COSArray) object : null;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(objectNumber).append(" ").append(generationNumber)
                    .append(" = ").append(cosType);
            if (!pdfType.isEmpty()) sb.append(" /Type=").append(pdfType);
            if (!pdfSubtype.isEmpty()) sb.append(" /Subtype=").append(pdfSubtype);
            if (isStream) sb.append(" stream(").append(streamLength).append(" bytes)");
            if (dictSize > 0) sb.append(" keys=").append(dictKeys);
            return sb.toString();
        }
    }
}
