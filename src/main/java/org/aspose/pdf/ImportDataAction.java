package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSString;

/**
 * ImportData action — imports form data from an FDF or XFDF file
 * (ISO 32000-1:2008, §12.6.4.17).
 */
public class ImportDataAction extends PdfAction {

    /**
     * Parses an ImportDataAction from an existing dictionary.
     *
     * @param dict the action dictionary
     */
    public ImportDataAction(COSDictionary dict) {
        this.actionDict = dict;
    }

    /**
     * Creates an ImportDataAction for the given file path.
     *
     * @param filePath the FDF/XFDF file path
     */
    public ImportDataAction(String filePath) {
        this.actionDict = new COSDictionary();
        actionDict.set(COSName.of("S"), COSName.of("ImportData"));
        actionDict.set(COSName.of("F"), new COSString(filePath));
    }

    /**
     * Returns the FDF/XFDF file path.
     *
     * @return the file path, or {@code null}
     */
    public String getFile() {
        COSBase f = resolve(actionDict.get("F"));
        if (f instanceof COSString) return ((COSString) f).getString();
        if (f instanceof COSDictionary) return ((COSDictionary) f).getString("F");
        return null;
    }
}
