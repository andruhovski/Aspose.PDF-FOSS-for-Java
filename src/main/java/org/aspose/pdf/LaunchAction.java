package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.PdfBase;
import org.aspose.pdf.engine.pdfobjects.PdfDictionary;
import org.aspose.pdf.engine.pdfobjects.PdfName;
import org.aspose.pdf.engine.pdfobjects.PdfString;

/**
 * Launch action — launches an application or opens a document
 * (ISO 32000-1:2008, §12.6.4.1).
 */
public class LaunchAction extends PdfAction {

    /**
     * Parses a LaunchAction from an existing dictionary.
     *
     * @param dict the action dictionary
     */
    public LaunchAction(PdfDictionary dict) {
        this.actionDict = dict;
    }

    /**
     * Creates a LaunchAction for the given file path.
     *
     * @param filePath the file to launch
     */
    public LaunchAction(String filePath) {
        this.actionDict = new PdfDictionary();
        actionDict.set(PdfName.of("S"), PdfName.of("Launch"));
        actionDict.set(PdfName.of("F"), new PdfString(filePath));
    }

    /**
     * Returns the file path or specification.
     *
     * @return the file path, or the empty string when the action carries no
     *         {@code /F} entry (matches Aspose, which never returns null here —
     *         PDFNET_51091 guarded against a NullReferenceException)
     */
    public String getFile() {
        PdfBase f = resolve(actionDict.get("F"));
        if (f instanceof PdfString) return ((PdfString) f).getString();
        if (f instanceof PdfDictionary) {
            String nested = ((PdfDictionary) f).getString("F");
            return nested != null ? nested : "";
        }
        return "";
    }

    /**
     * Returns whether a new window should be opened.
     *
     * @return {@code true} if /NewWindow is true
     */
    public boolean isNewWindow() {
        return actionDict.getBoolean("NewWindow", false);
    }

    /**
     * Sets whether a new window should be opened.
     *
     * @param newWindow {@code true} to open in a new window
     */
    public void setNewWindow(boolean newWindow) {
        actionDict.setBoolean("NewWindow", newWindow);
    }
}
