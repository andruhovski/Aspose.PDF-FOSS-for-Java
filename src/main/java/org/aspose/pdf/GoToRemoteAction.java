package org.aspose.pdf;

import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSBoolean;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSString;

import java.nio.charset.StandardCharsets;

/**
 * Go-To Remote action — navigate to a destination in another PDF (ISO 32000-1:2008, §12.6.4.3).
 */
public class GoToRemoteAction extends PdfAction {

    /**
     * Parses a GoToRemoteAction from a dictionary.
     *
     * @param dict the action dictionary
     */
    public GoToRemoteAction(COSDictionary dict) {
        this.actionDict = dict;
    }

    /**
     * Creates a GoToRemoteAction pointing to a specific page in another PDF file.
     *
     * @param file       the path to the remote PDF file
     * @param pageNumber the 1-based destination page number
     */
    public GoToRemoteAction(String file, int pageNumber) {
        this.actionDict = new COSDictionary();
        actionDict.set(COSName.of("S"), COSName.of("GoToR"));
        // File spec as a simple string
        actionDict.set(COSName.of("F"), new COSString(file.getBytes(StandardCharsets.UTF_8)));
        // Destination: [pageIndex /Fit] — pageNumber is 1-based, PDF array is 0-based
        COSArray dest = new COSArray();
        dest.add(COSInteger.valueOf(pageNumber - 1));
        dest.add(COSName.of("Fit"));
        actionDict.set(COSName.of("D"), dest);
    }

    /**
     * Creates a GoToRemoteAction pointing to a specific destination in another PDF file.
     *
     * @param file        the path to the remote PDF file
     * @param destination the explicit destination
     */
    public GoToRemoteAction(String file, ExplicitDestination destination) {
        this.actionDict = new COSDictionary();
        actionDict.set(COSName.of("S"), COSName.of("GoToR"));
        actionDict.set(COSName.of("F"), new COSString(file.getBytes(StandardCharsets.UTF_8)));
        if (destination != null) {
            actionDict.set(COSName.of("D"), destination.toCOSArray());
        }
    }

    /**
     * Returns the file specification (/F entry).
     *
     * @return the file path or specification, or null
     */
    public String getFile() {
        COSBase f = resolve(actionDict.get("F"));
        if (f instanceof COSString) return ((COSString) f).getString();
        if (f instanceof COSDictionary) {
            COSBase uf = ((COSDictionary) f).get("UF");
            if (uf instanceof COSString) return ((COSString) uf).getString();
            COSBase fVal = ((COSDictionary) f).get("F");
            if (fVal instanceof COSString) return ((COSString) fVal).getString();
        }
        return null;
    }

    /**
     * Sets the file specification.
     *
     * @param file the file path
     */
    public void setFile(String file) {
        actionDict.set(COSName.of("F"), new COSString(file.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Returns whether the destination document should be opened in a new window.
     *
     * @return true if new window, false otherwise (default false)
     */
    public boolean isNewWindow() {
        COSBase nw = actionDict.get("NewWindow");
        if (nw instanceof COSBoolean) return ((COSBoolean) nw).getValue();
        return false;
    }

    /**
     * Sets whether the destination document should be opened in a new window.
     *
     * @param newWindow true to open in new window
     */
    public void setNewWindow(boolean newWindow) {
        actionDict.set(COSName.of("NewWindow"), newWindow ? COSBoolean.TRUE : COSBoolean.FALSE);
    }
}
