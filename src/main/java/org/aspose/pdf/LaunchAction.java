package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSString;

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
    public LaunchAction(COSDictionary dict) {
        this.actionDict = dict;
    }

    /**
     * Creates a LaunchAction for the given file path.
     *
     * @param filePath the file to launch
     */
    public LaunchAction(String filePath) {
        this.actionDict = new COSDictionary();
        actionDict.set(COSName.of("S"), COSName.of("Launch"));
        actionDict.set(COSName.of("F"), new COSString(filePath));
    }

    /**
     * Returns the file path or specification.
     *
     * @return the file path, or {@code null}
     */
    public String getFile() {
        COSBase f = resolve(actionDict.get("F"));
        if (f instanceof COSString) return ((COSString) f).getString();
        if (f instanceof COSDictionary) return ((COSDictionary) f).getString("F");
        return null;
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
