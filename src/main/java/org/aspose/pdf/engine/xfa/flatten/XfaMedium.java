package org.aspose.pdf.engine.xfa.flatten;

import org.aspose.pdf.engine.xfa.model.XfaMeasurement;
import org.aspose.pdf.engine.xfa.model.template.Template;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/// Resolves the page dimensions an XFA form declares via its `<medium>`
/// element (XFA 3.0 §"medium") — the form's own page size, used to size the
/// flattened/painted page instead of a leftover placeholder MediaBox (which on
/// dynamic XFA PDFs is often inverted or wrong).
///
/// `<medium short long orientation stock>`: `short`/`long`
/// are the page's shorter/longer edges; `orientation="landscape"` swaps
/// width/height (default portrait). When `short`/`long` are absent a
/// `stock` name (letter/a4/legal) is used; the final fallback is US Letter.
public final class XfaMedium {

    /// US Letter in points (the default fallback).
    public static final double[] LETTER = {612.0, 792.0};
    /// ISO A4 in points.
    public static final double[] A4 = {595.32, 841.92};
    /// US Legal in points.
    public static final double[] LEGAL = {612.0, 1008.0};

    private XfaMedium() {
    }

    /// Resolves the page size `{width,height}` in points from the template's
    /// first `<medium>`, or US Letter if none is present.
    ///
    /// @param tpl the XFA template, or `null`
    /// @return the page size `{w,h}` in points (never `null`)
    public static double[] resolve(Template tpl) {
        if (tpl == null) {
            return LETTER.clone();
        }
        Element medium = findFirst(tpl.getElement(), "medium");
        if (medium == null) {
            return LETTER.clone();
        }
        double s = points(medium.getAttribute("short"));
        double l = points(medium.getAttribute("long"));
        if (Double.isNaN(s) || Double.isNaN(l) || s <= 0 || l <= 0) {
            return stock(medium.getAttribute("stock"));
        }
        boolean landscape = "landscape".equalsIgnoreCase(medium.getAttribute("orientation"));
        // portrait: width = short edge, height = long edge; landscape swaps.
        return landscape ? new double[]{l, s} : new double[]{s, l};
    }

    /// Resolves a single `<pageArea>`'s own page size from its direct `<medium>` child.
    /// An XFA `<pageSet>` may mix pageAreas of different size/orientation (e.g. a landscape
    /// table page and a portrait narrative page); each such pageArea carries its own `<medium>`.
    ///
    /// @param pageArea the `<pageArea>` element (may be `null`)
    /// @param fallback the page size to use when the pageArea declares no usable `<medium>`
    /// @return the page size `{w,h}` in points (never `null`)
    public static double[] resolvePageArea(Element pageArea, double[] fallback) {
        double[] fb = fallback != null ? fallback : LETTER.clone();
        if (pageArea == null) {
            return fb.clone();
        }
        Element medium = directChild(pageArea, "medium");
        if (medium == null) {
            return fb.clone();
        }
        double s = points(medium.getAttribute("short"));
        double l = points(medium.getAttribute("long"));
        if (Double.isNaN(s) || Double.isNaN(l) || s <= 0 || l <= 0) {
            String stock = medium.getAttribute("stock");
            return (stock == null || stock.isEmpty()) ? fb.clone() : stock(stock);
        }
        boolean landscape = "landscape".equalsIgnoreCase(medium.getAttribute("orientation"));
        return landscape ? new double[]{l, s} : new double[]{s, l};
    }

    /// First direct child element of `parent` with the given local name, or null.
    private static Element directChild(Element parent, String localName) {
        for (Node c = parent.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNodeType() == Node.ELEMENT_NODE) {
                String ln = c.getLocalName() != null ? c.getLocalName() : c.getNodeName();
                if (localName.equals(ln)) {
                    return (Element) c;
                }
            }
        }
        return null;
    }

    private static double[] stock(String name) {
        if (name != null) {
            switch (name.trim().toLowerCase()) {
                case "a4": return A4.clone();
                case "legal": return LEGAL.clone();
                case "letter":
                default: return LETTER.clone();
            }
        }
        return LETTER.clone();
    }

    private static double points(String raw) {
        XfaMeasurement m = XfaMeasurement.parse(raw);
        return m == null ? Double.NaN : XfaGeometry.toPoints(m);
    }

    /// Depth-first search for the first descendant element with the given local name.
    private static Element findFirst(Element el, String localName) {
        Node c = el.getFirstChild();
        while (c != null) {
            if (c.getNodeType() == Node.ELEMENT_NODE) {
                Element ce = (Element) c;
                String ln = ce.getLocalName() != null ? ce.getLocalName() : ce.getNodeName();
                if (localName.equals(ln)) {
                    return ce;
                }
                Element found = findFirst(ce, localName);
                if (found != null) {
                    return found;
                }
            }
            c = c.getNextSibling();
        }
        return null;
    }
}
