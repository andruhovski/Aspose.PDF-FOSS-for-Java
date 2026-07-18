package org.aspose.pdf.forms;

import org.aspose.pdf.Color;
import org.aspose.pdf.engine.pdfobjects.*;

import java.util.logging.Logger;

/// Wrapper around the widget appearance-characteristics dictionary
/// (`/MK`, ISO 32000-1:2008 §12.5.6.19).
///
/// Provides typed access to:
///
///   - `/BC` — border color ([#getBorder()] / [#setBorder(Color)])
///   - `/BG` — background color ([#getBackground()] / [#setBackground(Color)])
///   - `/R`  — rotation in degrees ([#getRotate()] / [#setRotate(int)])
///   - `/CA` — normal caption
///   - `/RC` — rollover caption
///   - `/AC` — alternate (down) caption
///
/// Mutations write back to the underlying `/MK` sub-dictionary of the
/// owning widget; the sub-dictionary is created lazily if absent.
public class AppearanceCharacteristics {

    private static final Logger LOG = Logger.getLogger(AppearanceCharacteristics.class.getName());

    private final PdfDictionary owner;

    public AppearanceCharacteristics(PdfDictionary owner) {
        this.owner = owner;
    }

    public Color getBackground() {
        return readColor("BG");
    }

    public void setBackground(Color color) {
        writeColor("BG", color);
    }

    public Color getBorder() {
        return readColor("BC");
    }

    public void setBorder(Color color) {
        writeColor("BC", color);
    }

    /// Returns the rotation in degrees (0, 90, 180, or 270). Defaults to 0
    /// when `/R` is absent or the owning widget has no `/MK`.
    ///
    /// @return the rotation, default 0
    public int getRotate() {
        PdfBase mk = owner.get("MK");
        if (!(mk instanceof PdfDictionary)) return 0;
        PdfBase r = ((PdfDictionary) mk).get("R");
        if (r instanceof PdfInteger) return ((PdfInteger) r).intValue();
        if (r instanceof PdfFloat) return (int) ((PdfFloat) r).doubleValue();
        return 0;
    }

    /// Sets the rotation. Should be a multiple of 90 (per spec); a warning is
    /// logged for non-multiples but the value is still written.
    ///
    /// @param rotate rotation in degrees
    public void setRotate(int rotate) {
        if (rotate % 90 != 0) {
            LOG.warning(() -> "MK /R rotate should be a multiple of 90, got " + rotate);
        }
        getOrCreateMk().set(PdfName.of("R"), PdfInteger.valueOf(rotate));
    }

    /// Returns the normal caption (/CA), or `null` if not set.
    public String getCaption() { return readString("CA"); }

    public void setCaption(String caption) { writeString("CA", caption); }

    /// Returns the rollover caption (/RC), or `null`.
    public String getRolloverCaption() { return readString("RC"); }

    public void setRolloverCaption(String caption) { writeString("RC", caption); }

    /// Returns the alternate ("down") caption (/AC), or `null`.
    public String getAlternateCaption() { return readString("AC"); }

    public void setAlternateCaption(String caption) { writeString("AC", caption); }

    /// Returns the underlying `/MK` dictionary, creating it on demand.
    /// Useful for callers that need to read or write entries not exposed here.
    ///
    /// @return the /MK dictionary (never null)
    public PdfDictionary getPdfDictionary() {
        return getOrCreateMk();
    }

    private PdfDictionary getOrCreateMk() {
        PdfBase mk = owner.get("MK");
        if (mk instanceof PdfDictionary) {
            return (PdfDictionary) mk;
        }
        PdfDictionary dict = new PdfDictionary();
        owner.set(PdfName.of("MK"), dict);
        return dict;
    }

    private Color readColor(String key) {
        PdfBase mk = owner.get("MK");
        if (!(mk instanceof PdfDictionary)) {
            return null;
        }
        PdfBase val = ((PdfDictionary) mk).get(key);
        if (!(val instanceof PdfArray)) {
            return null;
        }
        PdfArray arr = (PdfArray) val;
        if (arr.size() == 1) {
            return Color.fromGray(arr.getFloat(0, 0));
        }
        if (arr.size() == 3) {
            return Color.fromRgb(arr.getFloat(0, 0), arr.getFloat(1, 0), arr.getFloat(2, 0));
        }
        if (arr.size() == 4) {
            return Color.fromCmyk(arr.getFloat(0, 0), arr.getFloat(1, 0), arr.getFloat(2, 0), arr.getFloat(3, 0));
        }
        return null;
    }

    private void writeColor(String key, Color color) {
        PdfDictionary mk = getOrCreateMk();
        if (color == null) {
            mk.remove(PdfName.of(key));
            return;
        }
        PdfArray arr = new PdfArray();
        arr.add(new PdfFloat(color.getR()));
        arr.add(new PdfFloat(color.getG()));
        arr.add(new PdfFloat(color.getB()));
        mk.set(PdfName.of(key), arr);
    }

    private String readString(String key) {
        PdfBase mk = owner.get("MK");
        if (!(mk instanceof PdfDictionary)) return null;
        PdfBase v = ((PdfDictionary) mk).get(key);
        return (v instanceof PdfString) ? ((PdfString) v).getString() : null;
    }

    private void writeString(String key, String value) {
        PdfDictionary mk = getOrCreateMk();
        if (value == null) {
            mk.remove(PdfName.of(key));
            return;
        }
        mk.set(PdfName.of(key), new PdfString(value));
    }
}
