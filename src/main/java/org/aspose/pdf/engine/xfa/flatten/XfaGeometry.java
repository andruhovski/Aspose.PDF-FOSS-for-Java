package org.aspose.pdf.engine.xfa.flatten;

import org.aspose.pdf.Rectangle;
import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.aspose.pdf.engine.xfa.model.XfaNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/// Resolves an XFA layout box (a Form DOM field/draw node) to an absolute PDF
/// [Rectangle] on a page, for **positioned** layout (Stage C, sprint C1).
///
/// XFA geometry (XFA 3.0 §"Coordinate Spaces"; coordinate appendix
/// `CCx = Cx + Mx`) is measured from the container **top-left**: a node's
/// `x`/`y` give the offset of its `anchorType` point within the
/// parent container's _content_ region, `w`/`h` its size. The
/// absolute page position is the accumulation, along the container chain
/// (contentArea → subform(s) → field), of each container's own position
/// (`Cx`, anchor-resolved), its content margin inset (`Mx` =
/// `leftInset`/`topInset`), and the object's anchor origin
/// (`Ox`). PDF user space is the opposite — [Rectangle] is
/// `llx/lly/urx/ury` from the page **bottom-left** — so the accumulated
/// top-left coordinate is flipped against the page height.
///
/// Only positioned containers are resolved. A container whose `layout` is
/// flowed (`tb`/`lr-tb`/`rl-tb`/`row`/`table`) puts
/// its descendants in a flowed region; such a node yields `null` and is
/// recorded by the caller as a Stage-C (C3) gap — it is not faked. A leaf without
/// its own `x`/`y` is likewise unresolved.
public final class XfaGeometry {

    private XfaGeometry() {
    }

    /// Points per unit (PDF user-space unit = 1/72 inch). em/% are layout-relative → unresolved.
    private static double unitToPoints(String unit) {
        switch (unit == null ? "" : unit) {
            case "":
            case "pt": return 1.0;
            case "in": return 72.0;
            case "mm": return 72.0 / 25.4;
            case "cm": return 72.0 / 2.54;
            case "px": return 72.0 / 96.0; // CSS reference pixel
            default: return Double.NaN; // em / percent — not absolutely resolvable
        }
    }

    private static double measurePoints(String raw) {
        XfaMeasurement m = XfaMeasurement.parse(raw);
        if (m == null) {
            return Double.NaN;
        }
        double f = unitToPoints(m.getUnit());
        return Double.isNaN(f) ? Double.NaN : m.getValue() * f;
    }

    private static double orZero(double v) {
        return Double.isNaN(v) ? 0.0 : v;
    }

    /// Converts an XFA measurement to PDF points.
    ///
    /// @param m the measurement, or `null`
    /// @return the value in points, or `0` if `m` is null or layout-relative (em/%)
    public static double toPoints(XfaMeasurement m) {
        if (m == null) {
            return 0.0;
        }
        double f = unitToPoints(m.getUnit());
        return Double.isNaN(f) ? 0.0 : m.getValue() * f;
    }

    /// Resolves the absolute PDF rectangle for a Form DOM field/draw node, with the
    /// page (contentArea) origin at `(0,0)`.
    ///
    /// @param node       the Form DOM node (its backing element is attached to the form tree)
    /// @param pageHeight the target page height in points (for the Y-flip)
    /// @return the absolute PDF rectangle, or `null` if the node has no static
    ///         position (leaf `x`/`y` absent, or a flowed ancestor)
    public static Rectangle resolve(XfaNode node, double pageHeight) {
        return resolve(node, pageHeight, 0.0, 0.0);
    }

    /// Resolves the absolute PDF rectangle for a Form DOM field/draw node, offsetting
    /// the whole container chain by the contentArea origin `(baseX, baseY)`
    /// (measured from the page top-left).
    ///
    /// @param node       the Form DOM node
    /// @param pageHeight the target page height in points (for the Y-flip)
    /// @param baseX      the contentArea X origin within the page (points, from left)
    /// @param baseY      the contentArea Y origin within the page (points, from top)
    /// @return the absolute PDF rectangle, or `null` if unresolved (flowed/leaf-no-xy)
    public static Rectangle resolve(XfaNode node, double pageHeight, double baseX, double baseY) {
        if (node == null) {
            return null;
        }
        Element el = node.getElement();

        // The leaf must carry its own position; otherwise it is flowed/non-positional.
        double fx = measurePoints(el.getAttribute("x"));
        double fy = measurePoints(el.getAttribute("y"));
        if (Double.isNaN(fx) || Double.isNaN(fy)) {
            return null;
        }
        double fw = orZero(measurePoints(el.getAttribute("w")));
        double fh = orZero(measurePoints(el.getAttribute("h")));

        // Collect the element-ancestor container chain (immediate parent .. root).
        // A flowed ancestor means the leaf lives in a flowed region → not statically
        // resolvable here (C3).
        List<Element> chain = new ArrayList<>();
        Node p = el.getParentNode();
        while (p != null && p.getNodeType() == Node.ELEMENT_NODE) {
            Element pe = (Element) p;
            if (isFlowed(pe)) {
                return null;
            }
            chain.add(pe);
            p = p.getParentNode();
        }

        // Accumulate the container-chain transform (XFA top-left space), root → leaf-parent.
        // Each container: move to its anchor (Cx), rotate its frame, then move to its
        // content origin (anchor→top-left offset + Mx margin inset).
        Aff t = new Aff();
        t.translate(baseX, baseY);
        for (int i = chain.size() - 1; i >= 0; i--) {
            applyContainer(t, chain.get(i));
        }

        // The leaf: position at its anchor, rotate its own frame, then its box relative
        // to the anchor (Ox = anchor→top-left offset).
        t.translate(fx, fy);
        t.rotateDeg(rotateDegrees(el));
        double[] ftl = anchorTopLeft(fx, fy, fw, fh, el.getAttribute("anchorType"));
        double lx = ftl[0] - fx;
        double ly = ftl[1] - fy;

        // Map the four box corners through the transform; bounding box (handles rotation).
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        double[][] corners = {{lx, ly}, {lx + fw, ly}, {lx, ly + fh}, {lx + fw, ly + fh}};
        for (double[] pt : corners) {
            double mx = t.px(pt[0], pt[1]);
            double my = t.py(pt[0], pt[1]);
            minX = Math.min(minX, mx);
            maxX = Math.max(maxX, mx);
            minY = Math.min(minY, my);
            maxY = Math.max(maxY, my);
        }

        // top-left space (Y measured downward) → PDF bottom-left.
        double llx = minX;
        double urx = maxX;
        double ury = pageHeight - minY; // minY is the top edge
        double lly = pageHeight - maxY; // maxY is the bottom edge
        return new Rectangle(llx, lly, urx, ury);
    }

    /// Folds a positioned container into the running transform: translate to its anchor,
    /// rotate its frame (`rotate`, 0 if absent), then translate to its content
    /// origin (anchor→top-left offset + the `<margin>` left/top inset).
    private static void applyContainer(Aff t, Element c) {
        double cx = orZero(measurePoints(c.getAttribute("x")));
        double cy = orZero(measurePoints(c.getAttribute("y")));
        double cw = orZero(measurePoints(c.getAttribute("w")));
        double ch = orZero(measurePoints(c.getAttribute("h")));
        t.translate(cx, cy);
        t.rotateDeg(rotateDegrees(c));
        double[] tl = anchorTopLeft(cx, cy, cw, ch, c.getAttribute("anchorType"));
        double[] ins = margin(c);
        t.translate(tl[0] - cx + ins[0], tl[1] - cy + ins[1]);
    }

    /// The `rotate` attribute in degrees counterclockwise (XFA), 0 if absent/invalid.
    private static double rotateDegrees(Element c) {
        String raw = c.getAttribute("rotate");
        if (raw == null || raw.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /// Whether a container's `layout` marks a flowed region (descendants not positioned).
    private static boolean isFlowed(Element c) {
        String layout = c.getAttribute("layout");
        if (layout == null || layout.isEmpty()) {
            return false; // default / "position" → positioned
        }
        switch (layout) {
            case "tb":
            case "lr-tb":
            case "rl-tb":
            case "row":
            case "table":
                return true;
            default:
                return false; // "position" or unknown → positioned
        }
    }

    /// The container's `<margin>` left/top insets in points (0 if absent).
    private static double[] margin(Element c) {
        Element m = firstChild(c, "margin");
        if (m == null) {
            return new double[]{0, 0};
        }
        return new double[]{
                orZero(measurePoints(m.getAttribute("leftInset"))),
                orZero(measurePoints(m.getAttribute("topInset")))
        };
    }

    /// Resolves an anchor point `(x,y)` of a `w×h` box to the box top-left, in the
    /// container's content space. Exposed for the L1 flow layout, which places a positioned
    /// subtree inside a flowed parent by the same anchor rule this resolver uses internally.
    ///
    /// @param x      the anchor X
    /// @param y      the anchor Y
    /// @param w      the box width
    /// @param h      the box height
    /// @param anchor the `anchorType` (topLeft / topCenter / … / bottomRight)
    /// @return `{topLeftX, topLeftY}`
    public static double[] anchorTopLeft(double x, double y, double w, double h, String anchor) {
        double tlx = x;
        double tly = y;
        switch (anchor == null ? "" : anchor) {
            case "topCenter":    tlx = x - w / 2;                  break;
            case "topRight":     tlx = x - w;                      break;
            case "middleLeft":                    tly = y - h / 2; break;
            case "middleCenter": tlx = x - w / 2; tly = y - h / 2; break;
            case "middleRight":  tlx = x - w;     tly = y - h / 2; break;
            case "bottomLeft":                    tly = y - h;     break;
            case "bottomCenter": tlx = x - w / 2; tly = y - h;     break;
            case "bottomRight":  tlx = x - w;     tly = y - h;     break;
            default: break; // topLeft / unspecified
        }
        return new double[]{tlx, tly};
    }

    private static Element firstChild(Element el, String localName) {
        Node n = el.getFirstChild();
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String ln = n.getLocalName();
                if (localName.equals(ln != null ? ln : n.getNodeName())) {
                    return (Element) n;
                }
            }
            n = n.getNextSibling();
        }
        return null;
    }

    /// A 2×3 affine in XFA top-left space: `(x,y) → (a·x + c·y + e, b·x + d·y + f)`.
    /// Right-multiplication ([#translate]) composes a child transform under the
    /// current one, so accumulating root→leaf maps a leaf-local point to page space.
    /// Rotation (C1.2) plugs in as another right-multiplied factor.
    private static final class Aff {
        private double a = 1, b = 0, c = 0, d = 1, e = 0, f = 0;

        /// `this = this · translate(tx,ty)`.
        void translate(double tx, double ty) {
            e += a * tx + c * ty;
            f += b * tx + d * ty;
        }

        /// `this = this · rotate(deg)`, where `deg` is XFA counterclockwise
        /// degrees. In top-left (Y-down) space a visually counterclockwise rotation maps
        /// a local `(x,y)` to `(x·cosθ + y·sinθ, −x·sinθ + y·cosθ)`.
        void rotateDeg(double deg) {
            if (deg == 0) {
                return;
            }
            double r = Math.toRadians(deg);
            double cos = Math.cos(r);
            double sin = Math.sin(r);
            double na = a * cos - c * sin;
            double nc = a * sin + c * cos;
            double nb = b * cos - d * sin;
            double nd = b * sin + d * cos;
            a = na;
            b = nb;
            c = nc;
            d = nd;
        }

        double px(double x, double y) {
            return a * x + c * y + e;
        }

        double py(double x, double y) {
            return b * x + d * y + f;
        }
    }
}
