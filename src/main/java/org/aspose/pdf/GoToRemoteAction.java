package org.aspose.pdf;

import org.aspose.pdf.engine.pdfobjects.*;


/// Go-To Remote action — navigate to a destination in another PDF (ISO 32000-1:2008, §12.6.4.3).
public class GoToRemoteAction extends PdfAction {

    /// Parses a GoToRemoteAction from a dictionary.
    ///
    /// @param dict the action dictionary
    public GoToRemoteAction(PdfDictionary dict) {
        this.actionDict = dict;
    }

    /// Creates a GoToRemoteAction pointing to a specific page in another PDF file.
    ///
    /// @param file       the path to the remote PDF file
    /// @param pageNumber the 1-based destination page number
    public GoToRemoteAction(String file, int pageNumber) {
        this.actionDict = new PdfDictionary();
        actionDict.set(PdfName.of("S"), PdfName.of("GoToR"));
        // File spec as a simple string
        actionDict.set(PdfName.of("F"), new PdfString(file));
        // Destination: [pageIndex /Fit] — pageNumber is 1-based, PDF array is 0-based
        PdfArray dest = new PdfArray();
        dest.add(PdfInteger.valueOf(pageNumber - 1));
        dest.add(PdfName.of("Fit"));
        actionDict.set(PdfName.of("D"), dest);
    }

    /// Creates a GoToRemoteAction pointing to a specific destination in another PDF file.
    ///
    /// @param file        the path to the remote PDF file
    /// @param destination the explicit destination
    public GoToRemoteAction(String file, ExplicitDestination destination) {
        this.actionDict = new PdfDictionary();
        actionDict.set(PdfName.of("S"), PdfName.of("GoToR"));
        actionDict.set(PdfName.of("F"), new PdfString(file));
        if (destination != null) {
            actionDict.set(PdfName.of("D"), destination.toPdfArray());
        }
    }

    /// Returns the file specification (/F entry).
    ///
    /// @return the file path or specification, or null
    public String getFile() {
        PdfBase f = resolve(actionDict.get("F"));
        if (f instanceof PdfString) return ((PdfString) f).getString();
        if (f instanceof PdfDictionary) {
            PdfBase uf = ((PdfDictionary) f).get("UF");
            if (uf instanceof PdfString) return ((PdfString) uf).getString();
            PdfBase fVal = ((PdfDictionary) f).get("F");
            if (fVal instanceof PdfString) return ((PdfString) fVal).getString();
        }
        return null;
    }

    /// Sets the file specification.
    ///
    /// @param file the file path
    public void setFile(String file) {
        actionDict.set(PdfName.of("F"), new PdfString(file));
    }

    /// Returns whether the destination document should be opened in a new window.
    ///
    /// @return true if new window, false otherwise (default false)
    public boolean isNewWindow() {
        PdfBase nw = actionDict.get("NewWindow");
        if (nw instanceof PdfBoolean) return ((PdfBoolean) nw).getValue();
        return false;
    }

    /// Sets whether the destination document should be opened in a new window.
    ///
    /// @param newWindow true to open in new window
    public void setNewWindow(boolean newWindow) {
        actionDict.set(PdfName.of("NewWindow"), newWindow ? PdfBoolean.TRUE : PdfBoolean.FALSE);
    }
}
