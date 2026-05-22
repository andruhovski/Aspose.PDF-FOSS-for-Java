package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSObjectReference;
import org.aspose.pdf.engine.cos.COSStream;
import org.aspose.pdf.engine.cos.COSString;
import org.aspose.pdf.engine.cos.NameTree;
import org.aspose.pdf.engine.parser.PDFParser;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Provides access to the JavaScript name tree of a PDF document
 * (ISO 32000-1:2008, §12.6.4.16 and §7.9.6).
 *
 * <p>JavaScript actions are stored in the document catalog under
 * {@code /Names → /JavaScript}, a name tree mapping names to JavaScript
 * action dictionaries. The traversal is delegated to {@link NameTree}.</p>
 */
public class JavaScriptCollection implements Iterable<Map.Entry<String, String>> {

    private static final Logger LOG = Logger.getLogger(JavaScriptCollection.class.getName());

    private final Map<String, String> scripts;

    /**
     * Creates a JavaScriptCollection by reading the /Names → /JavaScript name tree
     * from the document catalog.
     *
     * @param catalog the document catalog dictionary
     * @param parser  the PDF parser for resolving references (may be null)
     */
    public JavaScriptCollection(COSDictionary catalog, PDFParser parser) {
        this.scripts = new LinkedHashMap<>();
        if (catalog == null) return;
        try {
            COSBase namesObj = resolve(catalog.get("Names"), parser);
            if (!(namesObj instanceof COSDictionary)) return;

            COSBase jsObj = resolve(((COSDictionary) namesObj).get("JavaScript"), parser);
            if (!(jsObj instanceof COSDictionary)) return;

            NameTree tree = new NameTree((COSDictionary) jsObj);
            for (Map.Entry<String, COSBase> entry : tree.entries()) {
                String jsCode = extractJavaScript(entry.getValue());
                if (jsCode != null) {
                    scripts.put(entry.getKey(), jsCode);
                }
            }
        } catch (Exception e) {
            LOG.fine(() -> "Failed to parse JavaScript name tree: " + e.getMessage());
        }
    }

    /**
     * Returns the set of JavaScript action names.
     *
     * @return the set of names (keys)
     */
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(scripts.keySet());
    }

    /**
     * Returns the JavaScript source code associated with the given name.
     *
     * @param name the JavaScript action name
     * @return the JavaScript source, or null if not found
     */
    public String get(String name) {
        return scripts.get(name);
    }

    /**
     * Returns the number of JavaScript entries.
     *
     * @return the count
     */
    public int size() {
        return scripts.size();
    }

    /**
     * Returns an iterator over the name-to-script entries.
     *
     * @return an iterator of map entries
     */
    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return scripts.entrySet().iterator();
    }

    /**
     * Extracts a JavaScript string from a name-tree value. The value can be a
     * direct string or a JavaScript action dictionary
     * ({@code /S /JavaScript /JS …}).
     */
    private String extractJavaScript(COSBase value) {
        if (value instanceof COSString) {
            return ((COSString) value).getString();
        }
        if (value instanceof COSDictionary) {
            COSDictionary actionDict = (COSDictionary) value;
            COSBase jsVal = resolve(actionDict.get("JS"), null);
            if (jsVal instanceof COSString) {
                return ((COSString) jsVal).getString();
            }
            if (jsVal instanceof COSStream) {
                try {
                    byte[] data = ((COSStream) jsVal).getDecodedData();
                    return new String(data, java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception e) {
                    LOG.fine(() -> "Failed to read JS stream: " + e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * Resolves an indirect reference. Prefers the parser when supplied (it can
     * pull objects from compressed object streams that the reference's own
     * resolver might not be aware of), and otherwise falls back to the
     * built-in {@link COSObjectReference#dereference} path.
     */
    private static COSBase resolve(COSBase obj, PDFParser parser) {
        if (obj == null) return null;
        if (parser != null) {
            try {
                return parser.resolveReference(obj);
            } catch (IOException e) {
                return obj;
            }
        }
        if (obj instanceof COSObjectReference) {
            try {
                return ((COSObjectReference) obj).dereference();
            } catch (IOException e) {
                return obj;
            }
        }
        return obj;
    }
}
