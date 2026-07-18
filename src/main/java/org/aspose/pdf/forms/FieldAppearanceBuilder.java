package org.aspose.pdf.forms;

import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.pdfobjects.*;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/// Builds Form-XObject `/AP/N` appearance streams for form fields
/// (checkbox and radio-button options).
///
/// Per ISO 32000-1:2008 §12.7.4.3, form-field widget annotations must have
/// an `/AP/N` entry that is either a stream (single-state fields like
/// text/button) or a dictionary keyed by state name (multi-state like checkbox
/// `/Yes /Off` and radio `/<option> /Off`). Without proper
/// `/AP/N`, viewers only render the field if `/NeedAppearances`
/// is true in the AcroForm dictionary, which is non-portable.
///
/// This builder uses the standard Zapf Dingbats font (a Type-1 standard
/// 14 font, requires no embedding) for box-style glyphs and a simple
/// black-on-white square for the off state.
///
/// Closes F-10: previously [CheckboxField] and [RadioButtonField]
/// stored [PdfNull#INSTANCE] placeholders or zero-BBox empty streams in
/// `/AP/N`, causing [Field#getAppearance()] to return null states.
public final class FieldAppearanceBuilder {

    private FieldAppearanceBuilder() {}

    /// Builds a checkbox appearance Form-XObject for the given state and glyph.
    ///
    /// @param rect the widget rectangle (used for /BBox)
    /// @param checked`true` for "on" state (renders the glyph),
    ///                `false` for "Off" (empty stream)
    /// @param style the box style; null defaults to [BoxStyle#Check]
    /// @return a [PdfStream] wrapped as Form-XObject
    public static PdfStream buildCheckboxAppearance(Rectangle rect, boolean checked, BoxStyle style) {
        return buildBoxAppearance(rect, checked, style != null ? style : BoxStyle.Check);
    }

    /// Builds a radio-button appearance Form-XObject for the given state.
    /// Defaults to a circle glyph when style is null (radio convention).
    ///
    /// @param rect the widget rectangle
    /// @param selected whether the option is selected
    /// @param style the box style; null defaults to [BoxStyle#Circle]
    /// @return a [PdfStream] wrapped as Form-XObject
    public static PdfStream buildRadioAppearance(Rectangle rect, boolean selected, BoxStyle style) {
        return buildBoxAppearance(rect, selected, style != null ? style : BoxStyle.Circle);
    }

    private static PdfStream buildBoxAppearance(Rectangle rect, boolean on, BoxStyle style) {
        double w = rect != null ? rect.getWidth() : 12;
        double h = rect != null ? rect.getHeight() : 12;
        if (w <= 0) w = 12;
        if (h <= 0) h = 12;

        StringBuilder cs = new StringBuilder(64);
        cs.append("q\n");
        if (on) {
            String glyph = boxStyleToGlyph(style);
            double fontSize = Math.min(w, h) * 0.8;
            // Centre the glyph horizontally and seat it slightly above the baseline.
            double tx = w * 0.15;
            double ty = h * 0.2;
            cs.append("BT\n");
            cs.append("/ZaDb ").append(fmt(fontSize)).append(" Tf\n");
            cs.append(fmt(tx)).append(' ').append(fmt(ty)).append(" Td\n");
            cs.append('(').append(glyph).append(") Tj\n");
            cs.append("ET\n");
        }
        cs.append("Q\n");

        PdfStream stream = new PdfStream();
        stream.setDecodedData(cs.toString().getBytes(StandardCharsets.ISO_8859_1));

        stream.set(PdfName.TYPE, PdfName.of("XObject"));
        stream.set(PdfName.SUBTYPE, PdfName.of("Form"));
        stream.set(PdfName.of("FormType"), PdfInteger.valueOf(1));

        PdfArray bbox = new PdfArray();
        bbox.add(new PdfFloat(0));
        bbox.add(new PdfFloat(0));
        bbox.add(new PdfFloat(w));
        bbox.add(new PdfFloat(h));
        stream.set(PdfName.BBOX, bbox);
        stream.set(PdfName.RESOURCES, buildResources());

        return stream;
    }

    /// Installs the per-state appearance streams on a widget dictionary. Replaces
    /// any existing `/AP/N/<state>` entries (including [PdfNull]
    /// placeholders) and sets `/AS` to `"Off"` if not already set.
    ///
    /// @param widgetDict the widget annotation dictionary
    /// @param onStateStream the stream for the "on" state
    /// @param onStateName the name of the on state (e.g. `"Yes"`)
    /// @param offStateStream the stream for `"Off"`
    public static void installAppearance(PdfDictionary widgetDict,
                                         PdfStream onStateStream,
                                         String onStateName,
                                         PdfStream offStateStream) {
        if (widgetDict == null || onStateName == null) return;

        PdfBase apVal = widgetDict.get(PdfName.of("AP"));
        PdfDictionary ap;
        if (apVal instanceof PdfDictionary) {
            ap = (PdfDictionary) apVal;
        } else {
            ap = new PdfDictionary();
            widgetDict.set(PdfName.of("AP"), ap);
        }

        PdfBase nVal = ap.get(PdfName.N);
        PdfDictionary apN;
        if (nVal instanceof PdfDictionary && !(nVal instanceof PdfStream)) {
            apN = (PdfDictionary) nVal;
        } else {
            apN = new PdfDictionary();
            ap.set(PdfName.N, apN);
        }

        if (onStateStream != null) apN.set(PdfName.of(onStateName), onStateStream);
        if (offStateStream != null) apN.set(PdfName.of("Off"), offStateStream);

        if (widgetDict.get(PdfName.of("AS")) == null) {
            widgetDict.set(PdfName.of("AS"), PdfName.of("Off"));
        }
    }

    /// Returns `true` if `/AP/N` on the given widget contains any
    /// [PdfNull] placeholder or is missing entirely. Used by attach paths
    /// to detect when to (re)generate appearances.
    public static boolean isAppearanceIncomplete(PdfDictionary widgetDict) {
        PdfBase apVal = widgetDict.get(PdfName.of("AP"));
        if (!(apVal instanceof PdfDictionary)) return true;
        PdfBase nVal = ((PdfDictionary) apVal).get(PdfName.N);
        if (nVal == null) return true;
        if (nVal instanceof PdfStream) return isStreamIncomplete((PdfStream) nVal);
        if (!(nVal instanceof PdfDictionary)) return true;
        PdfDictionary apN = (PdfDictionary) nVal;
        if (apN.size() == 0) return true;
        for (PdfName key : apN.keySet()) {
            PdfBase entry = apN.get(key);
            if (entry == null || entry instanceof PdfNull) return true;
            if (entry instanceof PdfStream) {
                if (isStreamIncomplete((PdfStream) entry)) return true;
            } else {
                return true;
            }
        }
        return false;
    }

    private static boolean isStreamIncomplete(PdfStream s) {
        // Missing or zero-area BBox = placeholder. Also empty data + no BBox = placeholder.
        PdfBase bbox = s.get(PdfName.BBOX);
        if (bbox instanceof PdfArray && ((PdfArray) bbox).size() == 4) {
            PdfArray bb = (PdfArray) bbox;
            double width = bb.getFloat(2, 0) - bb.getFloat(0, 0);
            double height = bb.getFloat(3, 0) - bb.getFloat(1, 0);
            return width <= 0 || height <= 0;
        }
        // BBox missing entirely — only complete if data is non-empty (legacy spec-lax forms)
        try {
            byte[] data = s.getDecodedData();
            return data == null || data.length == 0;
        } catch (Exception e) {
            return true;
        }
    }

    private static PdfDictionary buildResources() {
        PdfDictionary resources = new PdfDictionary();
        PdfDictionary fonts = new PdfDictionary();
        PdfDictionary zadb = new PdfDictionary();
        zadb.set(PdfName.TYPE, PdfName.FONT);
        zadb.set(PdfName.SUBTYPE, PdfName.of("Type1"));
        zadb.set(PdfName.BASE_FONT, PdfName.of("ZapfDingbats"));
        zadb.set(PdfName.of("Name"), PdfName.of("ZaDb"));
        fonts.set("ZaDb", zadb);
        resources.set(PdfName.of("Font"), fonts);
        return resources;
    }

    /// Returns the single-character Zapf Dingbats string for the given box style.
    /// Codes are the standard PDF check-glyph mapping (Adobe Acrobat convention).
    private static String boxStyleToGlyph(BoxStyle style) {
        if (style == null) return "4";
        switch (style) {
            case Check:   return "4";   // ✓
            case Circle:  return "l";   // ●
            case Cross:   return "8";   // ✕
            case Diamond: return "u";   // ◆
            case Square:  return "n";   // ■
            case Star:    return "H";   // ★
            default:      return "4";
        }
    }

    private static String fmt(double v) {
        if (v == (long) v) return Long.toString((long) v);
        String s = String.format(Locale.ROOT, "%.4f", v);
        int end = s.length();
        while (end > s.indexOf('.') + 2 && s.charAt(end - 1) == '0') end--;
        return s.substring(0, end);
    }
}
