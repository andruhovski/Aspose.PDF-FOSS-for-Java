package org.aspose.pdf.forms;

import org.aspose.pdf.Color;
import org.aspose.pdf.engine.cos.COSArray;
import org.aspose.pdf.engine.cos.COSBase;
import org.aspose.pdf.engine.cos.COSDictionary;
import org.aspose.pdf.engine.cos.COSFloat;
import org.aspose.pdf.engine.cos.COSInteger;
import org.aspose.pdf.engine.cos.COSName;
import org.aspose.pdf.engine.cos.COSString;

import java.util.logging.Logger;

/**
 * Wrapper around the widget appearance-characteristics dictionary
 * ({@code /MK}, ISO 32000-1:2008 §12.5.6.19).
 *
 * <p>Provides typed access to:</p>
 * <ul>
 *   <li>{@code /BC} — border color ({@link #getBorder()} / {@link #setBorder(Color)})</li>
 *   <li>{@code /BG} — background color ({@link #getBackground()} / {@link #setBackground(Color)})</li>
 *   <li>{@code /R}  — rotation in degrees ({@link #getRotate()} / {@link #setRotate(int)})</li>
 *   <li>{@code /CA} — normal caption</li>
 *   <li>{@code /RC} — rollover caption</li>
 *   <li>{@code /AC} — alternate (down) caption</li>
 * </ul>
 *
 * <p>Mutations write back to the underlying {@code /MK} sub-dictionary of the
 * owning widget; the sub-dictionary is created lazily if absent.</p>
 */
public class AppearanceCharacteristics {

    private static final Logger LOG = Logger.getLogger(AppearanceCharacteristics.class.getName());

    private final COSDictionary owner;

    public AppearanceCharacteristics(COSDictionary owner) {
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

    /**
     * Returns the rotation in degrees (0, 90, 180, or 270). Defaults to 0
     * when {@code /R} is absent or the owning widget has no {@code /MK}.
     *
     * @return the rotation, default 0
     */
    public int getRotate() {
        COSBase mk = owner.get("MK");
        if (!(mk instanceof COSDictionary)) return 0;
        COSBase r = ((COSDictionary) mk).get("R");
        if (r instanceof COSInteger) return ((COSInteger) r).intValue();
        if (r instanceof COSFloat) return (int) ((COSFloat) r).doubleValue();
        return 0;
    }

    /**
     * Sets the rotation. Should be a multiple of 90 (per spec); a warning is
     * logged for non-multiples but the value is still written.
     *
     * @param rotate rotation in degrees
     */
    public void setRotate(int rotate) {
        if (rotate % 90 != 0) {
            LOG.warning(() -> "MK /R rotate should be a multiple of 90, got " + rotate);
        }
        getOrCreateMk().set(COSName.of("R"), COSInteger.valueOf(rotate));
    }

    /**
     * Returns the normal caption (/CA), or {@code null} if not set.
     */
    public String getCaption() { return readString("CA"); }

    public void setCaption(String caption) { writeString("CA", caption); }

    /**
     * Returns the rollover caption (/RC), or {@code null}.
     */
    public String getRolloverCaption() { return readString("RC"); }

    public void setRolloverCaption(String caption) { writeString("RC", caption); }

    /**
     * Returns the alternate ("down") caption (/AC), or {@code null}.
     */
    public String getAlternateCaption() { return readString("AC"); }

    public void setAlternateCaption(String caption) { writeString("AC", caption); }

    /**
     * Returns the underlying {@code /MK} dictionary, creating it on demand.
     * Useful for callers that need to read or write entries not exposed here.
     *
     * @return the /MK dictionary (never null)
     */
    public COSDictionary getCOSDictionary() {
        return getOrCreateMk();
    }

    private COSDictionary getOrCreateMk() {
        COSBase mk = owner.get("MK");
        if (mk instanceof COSDictionary) {
            return (COSDictionary) mk;
        }
        COSDictionary dict = new COSDictionary();
        owner.set(COSName.of("MK"), dict);
        return dict;
    }

    private Color readColor(String key) {
        COSBase mk = owner.get("MK");
        if (!(mk instanceof COSDictionary)) {
            return null;
        }
        COSBase val = ((COSDictionary) mk).get(key);
        if (!(val instanceof COSArray)) {
            return null;
        }
        COSArray arr = (COSArray) val;
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
        COSDictionary mk = getOrCreateMk();
        if (color == null) {
            mk.remove(COSName.of(key));
            return;
        }
        COSArray arr = new COSArray();
        arr.add(new COSFloat(color.getR()));
        arr.add(new COSFloat(color.getG()));
        arr.add(new COSFloat(color.getB()));
        mk.set(COSName.of(key), arr);
    }

    private String readString(String key) {
        COSBase mk = owner.get("MK");
        if (!(mk instanceof COSDictionary)) return null;
        COSBase v = ((COSDictionary) mk).get(key);
        return (v instanceof COSString) ? ((COSString) v).getString() : null;
    }

    private void writeString(String key, String value) {
        COSDictionary mk = getOrCreateMk();
        if (value == null) {
            mk.remove(COSName.of(key));
            return;
        }
        mk.set(COSName.of(key), new COSString(value));
    }
}
