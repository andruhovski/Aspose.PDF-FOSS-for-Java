package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.*;

/// Hide action — shows or hides annotations (ISO 32000-1:2008, §12.6.4.10).
/// /T identifies annotations by name (string) or array of names/references.
/// /H = true means hide (default), false means show.
public class HideAction extends PdfAction {

    /// Parses a HideAction from an existing dictionary.
    ///
    /// @param dict the action dictionary
    public HideAction(PdfDictionary dict) {
        this.actionDict = dict;
    }

    /// Creates a HideAction targeting a single annotation by name.
    ///
    /// @param annotationName the annotation field name
    /// @param hide`true` to hide, `false` to show
    public HideAction(String annotationName, boolean hide) {
        this.actionDict = new PdfDictionary();
        actionDict.set(PdfName.of("S"), PdfName.of("Hide"));
        actionDict.set(PdfName.of("T"), new PdfString(annotationName));
        actionDict.setBoolean("H", hide);
    }

    /// Creates a HideAction targeting multiple annotations by name.
    ///
    /// @param annotationNames the annotation field names
    /// @param hide`true` to hide, `false` to show
    public HideAction(String[] annotationNames, boolean hide) {
        this.actionDict = new PdfDictionary();
        actionDict.set(PdfName.of("S"), PdfName.of("Hide"));
        PdfArray arr = new PdfArray();
        for (String name : annotationNames) {
            arr.add(new PdfString(name));
        }
        actionDict.set(PdfName.of("T"), arr);
        actionDict.setBoolean("H", hide);
    }

    /// Returns whether this action hides annotations (default `true`).
    ///
    /// @return `true` if hiding, `false` if showing
    public boolean isHide() {
        return actionDict.getBoolean("H", true);
    }

    /// Sets whether this action hides or shows annotations.
    ///
    /// @param hide`true` to hide, `false` to show
    public void setHide(boolean hide) {
        actionDict.setBoolean("H", hide);
    }

    /// Returns the annotation name(s) targeted by this action.
    ///
    /// @return array of annotation names (may be empty)
    public String[] getAnnotationNames() {
        PdfBase t = resolve(actionDict.get("T"));
        if (t instanceof PdfString) {
            return new String[]{((PdfString) t).getString()};
        }
        if (t instanceof PdfArray) {
            PdfArray arr = (PdfArray) t;
            String[] names = new String[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                PdfBase item = resolve(arr.get(i));
                names[i] = (item instanceof PdfString) ? ((PdfString) item).getString() : "";
            }
            return names;
        }
        return new String[0];
    }
}
