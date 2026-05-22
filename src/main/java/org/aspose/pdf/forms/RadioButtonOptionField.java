package org.aspose.pdf.forms;

import org.aspose.pdf.*;
import org.aspose.pdf.engine.cos.*;

/**
 * A single option in a radio button group.
 * <p>
 * Each radio button option is a widget annotation dictionary that is a child
 * (/Kids entry) of a {@link RadioButtonField}. The option's value is determined
 * by the non-"Off" key in its /AP/N appearance sub-dictionary.
 * </p>
 */
public class RadioButtonOptionField {

    /** The underlying COS dictionary for this option. */
    private final COSDictionary dict;

    /** The page this option belongs to (may be null). */
    private final Page page;

    /** Per-option appearance style (defaults to Circle for radios). */
    private BoxStyle style = BoxStyle.Circle;

    /** Cached typed accessor over the kid widget's /MK entry. */
    private AppearanceCharacteristics cachedCharacteristics;

    /**
     * Constructs a radio button option from a COS dictionary.
     *
     * @param dict the COS dictionary
     * @param page the page (may be null)
     */
    public RadioButtonOptionField(COSDictionary dict, Page page) {
        this.dict = dict;
        this.page = page;
    }

    /**
     * Returns the value this option represents.
     * <p>
     * Determined by finding the non-"Off" key in the /AP/N sub-dictionary.
     * </p>
     *
     * @return the option value, or null if undetermined
     */
    public String getOptionValue() {
        COSBase ap = dict.get("AP");
        if (ap instanceof COSDictionary) {
            COSBase n = ((COSDictionary) ap).get("N");
            if (n instanceof COSDictionary) {
                for (COSName key : ((COSDictionary) n).keySet()) {
                    if (!"Off".equals(key.getName())) return key.getName();
                }
            }
        }
        return null;
    }

    /**
     * Returns the rectangle of this option widget.
     *
     * @return the rectangle, or null if /Rect is not present
     */
    public Rectangle getRect() {
        COSBase r = dict.get("Rect");
        return (r instanceof COSArray) ? Rectangle.fromCOSArray((COSArray) r) : null;
    }

    /**
     * Returns the underlying COS dictionary.
     *
     * @return the COS dictionary
     */
    public COSDictionary getCOSDictionary() {
        return dict;
    }

    /**
     * Returns the per-option box style. Defaults to {@link BoxStyle#Circle}
     * for radio buttons (mirrors Aspose semantics).
     *
     * @return the box style
     */
    public BoxStyle getStyle() {
        return style;
    }

    /**
     * Sets the per-option box style.
     *
     * @param style the new style (must not be null)
     */
    public void setStyle(BoxStyle style) {
        if (style == null) throw new IllegalArgumentException("style must not be null");
        this.style = style;
    }

    /**
     * Returns typed access to this option's appearance characteristics
     * ({@code /MK} sub-dictionary on the kid widget). The wrapper creates
     * the entry lazily.
     *
     * @return the characteristics wrapper (never null)
     */
    public AppearanceCharacteristics getCharacteristics() {
        if (cachedCharacteristics == null) {
            cachedCharacteristics = new AppearanceCharacteristics(dict);
        }
        return cachedCharacteristics;
    }
}

