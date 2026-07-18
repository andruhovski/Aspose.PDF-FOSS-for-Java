package org.aspose.pdf;

import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfobjects.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/// Provides access to the JavaScript name tree of a PDF document
/// (ISO 32000-1:2008, §12.6.4.16 and §7.9.6).
///
/// JavaScript actions are stored in the document catalog under
/// `/Names → /JavaScript`, a name tree mapping names to JavaScript
/// action dictionaries. The traversal is delegated to [NameTree].
public class JavaScriptCollection implements Iterable<Map.Entry<String, String>> {

    private static final Logger LOG = Logger.getLogger(JavaScriptCollection.class.getName());

    private final Map<String, String> scripts;

    /// Creates a JavaScriptCollection by reading the /Names → /JavaScript name tree
    /// from the document catalog.
    ///
    /// @param catalog the document catalog dictionary
    /// @param parser  the PDF parser for resolving references (may be null)
    public JavaScriptCollection(PdfDictionary catalog, PDFParser parser) {
        this.scripts = new LinkedHashMap<>();
        if (catalog == null) return;
        try {
            PdfBase namesObj = resolve(catalog.get("Names"), parser);
            if (!(namesObj instanceof PdfDictionary)) return;

            PdfBase jsObj = resolve(((PdfDictionary) namesObj).get("JavaScript"), parser);
            if (!(jsObj instanceof PdfDictionary)) return;

            NameTree tree = new NameTree((PdfDictionary) jsObj);
            for (Map.Entry<String, PdfBase> entry : tree.entries()) {
                String jsCode = extractJavaScript(entry.getValue());
                if (jsCode != null) {
                    scripts.put(entry.getKey(), jsCode);
                }
            }
        } catch (Exception e) {
            LOG.fine(() -> "Failed to parse JavaScript name tree: " + e.getMessage());
        }
    }

    /// Returns the set of JavaScript action names.
    ///
    /// @return the set of names (keys)
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(scripts.keySet());
    }

    /// Returns the JavaScript source code associated with the given name.
    ///
    /// @param name the JavaScript action name
    /// @return the JavaScript source, or null if not found
    public String get(String name) {
        return scripts.get(name);
    }

    /// Returns the number of JavaScript entries.
    ///
    /// @return the count
    public int size() {
        return scripts.size();
    }

    /// Returns an iterator over the name-to-script entries.
    ///
    /// @return an iterator of map entries
    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return scripts.entrySet().iterator();
    }

    /// Extracts a JavaScript string from a name-tree value. The value can be a
    /// direct string or a JavaScript action dictionary
    /// (`/S /JavaScript /JS …`).
    private String extractJavaScript(PdfBase value) {
        if (value instanceof PdfString) {
            return ((PdfString) value).getString();
        }
        if (value instanceof PdfDictionary) {
            PdfDictionary actionDict = (PdfDictionary) value;
            PdfBase jsVal = resolve(actionDict.get("JS"), null);
            if (jsVal instanceof PdfString) {
                return ((PdfString) jsVal).getString();
            }
            if (jsVal instanceof PdfStream) {
                try {
                    byte[] data = ((PdfStream) jsVal).getDecodedData();
                    return new String(data, java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception e) {
                    LOG.fine(() -> "Failed to read JS stream: " + e.getMessage());
                }
            }
        }
        return null;
    }

    /// Resolves an indirect reference. Prefers the parser when supplied (it can
    /// pull objects from compressed object streams that the reference's own
    /// resolver might not be aware of), and otherwise falls back to the
    /// built-in [PdfObjectReference#dereference] path.
    private static PdfBase resolve(PdfBase obj, PDFParser parser) {
        if (obj == null) return null;
        if (parser != null) {
            try {
                return parser.resolveReference(obj);
            } catch (IOException e) {
                return obj;
            }
        }
        if (obj instanceof PdfObjectReference) {
            try {
                return ((PdfObjectReference) obj).dereference();
            } catch (IOException e) {
                return obj;
            }
        }
        return obj;
    }
}
