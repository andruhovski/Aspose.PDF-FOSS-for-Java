package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfString;

/// ImportData action — imports form data from an FDF or XFDF file
/// (ISO 32000-1:2008, §12.6.4.17).
public class ImportDataAction extends PdfAction {

    /// Parses an ImportDataAction from an existing dictionary.
    ///
    /// @param dict the action dictionary
    public ImportDataAction(PdfDictionary dict) {
        this.actionDict = dict;
    }

    /// Creates an ImportDataAction for the given file path.
    ///
    /// @param filePath the FDF/XFDF file path
    public ImportDataAction(String filePath) {
        this.actionDict = new PdfDictionary();
        actionDict.set(PdfName.of("S"), PdfName.of("ImportData"));
        actionDict.set(PdfName.of("F"), new PdfString(filePath));
    }

    /// Returns the FDF/XFDF file path.
    ///
    /// @return the file path, or `null`
    public String getFile() {
        PdfBase f = resolve(actionDict.get("F"));
        if (f instanceof PdfString) return ((PdfString) f).getString();
        if (f instanceof PdfDictionary) return ((PdfDictionary) f).getString("F");
        return null;
    }
}
