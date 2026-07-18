package org.aspose.pdf.engine.xfa.flatten.layout;

import org.aspose.pdf.engine.xfa.model.template.Template;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/// Applies XFA **simplex/duplex page qualification** to a paginated layout (Stage C, sprint L4.3):
/// honours a `<pageArea oddOrEven="odd|even">` restriction when the governing
/// `<pageSet relation="duplexPaginated">` forces content onto a physical odd/even side.
///
/// When a content page's assigned pageArea is qualified to a parity its natural physical index
/// does not satisfy, a **blank page** is inserted before it so it lands on the correct side — the
/// standard duplex imposition behaviour. `simplexPaginated` (or no relation) imposes no
/// constraint and the layout is returned unchanged, so non-duplex forms (e.g. 408975) are a no-op.
public final class XfaDuplex {

    private XfaDuplex() {
    }

    /// Inserts blank pages as needed so every oddOrEven-qualified pageArea lands on its required
    /// physical side, when `tpl`'s pageSet is `duplexPaginated`.
    ///
    /// @param pages   the paginated pages (in order)
    /// @param tpl     the template (pageSet relation + pageArea qualification), or `null`
    /// @param mediumW blank-page width in points
    /// @param mediumH blank-page height in points
    /// @return the pages with any required blanks inserted (the same list when no qualification applies)
    static List<XfaPaginator.PageLayout> qualify(List<XfaPaginator.PageLayout> pages, Template tpl,
                                                 double mediumW, double mediumH) {
        if (tpl == null || pages.isEmpty() || !isDuplex(tpl.getElement())) {
            return pages;
        }
        XfaPaginator.PageRegion blank =
                new XfaPaginator.PageRegion(null, -1, 0, 0, mediumW, mediumH, mediumW, mediumH);
        List<XfaPaginator.PageLayout> out = new ArrayList<>();
        for (XfaPaginator.PageLayout pl : pages) {
            String req = oddOrEven(pl.region == null ? null : pl.region.pageArea);
            if (req != null) {
                int physicalIndex = out.size() + 1; // 1-based side this page would take
                boolean wantOdd = "odd".equals(req);
                boolean isOdd = (physicalIndex & 1) == 1;
                if (wantOdd != isOdd) {
                    out.add(new XfaPaginator.PageLayout(new ArrayList<>(), blank)); // flip parity
                }
            }
            out.add(pl);
        }
        return out;
    }

    /// @return the `oddOrEven` qualification of a pageArea (`"odd"`/`"even"`), or null.
    private static String oddOrEven(Element pageArea) {
        if (pageArea == null) {
            return null;
        }
        String v = pageArea.getAttribute("oddOrEven");
        return "odd".equals(v) || "even".equals(v) ? v : null;
    }

    /// Whether the first `<pageSet>` declares `relation="duplexPaginated"`.
    private static boolean isDuplex(Element root) {
        Element pageSet = findFirst(root, "pageSet");
        return pageSet != null && "duplexPaginated".equals(pageSet.getAttribute("relation"));
    }

    private static Element findFirst(Element el, String localName) {
        for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element ce = (Element) n;
                String ln = ce.getLocalName() != null ? ce.getLocalName() : ce.getNodeName();
                if (localName.equals(ln)) {
                    return ce;
                }
                Element found = findFirst(ce, localName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
