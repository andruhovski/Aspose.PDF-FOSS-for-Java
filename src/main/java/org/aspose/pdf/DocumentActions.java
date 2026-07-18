package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfObjectReference;

import java.io.IOException;
import java.util.logging.Logger;

/// Document-level action triggers (ISO 32000-1:2008, §12.6.4.1, p.417).
///
/// Exposes the document's `/OpenAction` (run on open) and the entries
/// of its `/AA` (additional-actions) dictionary:
///
///   - `WC` — will close ([#getBeforeClosing]/[#setBeforeClosing])
///   - `WS` — will save ([#getBeforeSaving]/[#setBeforeSaving])
///   - `DS` — did save ([#getAfterSaving]/[#setAfterSaving])
///   - `WP` — will print ([#getBeforePrinting]/[#setBeforePrinting])
///   - `DP` — did print ([#getAfterPrinting]/[#setAfterPrinting])
///
/// Setting an action to `null` removes the corresponding entry. Reading
/// an entry whose value is a destination (not an action dictionary) returns
/// `null` for [#getOpenAction()]; see [#setOpenAction(PdfAction)]
/// to bypass the action wrapping.
public class DocumentActions {

    private static final Logger LOG = Logger.getLogger(DocumentActions.class.getName());

    private static final PdfName OPEN_ACTION = PdfName.of("OpenAction");
    private static final PdfName AA = PdfName.of("AA");
    private static final PdfName WC = PdfName.of("WC");
    private static final PdfName WS = PdfName.of("WS");
    private static final PdfName DS = PdfName.of("DS");
    private static final PdfName WP = PdfName.of("WP");
    private static final PdfName DP = PdfName.of("DP");

    private final PdfDictionary catalog;
    private final Document document;

    /// Wraps the given catalog dictionary as a document-actions view.
    ///
    /// @param catalog  the catalog PdfDictionary (must not be null)
    /// @param document the owning document, used as factory context for action
    ///                 dereferencing (may be null)
    /// @throws IllegalArgumentException if `catalog` is null
    public DocumentActions(PdfDictionary catalog, Document document) {
        if (catalog == null) {
            throw new IllegalArgumentException("Catalog must not be null");
        }
        this.catalog = catalog;
        this.document = document;
    }

    /// Returns the action that runs when the document is opened
    /// (`/OpenAction`), or `null` if absent or the entry is a
    /// destination array instead of an action.
    ///
    /// @return the open action, or null
    public PdfAction getOpenAction() {
        PdfBase value = resolve(catalog.get(OPEN_ACTION));
        if (value instanceof PdfDictionary) {
            try {
                return PdfAction.fromDictionary((PdfDictionary) value, document);
            } catch (IOException e) {
                LOG.warning(() -> "Failed to parse /OpenAction: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    /// Sets the `/OpenAction` entry. Passing `null` removes it.
    ///
    /// @param action the open action, or null to clear
    public void setOpenAction(PdfAction action) {
        if (action == null) {
            catalog.remove(OPEN_ACTION);
            return;
        }
        catalog.set(OPEN_ACTION, action.getPdfDictionary());
    }

    /// @return the will-close action (`/AA/WC`), or null.
    public PdfAction getBeforeClosing()  { return getAA(WC); }
    /// @return the will-save action (`/AA/WS`), or null.
    public PdfAction getBeforeSaving()   { return getAA(WS); }
    /// @return the did-save action (`/AA/DS`), or null.
    public PdfAction getAfterSaving()    { return getAA(DS); }
    /// @return the will-print action (`/AA/WP`), or null.
    public PdfAction getBeforePrinting() { return getAA(WP); }
    /// @return the did-print action (`/AA/DP`), or null.
    public PdfAction getAfterPrinting()  { return getAA(DP); }

    /// Sets the will-close (`/AA/WC`) action; null removes the entry.
    public void setBeforeClosing(PdfAction action)  { setAA(WC, action); }
    /// Sets the will-save (`/AA/WS`) action; null removes the entry.
    public void setBeforeSaving(PdfAction action)   { setAA(WS, action); }
    /// Sets the did-save (`/AA/DS`) action; null removes the entry.
    public void setAfterSaving(PdfAction action)    { setAA(DS, action); }
    /// Sets the will-print (`/AA/WP`) action; null removes the entry.
    public void setBeforePrinting(PdfAction action) { setAA(WP, action); }
    /// Sets the did-print (`/AA/DP`) action; null removes the entry.
    public void setAfterPrinting(PdfAction action)  { setAA(DP, action); }

    private PdfAction getAA(PdfName key) {
        PdfBase aaValue = resolve(catalog.get(AA));
        if (!(aaValue instanceof PdfDictionary)) return null;
        PdfBase entry = resolve(((PdfDictionary) aaValue).get(key));
        if (entry instanceof PdfDictionary) {
            try {
                return PdfAction.fromDictionary((PdfDictionary) entry, document);
            } catch (IOException e) {
                LOG.warning(() -> "Failed to parse /AA/" + key.getName() + ": " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    private void setAA(PdfName key, PdfAction action) {
        PdfDictionary aa = (PdfDictionary) resolve(catalog.get(AA));
        if (action == null) {
            if (aa != null) {
                aa.remove(key);
                if (aa.size() == 0) catalog.remove(AA);
            }
            return;
        }
        if (aa == null) {
            aa = new PdfDictionary();
            catalog.set(AA, aa);
        }
        aa.set(key, action.getPdfDictionary());
    }

    private static PdfBase resolve(PdfBase value) {
        if (value instanceof PdfObjectReference) {
            try {
                return ((PdfObjectReference) value).dereference();
            } catch (IOException e) {
                return null;
            }
        }
        return value;
    }
}
