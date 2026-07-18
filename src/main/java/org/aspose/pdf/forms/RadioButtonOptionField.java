package org.aspose.pdf.forms;

import org.aspose.pdf.Page;
import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.pdfobjects.*;

/// A single option in a radio button group.
///
/// Each radio button option is a widget annotation dictionary that is a child
/// (/Kids entry) of a [RadioButtonField]. The option's value is determined
/// by the non-"Off" key in its /AP/N appearance sub-dictionary.
///
public class RadioButtonOptionField {

    /// The underlying PDF dictionary for this option.
    private final PdfDictionary dict;

    /// The page this option belongs to (may be null).
    private final Page page;

    /// Per-option appearance style (defaults to Circle for radios).
    private BoxStyle style = BoxStyle.Circle;

    /// Cached typed accessor over the kid widget's /MK entry.
    private AppearanceCharacteristics cachedCharacteristics;

    /// Constructs a radio button option from a PDF dictionary.
    ///
    /// @param dict the PDF dictionary
    /// @param page the page (may be null)
    public RadioButtonOptionField(PdfDictionary dict, Page page) {
        this.dict = dict;
        this.page = page;
    }

    /// Creates a fresh, unattached radio-button option widget. The new
    /// dictionary is populated with the minimum entries every widget needs:
    /// `/Type /Annot`, `/Subtype /Widget`, `/FT /Btn`.
    ///
    /// Use this when constructing radio button groups programmatically;
    /// the option should be attached to a [RadioButtonField] via
    /// `radio.add(option)` before saving.
    public RadioButtonOptionField() {
        this(buildDefaultWidgetDict(), null);
    }

    /// Creates a fresh radio-button option widget attached to `page` with
    /// the given rectangle. Convenience overload — equivalent to
    /// [#RadioButtonOptionField()] followed by setting the page-link and
    /// `/Rect`.
    ///
    /// @param page the page this widget sits on (may be null)
    /// @param rect the widget rectangle in page coordinates (may be null)
    public RadioButtonOptionField(Page page, Rectangle rect) {
        this(buildDefaultWidgetDict(), page);
        if (rect != null) {
            this.dict.set(PdfName.of("Rect"), rect.toPdfArray());
        }
        if (page != null) {
            this.dict.set(PdfName.of("P"), page.getPdfDictionary());
        }
    }

    private static PdfDictionary buildDefaultWidgetDict() {
        PdfDictionary d = new PdfDictionary();
        d.set(PdfName.TYPE, PdfName.of("Annot"));
        d.set(PdfName.SUBTYPE, PdfName.of("Widget"));
        d.set(PdfName.of("FT"), PdfName.of("Btn"));
        return d;
    }

    /// Returns the value this option represents.
    ///
    /// Determined by finding the non-"Off" key in the /AP/N sub-dictionary.
    ///
    /// @return the option value, or null if undetermined
    public String getOptionValue() {
        PdfBase ap = dict.get("AP");
        if (ap instanceof PdfDictionary) {
            PdfBase n = ((PdfDictionary) ap).get("N");
            if (n instanceof PdfDictionary) {
                for (PdfName key : ((PdfDictionary) n).keySet()) {
                    if (!"Off".equals(key.getName())) return key.getName();
                }
            }
        }
        return null;
    }

    /// Returns the rectangle of this option widget.
    ///
    /// @return the rectangle, or null if /Rect is not present
    public Rectangle getRect() {
        PdfBase r = dict.get("Rect");
        return (r instanceof PdfArray) ? Rectangle.fromPdfArray((PdfArray) r) : null;
    }

    /// Returns the underlying PDF dictionary.
    ///
    /// @return the PDF dictionary
    public PdfDictionary getPdfDictionary() {
        return dict;
    }

    /// Returns the per-option box style. Defaults to [BoxStyle#Circle]
    /// for radio buttons (mirrors Aspose semantics).
    ///
    /// @return the box style
    public BoxStyle getStyle() {
        return style;
    }

    /// Sets the per-option box style.
    ///
    /// @param style the new style (must not be null)
    public void setStyle(BoxStyle style) {
        if (style == null) throw new IllegalArgumentException("style must not be null");
        this.style = style;
    }

    /// Returns typed access to this option's appearance characteristics
    /// (`/MK` sub-dictionary on the kid widget). The wrapper creates
    /// the entry lazily.
    ///
    /// @return the characteristics wrapper (never null)
    public AppearanceCharacteristics getCharacteristics() {
        if (cachedCharacteristics == null) {
            cachedCharacteristics = new AppearanceCharacteristics(dict);
        }
        return cachedCharacteristics;
    }

    /// Returns the export value (option name) of this option. Resolves first
    /// through [#getOptionValue()] (the non-"Off" key in `/AP/N`);
    /// falls back to `/AS` when /AP isn't populated yet (typical for
    /// freshly-constructed options where the user has only called
    /// [#setOptionName(String)]).
    ///
    /// @return the option name, or null if neither /AP/N nor /AS contains one
    public String getOptionName() {
        String fromAp = getOptionValue();
        if (fromAp != null) return fromAp;
        PdfBase as = dict.get("AS");
        if (as instanceof PdfName) return ((PdfName) as).getName();
        return null;
    }

    /// Sets the export value (option name) of this option. The given name
    /// becomes the active appearance state (`/AS`) and is registered as
    /// a key in `/AP/N` alongside `/Off` so that subsequent reads
    /// via [#getOptionValue()] round-trip.
    ///
    /// Passing `null` removes `/AS` and the named entry; the
    /// "Off" placeholder remains so the widget stays toggleable.
    ///
    /// @param name the option name (export value)
    public void setOptionName(String name) {
        // Ensure /AP/N substructure exists
        PdfDictionary ap = (PdfDictionary) dict.get(PdfName.of("AP"));
        if (ap == null) {
            ap = new PdfDictionary();
            dict.set(PdfName.of("AP"), ap);
        }
        PdfDictionary n = (PdfDictionary) ap.get(PdfName.of("N"));
        if (n == null) {
            n = new PdfDictionary();
            ap.set(PdfName.of("N"), n);
        }
        // Drop any previous non-Off entries so a single name remains
        java.util.List<PdfName> toRemove = new java.util.ArrayList<>();
        for (PdfName key : n.keySet()) {
            if (!"Off".equals(key.getName())) toRemove.add(key);
        }
        for (PdfName key : toRemove) n.remove(key);

        // Always keep an Off placeholder
        if (n.get(PdfName.of("Off")) == null) {
            n.set(PdfName.of("Off"), emptyAppearanceStream());
        }

        if (name == null) {
            dict.remove(PdfName.of("AS"));
            return;
        }
        n.set(PdfName.of(name), emptyAppearanceStream());
        dict.set(PdfName.of("AS"), PdfName.of(name));
    }

    /// Sets the widget width by adjusting the right edge of `/Rect`.
    /// If no rectangle is set, creates a 0,0,width,0 rectangle.
    ///
    /// @param width new width in page units
    public void setWidth(double width) {
        Rectangle r = getRect();
        Rectangle next = r != null
                ? new Rectangle(r.getLLX(), r.getLLY(), r.getLLX() + width, r.getURY())
                : new Rectangle(0, 0, width, 0);
        dict.set(PdfName.of("Rect"), next.toPdfArray());
    }

    /// Sets the widget height by adjusting the top edge of `/Rect`.
    /// If no rectangle is set, creates a 0,0,0,height rectangle.
    ///
    /// @param height new height in page units
    public void setHeight(double height) {
        Rectangle r = getRect();
        Rectangle next = r != null
                ? new Rectangle(r.getLLX(), r.getLLY(), r.getURX(), r.getLLY() + height)
                : new Rectangle(0, 0, 0, height);
        dict.set(PdfName.of("Rect"), next.toPdfArray());
    }

    private static PdfStream emptyAppearanceStream() {
        PdfStream s = new PdfStream();
        s.set(PdfName.TYPE, PdfName.of("XObject"));
        s.set(PdfName.SUBTYPE, PdfName.of("Form"));
        s.setDecodedData(new byte[0]);
        return s;
    }

    /// Returns the page this option's widget belongs to.
    ///
    /// @return the page, or null
    public Page getPage() {
        return page;
    }

    /// Sets the widget rectangle (`/Rect`) by directly writing into the
    /// underlying dictionary. Convenience to mirror Aspose's C# property setter.
    ///
    /// @param rect the rectangle, or null to clear the entry
    public void setRect(Rectangle rect) {
        if (rect == null) {
            dict.remove(PdfName.of("Rect"));
        } else {
            dict.set(PdfName.of("Rect"), rect.toPdfArray());
        }
    }

    /// Returns the border (`/BS` dictionary) attached to this option's
    /// widget, or `null` if absent.
    ///
    /// @return the border wrapper, or null
    public org.aspose.pdf.annotations.Border getBorder() {
        PdfBase v = dict.get("BS");
        if (v instanceof PdfDictionary) {
            org.aspose.pdf.annotations.Border b =
                    new org.aspose.pdf.annotations.Border((org.aspose.pdf.annotations.Annotation) null);
            // Width is the only entry our Border class round-trips through COS today
            PdfBase w = ((PdfDictionary) v).get("W");
            if (w instanceof org.aspose.pdf.engine.pdfobjects.PdfFloat) {
                b.setWidth(((org.aspose.pdf.engine.pdfobjects.PdfFloat) w).doubleValue());
            } else if (w instanceof org.aspose.pdf.engine.pdfobjects.PdfInteger) {
                b.setWidth(((org.aspose.pdf.engine.pdfobjects.PdfInteger) w).intValue());
            }
            return b;
        }
        return null;
    }

    /// Sets the border (`/BS` sub-dictionary) on this option's widget.
    /// Passing `null` removes the entry.
    ///
    /// @param border the border wrapper, or null
    public void setBorder(org.aspose.pdf.annotations.Border border) {
        if (border == null) {
            dict.remove(PdfName.of("BS"));
            return;
        }
        PdfDictionary bs = new PdfDictionary();
        bs.set(PdfName.TYPE, PdfName.of("Border"));
        bs.set(PdfName.of("W"), new org.aspose.pdf.engine.pdfobjects.PdfFloat(border.getWidth()));
        // /S = solid/dashed/beveled/inset/underline — we serialise the enum's first letter
        bs.set(PdfName.of("S"),
                PdfName.of(border.getStyle() != null ? border.getStyle().name().substring(0, 1) : "S"));
        dict.set(PdfName.of("BS"), bs);
    }

    /// Returns the visible caption shown alongside this option (`/MK/CA`).
    public String getCaption() {
        return getCharacteristics().getCaption();
    }

    /// Sets the visible caption shown alongside this option (`/MK/CA`).
    ///
    /// @param caption the caption text; null clears the entry
    public void setCaption(String caption) {
        getCharacteristics().setCaption(caption);
    }

    /// Returns the border colour (`/MK/BC`) used for this option.
    public org.aspose.pdf.Color getColor() {
        return getCharacteristics().getBorder();
    }

    /// Sets the border colour (`/MK/BC`) used for this option's check
    /// glyph and outline.
    ///
    /// @param color the colour; null clears the entry
    public void setColor(org.aspose.pdf.Color color) {
        getCharacteristics().setBorder(color);
    }

    /// Returns the `/DA` default-appearance string for this option, or
    /// null if not set.
    ///
    /// @return the DA string, or null
    public String getDefaultAppearance() {
        PdfBase v = dict.get("DA");
        if (v instanceof org.aspose.pdf.engine.pdfobjects.PdfString) {
            return ((org.aspose.pdf.engine.pdfobjects.PdfString) v).getString();
        }
        return null;
    }

    /// Sets the `/DA` default-appearance string raw. Pass `null`
    /// to clear.
    ///
    /// @param da the DA string, or null
    public void setDefaultAppearance(String da) {
        if (da == null) {
            dict.remove(PdfName.of("DA"));
        } else {
            dict.set(PdfName.of("DA"), new org.aspose.pdf.engine.pdfobjects.PdfString(da));
        }
    }

    /// Convenience overload — accepts a typed [org.aspose.pdf.annotations.DefaultAppearance]
    /// and serialises it via its `toString()`.
    ///
    /// @param da the typed default appearance, or null
    public void setDefaultAppearance(org.aspose.pdf.annotations.DefaultAppearance da) {
        if (da == null) {
            dict.remove(PdfName.of("DA"));
        } else {
            setDefaultAppearance(da.toString());
        }
    }

    /// Returns the typed appearance view over this option's `/AP`
    /// sub-dictionary. Mirrors [Field#getAppearance()] for radio options
    /// (which do not extend [Field]).
    ///
    /// @return the appearance dictionary (never null; /AP created lazily)
    public AppearanceDictionary getAppearance() {
        PdfBase ap = dict.get(PdfName.of("AP"));
        PdfDictionary apDict;
        if (ap instanceof PdfDictionary) {
            apDict = (PdfDictionary) ap;
        } else {
            apDict = new PdfDictionary();
            dict.set(PdfName.of("AP"), apDict);
        }
        return new AppearanceDictionary(apDict);
    }

    /// Rebuilds the `/AP/N` appearance streams for this option from its
    /// current `/Rect`, `style` and [#getOptionName()].
    ///
    /// Closes F-10 for radio-option widgets. Idempotent; no-op when the
    /// widget has no rectangle yet.
    public void regenerateAppearance() {
        Rectangle r = getRect();
        if (r == null) return;
        String onState = getOptionName();
        if (onState == null) onState = "On";
        PdfStream onStream = FieldAppearanceBuilder.buildRadioAppearance(r, true, style);
        PdfStream offStream = FieldAppearanceBuilder.buildRadioAppearance(r, false, style);
        FieldAppearanceBuilder.installAppearance(dict, onStream, onState, offStream);
    }
}

