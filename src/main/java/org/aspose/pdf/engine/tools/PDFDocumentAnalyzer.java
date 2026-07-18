package org.aspose.pdf.engine.tools;

import org.aspose.pdf.engine.io.RandomAccessReader;
import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfobjects.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/// Programmatic analyzer for PDF documents.
/// Opens a PDF file, parses it, and provides structured access to all PDF objects,
/// page tree, trailer, catalog, etc.
///
/// This class contains NO console/CLI logic — it is purely a data layer.
/// For CLI inspection use `org.aspose.pdf.tools.PDFInspector` in the pdf-tools module.
///
/// Usage example:
///
/// <pre>
///   PDFDocumentAnalyzer analyzer = new PDFDocumentAnalyzer();
///   analyzer.open("file.pdf");
///   List&lt;ObjectInfo&gt; objects = analyzer.getObjects();
///   ObjectInfo catalog = analyzer.findByType("Catalog");
///   analyzer.close();
/// </pre>
public class PDFDocumentAnalyzer implements Closeable {

    private PDFParser parser;
    private RandomAccessReader reader;
    private String sourceName;
    private long sourceSize;
    private long parseTimeMs;

    // ─── Open methods ────────────────────────────────────────────

    /// Open a PDF from a file path.
    ///
    /// @param path path to the PDF file
    /// @throws IOException if the file cannot be read or parsed
    public void open(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + path);
        }
        open(RandomAccessReader.fromFile(file), file.getAbsolutePath(), file.length());
    }

    /// Open a PDF from an InputStream. The stream is read entirely into memory.
    ///
    /// @param inputStream the input stream containing PDF data
    /// @throws IOException if the stream cannot be read or parsed
    public void open(InputStream inputStream) throws IOException {
        open(inputStream, "<InputStream>");
    }

    /// Open a PDF from an InputStream with a source name for diagnostics.
    ///
    /// @param inputStream the input stream containing PDF data
    /// @param sourceName  descriptive name of the source (for logging)
    /// @throws IOException if the stream cannot be read or parsed
    public void open(InputStream inputStream, String sourceName) throws IOException {
        RandomAccessReader r = RandomAccessReader.fromStream(inputStream);
        open(r, sourceName, r.getLength());
    }

    /// Open a PDF from a byte array.
    ///
    /// @param pdfBytes the PDF content as bytes
    /// @throws IOException if the data cannot be parsed
    public void open(byte[] pdfBytes) throws IOException {
        open(pdfBytes, "<byte[]>");
    }

    /// Open a PDF from a byte array with a source name for diagnostics.
    ///
    /// @param pdfBytes   the PDF content as bytes
    /// @param sourceName descriptive name of the source (for logging)
    /// @throws IOException if the data cannot be parsed
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

    /// Returns the underlying parser for direct access.
    public PDFParser getParser() { return parser; }

    /// PDF version (e.g. 1.4, 1.5, 2.0).
    public float getVersion() { return parser.getVersion(); }

    /// Number of objects in the xref table.
    public int getObjectCount() { return parser.getAllObjectKeys().size(); }

    /// Trailer dictionary.
    public PdfDictionary getTrailer() { return parser.getTrailer(); }

    /// Source name (file path or descriptive label).
    public String getSourceName() { return sourceName; }

    /// Source size in bytes.
    public long getSourceSize() { return sourceSize; }

    /// Time spent parsing, in milliseconds.
    public long getParseTimeMs() { return parseTimeMs; }

    // ─── Object access ──────────────────────────────────────────

    /// Load a single object by number (generation = 0).
    ///
    /// @param objectNumber the object number
    /// @return the PDF object, or null if not found
    /// @throws IOException if the object cannot be read
    public PdfBase getObject(int objectNumber) throws IOException {
        return parser.getObject(new PdfObjectKey(objectNumber, 0));
    }

    /// Resolve an indirect reference to its target object.
    ///
    /// @param obj a PDF object (possibly a PdfObjectReference)
    /// @return the resolved object
    /// @throws IOException if resolution fails
    public PdfBase resolve(PdfBase obj) throws IOException {
        return parser.resolveReference(obj);
    }

    /// All object keys, sorted by object number.
    public List<PdfObjectKey> getSortedKeys() {
        List<PdfObjectKey> keys = new ArrayList<>(parser.getAllObjectKeys());
        Collections.sort(keys);
        return keys;
    }

    /// Load all objects into a map. Errors are collected, not thrown.
    ///
    /// @return map of object key to loaded PDF object
    public Map<PdfObjectKey, PdfBase> loadAllObjects() {
        Map<PdfObjectKey, PdfBase> result = new LinkedHashMap<>();
        for (PdfObjectKey key : getSortedKeys()) {
            try {
                PdfBase obj = parser.getObject(key);
                if (obj != null && !(obj instanceof PdfNull)) {
                    result.put(key, obj);
                }
            } catch (Exception e) {
                // skip — caller can use getObjects() for error-tolerant listing
            }
        }
        return result;
    }

    // ─── Structured queries ─────────────────────────────────────

    /// Get all objects with metadata as [ObjectInfo] records.
    ///
    /// @return list of ObjectInfo for every object in the document
    public List<ObjectInfo> getObjects() {
        List<ObjectInfo> result = new ArrayList<>();
        for (PdfObjectKey key : getSortedKeys()) {
            try {
                PdfBase obj = parser.getObject(key);
                if (obj != null && !(obj instanceof PdfNull)) {
                    result.add(new ObjectInfo(key, obj, getCosTypeName(obj)));
                }
            } catch (Exception e) {
                result.add(new ObjectInfo(key, PdfNull.INSTANCE, "ERROR:" + e.getMessage()));
            }
        }
        return result;
    }

    /// Find the first object with the given /Type value, or null.
    public ObjectInfo findByType(String type) {
        return getObjects().stream().filter(o -> o.isType(type)).findFirst().orElse(null);
    }

    /// Find all objects with the given /Type value.
    public List<ObjectInfo> findAllByType(String type) {
        return getObjects().stream().filter(o -> o.isType(type)).collect(Collectors.toList());
    }

    /// Find all objects whose dictionary contains the given key.
    public List<ObjectInfo> findByKey(String key) {
        return getObjects().stream().filter(o -> o.hasKey(key)).collect(Collectors.toList());
    }

    /// Find all stream objects.
    public List<ObjectInfo> getStreams() {
        return getObjects().stream().filter(o -> o.isStream).collect(Collectors.toList());
    }

    /// Count objects by PDF object type name.
    public Map<String, Integer> getTypeCounts() {
        Map<String, Integer> counts = new TreeMap<>();
        for (PdfObjectKey key : parser.getAllObjectKeys()) {
            try {
                PdfBase obj = parser.getObject(key);
                String type = getCosTypeName(obj);
                counts.merge(type, 1, Integer::sum);
            } catch (Exception e) {
                counts.merge("ERROR", 1, Integer::sum);
            }
        }
        return counts;
    }

    /// Get the page tree: list of Page objects in document order.
    ///
    /// @return list of ObjectInfo for each page
    /// @throws IOException if page tree cannot be traversed
    public List<ObjectInfo> getPages() throws IOException {
        List<ObjectInfo> pages = new ArrayList<>();
        PdfDictionary catalog = parser.getCatalog();
        PdfBase pagesRef = catalog.get("Pages");
        PdfBase pagesObj = parser.resolveReference(pagesRef);
        if (pagesObj instanceof PdfDictionary) {
            collectPages((PdfDictionary) pagesObj, pages);
        }
        return pages;
    }

    private void collectPages(PdfDictionary node, List<ObjectInfo> pages) throws IOException {
        String type = node.getNameAsString("Type");
        if ("Page".equals(type)) {
            PdfObjectKey key = node.getObjectKey();
            if (key == null) key = new PdfObjectKey(0, 0);
            pages.add(new ObjectInfo(key, node, "Dictionary"));
        } else if ("Pages".equals(type)) {
            PdfBase kidsObj = node.get("Kids");
            PdfBase kids = parser.resolveReference(kidsObj);
            if (kids instanceof PdfArray) {
                for (int i = 0; i < ((PdfArray) kids).size(); i++) {
                    PdfBase childRef = ((PdfArray) kids).get(i);
                    PdfBase child = parser.resolveReference(childRef);
                    if (child instanceof PdfDictionary) {
                        collectPages((PdfDictionary) child, pages);
                    }
                }
            }
        }
    }

    /// Check whether a PDF object tree contains a reference to a given object number.
    ///
    /// @param obj          the root of the subtree to search
    /// @param targetObjNum the target object number
    /// @return true if a reference to targetObjNum is found
    public boolean containsReference(PdfBase obj, int targetObjNum) {
        return containsReference(obj, targetObjNum, new HashSet<>());
    }

    private boolean containsReference(PdfBase obj, int targetObjNum, Set<Integer> visited) {
        if (obj instanceof PdfObjectReference) {
            return ((PdfObjectReference) obj).getKey().getObjectNumber() == targetObjNum;
        }
        if (obj instanceof PdfDictionary) {
            for (var entry : (PdfDictionary) obj) {
                if (containsReference(entry.getValue(), targetObjNum, visited)) return true;
            }
        }
        if (obj instanceof PdfArray) {
            PdfArray arr = (PdfArray) obj;
            for (int i = 0; i < arr.size(); i++) {
                if (containsReference(arr.get(i), targetObjNum, visited)) return true;
            }
        }
        return false;
    }

    // ─── PDF object type utilities ─────────────────────────────────────

    /// Get a human-readable PDF object type name for an object.
    ///
    /// @param obj a PDF object
    /// @return type name such as "Dictionary", "Stream", "Array", "Integer", etc.
    public static String getCosTypeName(PdfBase obj) {
        if (obj instanceof PdfStream) return "Stream";
        if (obj instanceof PdfDictionary) return "Dictionary";
        if (obj instanceof PdfArray) return "Array";
        if (obj instanceof PdfInteger) return "Integer";
        if (obj instanceof PdfFloat) return "Float";
        if (obj instanceof PdfString) return "String";
        if (obj instanceof PdfName) return "Name";
        if (obj instanceof PdfBoolean) return "Boolean";
        if (obj instanceof PdfNull) return "Null";
        if (obj instanceof PdfObjectReference) return "Reference";
        return obj.getClass().getSimpleName();
    }

    /// Get the /Type value from a dictionary object, or empty string.
    ///
    /// @param obj a PDF object
    /// @return the /Type value, or "" if not a dictionary or no /Type key
    public static String getPdfType(PdfBase obj) {
        if (obj instanceof PdfDictionary) {
            String type = ((PdfDictionary) obj).getNameAsString("Type");
            return type != null ? type : "";
        }
        return "";
    }

    // ─── ObjectInfo ─────────────────────────────────────────────

    /// Metadata record for a single PDF object — for programmatic enumeration and inspection.
    public static class ObjectInfo {
        /// Object number in the xref table.
        public final int objectNumber;
        /// Generation number.
        public final int generationNumber;
        /// Object key (number + generation).
        public final PdfObjectKey key;
        /// The PDF object itself.
        public final PdfBase object;
        /// PDF object type name: "Dictionary", "Stream", "Array", "Integer", etc.
        public final String cosType;
        /// /Type value from dictionary: "Catalog", "Pages", "Page", "Font", etc. Empty if none.
        public final String pdfType;
        /// /Subtype (or /S) value from dictionary. Empty if none.
        public final String pdfSubtype;
        /// True if this is a PdfStream.
        public final boolean isStream;
        /// Number of dictionary keys (-1 if not a dictionary).
        public final int dictSize;
        /// Encoded stream length in bytes (-1 if not a stream).
        public final long streamLength;
        /// Dictionary key names (empty list if not a dictionary).
        public final List<String> dictKeys;

        ObjectInfo(PdfObjectKey key, PdfBase obj, String cosType) {
            this.key = key;
            this.objectNumber = key.getObjectNumber();
            this.generationNumber = key.getGenerationNumber();
            this.object = obj;
            this.cosType = cosType;
            this.isStream = obj instanceof PdfStream;

            if (obj instanceof PdfDictionary) {
                PdfDictionary dict = (PdfDictionary) obj;
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

            if (obj instanceof PdfStream) {
                this.streamLength = ((PdfStream) obj).getLength();
            } else {
                this.streamLength = -1;
            }
        }

        /// Check if this object has the given /Type.
        public boolean isType(String type) { return type.equals(pdfType); }

        /// Check if the dictionary contains the given key.
        public boolean hasKey(String key) { return dictKeys.contains(key); }

        /// Cast to PdfDictionary, or null.
        public PdfDictionary asDict() {
            return (object instanceof PdfDictionary) ? (PdfDictionary) object : null;
        }

        /// Cast to PdfStream, or null.
        public PdfStream asStream() {
            return (object instanceof PdfStream) ? (PdfStream) object : null;
        }

        /// Cast to PdfArray, or null.
        public PdfArray asArray() {
            return (object instanceof PdfArray) ? (PdfArray) object : null;
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
