package org.aspose.pdf;

import org.aspose.pdf.engine.parser.PDFParser;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;

import java.io.IOException;
import java.util.logging.Logger;

/// Wraps a PDF resource dictionary (ISO 32000-1:2008, §7.8.3).
///
/// A resource dictionary maps resource names to objects required for rendering
/// page content: fonts, XObjects, graphics states, color spaces, patterns,
/// shadings, and properties. Each getter lazily retrieves the corresponding
/// sub-dictionary and dereferences indirect object references.
///
public class Resources {

    private static final Logger LOG = Logger.getLogger(Resources.class.getName());

    private static final PdfName FONT = PdfName.of("Font");
    private static final PdfName XOBJECT = PdfName.of("XObject");
    private static final PdfName EXT_G_STATE = PdfName.of("ExtGState");
    private static final PdfName COLOR_SPACE = PdfName.of("ColorSpace");
    private static final PdfName PATTERN = PdfName.of("Pattern");
    private static final PdfName SHADING = PdfName.of("Shading");
    private static final PdfName PROPERTIES = PdfName.of("Properties");

    private final PdfDictionary dict;
    private final PDFParser parser;

    /// Creates a Resources wrapper around the given PDF dictionary.
    ///
    /// @param dict the /Resources dictionary from a page or form XObject
    /// @throws IllegalArgumentException if dict is null
    public Resources(PdfDictionary dict) {
        this(dict, null);
    }

    /// Creates a Resources wrapper with a PDF parser for resolving indirect references.
    ///
    /// @param dict   the /Resources dictionary
    /// @param parser the PDF parser (may be null)
    /// @throws IllegalArgumentException if dict is null
    public Resources(PdfDictionary dict, PDFParser parser) {
        if (dict == null) {
            throw new IllegalArgumentException("Resources dictionary must not be null");
        }
        this.dict = dict;
        this.parser = parser;
        LOG.fine(() -> "Resources created with " + dict.size() + " entries");
    }

    /// Returns the /Font sub-dictionary, or null if absent.
    ///
    /// @return the font dictionary, or null
    public PdfDictionary getFonts() {
        return getSubDictionary(FONT);
    }

    /// Returns the /XObject sub-dictionary, or null if absent.
    ///
    /// @return the XObject dictionary, or null
    public PdfDictionary getXObjects() {
        return getSubDictionary(XOBJECT);
    }

    /// Returns the /ExtGState sub-dictionary, or null if absent.
    ///
    /// @return the extended graphics state dictionary, or null
    public PdfDictionary getExtGState() {
        return getSubDictionary(EXT_G_STATE);
    }

    /// Returns the /ColorSpace sub-dictionary, or null if absent.
    ///
    /// @return the color space dictionary, or null
    public PdfDictionary getColorSpaces() {
        return getSubDictionary(COLOR_SPACE);
    }

    /// Returns the /Pattern sub-dictionary, or null if absent.
    ///
    /// @return the pattern dictionary, or null
    public PdfDictionary getPatterns() {
        return getSubDictionary(PATTERN);
    }

    /// Returns the /Shading sub-dictionary, or null if absent.
    ///
    /// @return the shading dictionary, or null
    public PdfDictionary getShadings() {
        return getSubDictionary(SHADING);
    }

    /// Returns the /Properties sub-dictionary, or null if absent.
    ///
    /// @return the properties dictionary, or null
    public PdfDictionary getProperties() {
        return getSubDictionary(PROPERTIES);
    }

    /// Returns the collection of image XObjects from /XObject.
    ///
    /// @return the image collection, or null if no /XObject dictionary
    public XImageCollection getImages() {
        PdfDictionary xobjects = getXObjects();
        if (xobjects == null) {
            // Lazy-create /XObject dictionary for new pages
            xobjects = new PdfDictionary();
            dict.set(PdfName.of("XObject"), xobjects);
        }
        return new XImageCollection(dict, xobjects, parser);
    }

    /// Returns the collection of Form XObjects from /XObject (ISO 32000-1:2008, §8.10).
    ///
    /// The collection is a live view: entries added to the underlying /XObject
    /// dictionary with `/Subtype /Form` appear on subsequent access. Lazily
    /// creates the /XObject sub-dictionary if absent.
    ///
    /// @return the form collection (never null; may be empty)
    public XFormCollection getForms() {
        PdfDictionary xobjects = getXObjects();
        if (xobjects == null) {
            xobjects = new PdfDictionary();
            dict.set(XOBJECT, xobjects);
        }
        return new XFormCollection(xobjects, parser);
    }

    /// Returns the underlying PDF dictionary.
    ///
    /// @return the raw PDF dictionary
    public PdfDictionary getPdfDictionary() {
        return dict;
    }

    /// Retrieves a sub-dictionary by key, dereferencing indirect references if needed.
    ///
    /// @param key the dictionary key
    /// @return the sub-dictionary, or null if absent or not a dictionary
    private PdfDictionary getSubDictionary(PdfName key) {
        PdfBase value = dict.get(key);
        if (value == null) {
            return null;
        }
        // Dereference indirect object references
        PdfBase resolved = value;
        if (resolved instanceof PdfObjectReference) {
            try {
                resolved = ((PdfObjectReference) resolved).dereference();
            } catch (IOException e) {
                LOG.warning(() -> "Failed to dereference " + key + ": " + e.getMessage());
                return null;
            }
        }
        if (resolved instanceof PdfDictionary) {
            return (PdfDictionary) resolved;
        }
        PdfBase finalResolved = resolved;
        LOG.fine(() -> "Value for " + key + " is not a dictionary: " + finalResolved.getClass().getSimpleName());
        return null;
    }
}
