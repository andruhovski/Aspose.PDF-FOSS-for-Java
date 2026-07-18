package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;

/// Transition action — controls page transitions during presentations
/// (ISO 32000-1:2008, §12.6.4.14).
public class TransitionAction extends PdfAction {

    /// Parses a TransitionAction from an existing dictionary.
    ///
    /// @param dict the action dictionary
    public TransitionAction(PdfDictionary dict) {
        this.actionDict = dict;
    }

    /// Returns the transition dictionary (/Trans).
    ///
    /// @return the transition dictionary, or `null`
    public PdfDictionary getTransition() {
        PdfBase t = resolve(actionDict.get("Trans"));
        return (t instanceof PdfDictionary) ? (PdfDictionary) t : null;
    }
}
