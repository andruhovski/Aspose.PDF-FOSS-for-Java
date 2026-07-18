package org.aspose.pdf.facades;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.PageCollection;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.pdfobjects.PdfArray;
import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfString;
import org.aspose.pdf.text.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/// Provides methods for editing PDF content, primarily text replacement operations.
///
/// Usage pattern: create an instance, bind a PDF via [#bindPdf(String)],
/// perform operations, then [#save(String)].
public class PdfContentEditor implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(PdfContentEditor.class.getName());

    private Document document;
    private TextReplaceOptions textReplaceOptions;
    private final ReplaceTextStrategy replaceTextStrategy = new ReplaceTextStrategy();

    /// Creates a new `PdfContentEditor` instance.
    public PdfContentEditor() {
    }

    /// Returns the text replace options used by this editor.
    ///
    /// @return the text replace options, or null
    public TextReplaceOptions getTextReplaceOptions() {
        return textReplaceOptions;
    }

    /// Sets the text replace options.
    ///
    /// @param options the text replace options
    public void setTextReplaceOptions(TextReplaceOptions options) {
        this.textReplaceOptions = options;
    }

    /// Returns text-replacement strategy settings.
    ///
    /// @return replacement strategy configuration
    public ReplaceTextStrategy getReplaceTextStrategy() {
        return replaceTextStrategy;
    }

    /// Binds a PDF file to this editor.
    ///
    /// @param inputFile path to the PDF file
    /// @return `true` on success
    public boolean bindPdf(String inputFile) {
        try {
            this.document = new Document(inputFile);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to bind PDF from file: " + inputFile, e);
            return false;
        }
    }

    /// Binds a PDF from an input stream.
    ///
    /// @param inputStream the input stream containing PDF data
    /// @return `true` on success
    public boolean bindPdf(InputStream inputStream) {
        try {
            this.document = new Document(inputStream);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to bind PDF from stream", e);
            return false;
        }
    }

    /// Binds an existing [Document] to this editor.
    ///
    /// @param document the document to bind
    /// @return `true` on success
    public boolean bindPdf(Document document) {
        if (document == null) {
            LOG.warning("Cannot bind null document");
            return false;
        }
        this.document = document;
        return true;
    }

    /// Saves the bound document to a file.
    ///
    /// @param outputFile path to the output file
    /// @return `true` on success
    public boolean save(String outputFile) {
        try {
            document.requestFullRewrite();
            document.save(outputFile);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to save PDF to file: " + outputFile, e);
            return false;
        }
    }

    /// Saves the bound document to an output stream.
    ///
    /// @param outputStream the output stream
    /// @return `true` on success
    public boolean save(OutputStream outputStream) {
        try {
            document.requestFullRewrite();
            document.save(outputStream);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to save PDF to stream", e);
            return false;
        }
    }

    /// Replaces all occurrences of the specified text throughout the entire document.
    ///
    /// @param searchText  the text to find
    /// @param replaceText the replacement text
    /// @return `true` if at least one replacement was made
    public boolean replaceText(String searchText, String replaceText) {
        try {
            TextFragmentAbsorber absorber = createAbsorber(searchText);
            PageCollection pages = document.getPages();
            boolean replaced = false;
            for (int i = 1; i <= pages.getCount(); i++) {
                Page page = pages.get(i);
                page.accept(absorber);
            }
            TextFragmentCollection fragments = absorber.getTextFragments();
            int limit = replacementLimit(fragments);
            for (int i = 1; i <= limit; i++) {
                TextFragment fragment = fragments.get(i);
                fragment.setText(replaceText);
                replaced = true;
            }
            LOG.fine("Replaced text '" + searchText + "' with '" + replaceText + "' in document");
            return replaced;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to replace text", e);
            return false;
        }
    }

    /// Replaces all occurrences of the specified text on a specific page.
    ///
    /// @param searchText  the text to find
    /// @param pageNumber  1-based page number
    /// @param replaceText the replacement text
    /// @return `true` if at least one replacement was made
    public boolean replaceText(String searchText, int pageNumber, String replaceText) {
        try {
            PageCollection pages = document.getPages();
            if (pageNumber < 1 || pageNumber > pages.getCount()) {
                LOG.warning("Page number " + pageNumber + " is out of range");
                return false;
            }
            TextFragmentAbsorber absorber = createAbsorber(searchText);
            Page page = pages.get(pageNumber);
            page.accept(absorber);
            boolean replaced = false;
            TextFragmentCollection fragments = absorber.getTextFragments();
            int limit = replacementLimit(fragments);
            for (int i = 1; i <= limit; i++) {
                TextFragment fragment = fragments.get(i);
                fragment.setText(replaceText);
                replaced = true;
            }
            LOG.fine("Replaced text on page " + pageNumber);
            return replaced;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to replace text on page " + pageNumber, e);
            return false;
        }
    }

    /// Returns the bound document.
    ///
    /// @return the document, or `null` if no document is bound
    public Document getDocument() {
        return document;
    }

    /// Returns stamp metadata stored on a page.
    ///
    /// @param pageNumber 1-based page number
    /// @return an array of stamp info records
    public StampInfo[] getStamps(int pageNumber) {
        if (document == null) {
            return new StampInfo[0];
        }
        try {
            PageCollection pages = document.getPages();
            if (pageNumber < 1 || pageNumber > pages.getCount()) {
                return new StampInfo[0];
            }
            PdfArray records = pages.get(pageNumber).getStampInfoRecords();
            java.util.List<StampInfo> infos = new java.util.ArrayList<>();
            for (int i = 0; i < records.size(); i++) {
                PdfBase item = records.get(i);
                if (!(item instanceof PdfDictionary)) {
                    continue;
                }
                PdfDictionary dict = (PdfDictionary) item;
                String type = null;
                PdfBase typeBase = dict.get("Type");
                if (typeBase instanceof PdfString) {
                    type = ((PdfString) typeBase).getString();
                }
                String text = null;
                PdfBase textBase = dict.get("Text");
                if (textBase instanceof PdfString) {
                    text = ((PdfString) textBase).getString();
                }
                int stampId = dict.getInt("StampId", 0);
                double x = dict.getFloat("X", 0);
                double y = dict.getFloat("Y", 0);
                double width = dict.getFloat("Width", 0);
                double height = dict.getFloat("Height", 0);
                infos.add(new StampInfo(stampId, type, text, new Rectangle(x, y, x + width, y + height)));
            }
            return infos.toArray(new StampInfo[0]);
        } catch (IOException e) {
            LOG.log(Level.FINE, "Failed to enumerate stamps", e);
            return new StampInfo[0];
        }
    }

    /// Deletes a stamp metadata entry by stamp id.
    ///
    /// @param pageNumber 1-based page number
    /// @param stampId stamp identifier
    /// @return `true` if a metadata entry was removed
    public boolean deleteStampById(int pageNumber, int stampId) {
        if (document == null) {
            return false;
        }
        try {
            PageCollection pages = document.getPages();
            if (pageNumber < 1 || pageNumber > pages.getCount()) {
                return false;
            }
            boolean removed = pages.get(pageNumber).removeStampById(stampId);
            if (removed) {
                document.requestFullRewrite();
            }
            return removed;
        } catch (IOException e) {
            LOG.log(Level.FINE, "Failed to delete stamp metadata", e);
        }
        return false;
    }

    /// Deletes stamp metadata entries by stamp id from all pages.
    ///
    /// @param stampId stamp identifier
    /// @return `true` if at least one metadata entry was removed
    public boolean deleteStampById(int stampId) {
        if (document == null) {
            return false;
        }
        boolean removed = false;
        try {
            PageCollection pages = document.getPages();
            for (int i = 1; i <= pages.getCount(); i++) {
                removed |= deleteStampById(i, stampId);
            }
        } catch (IOException e) {
            LOG.log(Level.FINE, "Failed to delete stamp metadata", e);
        }
        return removed;
    }

    /// Creates a [org.aspose.pdf.annotations.TextAnnotation] on
    /// the given page at the given rectangle. Mirrors C#
    /// `PdfContentEditor.CreateText(Rectangle, string, string, bool, string, int)`.
    public boolean createText(org.aspose.pdf.Rectangle rect, String title,
                              String contents, boolean open, String icon, int pageNumber) {
        if (document == null) {
            LOG.warning("createText requires a bound document");
            return false;
        }
        try {
            org.aspose.pdf.PageCollection pages = document.getPages();
            if (pageNumber < 1 || pageNumber > pages.getCount()) return false;
            org.aspose.pdf.Page page = pages.get(pageNumber);
            org.aspose.pdf.annotations.TextAnnotation a =
                    new org.aspose.pdf.annotations.TextAnnotation(page, rect);
            if (title != null) a.setTitle(title);
            if (contents != null) a.setContents(contents);
            a.setOpen(open);
            if (icon != null) a.setIcon(icon);
            page.getAnnotations().add(a);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "createText failed", e);
            return false;
        }
    }

    /// Closes the editor and releases the bound document.
    public void close() {
        if (document != null) {
            try {
                document.close();
            } catch (IOException e) {
                LOG.log(Level.FINE, "Error closing document", e);
            }
            document = null;
        }
    }

    private int replacementLimit(TextFragmentCollection fragments) {
        // textReplaceOptions takes precedence when explicitly set: the C# usage
        // pattern is to mutate that property without touching the (legacy)
        // ReplaceTextStrategy, and expect REPLACE_ALL to override the default
        // REPLACE_FIRST scope on ReplaceTextStrategy.
        if (textReplaceOptions != null) {
            return textReplaceOptions.getReplaceScope() == TextReplaceOptions.Scope.REPLACE_FIRST
                    ? Math.min(1, fragments.getCount())
                    : fragments.getCount();
        }
        if (replaceTextStrategy.getReplaceScope() == ReplaceTextStrategy.Scope.REPLACE_FIRST) {
            return Math.min(1, fragments.getCount());
        }
        return fragments.getCount();
    }

    private TextFragmentAbsorber createAbsorber(String searchText) {
        if (replaceTextStrategy.isRegularExpressionUsed()) {
            return new TextFragmentAbsorber(searchText, new TextSearchOptions(true));
        }
        return new TextFragmentAbsorber(searchText);
    }
}
