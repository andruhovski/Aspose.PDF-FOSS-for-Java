package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;

/// Named action — predefined action (ISO 32000-1:2008, §12.6.4.11).
///
/// Standard names: "NextPage", "PrevPage", "FirstPage", "LastPage".
///
public class NamedAction extends PdfAction {

    /// Creates a NamedAction with the given name.
    ///
    /// @param name the action name (e.g., "NextPage")
    public NamedAction(String name) {
        this.actionDict = new PdfDictionary();
        actionDict.set(PdfName.of("S"), PdfName.of("Named"));
        actionDict.set(PdfName.of("N"), PdfName.of(name));
    }

    /// Parses a NamedAction from a dictionary.
    ///
    /// @param dict the action dictionary
    public NamedAction(PdfDictionary dict) {
        this.actionDict = dict;
    }

    /// Returns the action name.
    ///
    /// @return the name (e.g., "NextPage")
    public String getActionName() {
        return actionDict.getNameAsString("N");
    }
}
