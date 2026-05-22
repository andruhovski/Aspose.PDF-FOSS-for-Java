package org.aspose.pdf;

import org.aspose.pdf.engine.cos.*;

/**
 * PDF viewer preferences (ISO 32000-1:2008, §12.2, Table 150).
 * <p>
 * Controls how the document is displayed when opened: toolbar/menubar visibility,
 * window behavior, reading direction, print options.
 * </p>
 */
public class ViewerPreferences {

    private final COSDictionary dict;

    /** Wraps a viewer preferences dictionary. */
    public ViewerPreferences(COSDictionary dict) {
        this.dict = dict != null ? dict : new COSDictionary();
    }

    // ── Boolean preferences ──

    /** /HideToolbar. */
    public boolean getHideToolbar() { return dict.getBoolean("HideToolbar", false); }
    public void setHideToolbar(boolean v) { dict.set(COSName.of("HideToolbar"), COSBoolean.valueOf(v)); }

    /** /HideMenubar. */
    public boolean getHideMenubar() { return dict.getBoolean("HideMenubar", false); }
    public void setHideMenubar(boolean v) { dict.set(COSName.of("HideMenubar"), COSBoolean.valueOf(v)); }

    /** /HideWindowUI. */
    public boolean getHideWindowUI() { return dict.getBoolean("HideWindowUI", false); }
    public void setHideWindowUI(boolean v) { dict.set(COSName.of("HideWindowUI"), COSBoolean.valueOf(v)); }

    /** /FitWindow. */
    public boolean getFitWindow() { return dict.getBoolean("FitWindow", false); }
    public void setFitWindow(boolean v) { dict.set(COSName.of("FitWindow"), COSBoolean.valueOf(v)); }

    /** /CenterWindow. */
    public boolean getCenterWindow() { return dict.getBoolean("CenterWindow", false); }
    public void setCenterWindow(boolean v) { dict.set(COSName.of("CenterWindow"), COSBoolean.valueOf(v)); }

    /** /DisplayDocTitle. */
    public boolean getDisplayDocTitle() { return dict.getBoolean("DisplayDocTitle", false); }
    public void setDisplayDocTitle(boolean v) { dict.set(COSName.of("DisplayDocTitle"), COSBoolean.valueOf(v)); }

    // ── Enum preferences ──

    /** /NonFullScreenPageMode: UseNone, UseOutlines, UseThumbs, UseOC. */
    public String getNonFullScreenPageMode() {
        String m = dict.getNameAsString("NonFullScreenPageMode");
        return m != null ? m : "UseNone";
    }
    public void setNonFullScreenPageMode(String mode) {
        dict.set(COSName.of("NonFullScreenPageMode"), COSName.of(mode));
    }

    /** /Direction: L2R or R2L. */
    public String getDirection() {
        String d = dict.getNameAsString("Direction");
        return d != null ? d : "L2R";
    }
    public void setDirection(String dir) { dict.set(COSName.of("Direction"), COSName.of(dir)); }

    /** /Duplex: Simplex, DuplexFlipShortEdge, DuplexFlipLongEdge. */
    public String getDuplex() { return dict.getNameAsString("Duplex"); }
    public void setDuplex(String duplex) { dict.set(COSName.of("Duplex"), COSName.of(duplex)); }

    /** /PrintScaling: AppDefault, None. */
    public String getPrintScaling() {
        String s = dict.getNameAsString("PrintScaling");
        return s != null ? s : "AppDefault";
    }
    public void setPrintScaling(String scaling) {
        dict.set(COSName.of("PrintScaling"), COSName.of(scaling));
    }

    /** /NumCopies. */
    public int getNumCopies() { return dict.getInt("NumCopies", 0); }
    public void setNumCopies(int n) { dict.set(COSName.of("NumCopies"), COSInteger.valueOf(n)); }

    /** /PrintPageRange — array of page ranges. */
    public int[] getPrintPageRange() {
        COSBase r = dict.get("PrintPageRange");
        if (r instanceof COSArray) {
            COSArray arr = (COSArray) r;
            int[] result = new int[arr.size()];
            for (int i = 0; i < arr.size(); i++) result[i] = arr.getInt(i, 0);
            return result;
        }
        return null;
    }

    /** Returns the underlying dictionary. */
    public COSDictionary getCOSDictionary() { return dict; }
}
