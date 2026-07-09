package org.aspose.pdf.engine.xfa.flatten.layout;

import org.aspose.pdf.engine.layout.TextLayoutHelper;
import org.aspose.pdf.engine.xfa.flatten.XfaGeometry;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Computes the height of a growable XFA leaf (field / draw) from its bound data
 * (Stage C, sprint L1.2).
 *
 * <p>XFA height resolution (XFA 3.0 §"Sizing of Objects"): an object with a fixed
 * {@code h} is exactly that tall. An object with no {@code h} is <b>growable</b> — it
 * sizes to its content between {@code minH} (default 0) and {@code maxH} (default
 * unbounded). For a text-bearing object the content height is the number of wrapped
 * lines of its value times the line height, plus the top/bottom margin insets.</p>
 *
 * <p>Line breaking, text measurement and line height all come from
 * {@link TextLayoutHelper} (the repo's existing layout primitive) — there is no second
 * line-breaker here. The font typeface/size are read from the object's {@code <font>}
 * and mapped to a standard-14 base font (the same mapping the C2 painter uses), so the
 * measured width matches what is ultimately painted.</p>
 */
public final class XfaGrowableHeight {

    private XfaGrowableHeight() {
    }

    /**
     * @param el a field/draw element
     * @return {@code true} if the object is growable vertically (no fixed {@code h})
     */
    public static boolean isGrowable(Element el) {
        return el != null && !hasMeasure(el, "h");
    }

    /**
     * Resolves the height of a leaf object placed at the given box width.
     *
     * @param el        the field/draw element
     * @param value     the bound display value (may be {@code null}/empty)
     * @param boxWidth  the object's box width in points (its column width in the flow)
     * @return the resolved height in points
     */
    public static double height(Element el, String value, double boxWidth) {
        // Fixed height wins outright.
        double h = measure(el, "h");
        if (!Double.isNaN(h)) {
            return h;
        }

        // A <value><line> draw is a rule, not text: with no explicit h it is a HORIZONTAL line (height 0)
        // spanning its width. Growing it to a text line-height instead made the box w×lineHeight, so the
        // default-slope line was drawn corner-to-corner — the stray diagonal above 14758's "Total Net".
        if (isLineValue(el)) {
            return 0;
        }

        double[] ins = insets(el); // left, top, right, bottom
        double size = fontSize(el);
        String font = fontName(el);
        double lineHeight = TextLayoutHelper.getLineHeight(font, size);

        // Text drives the growth. Wrap at the content width (box minus left/right insets).
        String text = value != null ? value : staticText(el);
        double contentWidth = boxWidth - ins[0] - ins[2];
        if (contentWidth <= 0) {
            contentWidth = boxWidth > 0 ? boxWidth : 1; // degenerate width — at least one column
        }
        int lines;
        if (text == null || text.isEmpty()) {
            lines = 1; // an empty field still occupies one line
        } else {
            lines = TextLayoutHelper.wrapText(text, font, size, contentWidth).size();
            if (lines < 1) {
                lines = 1;
            }
        }

        double content = lines * lineHeight + ins[1] + ins[3];
        return clamp(content, minH(el), maxH(el));
    }

    /**
     * @param el a field/draw element
     * @return its lower height bound {@code minH} in points (0 if absent)
     */
    public static double minH(Element el) {
        double v = measure(el, "minH");
        return Double.isNaN(v) ? 0.0 : v;
    }

    /**
     * @param el a field/draw element
     * @return its upper height bound {@code maxH} in points ({@link Double#POSITIVE_INFINITY} if absent)
     */
    public static double maxH(Element el) {
        double v = measure(el, "maxH");
        return Double.isNaN(v) ? Double.POSITIVE_INFINITY : v;
    }

    private static double clamp(double v, double lo, double hi) {
        if (v < lo) {
            return lo;
        }
        return v > hi ? hi : v;
    }

    /* ----------------------------- attribute reads ----------------------------- */

    private static boolean hasMeasure(Element el, String name) {
        return !Double.isNaN(measure(el, name));
    }

    /** A measurement attribute in points, or {@link Double#NaN} if absent/relative. */
    private static double measure(Element el, String name) {
        if (el == null || !el.hasAttribute(name)) {
            return Double.NaN;
        }
        XfaMeasurement m = XfaMeasurement.parse(el.getAttribute(name));
        if (m == null) {
            return Double.NaN;
        }
        // toPoints returns 0 for relative (em/%) units; treat those as "no absolute value".
        double pts = XfaGeometry.toPoints(m);
        return pts == 0.0 && m.getValue() != 0.0 ? Double.NaN : pts;
    }

    /** The {@code <margin>} left/top/right/bottom insets in points (0 if absent). */
    static double[] insets(Element el) {
        Element m = firstChild(el, "margin");
        if (m == null) {
            return new double[]{0, 0, 0, 0};
        }
        return new double[]{
                orZero(measure(m, "leftInset")),
                orZero(measure(m, "topInset")),
                orZero(measure(m, "rightInset")),
                orZero(measure(m, "bottomInset"))
        };
    }

    private static double orZero(double v) {
        return Double.isNaN(v) ? 0.0 : v;
    }

    /** The object's font size in points (default 10pt, the XFA caption/value default). */
    static double fontSize(Element el) {
        Element font = descendFont(el);
        if (font != null && font.hasAttribute("size")) {
            double s = measure(font, "size");
            if (!Double.isNaN(s) && s > 0) {
                return s;
            }
        }
        return 10.0;
    }

    /** Maps the object's {@code <font>} typeface/weight/posture to a standard-14 base font. */
    static String fontName(Element el) {
        Element font = descendFont(el);
        boolean bold = font != null && "bold".equalsIgnoreCase(attr(font, "weight"));
        boolean italic = font != null && "italic".equalsIgnoreCase(attr(font, "posture"));
        String tf = font != null ? attr(font, "typeface") : null;
        tf = tf == null ? "helvetica" : tf.toLowerCase();
        if (tf.contains("times")) {
            return bold && italic ? "Times-BoldItalic" : bold ? "Times-Bold" : italic ? "Times-Italic" : "Times-Roman";
        }
        if (tf.contains("courier")) {
            return bold && italic ? "Courier-BoldOblique" : bold ? "Courier-Bold" : italic ? "Courier-Oblique" : "Courier";
        }
        return bold && italic ? "Helvetica-BoldOblique" : bold ? "Helvetica-Bold"
                : italic ? "Helvetica-Oblique" : "Helvetica";
    }

    private static String attr(Element e, String name) {
        if (e == null) {
            return null;
        }
        String v = e.getAttribute(name);
        return v == null || v.isEmpty() ? null : v;
    }

    /** {@code <font>} on the object or one level down (e.g. under {@code <value>}/{@code <caption>}). */
    private static Element descendFont(Element el) {
        Element f = firstChild(el, "font");
        if (f != null) {
            return f;
        }
        for (Node n = el == null ? null : el.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element g = firstChild((Element) n, "font");
                if (g != null) {
                    return g;
                }
            }
        }
        return null;
    }

    /** Whether {@code el}'s value is a {@code <line>} vector shape (a rule, sized by its w/h, not text). */
    private static boolean isLineValue(Element el) {
        Element value = firstChild(el, "value");
        return value != null && firstChild(value, "line") != null;
    }

    /** The object's static {@code <value><text>} (a draw label), or {@code null}. */
    private static String staticText(Element el) {
        Element value = firstChild(el, "value");
        Element text = value == null ? null : firstChild(value, "text");
        return text == null ? null : text.getTextContent();
    }

    private static Element firstChild(Element el, String localName) {
        if (el == null) {
            return null;
        }
        for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String ln = n.getLocalName() != null ? n.getLocalName() : n.getNodeName();
                if (localName.equals(ln)) {
                    return (Element) n;
                }
            }
        }
        return null;
    }
}
